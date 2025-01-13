package blitz2;

import battlecode.common.*;

public class BunnyComms extends Comms {

//    public final int MESSAGE_BUFFER_SIZE = 5;


    //    public final int MAP_COOLDOWN = 100;
    public final int MAP_COOLDOWN = 50;
    public final int MAP2_COOLDOWN = 50; // Used for larger maps


    public int lastMapUpdate = -(MAP_COOLDOWN + 1);
    public int lastMap2Update = -(MAP2_COOLDOWN + 1); // used for larger maps.

    public final int BUFFER_COOLDOWN = 10;
    public int lastBufferUpdate = -(BUFFER_COOLDOWN + 1);

    public final int NUM_ROUNDS_TO_WAIT_FOR_MAP_UPDATE = 4;

    // -1 is used to represent empty messages.
    public int[] messageBuffer = {-1,-1,-1,-1,-1}; // Buffer fills up at 5 sectors. Hardcoded for bytecode.
    public int messageBufferIndex = 0; // Stores the first invalid index.
    public int messagesTransmitted = 0;

    boolean waitingForMap = false;
    boolean waitingForMap2= false; // used for larger maps.

    public int mapRequestRound = -1;

    public BunnyComms(RobotController rc, Robot robot) {
        super(rc);
    }

    public void updateBunnyBuffer(int roundNum, int sectorID, int msg) throws GameActionException {
        assert roundNum <= 2001;
        assert sectorID < 144;
        messageBuffer[messageBufferIndex] = (roundNum << 16) + (sectorID << 8) + msg;
        Util.log("RoundNum = " + roundNum + ", sectorID = " + sectorID + ", msg = " + msg);
        Util.log("Bunny buffer updated to " + messageBuffer[messageBufferIndex]);
        messageBufferIndex++;
        messageBufferIndex %= messageBuffer.length;
        Util.log("New buffer index: " + messageBufferIndex);
    }

    /**
     * To be called when there is a friendly tower nearby. Chooses to send either buffer information or map request.
     */
    public void sendMessages(RobotInfo tower) throws GameActionException {

        // When the buffer cooldown expires, prioritize sending bunny memories.
        if(rc.getRoundNum() - lastBufferUpdate > BUFFER_COOLDOWN) {
            sendBufferUpdateMessage(tower);
        } else {
            Util.log("Robot " + rc.getID() + ": Buffer transmission is already complete. Skipping retransmission.");
        }

        // Otherwise, when the map cooldown expires, request a map.
        if(rc.getRoundNum() - lastMapUpdate > MAP_COOLDOWN) {
            // Map cooldown has expired, request a map.
            sendMapUpdateRequestMessage(tower);
        } else {
            // Map is fresh enough!
            Util.log("BunnyComms map is fresh enough! (no map request sent!)");
            Util.log("Last Map Update: " + lastMapUpdate + ", Current Round: " + rc.getRoundNum());
        }

        // If there is a larger map and the larger map hasn't been updated, request it.
        if(sectorCount >= MAX_MAP_SECTORS_SENT_PER_ROUND && (rc.getRoundNum() - lastMap2Update > MAP2_COOLDOWN) ) {
            sendMap2UpdateRequestMessage(tower);
            Util.log("Big map. Bunny " + rc.getID() + " just sent second request!");
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
            if(rc.canSendMessage(tower.getLocation())) {

                // Send the next message in the buffer.
                rc.sendMessage(tower.getLocation(), messageBuffer[messagesTransmitted]);
                Util.log("BunnyComms sendMessages successful to " + tower.getLocation());

                // Shift to next index of the buffer to transmit.
                messagesTransmitted++;
            } else {
                Util.log("BunnyComms sendMessages failed for " + tower.getLocation());
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
            Util.log("BunnyComms requested map from " + tower.getLocation());

            // Map transfer is complete and cooldown is reset.
            mapRequestRound = rc.getRoundNum();

            // Boolean updated to constrain motion and wait for map.
            waitingForMap = true;

        } else {
            Util.log("BunnyComms couldn't request map from " + tower.getLocation());
        }
    }

    /**
     * Bunny sends a request to a tower for a second part of the map. This is used for larger maps.
     */
    public void sendMap2UpdateRequestMessage(RobotInfo tower) throws GameActionException {
        if(rc.canSendMessage(tower.getLocation())) {
            rc.sendMessage(tower.getLocation(), MAP2_UPDATE_REQUEST_CODE);
            Util.log("BunnyComms requested map 2 from " + tower.getLocation());

            // Map transfer is complete and cooldown is reset.
            mapRequestRound = rc.getRoundNum();

            // Boolean updated to constrain motion and wait for map.
            waitingForMap2 = true;

        } else {
            Util.log("BunnyComms couldn't request map 2 from " + tower.getLocation());
        }
    }

    /**
     * Processes map updates and acts on received data.
     * Handles the first 80 sectors for large maps.
     */
    public void processMap() throws GameActionException {
        boolean successfulRequest = processMapUpdates(0, "BunnyComms loaded map 1!", rc.getRoundNum());
        waitingForMap = !successfulRequest;

    }

    /**
     * Processes map2 updates and acts on received data.
     * Handles the first 80 sectors for large maps.
     */
    public void processMap2() throws GameActionException {
        boolean successfulRequest = processMapUpdates(MAX_MAP_SECTORS_SENT_PER_ROUND, "BunnyComms loaded large map 2!", rc.getRoundNum());
        waitingForMap2 = !successfulRequest;
    }

    /**
     * Shared logic for processing map updates. Returns if the processing was successful.
     */
    private boolean processMapUpdates(int startIndex, String successMessage, int roundNum) throws GameActionException {
        Message[] messages = rc.readMessages(roundNum);
        if(messages.length > 0) {
            Util.log("Bunny " + rc.getID() + " received " + messages.length + " map messages");
        }

        if (messages.length == 0) return false;

        loadSectors(startIndex, messages);

        Util.log("Bunny has finished processing its new map.");
        Util.logArray("Bunny's new world", myWorld);

        lastMapUpdate = roundNum; // Refresh map update.
        Util.log(successMessage);
        return true;
    }

    // Starts at sectorIndex and converts messages into sectors from there.
    public void loadSectors(int sectorIndex, Message[] messages) throws GameActionException {
        for (Message message : messages) {
            int bytes = message.getBytes();
            for (int i = 0; i < 4 && sectorIndex < sectorCount; i++, sectorIndex++) {
                Util.log("Updating sector: " + sectorIndex + "/" + (sectorCount - 1));
                myWorld[sectorIndex] = bytes & 0xFF;
                bytes >>>= 8;
            }
        }
    }

    /**
     * Given a bunny's current location, updates the bunny's  world accordingly using visible sector.
     */
    public void updateSectorInVision(MapLocation currectLocation) throws GameActionException {
        int sectorIndex = getFullyEnclosedSectorID(currectLocation);


        // Checking bunny world
        Util.log("Bunny looking for a sector to update its world with");
        // If sector is -1, no sector is fully enclosed
        if(sectorIndex != -1) {
            // This has been tested! Scan result works!
            ScanResult sr = scanSector(sectorIndex);
//            Util.log(sr.toString());

            int encodedSector = encodeSector(sr);
            Util.log("Sector found.");
            Util.log("Sector Index: " + sectorIndex);
            Util.log("Sector Center: " + getSectorCenter(sectorIndex));

            // If this encoding is different from the known encoding, add the message to the buffer.
            if(encodedSector != myWorld[sectorIndex]) {
                updateBunnyBuffer(rc.getRoundNum(), sectorIndex, encodedSector);

                // update comms.myWorld with this new information
                myWorld[sectorIndex] = encodedSector;

                Util.log("New info. World updated.");
            }
            Util.log(Util.getSectorDescription(myWorld[sectorIndex]));

        } else {
            Util.log("No sector found");
        }

        // Check the bunny buffer
        Util.logArray("bunnyBuffer", messageBuffer);

    }
}