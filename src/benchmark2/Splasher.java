package benchmark2;

import battlecode.common.*;

public class Splasher extends Bunny {

    public Splasher(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run(); // Call shared logic for all bunnies

        // 1. Replenish or Perform Splash Attack
        if (tryingToReplenish) {
            replenishLogic();
        } else {
            splashAttack();
            // 2. Movement Logic
            if (canMove()) {
                moveLogic();
            }
            splashAttack();
        }

        // 4. End of Turn Logic
        sharedEndFunction();
    }

    /**
     * Perform a splash attack, prioritizing clusters of enemy paint or robots.
     */
    public void splashAttack() throws GameActionException {
        MapInfo[] actionableTiles = rc.senseNearbyMapInfos(UnitType.SPLASHER.actionRadiusSquared);

        MapLocation bestTarget = null;
        int bestScore = 0;

        for (MapInfo tile : actionableTiles) {
            MapLocation targetLocation = tile.getMapLocation();
            if (!rc.canAttack(targetLocation)) continue;

            int score = 0;

            // Prioritize tiles with enemy paint or robots
            if (tile.getPaint().isEnemy()) {
                score += 100;
                // Favor clusters of enemy tiles
                score += 10 * adjacencyToEnemy(targetLocation);
            }

            // Hit it if it has more than 5 empty adjacent. Hit the one with max adjacent.
            int touchingEmpty = adjacencyToEmpty(targetLocation);
            if(touchingEmpty > 5) {
                score += 10 * touchingEmpty;
            }

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
    public int adjacencyToEnemy(MapLocation loc) throws GameActionException {
        Direction[] directions = Direction.allDirections();
        int adjacencyToEnemyScore = 0;
        for (Direction dir : directions) {
            MapLocation adjacent = loc.add(dir);
            if (rc.canSenseLocation(adjacent)) {
                MapInfo adjacentTile = rc.senseMapInfo(adjacent);
                if (adjacentTile.getPaint().isEnemy()) {
                    adjacencyToEnemyScore++;
                }
                // ATTACK ENEMY RUINS
                if(adjacentTile.hasRuin()) {
                    adjacencyToEnemyScore += 1000;
                }

            }
            // If you sense an enemy tower, hit that.
            if(rc.canSenseRobotAtLocation(adjacent)) {
                RobotInfo rob = rc.senseRobotAtLocation(adjacent);
                if(rob.getType().isTowerType() && rob.getTeam() != rc.getTeam()) {
                    adjacencyToEnemyScore+= 100;
                }
            }

        }
        return adjacencyToEnemyScore;
    }

    /**
     * Calculate adjacency to enemy-painted tiles.
     */
    public int adjacencyToEmpty(MapLocation loc) throws GameActionException {
        Direction[] directions = Direction.allDirections();
        int adjacencyToEmptyScore = 0;
        for (Direction dir : directions) {
            MapLocation adjacent = loc.add(dir);
            if (rc.canSenseLocation(adjacent)) {
                MapInfo adjacentTile = rc.senseMapInfo(adjacent);
                if (adjacentTile.getPaint() == PaintType.EMPTY) {
                    adjacencyToEmptyScore++;
                }
            }

        }
        return adjacencyToEmptyScore;
    }

    /**
     * Returns a score evaluating how favorable it would be for this robot to move to this sector.
     */
    public int evaluateSector(int encodedSector) {
        ScanResult sr = comms.decodeSector(encodedSector);
        int tileScore = 0;
        // Move towards areas with high enemy paint.
        if(sr.enemyPaintLevel >= 2) {
            tileScore+= 50;
        }

//        // Tiebreak by going to unexperienced places.
//        if((encodedSector & 1) == 0) {
//            tileScore++;
//        }

        return tileScore;
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
            nav.goToBug(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
            return;
        }

        macroMove(0);
    }

}
