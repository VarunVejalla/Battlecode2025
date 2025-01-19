package blitz;

import battlecode.common.*;

public class BunnyComms extends Comms {

    public final int MAP_COOLDOWN = 50;
    public final int MAP2_COOLDOWN = 50; // Used for larger maps

    public int lastMapUpdate = -(MAP_COOLDOWN + 1);
    public int lastMap2Update = -(MAP2_COOLDOWN + 1); // used for larger maps.

    public final int BUFFER_COOLDOWN = 20;
    public int lastBufferUpdate = -(BUFFER_COOLDOWN + 1);

    public final boolean isLargeMap = sectorCount >= MAX_MAP_SECTORS_SENT_PER_ROUND;

    // -1 is used to represent empty messages.
    public int[] messageBuffer = {-1,-1,-1,-1,-1}; // Buffer fills up at 5 sectors. Hardcoded for bytecode.
    public int messageBufferIndex = 0; // Stores the first invalid index.
    public int messagesTransmitted = 0;

    boolean waitingForMap = false;
    boolean waitingForMap2 = false; // used for larger maps.

    Bunny bunny;

    public BunnyComms(RobotController rc, Bunny bunny) {
        super(rc);
        this.bunny = bunny;
    }

    public void updateBunnyBuffer(int roundNum, int sectorID, int msg) throws GameActionException {
        assert roundNum <= 2001;
        assert sectorID < 144;
        messageBuffer[messageBufferIndex] = (roundNum << 16) + (sectorID << 8) + msg;
        messageBufferIndex++;
        messageBufferIndex %= messageBuffer.length;
    }

    /**
     * To be called when there is a friendly tower nearby. Chooses to send either buffer information or map request.
     */
    public void sendMessages(RobotInfo tower) throws GameActionException {

        // When the buffer cooldown expires, prioritize sending bunny memories.
        if(rc.getRoundNum() - lastBufferUpdate > BUFFER_COOLDOWN) {
            sendBufferUpdateMessage(tower);
        }

        // Otherwise, when the map cooldown expires, request a map.
        else if(rc.getRoundNum() - lastMapUpdate > MAP_COOLDOWN || waitingForMap) {
            sendMapUpdateRequestMessage(tower);
        }

        // If there is a larger map and the larger map hasn't been updated, request it.
        else if( (isLargeMap && (rc.getRoundNum() - lastMap2Update > MAP2_COOLDOWN)) || waitingForMap2 ) {
            sendMap2UpdateRequestMessage(tower);
        }
        // Otherwise, big map is not needed this round.
    }


    /**
     * Bunny sends a tower the next entry in its buffer.
     */
    public void sendBufferUpdateMessage(RobotInfo tower) throws GameActionException {
        // Send messages until messagesTransmitted is the size of the buffer.
        // You can only send one message per round. If there's no messages left in the buffer, request a map!
        if(messagesTransmitted < messageBuffer.length && messageBuffer[messagesTransmitted] != -1) {
            // Send the next message in the buffer.
            if(rc.canSendMessage(tower.getLocation())) {
                rc.sendMessage(tower.getLocation(), messageBuffer[messagesTransmitted]);
                messagesTransmitted++;
            }
        }
        else {
            // Buffer transfer is complete and cooldown is reset.
            messagesTransmitted = 0;
            lastBufferUpdate = rc.getRoundNum();
        }
    }


    /**
     * Bunny sends a request to a tower for a map.
     */
    public void sendMapUpdateRequestMessage(RobotInfo tower) throws GameActionException {
        if(rc.canSendMessage(tower.getLocation())) {
            rc.sendMessage(tower.getLocation(), MAP_UPDATE_REQUEST_CODE);
            waitingForMap = true;
        }
    }

    /**
     * Bunny sends a request to a tower for a second part of the map. This is used for larger maps.
     */
    public void sendMap2UpdateRequestMessage(RobotInfo tower) throws GameActionException {
        if(rc.canSendMessage(tower.getLocation())) {
            rc.sendMessage(tower.getLocation(), MAP2_UPDATE_REQUEST_CODE);
            waitingForMap2 = true;
        }
    }

    /**
     * Processes map updates and acts on received data.
     * Handles the first 80 sectors for large maps.
     */
    public void processMap() throws GameActionException {
        boolean successfulRequest = processMapUpdates(0, "BunnyComms loaded map 1!", rc.getRoundNum());
        lastMapUpdate = rc.getRoundNum(); // Refresh map update.
        waitingForMap = !successfulRequest;

    }

    /**
     * Processes map2 updates and acts on received data.
     * Handles the first 80 sectors for large maps.
     */
    public void processMap2() throws GameActionException {
        boolean successfulRequest = processMapUpdates(MAX_MAP_SECTORS_SENT_PER_ROUND, "BunnyComms loaded large map 2!", rc.getRoundNum());
        lastMap2Update = rc.getRoundNum();
        waitingForMap2 = !successfulRequest;
    }

    /**
     * Shared logic for processing map updates. Returns if the processing was successful.
     */
    private boolean processMapUpdates(int startIndex, String successMessage, int roundNum) throws GameActionException {
        Message[] messages = rc.readMessages(roundNum-1);
        if (messages.length == 0) return false;
        loadSectors(startIndex, messages);
        return true;
    }

    // Starts at sectorIndex and converts messages into sectors from there.
    public void loadSectors(int sectorIndex, Message[] messages) throws GameActionException {
        for (Message message : messages) {
            int bytes = message.getBytes();
            for (int i = 0; i < 4 && sectorIndex < sectorCount; i++, sectorIndex++) {
                // Util.log("Updating sector: " + sectorIndex + "/" + (sectorCount - 1));
                int update = bytes & 0xFF;
                int ruinCondition = (update >> 1) & 0b111;
                if(ruinCondition == 2){
                    bunny.addInKnownTowerIfNotAlreadyIn(getSectorCenter(sectorIndex), TowerType.PaintTower);
                } else if(ruinCondition == 3){
                    bunny.addInKnownTowerIfNotAlreadyIn(getSectorCenter(sectorIndex), TowerType.MoneyTower);
                } else if(ruinCondition == 4){
                    bunny.addInKnownTowerIfNotAlreadyIn(getSectorCenter(sectorIndex), TowerType.DefenseTower);
                }
                myWorld[sectorIndex] = update;
                bytes >>>= 8;
            }
        }
    }

    /**
     * Given a bunny's current location, updates the bunny's  world accordingly using visible sector.
     */
    public void  updateSectorInVision(MapLocation currectLocation) throws GameActionException {
        int sectorIndex = getFullyEnclosedSectorID(currectLocation);

        // Checking bunny world
        if(sectorIndex != -1) {
            ScanResult sr = scanSector(sectorIndex);

            int encodedSector = encodeSector(sr);

            // If this encoding is different from the known encoding, add the message to the buffer.
            if(encodedSector != myWorld[sectorIndex]) {
                updateBunnyBuffer(rc.getRoundNum(), sectorIndex, encodedSector);

                // update comms.myWorld with this new information
                myWorld[sectorIndex] = encodedSector;
            }

        } else {
            // Util.log("No sector found");
        }
    }

    /**
     * Scans a sector and returns the scan result containing tower type, enemy paint count, and empty paint count.
     */
    public ScanResult scanSector(int sectorIndex) throws GameActionException {
        int towerType = 0; // Default value
        int enemyPaintCount = 0;
        int emptyPaintCount = 0;

        MapLocation myLoc = rc.getLocation();
        MapLocation sectorCenter = getSectorCenter(sectorIndex);

        // Start from the bottom-left corner of the sector
        for (int x = sectorCenter.x - 2; x < sectorCenter.x + 3; x++) {
            for (int y = sectorCenter.y - 2; y < sectorCenter.y + 3; y++) {
                MapLocation scanLoc = new MapLocation(x, y);
                if (!rc.canSenseLocation(scanLoc)) {
                    continue; // Skip out-of-bounds locations
                }
                MapInfo mapInfo = bunny.nearbyMapInfos[Util.getMapInfoIndex(x - myLoc.x, y - myLoc.y)];

                if (towerType == 0) { // only Check for tower if we have not already found one
                    if (mapInfo.hasRuin()) {
                        towerType = 1; //set tower type to one if it has a ruin
                    }
                    if (rc.canSenseRobotAtLocation(scanLoc)) {
                        RobotInfo robot = rc.senseRobotAtLocation(scanLoc);
                        if (Util.isTower(robot.type)) {
                            // Determine tower type based on additional properties
                            towerType = determineTowerType(robot);
                        }
                    }
                }

                if (mapInfo.getPaint() == PaintType.ENEMY_PRIMARY || mapInfo.getPaint() == PaintType.ENEMY_SECONDARY) {
                    enemyPaintCount++;
                } else if (mapInfo.getPaint() == PaintType.EMPTY) {
                    emptyPaintCount++;
                }
            }
        }

        return new ScanResult(towerType,
                convertTileCounts(enemyPaintCount),
                convertTileCounts(emptyPaintCount));
    }
}