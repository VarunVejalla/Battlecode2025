package goat;

import battlecode.common.*;

public class Splasher extends Bunny {

    public Splasher(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run(); // Call shared logic for all bunnies
        // Updated in Bunny.
//        updateDestinationIfNeeded();

        // 1. Replenish or Perform Splash Attack
        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                splashAttack();
            }
        }

        // 2. Movement Logic
        if (canMove()) {
            moveLogic();
        }

        // 3. Recheck for Splash Attack or Replenish
        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                splashAttack();
            }
        }

        // 4. End of Turn Logic
        sharedEndFunction();
    }

    /**
     * Perform a splash attack, prioritizing clusters of enemy paint or robots.
     */
    public void splashAttack() throws GameActionException {
        MapInfo[] actionableTiles = rc.senseNearbyMapInfos(UnitType.SPLASHER.actionRadiusSquared);
        MapLocation myLoc = rc.getLocation();

        MapLocation bestTarget = null;
        int bestScore = 0;

        for (MapInfo tile : actionableTiles) {
            if (!rc.canAttack(tile.getMapLocation())) continue;

            int score = 0;

            // Prioritize tiles with enemy paint or robots
            if (tile.getPaint().isEnemy()) {
                score += 100;
                // Favor clusters of enemy tiles
                score += 10 * adjacencyToEnemyPaint(tile.getMapLocation());
            }



            // Penalize distance
//            score -= myLoc.distanceSquaredTo(tile.getMapLocation());

            if (score > bestScore) {
                bestScore = score;
                bestTarget = tile.getMapLocation();
            }
        }

        if (bestTarget != null && rc.isActionReady()) {
            rc.attack(bestTarget);
            //Util.log("Splasher attacked: " + bestTarget);
        }
    }

    /**
     * Calculate adjacency to enemy-painted tiles.
     */
    public int adjacencyToEnemyPaint(MapLocation loc) throws GameActionException {
        Direction[] directions = Direction.allDirections();
        int adjacentEnemyTiles = 0;
        for (Direction dir : directions) {
            MapLocation adjacent = loc.add(dir);
            if (rc.canSenseLocation(adjacent)) {
                MapInfo adjacentTile = rc.senseMapInfo(adjacent);
                if (adjacentTile.getPaint().isEnemy()) {
                    adjacentEnemyTiles++;
                }
            }
        }
        return adjacentEnemyTiles;
    }

    /**
     * Returns a score evaluating how favorable it would be for this robot to move to this sector.
     */
    public int evaluateSector(int encodedSector) {
//        throw new NotImplementedException();
        ScanResult sr = comms.decodeSector(encodedSector);
        if(sr.enemyPaintLevel >= 2) {
            return 1;
        }
        return 0;
    }

    /**
     * Movement logic for the splasher:
     * - Move toward clusters of enemy-painted tiles or robots.
     * - If no meaningful targets are found, move to the destination.
     */
    public void moveLogic() throws GameActionException {
        myLoc = rc.getLocation();

        if (tryingToReplenish && nearestAlliedPaintTowerLoc != null &&
                myLoc.distanceSquaredTo(nearestAlliedPaintTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {
            nav.goTo(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
            return;
        }

        //Util.log("Splasher fallback to destination: " + destination);
//        nav.goTo(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);

        macroMove();
    }

    /**
     * Evaluate the desirability of a tile for movement.
     */
    private int evaluateMoveTile(MapInfo tile) throws GameActionException {
        int score = 0;

        // Favor tiles adjacent to enemy paint
        if (tile.getPaint().isAlly()) {
            score += 100;
        }

        // Penalize distance from the current location
        score -= myLoc.distanceSquaredTo(tile.getMapLocation());

        return score;
    }
}
