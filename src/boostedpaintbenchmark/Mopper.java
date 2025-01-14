package boostedpaintbenchmark;

import battlecode.common.*;

public class Mopper extends Bunny {
    RobotInfo[] actionableOpponents;
    MapInfo[] actionableTiles;
    public Mopper(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        actionableOpponents = rc.senseNearbyRobots(2, rc.getTeam().opponent());
        actionableTiles = rc.senseNearbyMapInfos(2);

        updateDestinationIfNeeded();

        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                doBestAction();
            }
        }

        if (canMove()) {
            moveLogic();
        }

        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                doBestAction();
            }
        }

        // 6. End of Turn Logic
        // Perform any shared cleanup or post-turn logic
        sharedEndFunction();
    }

    /**
     * Returns a score evaluating how favorable it would be for this robot to move to this sector.
     */
    public int evaluateSector(int encodedSector) {
//        throw new NotImplementedException();
        return 0; // this should not be called yet
    }

    /**
     * Choose where to move:
     * - If there’s an ally-marked empty tile, move toward it to paint/attack.
     * - Otherwise move randomly.
     */
    public void moveLogic() throws GameActionException {
        myLoc = rc.getLocation();

        // if we are trying to replenish, move towards the nearest tower if we're not
        // close enough
        if (nearestAlliedPaintTowerLoc != null
                && tryingToReplenish
                && myLoc.distanceSquaredTo(nearestAlliedPaintTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {

            nav.goTo(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);

            return;
        }

        int bestDistance = Integer.MAX_VALUE;
        MapLocation bestLocation = null;

        for (MapInfo tile : nearbyMapInfos) {
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
            nav.goTo(bestLocation, UnitType.MOPPER.actionRadiusSquared);
        } else {
            // Move in the direction
            nav.goTo(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
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
            return;
        } else if (bestIndividualTargetOnEmpty != null) {
            rc.attack(bestIndividualTargetOnEmpty);
            return;
        } else {
            MapLocation tileToMop = getTileToMop();
            if (tileToMop != null) {
                rc.attack(tileToMop);
            }
            return;
        }
    }


}
