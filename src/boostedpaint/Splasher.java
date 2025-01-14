package boostedpaint;

import battlecode.common.*;

public class Splasher extends Bunny {

    public Splasher(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run(); // Call shared logic for all bunnies
        scanSurroundings();

        // 1. Handle Ruins or Resource Patterns
        MapInfo curRuin = findUnmarkedRuin();
//        if (curRuin != null && !tryingToReplenish) {
//            handleUnmarkedRuin(curRuin);
//        } else if (!tryingToReplenish) {
//            attemptMarkResourcePattern();
//        }

        // 2. Replenish or Perform Splash Attack
        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                splashAttack();
            }
        }

        // 3. Movement Logic
        if (rc.isMovementReady()) {
            moveLogic();
        }

        // 4. Recheck for Splash Attack or Replenish
        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                splashAttack();
            }
        }

        // 5. End of Turn Logic
        sharedEndFunction();
    }

    /**
     * Perform a splash attack, prioritizing clusters of enemy paint or robots.
     */
    public void splashAttack() throws GameActionException {
        MapInfo[] actionableTiles = rc.senseNearbyMapInfos(UnitType.SPLASHER.actionRadiusSquared);
        MapLocation myLoc = rc.getLocation();

        MapLocation bestTarget = null;
        int bestScore = Integer.MIN_VALUE;

        for (MapInfo tile : actionableTiles) {
            if (!rc.canAttack(tile.getMapLocation())) continue;

            int score = 0;

            // Prioritize tiles with enemy paint or robots
            if (tile.getPaint().isEnemy()) {
                score += 100;
            }

            // Favor clusters of enemy tiles
            score += 10 * adjacencyToEnemyPaint(tile.getMapLocation());

            // Penalize distance
            score -= myLoc.distanceSquaredTo(tile.getMapLocation());

            if (score > bestScore) {
                bestScore = score;
                bestTarget = tile.getMapLocation();
            }
        }

        if (bestTarget != null && rc.isActionReady()) {
            rc.attack(bestTarget);
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

        MapLocation bestLocation = null;
        int bestScore = Integer.MIN_VALUE;

        for (Direction dir : Direction.allDirections()) {
            if (!rc.canMove(dir)) continue;

            MapLocation targetLoc = myLoc.add(dir);
            MapInfo tile = rc.senseMapInfo(targetLoc);

            int score = evaluateMoveTile(tile);

            if (score > bestScore) {
                bestScore = score;
                bestLocation = targetLoc;
            }
        }

        if (bestLocation != null) {
            nav.goTo(bestLocation, 0);
        } else {
            nav.goTo(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
        }
    }

    /**
     * Evaluate the desirability of a tile for movement.
     */
    private int evaluateMoveTile(MapInfo tile) throws GameActionException {
        int score = 0;

        // Favor tiles adjacent to enemy paint
//        if (adjacencyToEnemyPaint(tile.getMapLocation()) > 0) {
//            score += 100;
            if (tile.getPaint().isAlly()) {
                score += 100;
            }
//        }

        // Penalize distance from the current location
        score -= myLoc.distanceSquaredTo(tile.getMapLocation());

        return score;
    }
}
