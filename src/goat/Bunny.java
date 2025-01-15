package goat;

import battlecode.common.*;

enum TowerType {
    PaintTower, MoneyTower, DefenseTower;

    public static TowerType from(UnitType unitType){
        if(Util.isPaintTower(unitType)){
            return PaintTower;
        }
        if(Util.isMoneyTower(unitType)){
            return MoneyTower;
        }
        return DefenseTower;
    }
}

enum SymmetryType {
    HORIZONTAL,
    VERTICAL,
    ROTATIONAL,
    DIAGONAL_RIGHT,
    DIAGONAL_LEFT
}

public abstract class Bunny extends Robot {

    MapLocation[] knownAlliedTowerLocs = new MapLocation[20];
    TowerType[] knownAlliedTowerTypes = new TowerType[20];
    TowerType nearestAlliedTowerType;
    MapLocation nearestAlliedPaintTowerLoc;
    MapLocation destination; // long-term destination
    MapInfo[] nearbyMapInfos;
    RobotInfo[] nearbyFriendlies;
    RobotInfo[] nearbyOpponents;
    boolean tryingToReplenish = false;
    BunnyComms comms = new BunnyComms(rc, this);
    SymmetryType[] possibleSymmetries = {SymmetryType.HORIZONTAL, SymmetryType.VERTICAL, SymmetryType.ROTATIONAL, SymmetryType.DIAGONAL_LEFT, SymmetryType.DIAGONAL_RIGHT};

    public Bunny(RobotController rc) throws GameActionException {
        super(rc);
        MarkingUtils.bunny = this;
        MarkingUtils.rc = rc;
        destination = Util.getRandomMapLocation();
        if(this.mapHeight != this.mapWidth){
            for(int i = 0; i < possibleSymmetries.length; i++){
                if(possibleSymmetries[i] == SymmetryType.DIAGONAL_LEFT || possibleSymmetries[i] == SymmetryType.DIAGONAL_RIGHT){
                    possibleSymmetries[i] = null;
                }
            }
        }
    }

    public void run() throws GameActionException {
        super.run();
        // Comms is run inside of scan surroundings (and nearest allied paint tower, which is called in surroundings)!
        scanSurroundings();

        // If waiting for a map, stay in place. Otherwise, move!
        if(comms.waitingForMap){ // don't move if we're waiting to receive a map from a tower
            Util.log("Bunny @ " + rc.getLocation() + ". Pausing movement because I'm waiting for a map!");
        }
    }

    public boolean canMove() {
        return rc.isMovementReady(); //&& !comms.waitingForMap;
    }

    /**
     * Evalute the sectors that are neighboring your current sector and move towards the best one.
     */
    // TODO: There are bugs in this method. This needs to be checked and not used yet.
    public void macroMove() throws GameActionException {
        int bestScore = Integer.MIN_VALUE;
        int bestSector = -1;
        int[] neighborSectorIndexes = comms.getSectorAndNeighbors(myLoc);
        int sectorScore;

        for (int neighorSectorIndex : neighborSectorIndexes) {
            sectorScore = evaluateSector(comms.myWorld[neighorSectorIndex]);
            if(sectorScore > bestScore) {
                bestScore = sectorScore;
                bestSector = neighorSectorIndex;
            }
        }

        // Go to the center of that sector.
        nav.goTo(comms.getSectorCenter(bestSector), 0);
    }

    /**
     * Evalute the encoded information about each sector depending on the specific Bunny implementation.
     * Returns an int score. Higher scores are considered better.
     */
    public abstract int evaluateSector(int encodedSector) throws GameActionException;

    /**
     * Scan stuff around you (this method is executed at the beginning of every
     * turn)
     */
    public void scanSurroundings() throws GameActionException {

        nearbyMapInfos = Util.getFilledInMapInfo(rc.senseNearbyMapInfos());
        nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
        nearbyOpponents = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        // COMMS IS HERE
        // Find sector that is fully enclosed and update bunny world.
        comms.updateSectorInVision(rc.getLocation());

        // If you requested a map, wait for the tower to send it.
        if(comms.waitingForMap) {
            comms.processMap();
        } else if (comms.waitingForMap2) {
            comms.processMap2();
        }

        // Updates both nearest allied paint tower and nearest allied tower.
        updateKnownTowers();
        setNearestAlliedTowers();
    }

    public void updateKnownTowers() throws GameActionException {
        // Filter out old tower locations.
        for (int i = 0; i < knownAlliedTowerLocs.length; i++) {
            if (knownAlliedTowerLocs[i] == null) {
                continue;
            }
            if (rc.canSenseLocation(knownAlliedTowerLocs[i])) {
                RobotInfo info = rc.senseRobotAtLocation(knownAlliedTowerLocs[i]);
                if (info == null || info.getTeam() != myTeam) {
                    knownAlliedTowerLocs[i] = null;
                    knownAlliedTowerTypes[i] = null;
                } else {
                    knownAlliedTowerTypes[i] = TowerType.from(info.getType());
                }
            }
        }

        // Add in any new locations.
        for (RobotInfo bot : nearbyFriendlies) {
            if (!Util.isTower(bot.getType())) {
                continue;
            }

            MapLocation currAlliedTowerLocation = bot.getLocation();

            // TODO This is wasteful. We request a map multiple times even if ours is currently being serviced.
            // Always sendMessages if you're in range of a tower. The sendMessages method assesses what message to send.

            // Util.log("Bunny " + rc.getID() + " at " + rc.getLocation() + " found a tower nearby " + bot.getLocation());
            comms.sendMessages(bot);

            boolean alreadyIn = false;
            int nullIdx = -1;
            for (int i = 0; i < knownAlliedTowerLocs.length; i++) {
                if (knownAlliedTowerLocs[i] == null) {
                    nullIdx = i;
                    continue;
                }
                if (currAlliedTowerLocation.equals(knownAlliedTowerLocs[i])) {
                    alreadyIn = true;
                }
            }
            if (!alreadyIn && nullIdx != -1) {
                knownAlliedTowerLocs[nullIdx] = currAlliedTowerLocation;
                knownAlliedTowerTypes[nullIdx] = TowerType.from(bot.getType());
            }
        }
    }

    /**
     * Update the nearest allied tower location to replenish paint from based on
     * surroundings
     */
    public void setNearestAlliedTowers() throws GameActionException {
        // TODO: Current behavior may cause paint tower to switch between two constantly while tryna navigate to it.
        // TODO: If that happens, then we needa hold a consistent nearestAlliedPaintTowerLoc, so the code below might help.
        int maxDist = Integer.MAX_VALUE;
        int maxPaintDist = Integer.MAX_VALUE;
        nearestAlliedPaintTowerLoc = null;
        nearestAlliedTowerType = null;
        MapLocation myLocation = rc.getLocation();
        for(int i = 0; i < knownAlliedTowerLocs.length; i++){
            if(knownAlliedTowerLocs[i] == null){
                continue;
            }
            int dist = myLocation.distanceSquaredTo(knownAlliedTowerLocs[i]);
            if(dist < maxDist){
                maxDist = dist;
                nearestAlliedTowerType = knownAlliedTowerTypes[i];
            }
            if(knownAlliedTowerTypes[i] == TowerType.PaintTower && dist < maxPaintDist){
                nearestAlliedPaintTowerLoc = knownAlliedTowerLocs[i];
                maxPaintDist = dist;
            }
        }
    }

    /**
     * Tries to replenish paint from the nearest allied tower if within range to
     * transfer paint
     */
    public void tryReplenish() throws GameActionException {
//        if (nearestAlliedPaintTowerLoc == null) return;

        if (rc.getLocation()
                .distanceSquaredTo(nearestAlliedPaintTowerLoc) <= GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {
            int towerPaintQuantity = rc.senseRobotAtLocation(nearestAlliedPaintTowerLoc).getPaintAmount();
            int paintToFillUp = Math.min(
                    rc.getType().paintCapacity - rc.getPaint(), // amount of paint needed to fully top off
                    towerPaintQuantity); // amount of paint available in the tower

            if (rc.isActionReady() && rc.canTransferPaint(nearestAlliedPaintTowerLoc, -paintToFillUp)) {
                rc.transferPaint(nearestAlliedPaintTowerLoc, -paintToFillUp);
            }

            if (checkIfImDoneReplenishing()) {
                // Util.log("DONE REPLENISHING");
                tryingToReplenish = false;
            }
        }
    }

    /**
     * Determine whether current you should go back to an ally tower to replenish on
     * paint
     * based on current paint quantity and distance to nearest tower.
     */
    public boolean checkIfIShouldStartReplenishing() throws GameActionException {
        // TODO: make this a more intelligent decision based on factors like:
        // - distance to nearest tower
        // - whether you're really close to finishing a pattern, in which case you
        // should consider sacrificing yourself for the greater good

        return rc.getPaint() <= Constants.PAINT_THRESHOLD_TO_REPLENISH;
    }

    public boolean checkIfImDoneReplenishing() throws GameActionException {
        // TODO: make this a more intelligent decision based on factors like:
        // - how much paint the tower im getting it from has
        return rc.getPaint() >= rc.getType().paintCapacity * 0.8;
    }


    /**
     * Finds a ruin that is not claimed by your team.
     */
    public MapInfo findUnmarkedRuin() throws GameActionException {
        // MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyMapInfos) {
            if (rc.canSenseLocation(tile.getMapLocation()) && tile.hasRuin()) {
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
        if (nearestAlliedPaintTowerLoc != null && (tryingToReplenish || checkIfIShouldStartReplenishing())) {
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
