package commsTesting;

import battlecode.common.*;

public class Bunny extends Robot {

    MapLocation nearestAlliedTowerLoc;
    MapLocation nearestAlliedPaintTowerLoc;
    MapLocation destination; // long-term destination
    MapInfo[] nearbyMapInfos;
    RobotInfo[] nearbyFriendlies;
    RobotInfo[] nearbyOpponents;
    boolean tryingToReplenish = false;


    boolean isSendingMessages = false;
    boolean isWaitingToReceiveMessages = false;


    BunnyComms comms = new BunnyComms(rc, this);

    public Bunny(RobotController rc) throws GameActionException {
        super(rc);
        destination = Util.getRandomMapLocation();
    }

    public void run() throws GameActionException {
        super.run();
        scanSurroundings();
    }

    /**
     * Scan stuff around you (this method is executed at the beginning of every
     * turn)
     */
    public void scanSurroundings() throws GameActionException {
        nearbyMapInfos = rc.senseNearbyMapInfos();
        nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
        nearbyOpponents = rc.senseNearbyRobots(-1, rc.getTeam().opponent());


        // HERE IS COMMS
        // Find sector index that is fully enclosed.
        int sectorIndex = comms.getFullyEnclosedSectorID(rc.getLocation());
        // If sector is -1, no sector is fully enclosed
        if(sectorIndex != -1) {
            // This has been tested! Scan result works!
            ScanResult sr = comms.scanSector(sectorIndex);

            Util.log("Sector Index: " + sectorIndex);
            Util.log("Sector Center: " + comms.getSectorCenter(sectorIndex));
            Util.log(sr.toString());

            int encodedSector = comms.encodeSector(sr);

            Util.log("Encoded Sector: " + encodedSector);

            // If this encoding is different from the known encoding, add the message to the buffer.
            if(encodedSector != comms.myWorld[sectorIndex]) {
                comms.updateBunnyBuffer(rc.getRoundNum(), sectorIndex, encodedSector);

                // update comms.myWorld with this new information
                comms.myWorld[sectorIndex] = encodedSector;
            }
        }

        // Check the bunny buffer
        Util.logArray("bunnyBuffer", comms.messageBuffer);
        // Checking bunny world
        Util.log("Bunny looking for a sector to update its world with");
        if(sectorIndex == -1) {
            Util.log("No sector found");
        }
        else {
            Util.log(Util.getSectorDescription(comms.myWorld[sectorIndex]));
        }


        // Updates both nearest allied paint tower and nearest allied tower.
        updateNearestAlliedPaintTowerLoc();

    }


    /**
     * Update the nearest allied tower location to replenish paint from based on
     * surroundings
     */
    public void updateNearestAlliedPaintTowerLoc() throws GameActionException {
        for (RobotInfo bot : nearbyFriendlies) {
            if (!Util.isTower(bot.getType())) {
                continue;
            }

            MapLocation currAlliedTowerLocation = bot.getLocation();
            MapLocation myLocation = rc.getLocation();

            // TODO Run comms here.


            if(comms.waitingForMap) {  // if we have previously requested a map from a tower and are waiting for a tower to send it...
//                rc.resign();
                comms.processMap();  // try reading the map

                // if we've waited long enough for a map update, stop waiting
                if(rc.getRoundNum() - comms.mapRequestRound > comms.NUM_ROUNDS_TO_WAIT_FOR_MAP_UPDATE) {
                    comms.waitingForMap = false;
                    comms.mapRequestRound = -1;
                }
            }
            else {
                // if we're not waiting for a map, send whatever messages we have in the buffer
                comms.sendMessages(bot);
            }


            // Update nearest allied tower location
            if (nearestAlliedTowerLoc == null ||
                    myLocation.distanceSquaredTo(currAlliedTowerLocation) < myLocation.distanceSquaredTo(nearestAlliedTowerLoc)) {
                nearestAlliedTowerLoc = currAlliedTowerLocation;
            }

            // Update nearest allied paint tower location
            if (Util.isPaintTower(bot.getType()) &&
                    (nearestAlliedPaintTowerLoc == null ||
                            myLocation.distanceSquaredTo(currAlliedTowerLocation) < myLocation.distanceSquaredTo(nearestAlliedPaintTowerLoc))) {
                nearestAlliedPaintTowerLoc = currAlliedTowerLocation;
            }
        }
    }

    /**
     * Tries to replenish paint from the nearest allied tower if within range to
     * transfer paint
     */
    public void tryReplenish() throws GameActionException {
        if (nearestAlliedPaintTowerLoc != null) {
            if (rc.getLocation()
                    .distanceSquaredTo(nearestAlliedPaintTowerLoc) <= GameConstants.PAINT_TRANSFER_RADIUS_SQUARED && rc.canSenseRobotAtLocation(nearestAlliedPaintTowerLoc)) {

                int towerPaintQuantity = rc.senseRobotAtLocation(nearestAlliedPaintTowerLoc).getPaintAmount();
                int paintToFillUp = Math.min(
                        rc.getType().paintCapacity - rc.getPaint(), // amount of paint needed to fully top off
                        towerPaintQuantity); // amount of paint available in the tower

                if (rc.isActionReady() && rc.canTransferPaint(nearestAlliedPaintTowerLoc, -paintToFillUp)) {
                    rc.transferPaint(nearestAlliedPaintTowerLoc, -paintToFillUp);
                }

                if (!checkIfIShouldReplenish()) {
                    tryingToReplenish = false;
                }
            }

        }
    }

    /**
     * Determine whether current you should go back to an ally tower to replenish on
     * paint
     * based on current paint quantity and distance to nearest tower.
     */
    public boolean checkIfIShouldReplenish() throws GameActionException {
        // TODO: make this a more intelligent decision based on factors like:
        // - distance to nearest tower
        // - whether you're really close to finishing a pattern, in which case you
        // should consider sacrificing yourself for the greater good
        // - how much paint you have left

        return rc.getPaint() <= Constants.PAINT_THRESHOLD_TO_REPLENISH;
    }

    /**
     * Finds a ruin that is not claimed by your team.
     */
    public MapInfo findUnmarkedRuin() throws GameActionException {
        // MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyMapInfos) {
            if (tile.hasRuin()) {
                RobotInfo robotAtRuin = rc.senseRobotAtLocation(tile.getMapLocation());
                // We want a ruin either unoccupied or not controlled by our team
                if (robotAtRuin == null || robotAtRuin.team != rc.getTeam()) {
                    return tile;
                }
            }
        }
        return null;
    }

    /**
     * Update the destination we're travelling to if we need to replenish, or we've
     * reached our current destination
     */
    public void updateDestinationIfNeeded() throws GameActionException {
        if (nearestAlliedPaintTowerLoc != null && (tryingToReplenish || checkIfIShouldReplenish())) {
            destination = nearestAlliedPaintTowerLoc;
            tryingToReplenish = true;
            Util.addToIndicatorString("REP");
        }

        else if (destination == null ||
                rc.getLocation().distanceSquaredTo(destination) <= Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION) {
            destination = Util.getRandomMapLocation();
        }
    }

    public MapLocation findEmptyTiles() throws GameActionException {
        // MapInfo[] visionTiles = rc.senseNearbyMapInfos();
        int emptyX = 0;
        int emptyY = 0;
        int emptyCount = 0;
        for (MapInfo tile : nearbyMapInfos) {
            if (tile.getPaint() == PaintType.EMPTY) {
                emptyX += tile.getMapLocation().x;
                emptyY += tile.getMapLocation().y;
                emptyCount++;
            }
        }

        if (emptyCount == 0) {
            return null;
        }

        emptyX /= emptyCount;
        emptyY /= emptyCount;

        return new MapLocation(emptyX, emptyY);
    }

    public MapLocation findEnemyPaintCOM() throws GameActionException {
        // MapInfo[] visionTiles = rc.senseNearbyMapInfos();
        int enemyPaintX = 0;
        int enemyPaintY = 0;
        int enemyPaintCount = 0;
        for (MapInfo tile : nearbyMapInfos) {
            if (tile.getPaint().isEnemy()) {
                enemyPaintX += tile.getMapLocation().x;
                enemyPaintY += tile.getMapLocation().y;
                enemyPaintCount++;
            }
        }

        if (enemyPaintCount == 0) {
            return null;
        }

        enemyPaintX /= enemyPaintCount;
        enemyPaintY /= enemyPaintCount;

        return new MapLocation(enemyPaintX, enemyPaintY);
    }



}
