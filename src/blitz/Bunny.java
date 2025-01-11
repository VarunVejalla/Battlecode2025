package blitz;

import battlecode.common.*;

public class Bunny extends Robot {

    MapLocation[] knownAlliedPaintTowerLocs = new MapLocation[10];
    MapLocation nearestAlliedPaintTowerLoc;
    MapLocation destination; // long-term destination
    MapInfo[] nearbyMapInfos;
    RobotInfo[] nearbyFriendlies;
    RobotInfo[] nearbyOpponents;
    boolean tryingToReplenish = false;
    int knownSpawnTowersIdx = 0;
    MapLocation[] knownSpawnTowers = new MapLocation[5];
    MapLocation[] knownEnemyTowerLocs = new MapLocation[10];
    MapLocation[] emptyPotentialEnemyTowersLocs = new MapLocation[20];

    public Bunny(RobotController rc) throws GameActionException {
        super(rc);
        destination = Util.getRandomMapLocation();
        scanSurroundings();
        for(RobotInfo info : nearbyFriendlies){
            Util.log("Friendly: " + info);
            if(info.getTeam() == myTeam && Util.isTower(info.getType())){
                knownSpawnTowers[knownSpawnTowersIdx] = info.getLocation();
                knownSpawnTowersIdx++;
            }
        }
    }

    public void run() throws GameActionException {
        super.run();
        scanSurroundings();
        updateEnemyTowerLocs();
        updateSymmetries();
    }

    /**
     * Scan stuff around you (this method is executed at the beginning of every
     * turn)
     */
    public void scanSurroundings() throws GameActionException {
        nearbyMapInfos = rc.senseNearbyMapInfos();
        nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
        nearbyOpponents = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        updateNearestAlliedPaintTowerLoc();
    }


    /**
     * Update the nearest allied tower location to replenish paint from based on
     * surroundings
     */
    public void updateNearestAlliedPaintTowerLoc() throws GameActionException {
        // Update list of known allied paint towers.
        for(int i = 0; i < knownAlliedPaintTowerLocs.length; i++){
            if(knownAlliedPaintTowerLocs[i] == null){
                continue;
            }
            if(rc.canSenseRobotAtLocation(knownAlliedPaintTowerLocs[i])){
                RobotInfo info = rc.senseRobotAtLocation(knownAlliedPaintTowerLocs[i]);
                if(info == null || info.getTeam() != myTeam || !Util.isPaintTower(info.getType())){
                    knownAlliedPaintTowerLocs[i] = null;
                }
            }
        }


        for (RobotInfo bot : nearbyFriendlies) {
            if (bot.getTeam() != myTeam || !Util.isPaintTower(bot.getType())) {
                continue;
            }

            boolean alreadyIn = false;
            for (int i = 0; i < knownAlliedPaintTowerLocs.length; i++) {
                if (bot.getLocation().equals(knownAlliedPaintTowerLocs[i])) {
                    alreadyIn = true;
                    break;
                }
            }
            if (!alreadyIn) {
                for (int i = 0; i < knownAlliedPaintTowerLocs.length; i++) {
                    if (knownAlliedPaintTowerLocs[i] == null) {
                        knownAlliedPaintTowerLocs[i] = bot.getLocation();
                        break;
                    }
                }
            }
        }

        nearestAlliedPaintTowerLoc = null;
        int closestDist = Integer.MAX_VALUE;

        for(int i = 0; i < knownAlliedPaintTowerLocs.length; i++) {
            if(knownAlliedPaintTowerLocs[i] == null) {
                continue;
            }
            int dist = rc.getLocation().distanceSquaredTo(knownAlliedPaintTowerLocs[i]);
            if(dist < closestDist) {
                nearestAlliedPaintTowerLoc = knownAlliedPaintTowerLocs[i];
                closestDist = dist;
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
                    .distanceSquaredTo(nearestAlliedPaintTowerLoc) <= GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {

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
            Util.log("REPPPPPPPPPPPPPPPPPPPPP");
            Util.addToIndicatorString("REP");
        }

        else if (destination == null ||
                rc.getLocation().distanceSquaredTo(destination) <= Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION) {
            destination = Util.getRandomMapLocation();
        }
    }

    public void updateEnemyTowerLocs() throws GameActionException {
        // Clear existing slots
        for(int i = 0; i < knownEnemyTowerLocs.length; i++){
            if(knownEnemyTowerLocs[i] != null && rc.canSenseLocation(knownEnemyTowerLocs[i])){
                RobotInfo info = rc.senseRobotAtLocation(knownEnemyTowerLocs[i]);
                if(info == null || info.getTeam() != oppTeam || !Util.isTower(info.getType())){
                    knownEnemyTowerLocs[i] = null;
                }
            }
        }

        for(int i = 0; i < emptyPotentialEnemyTowersLocs.length; i++){
            if(emptyPotentialEnemyTowersLocs[i] == null){
                continue;
            }
            if(rc.canSenseRobotAtLocation(emptyPotentialEnemyTowersLocs[i])){
                RobotInfo info = rc.senseRobotAtLocation(emptyPotentialEnemyTowersLocs[i]);
                if(info != null && info.getTeam() != oppTeam && Util.isTower(info.getType())){
                    emptyPotentialEnemyTowersLocs[i] = null;
                }
            }
        }

        for(int s = 0; s < possibleSymmetries.length; s++){
            SymmetryType symmetry = possibleSymmetries[s];
            if(symmetry == null){
                continue;
            }
            for(int i = 0; i < knownSpawnTowers.length; i++){
                if(knownSpawnTowers[i] == null){
                    continue;
                }
                MapLocation potentialEnemyLoc = Util.applySymmetry(knownSpawnTowers[i], symmetry);
                if(rc.canSenseLocation(potentialEnemyLoc)){
                    RobotInfo info = rc.senseRobotAtLocation(potentialEnemyLoc);
                    if(info == null || info.getTeam() != oppTeam || !Util.isTower(info.getType())){
                        boolean alreadyIn = false;
                        for(int j = 0; j < emptyPotentialEnemyTowersLocs.length; j++){
                            if(potentialEnemyLoc.equals(emptyPotentialEnemyTowersLocs[j])){
                                alreadyIn = true;
                                break;
                            }
                        }
                        if(!alreadyIn){
                            for(int j = 0; j < emptyPotentialEnemyTowersLocs.length; j++){
                                if(emptyPotentialEnemyTowersLocs[j] == null){
                                    emptyPotentialEnemyTowersLocs[j] = potentialEnemyLoc;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        for(RobotInfo info : nearbyOpponents){
//            Util.log("Nearby opponent: " + info);
            if(Util.isTower(info.getType())){
                MapLocation enemyTowerLoc = info.getLocation();
                boolean alreadyIn = false;
                for(int i = 0; i < 10; i++){
                    if(enemyTowerLoc.equals(knownEnemyTowerLocs[i])){
                        alreadyIn = true;
                        break;
                    }
                }
                if(!alreadyIn){
                    for(int i = 0; i < 10; i ++){
                        if(knownEnemyTowerLocs[i] == null){
                            knownEnemyTowerLocs[i] = enemyTowerLoc;
                            break;
                        }
                    }
                }
            }
        }
    }

    void updateSymmetries() throws GameActionException {
        for(int i = 0; i < possibleSymmetries.length; i++){
            SymmetryType symmetry = possibleSymmetries[i];
            if(symmetry == null){
                continue;
            }
            for(int j = 0; j < knownSpawnTowersIdx; j++){
                MapLocation symmetryLoc = Util.applySymmetry(knownSpawnTowers[j], symmetry);
                if(rc.canSenseLocation(symmetryLoc)){
                    // Check if symmetry is violated.
                    RobotInfo info = rc.senseRobotAtLocation(symmetryLoc);
                    MapInfo tile = rc.senseMapInfo(symmetryLoc);
                    boolean symmetryViolated = true;
                    if(tile.hasRuin()) {
                        symmetryViolated = false;
                    } else if(info != null && Util.isTower(info.getType())){
                        symmetryViolated = false;
                    }
                    if(symmetryViolated){
                        possibleSymmetries[i] = null;
                        break;
                    }
                }
            }
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
