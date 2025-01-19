package bettermoppers;

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

    public String toString(){
        return switch (this) {
            case PaintTower -> "PT";
            case MoneyTower -> "MT";
            case DefenseTower -> "DT";
        };
    }
}

enum SymmetryType {
    HORIZONTAL,
    VERTICAL,
    ROTATIONAL
}

public abstract class Bunny extends Robot {
    final MapLocation noRuinLoc = new MapLocation(-1, -1);
    MapLocation[] knownRuinsBySector = new MapLocation[144];
    MapLocation[] knownAlliedTowerLocs = new MapLocation[10];
    TowerType[] knownAlliedTowerTypes = new TowerType[10];
    MapLocation nearestAlliedTowerLoc;
    TowerType nearestAlliedTowerType;
    MapLocation nearestAlliedPaintTowerLoc;
    MapLocation destination; // long-term destination
    MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
    MapInfo[] nearbyMapInfos;
    RobotInfo[] nearbyFriendlies;
    RobotInfo[] nearbyOpponents;
    boolean tryingToReplenish = false;
    BunnyComms comms = new BunnyComms(rc, this);
    MapLocation prevUpdateLoc;
    boolean symmetryUpdate = false;
    SymmetryType[] possibleSymmetries = {SymmetryType.HORIZONTAL, SymmetryType.VERTICAL, SymmetryType.ROTATIONAL};

    public Bunny(RobotController rc) throws GameActionException {
        super(rc);
        MarkingUtils.bunny = this;
        MarkingUtils.rc = rc;
        destination = Util.getRandomMapLocation();
    }

    public void run() throws GameActionException {
        super.run();
        // Comms is run inside of scan surroundings (and nearest allied paint tower, which is called in surroundings)!
        scanSurroundings();
        checkForUpgrades();

//        if(tryingToReplenish) {
//            Util.addToIndicatorString("REP");
//        }
//        if(rc.getRoundNum() > 270) rc.resign();
//        if(rc.getRoundNum() == 260) comms.describeWorld();
    }

    public void checkForUpgrades() throws GameActionException {
        int threshold = Integer.MAX_VALUE;
        for (RobotInfo friendlyRobot : rc.senseNearbyRobots(GameConstants.BUILD_TOWER_RADIUS_SQUARED, myTeam)) {
            if (friendlyRobot.getType().isTowerType()) {
                if (friendlyRobot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                    threshold = 2500;
                } else if (friendlyRobot.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                    threshold = 2550;
                } else if (friendlyRobot.getType() == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
                    threshold = 2600;
                } else if (friendlyRobot.getType() == UnitType.LEVEL_TWO_PAINT_TOWER) {
                    threshold = 5000;
                } else if (friendlyRobot.getType() == UnitType.LEVEL_TWO_MONEY_TOWER) {
                    threshold = 5050;
                } else if (friendlyRobot.getType() == UnitType.LEVEL_TWO_DEFENSE_TOWER) {
                    threshold = 5100;
                }
                if (rc.getChips() >= threshold) {
                    rc.upgradeTower(friendlyRobot.getLocation());
                }
                return;
            }
        }
    }

    public boolean canMove() {
        if(comms.waitingForMap || comms.waitingForMap2) {
           Util.addToIndicatorString("Waiting for a map");
        }
        return rc.isMovementReady() && !comms.waitingForMap && !comms.waitingForMap2;
    }

    /**
     * Evalute the sectors that are neighboring your current sector and move towards the best one.
     */
    public void macroMove(int dist_to_best_sector) throws GameActionException {
        int bestScore = 0;
        int bestSector = -1;
        int[] neighborSectorIndexes = comms.getSectorAndNeighbors(myLoc, 1);
        int sectorScore;

        for (int neighorSectorIndex : neighborSectorIndexes) {
            sectorScore = evaluateSector(comms.myWorld[neighorSectorIndex]);
            if(sectorScore > bestScore) {
                bestScore = sectorScore;
                bestSector = neighorSectorIndex;
            }
        }

        if(bestSector != -1) {
            // Go to the center of that sector.
            nav.goToBug(comms.getSectorCenter(bestSector), dist_to_best_sector);
        } else {
            // Goes to random destination
            nav.goToBug(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
            // Go towards the center
        }

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
    // 8k bytecode
    public void scanSurroundings() throws GameActionException {
        // 300 bytecode
        nearbyMapInfos = Util.getFilledInMapInfo(rc.senseNearbyMapInfos());
        nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
        nearbyOpponents = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        // COMMS IS HERE
        // Find sector that is fully enclosed and update bunny world.
        // 2.2k bytecode
        comms.updateSectorInVision(rc.getLocation());

        // If you requested a map, wait for the tower to send it.
        // 2k bytecode
        if(comms.waitingForMap) {
            comms.processMap();
        } else if (comms.waitingForMap2) {
            comms.processMap2();
        }

        // Updates both nearest allied paint tower and nearest allied tower.
        // 1.7k bytecode
        updateKnownTowers();
        // 200 bytecode
        setNearestAlliedTowers();
        // Faster now I think
        updateKnownRuinsAndSymmetries();
    }

    public void updateKnownRuinsAndSymmetries() throws GameActionException {
        // TODO: Should optimize this by only searching new squares based on previous move.
//        for(MapInfo info : nearbyMapInfos){
        Direction lastMoveDir = null;
        if(prevUpdateLoc != null){
            lastMoveDir = prevUpdateLoc.directionTo(rc.getLocation());
        }
        int[] indices = Util.getNewVisionIndicesAfterMove(lastMoveDir);
        for(int idx : indices) {
            MapInfo info = nearbyMapInfos[idx];
            if(info == null || !info.hasRuin()){
                continue;
            }
            MapLocation infoLoc = info.getMapLocation();
            // There can only be one ruin per sector index.
            int sectorIdx = comms.getSectorIndex(infoLoc);
            if(knownRuinsBySector[sectorIdx] == null){
                knownRuinsBySector[sectorIdx] = infoLoc;
            }

            if(possibleSymmetries.length > 1) {
                for (int i = 0; i < possibleSymmetries.length; i++) {
                    SymmetryType symmetry = possibleSymmetries[i];
                    if (symmetry == null) {
                        continue;
                    }
                    MapLocation symmetryLoc = Util.applySymmetry(infoLoc, symmetry);
                    int symmetrySectorIdx = comms.getSectorIndex(symmetryLoc);
                    if(knownRuinsBySector[symmetrySectorIdx] == null){
                        continue;
                    }

                    // If there's no ruin in the symmetry sector (in which case its -1, -1), or there's no smth, the symmetry is invalid.
                    if(!knownRuinsBySector[symmetrySectorIdx].equals(symmetryLoc)){
                        possibleSymmetries[i] = null;
                    }
                }
            }
        }

        // If there's no ruin at all in the sector, set it to (-1, -1). Check if this can also eliminate any symmetries.
        int fullyEnclosedSectorID = comms.getFullyEnclosedSectorID(rc.getLocation());
        if(fullyEnclosedSectorID != -1 && knownRuinsBySector[fullyEnclosedSectorID] == null){
            knownRuinsBySector[fullyEnclosedSectorID] = noRuinLoc;

            if(possibleSymmetries.length > 1) {
                for (int i = 0; i < possibleSymmetries.length; i++) {
                    SymmetryType symmetry = possibleSymmetries[i];
                    if (symmetry == null) {
                        continue;
                    }
                    MapLocation symmetryLoc = Util.applySymmetry(rc.getLocation(), symmetry);
                    int symmetrySectorIdx = comms.getSectorIndex(symmetryLoc);
                    if(knownRuinsBySector[symmetrySectorIdx] == null){
                        continue;
                    }

                    // If there's a ruin in the symmetry sector, the symmetry is invalid.
                    if(!knownRuinsBySector[symmetrySectorIdx].equals(noRuinLoc)){
                        possibleSymmetries[i] = null;
                    }
                }
            }
        }
        prevUpdateLoc = rc.getLocation();
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
        nearestAlliedTowerLoc = null;
        MapLocation myLocation = rc.getLocation();
        for(int i = 0; i < knownAlliedTowerLocs.length; i++){
            if(knownAlliedTowerLocs[i] == null){
                continue;
            }
            int dist = myLocation.distanceSquaredTo(knownAlliedTowerLocs[i]);
            if(dist < maxDist){
                maxDist = dist;
                nearestAlliedTowerLoc = knownAlliedTowerLocs[i];
                nearestAlliedTowerType = knownAlliedTowerTypes[i];
            }
            if(knownAlliedTowerTypes[i] == TowerType.PaintTower && dist < maxPaintDist){
                nearestAlliedPaintTowerLoc = knownAlliedTowerLocs[i];
                maxPaintDist = dist;
            }
        }
    }

    public void replenishLogic() throws GameActionException {
        MapLocation homebase = nearestAlliedPaintTowerLoc;
        // TODO: Make this smarter. Wander around friendly territory till you find someone to replenish you.
        if(homebase == null){
            homebase = nearestAlliedTowerLoc;
        }
        // In the case they're both null, search through comms
        if(homebase == null) {
            // TODO if theres a money tower go there too
//            int[] neighborSectorIndexes = comms.getSectorAndNeighbors(myLoc, 2);
//            for(int sectorIndex : neighborSectorIndexes){
//                int encodedSector = comms.myWorld[sectorIndex];
//                int tower = (encodedSector >> 1) & 0b111;
//                // If there's a friendly paint tower, go there
//                if(tower == 2) {
//                    homebase = comms.getSectorCenter(sectorIndex);
//                    break;
//                }
//            }
//            if(homebase == null) {
//                return;
//            }
            return;
        }

        if(rc.getLocation().distanceSquaredTo(homebase) > 9) {
            nav.goToBug(homebase, 0);
        } else {
            tryReplenish();
            nav.goToFuzzy(homebase, 0);
            if(rc.isActionReady()){
                tryReplenish();
            }
        }
    }

    /**
     * Tries to replenish paint from the nearest allied tower if within range to
     * transfer paint
     */
    public void tryReplenish() throws GameActionException {
        if (nearestAlliedPaintTowerLoc == null) return;

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
    public abstract boolean checkIfIShouldStartReplenishing() throws GameActionException;

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
        if ((nearestAlliedPaintTowerLoc != null || nearestAlliedTowerLoc != null) && (tryingToReplenish || checkIfIShouldStartReplenishing())) {
            if(nearestAlliedPaintTowerLoc != null){
                destination = nearestAlliedPaintTowerLoc;
            } else {
                destination = nearestAlliedTowerLoc;
            }
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
