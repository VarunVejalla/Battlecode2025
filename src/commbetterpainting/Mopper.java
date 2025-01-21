package commbetterpainting;

import battlecode.common.*;

public class Mopper extends Bunny {
    boolean enemyPaintNearby = false;
    boolean enemyNearby = false;
    RobotInfo[] enemyTowerInfos;
    public Mopper(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        updateDestinationIfNeeded();

        enemyNearby = false;
        enemyPaintNearby = false;

        RobotInfo[] enemyInfos = rc.senseNearbyRobots(GameConstants.VISION_RADIUS_SQUARED, oppTeam);
        int numEnemyTowerInfos = 0;
        for(int i = 0; i < enemyInfos.length; i++){
            if(enemyInfos[i].getType().isTowerType()){
                enemyInfos[numEnemyTowerInfos] = enemyInfos[i];
                numEnemyTowerInfos++;
            }
        }
        enemyTowerInfos = new RobotInfo[numEnemyTowerInfos];
        for(int i = 0; i < numEnemyTowerInfos; i++){
            enemyTowerInfos[i] = enemyInfos[i];
        }

        Util.logBytecode("Before calcing heuristics");
        MapLocation heuristicLocation = calcHeuristics();
        Util.logBytecode("After calcing heuristics");

        Util.log("Location at beginning of round: " + rc.getLocation());
        if(enemyNearby || enemyPaintNearby) {
            if (rc.isActionReady()) {
                forceMopperMove(heuristicLocation);
                Util.log("Location after force nav: " + rc.getLocation());
                Util.addToIndicatorString("DBA");
                doBestAction();
            } else {
                mopperSafeNav();
                Util.log("Location after safe nav: " + rc.getLocation());
            }
        } else {
            moveLogic();
            Util.log("Location after move logic: " + rc.getLocation());
        }

        sharedEndFunction();
    }

    public boolean checkIfIShouldStartReplenishing() throws GameActionException {
        return rc.getPaint() <= Constants.PAINT_THRESHOLD_TO_REPLENISH;
    }

    /**
     * Returns a score evaluating how favorable it would be for this robot to move to this sector.
     */
    public int evaluateSector(int encodedSector) {
        ScanResult sr = comms.decodeSector(encodedSector);
        if(sr.enemyPaintLevel >= 1) {
            return 1;
        }
        return 0;
    }

    /**
     * Choose where to move:
     * - If there’s an ally-marked empty tile, move toward it to paint/attack.
     * - Otherwise move randomly.
     */
    public void moveLogic() throws GameActionException {
        myLoc = rc.getLocation();

        // Move in the direction
        int bestSector = getBestSector();
        if(bestSector != -1) {
            // Go to the center of that sector.
            mopperNav(comms.getSectorCenter(bestSector));
        } else {
            // Goes to random destination
            mopperNav(destination);
            // Go towards the center
        }
    }

    public MapLocation getTileToMop() throws GameActionException {
        MapInfo[] actionableTiles = rc.senseNearbyMapInfos(2);
        for (MapInfo tile : actionableTiles) {
            if (tile.getPaint().isEnemy() && rc.canAttack(tile.getMapLocation())) {
                return tile.getMapLocation();
            }
        }
        return null;
    }

    public void forceMopperMove(MapLocation target) throws GameActionException {
        Util.addToIndicatorString("FMN" + target);
        Direction toTarget = myLoc.directionTo(target);
        Direction[] moveOptions = {
                toTarget,
                toTarget.rotateLeft(),
                toTarget.rotateRight(),
                toTarget.rotateLeft().rotateLeft(),
                toTarget.rotateRight().rotateRight(),
        };
        generalMopperNav(moveOptions, target, true, 0, 2, 4, 1, 100);
    }


    public void mopperSafeNav() throws GameActionException {
        Util.addToIndicatorString("SMN");
        generalMopperNav(Direction.allDirections(), null, false, 0, 10, 20, 5, 100);
    }

    public void generalMopperNav(Direction[] moveOptions, MapLocation target, boolean force_lower_dist, int allyPaintHeuristic, int emptyPaintHeuristic, int enemyPaintHeuristic, int nearbyAllyHeuristic, int nearbyEnemyTowerHeuristic) throws GameActionException {
        Direction bestDir = null;
        int leastNumMoves = Integer.MAX_VALUE;
        int leastHeuristic = Integer.MAX_VALUE;

        int numMoves = 0;
        int distance = 0;
        for (Direction dir : moveOptions) {
            MapLocation newLoc = myLoc.add(dir);

            if (dir != Direction.CENTER && !rc.canMove(dir)) {
                continue;
            }

            if (!rc.canSenseLocation(newLoc) || !rc.sensePassability(newLoc)) {
                continue;
            }

            if(target != null){
                numMoves = Util.minMovesToReach(newLoc, target);
                int distanceSquared = newLoc.distanceSquaredTo(target);
                distance = (int)Math.sqrt(distanceSquared);
            }

            MapInfo info = rc.senseMapInfo(newLoc);

            int paintHeuristic = allyPaintHeuristic;
            if(info.getPaint() == PaintType.EMPTY){
                paintHeuristic = emptyPaintHeuristic;
            } else if(info.getPaint().isEnemy()){
                paintHeuristic = enemyPaintHeuristic;
            }

            int numAllies = rc.senseNearbyRobots(newLoc, 2, myTeam).length;
            int allyHeuristic = numAllies * nearbyAllyHeuristic;

            int enemyTowerHeuristic = 0;
            for(RobotInfo enemyInfo : enemyTowerInfos){
                if(enemyInfo.getLocation().distanceSquaredTo(newLoc) <= enemyInfo.getType().actionRadiusSquared){
                    enemyTowerHeuristic += nearbyEnemyTowerHeuristic;
                }
            }

            int heuristic = numMoves + distance + paintHeuristic + allyHeuristic + enemyTowerHeuristic;
            Util.log("Direction: " + dir + ", heuristic: " + heuristic + ", numMoves: " + numMoves + ", distance: " + distance + ", paintHeuristic: " + paintHeuristic + ", alllyHeuristic: " + allyHeuristic + ", enemyTowerHeuristic: " + enemyTowerHeuristic);

            if(force_lower_dist){
                if (numMoves < leastNumMoves || (numMoves == leastNumMoves && heuristic < leastHeuristic)) {
                    leastNumMoves = numMoves;
                    leastHeuristic = heuristic;
                    bestDir = dir;
                }
            } else {
                if (heuristic < leastHeuristic) {
                    leastHeuristic = heuristic;
                    bestDir = dir;
                }
            }
        }

        Util.log("Best dir: " + bestDir);
        Util.addToIndicatorString("BD" + bestDir);
        if(bestDir != null && bestDir != Direction.CENTER){
            Util.move(bestDir);
        }
    }


    public void mopperNav(MapLocation target) throws GameActionException {
        Util.addToIndicatorString("MN" + target);
        Direction toTarget = myLoc.directionTo(target);
        Direction[] moveOptions = {
                toTarget,
                toTarget.rotateLeft(),
                toTarget.rotateRight(),
                toTarget.rotateLeft().rotateLeft(),
                toTarget.rotateRight().rotateRight(),
                Direction.CENTER
        };
        generalMopperNav(moveOptions, target, false, 0, 1, 2, 0, 100);
    }

    // TODO: Add in heuristic for setting yourself up for a mop swing.
    public MapLocation calcHeuristics() throws GameActionException {
        int[] heuristics = new int[69];
        boolean[] adjacentToEnemyPaint = new boolean[69];
//        int[] importantSquare = new int[69];
//
        MapLocation myLoc = rc.getLocation();
//        MapLocation[] ruins = rc.senseNearbyRuins(GameConstants.VISION_RADIUS_SQUARED);
//        for(MapLocation ruin : ruins){
//            int startX = ruin.x - myLoc.x - 2;
//            int startY = ruin.y - myLoc.y - 2;
//            int endX = ruin.x + myLoc.x + 2;
//            int endY = ruin.y + myLoc.y + 2;
//            for(int x = startX; x <= endX; x++){
//                for(int y = startY; y <= endY; y++){
//                    int index = Util.getMapInfoIndex(x, y);
//                    if(index != -1) {
//                        importantSquare[index] = 1;
//                    }
//                }
//            }
//        }

        // On enemy paint = -4
        // On neutral paint = -2
        // On ally paint = 0

        // Next to enemy paint = +5 per enemy square
        // Extra boost for next to ruin
        // Extra boost for next to resource center

        // Next to ally = -2 per ally
        // Next to enemy = +15 per enemy

        for(int index = 0; index < 69; index++){
            MapInfo info = nearbyMapInfos[index];
            if(info == null){
                continue;
            }
            switch(info.getPaint()){
                case PaintType.EMPTY:
                    heuristics[index] -= 2;
                    break;
                case PaintType.ENEMY_PRIMARY:
                case PaintType.ENEMY_SECONDARY:
                    enemyPaintNearby = true;
                    heuristics[index] -= 4;
//                        MopperUtils.updateHeuristicAdjacents(heuristics, index, 5);
                    MopperUtils.updateHeuristicAdjacentsBoolean(adjacentToEnemyPaint, index);
                    break;
            }
        }

        RobotInfo[] infos = rc.senseNearbyRobots(GameConstants.VISION_RADIUS_SQUARED);
        for(RobotInfo info : infos){
            Team infoTeam = info.getTeam();
            MapLocation infoLoc = info.getLocation();
            int index = Util.getMapInfoIndex(infoLoc.x - myLoc.x, infoLoc.y - myLoc.y);
            if(info.getType().isTowerType() && infoTeam == oppTeam){
                MopperUtils.updateHeuristicDist9(heuristics, index, -100);
                enemyNearby = true;
            } else {
                int plus = -2;
                if(info.getTeam() == oppTeam){
                    plus = 15;
                    enemyNearby = true;
                }
                MopperUtils.updateHeuristicAdjacents(heuristics, index, plus);
            }
        }

        int bestIndex = -1;
        int bestHeuristic = Integer.MIN_VALUE;
        for(int index = 0; index < heuristics.length; index++){
            if(nearbyMapInfos[index] == null){
                continue;
            }
            if(adjacentToEnemyPaint[index]){
                heuristics[index] += 5;
            }
            if(heuristics[index] > bestHeuristic){
                bestHeuristic = heuristics[index];
                bestIndex = index;
            }
        }
        MapLocation bestLoc = nearbyMapInfos[bestIndex].getMapLocation();
        Util.addToIndicatorString("BL" + bestLoc);
        return bestLoc;
    }

    // TODO: Check for mop swing.
    public void doBestAction() throws GameActionException {
        RobotInfo[] actionableOpponents = rc.senseNearbyRobots(2, rc.getTeam().opponent());
        if (actionableOpponents.length == 0) {
            MapLocation tileToMop = getTileToMop();
            Util.addToIndicatorString("TE1" + tileToMop);
            if (tileToMop != null) {
                rc.attack(tileToMop);
            }
            return;
        }

        MapLocation bestIndividualTargetOnPaint = null;
        int lowestEnemyPaintOnPaint = Integer.MAX_VALUE;
        MapLocation bestIndividualTargetOnEmpty = null;
        int lowestEnemyPaintOnEmpty = Integer.MAX_VALUE;

        for (RobotInfo opponent : actionableOpponents) {
            if (opponent.getType().isTowerType()) {
                continue;
            }
            int individualPaint = opponent.getPaintAmount();
            if (individualPaint >= lowestEnemyPaintOnPaint || individualPaint >= lowestEnemyPaintOnEmpty
                    || !rc.canAttack(opponent.getLocation())) {
                continue;
            }

            if (rc.senseMapInfo(opponent.getLocation()).getPaint().isEnemy()) {
                if (individualPaint < lowestEnemyPaintOnPaint) {
                    bestIndividualTargetOnPaint = opponent.location;
                    lowestEnemyPaintOnPaint = individualPaint;
                }
            } else {
                if (individualPaint < lowestEnemyPaintOnEmpty) {
                    bestIndividualTargetOnEmpty = opponent.location;
                    lowestEnemyPaintOnEmpty = individualPaint;
                }
            }
        }

        if (bestIndividualTargetOnPaint != null) {
            Util.addToIndicatorString("BTP" + bestIndividualTargetOnPaint);
            rc.attack(bestIndividualTargetOnPaint);
        } else if (bestIndividualTargetOnEmpty != null) {
            Util.addToIndicatorString("BTE" + bestIndividualTargetOnEmpty);
            rc.attack(bestIndividualTargetOnEmpty);
        } else {
            MapLocation tileToMop = getTileToMop();
            Util.addToIndicatorString("TE2" + tileToMop);
            if (tileToMop != null) {
                rc.attack(tileToMop);
            }
        }
    }


}
