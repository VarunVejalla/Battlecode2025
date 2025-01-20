package bettermoppers;

import battlecode.common.*;

import java.util.regex.Pattern;

public class Mopper extends Bunny {
    RobotInfo[] actionableOpponents;
    MapInfo[] actionableTiles;
    public Mopper(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        updateDestinationIfNeeded();

        actionableOpponents = rc.senseNearbyRobots(2, rc.getTeam().opponent());
        actionableTiles = rc.senseNearbyMapInfos(2);

        Util.log("Location at beginning of round: " + rc.getLocation());
        Util.logBytecode("Before calcing heuristics");
        calcHeuristics();
        Util.logBytecode("After calcing heuristics");
        if (rc.isActionReady()) {
            Util.addToIndicatorString("DBA");
            doBestAction();
            Util.log("Location after best action: " + rc.getLocation());
        }
        if (canMove()) {
            moveLogic();
            Util.log("Location after movement: " + rc.getLocation());
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

        int bestDistance = Integer.MAX_VALUE;
        MapLocation bestLocation = null;

        for (MapInfo tile : nearbyMapInfos) {
            if (tile == null) {
                continue;
            }
            if (tile.getPaint().isEnemy()) {

                int newDistance = Math.max(Math.abs(tile.getMapLocation().x - rc.getLocation().x),
                        Math.abs(tile.getMapLocation().y - rc.getLocation().y));

                if (newDistance < bestDistance) {
                    bestDistance = newDistance;
                    bestLocation = tile.getMapLocation();
                }
            }
        }

        if (bestLocation != null) {
            mopperNav(bestLocation);
        } else {
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
    }

    public MapLocation getTileToMop() throws GameActionException {
        for (MapInfo tile : actionableTiles) {
            if (tile.getPaint().isEnemy() && rc.canAttack(tile.getMapLocation())) {
                return tile.getMapLocation();
            }
        }
        return null;
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

        Direction bestDir = null;
        int leastHeuristic = Integer.MAX_VALUE;

        for (Direction dir : moveOptions) {
            MapLocation newLoc = myLoc.add(dir);

            if (dir != Direction.CENTER && !rc.canMove(dir)) {
                continue;
            }

            if (!rc.canSenseLocation(newLoc) || !rc.sensePassability(newLoc)) {
                continue;
            }

            int numMoves = Util.minMovesToReach(newLoc, target);
            int distanceSquared = newLoc.distanceSquaredTo(target);
            int distance = (int)Math.sqrt(distanceSquared);
            MapInfo info = rc.senseMapInfo(newLoc);

            int paintHeuristic = -5;
            if(info.getPaint() == PaintType.EMPTY){
                paintHeuristic = 10;
            } else if(info.getPaint().isEnemy()){
                paintHeuristic = 20;
            }

            int numAllies = 0;
            RobotInfo[] adjAllies = rc.senseNearbyRobots(newLoc, 2, myTeam);
            for(RobotInfo ally : adjAllies){
                if(!Util.isTower(ally.getType())){
                    numAllies++;
                }
            }
            int allyHeuristic = numAllies * 5;

            int heuristic = numMoves + distance + paintHeuristic + allyHeuristic;
            Util.log("Direction: " + dir + ", heuristic: " + heuristic + ", numMoves: " + numMoves + ", distance: " + distance + ", paintHeuristic: " + paintHeuristic + ", alllyHeuristic: " + allyHeuristic);

            if (heuristic < leastHeuristic) {
                leastHeuristic = heuristic;
                bestDir = dir;
            }
        }

        Util.log("Best dir: " + bestDir);
        Util.addToIndicatorString("BD" + bestDir);
        if(bestDir != null && bestDir != Direction.CENTER){
            Util.move(bestDir);
        }
    }

    public void calcHeuristics() throws GameActionException {
        int[] heuristics = new int[69];
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
                        heuristics[index] -= 4;
                        MopperUtils.updateHeuristicAdjacents(heuristics, index, 5);
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
            } else {
                int plus = -2;
                if(info.getTeam() == oppTeam){
                    plus = 15;
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
            if(heuristics[index] > bestHeuristic){
                bestHeuristic = heuristics[index];
                bestIndex = index;
            }
        }
        MapLocation bestLoc = nearbyMapInfos[bestIndex].getMapLocation();
        Util.addToIndicatorString("BL" + bestLoc);
    }

    public void doBestAction() throws GameActionException {
        if (actionableOpponents.length == 0) {
            MapLocation tileToMop = getTileToMop();
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
            rc.attack(bestIndividualTargetOnPaint);
        } else if (bestIndividualTargetOnEmpty != null) {
            rc.attack(bestIndividualTargetOnEmpty);
        } else {
            MapLocation tileToMop = getTileToMop();
            if (tileToMop != null) {
                rc.attack(tileToMop);
            }
        }
    }


}
