package cleanupcopy;

import battlecode.common.*;

public class Soldier extends Bunny {

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
        updateDestinationIfNeeded();

        // 1. Handle Marking
        if (!tryingToReplenish) {
            // Check if there are any unmarked ruins nearby. If a ruin is found:
            // - Move toward the ruin if we are far away (distance > 2).
            // - Once in range, try mark it with a tower pattern (so we can build a tower)

            MapInfo ruin = MarkingUtils.findUnclaimedRuin();
            if(ruin != null && !MarkingUtils.isRuinMarked(ruin.getMapLocation())){
                MarkingUtils.handleUnmarkedRuin(ruin.getMapLocation()); // Move to and interact with the ruin if you're not trying to replenish
            }

            // Attempt to Mark a Resource Pattern if you're not trying to replenish
            // If no ruins are found, check if we can mark a resource pattern at our current
            // location.
            // - Only do this if the location is not already marked by our team
            MarkingUtils.attemptMarkResourcePattern();
        }

        // 2. Replenish or Paint/Attack if you can
        // After handling ruins and resource marking, check if we can act:
        // If we're trying to replenish and we're in range of a tower, try to replenish
        // Otherwise
        // - Paint or attack a nearby tile based on priority:
        // - Ally-marked but unpainted tiles take precedence.
        // - If no such tiles exist, attack an unpainted tile nearby.
        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                paintOrAttack();
            }
        }

        // 3. Movement Logic
        // If movement is possible:
        // - Move toward the nearest tower if you're trying to replenish.

        // - Otherwise Prioritize moving toward ally-marked tiles that are empty
        // (unpainted).
        // - If no such tiles are found, move to your destination
        if (rc.isMovementReady()) {
            moveLogic();
        }

        // 4. Recheck for Replenish or Painting/Attacking

        // After handling ruins and resource marking, check if we can act:
        // If we're trying to replenish and we're in range of a tower, try to replenish

        // Otherwise
        // - Paint or attack a nearby tile based on priority:
        // - Ally-marked but unpainted tiles take precedence.
        // - If no such tiles exist, attack an unpainted tile nearby.
        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                paintOrAttack();
            }
        }

        MarkingUtils.tryPatternCompletion();

        // 6. End of Turn Logic
        // Perform any shared cleanup or post-turn logic
        sharedEndFunction();
    }

    /**
     * Attempt to paint or attack nearby tiles if possible.
     */
    public void paintOrAttack() throws GameActionException {
        MapInfo[] actionableTiles = rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared);
        MapLocation myLoc = rc.getLocation();

        if (rc.getPaint() < Constants.MIN_PAINT_NEEDED_FOR_SOLDIER_ATTACK) {
            return; // Not enough paint to do anything in this method
        }


        // loop over actionable enemies and try to attack them if they're a tower
        for (RobotInfo robot : rc.senseNearbyRobots(UnitType.SOLDIER.actionRadiusSquared, rc.getTeam().opponent())) {
            if (Util.isPaintTower(robot.getType())) {
                if (rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
                    return;
                }
            }
        }


        MapLocation bestPaintLoc = null;
        int bestScore = 0;
        boolean secondaryPaint = false;

        for (MapInfo tile : actionableTiles) {
            // Make sure tile can be painted.
            if (!rc.canPaint(tile.getMapLocation())) {
                continue;
            }

            // Tile score is intentionally set to 0 and not min_value.
            int tileScore = 0;
            // Prioritize painting marked tiles.
            if (tile.getMark().isAlly()) {
                // Tile is empty or wrong color.
                boolean tileEmpty = tile.getPaint() == PaintType.EMPTY;
                boolean wrongColor = tile.getMark().isSecondary() != tile.getPaint().isSecondary();
                if (tileEmpty || wrongColor) {
                    // Make sure soldier is ready and tile can be painted.
                    if (rc.isActionReady() && rc.canPaint(tile.getMapLocation())) {
                        // tileScore += 1000;
                        // Attack immediately to save bytecode.
                        rc.attack(tile.getMapLocation(), tile.getMark().isSecondary());
                        Util.log("Square Attacked: " + tile.getMapLocation().toString());
                        return;
                    }
                }
            }

            // If there are no marked tiles, paint an empty one.
            else if (tile.getPaint() == PaintType.EMPTY) {
                // Reward for adjacency.
                tileScore += 500 * adjacencyToAllyPaint(tile.getMapLocation()) + 500;
            }

            tileScore -= myLoc.distanceSquaredTo(tile.getMapLocation());
            if (tileScore > bestScore) {
                bestScore = tileScore;
                bestPaintLoc = tile.getMapLocation();
                secondaryPaint = tile.getMark().isSecondary();
            }
        }

        // Paint the best tile that was found.
        if (bestPaintLoc != null && rc.isActionReady()) {
            rc.attack(bestPaintLoc, secondaryPaint);
            Util.log("Square Attacked: " + bestPaintLoc.toString());
        }
    }

    public int adjacencyToAllyPaint(MapLocation loc) throws GameActionException {
        Direction[] directions = Direction.allDirections();
        int adjacentAllyTiles = 0;
        for (Direction dir : directions) {
            MapLocation adjacent = loc.add(dir);

            // Check if the adjacent tile is within bounds and has allied paint
            if (rc.canSenseLocation(adjacent)) {
                MapInfo adjacentTile = rc.senseMapInfo(adjacent);
                if (adjacentTile.getPaint().isAlly()) {
                    adjacentAllyTiles++;
                }
            }
        }
        return adjacentAllyTiles;
    }


    /**
     * Returns a score evaluating how favorable it would be for this robot to move to this sector.
     */
    public int evaluateSector(int encodedSector) {
        ScanResult sr = comms.decodeSector(encodedSector);
        int sectorScore = 0;

        // If the region is mostly empty and there's an unbuilt ruin, go there first.
        if(sr.emptyPaintLevel >= 2 && sr.towerType == 1) {
            sectorScore += 1000;
        }

        // Prefer regions with 5-12 empty cells.
        if(sr.emptyPaintLevel == 2) {
            sectorScore += 500;
        }

        // Avoid regions with enemy cells.
        if(sr.enemyPaintLevel >= 1) {
            sectorScore -= 500;
        }

        return sectorScore;

    }

    /**
     * Choose where to move:
     * - If there’s an ally-marked empty tile, move toward it to paint/attack.
     * - Otherwise move randomly.
     */
    public void moveLogic() throws GameActionException {
        myLoc = rc.getLocation();

        // If trying to replenish, go to nearest tower immediately.
        if (tryingToReplenish &&
                nearestAlliedPaintTowerLoc != null &&
                myLoc.distanceSquaredTo(nearestAlliedPaintTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {

            nav.goTo(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
            Util.log("Trying to replenish paint");
            return;
        }

        MapLocation bestDirection = null;
        int bestScore = 0;

        for (Direction dir : Direction.allDirections()) {
            if (!rc.canMove(dir)) {
                continue;
            }
            MapInfo tile = rc.senseMapInfo(myLoc.add(dir));
            int tileScore = 0;

            // Strongly favor tiles with ally color on the boundary.
            if (isAllyBoundaryTile(tile)) {
                tileScore += 1000;
                // nav.goTo(tile.getMapLocation(), 0);
                // Util.log("My next tile is a boundary!");
                // return;
                // // Favor staying on your color
                if (tile.getPaint().isAlly()) {
                    tileScore += 5;
                }
            }

            // // If there's a mark and it's unpainted, favor that too.
            // if (tile.getMark().isAlly() && tile.getPaint() == PaintType.EMPTY) {
            // tileScore += 100;
            // }

            if (tileScore > bestScore) {
                bestScore = tileScore;
                bestDirection = tile.getMapLocation();
            }
        }

        if (bestDirection != null) {
            Util.log("Moving in direction: " + bestDirection.toString());
            nav.goTo(bestDirection, 0);
        } else {

//            // Make a macro informed movement.
//            macroMove();

            // Move in a pre-determined global direction.
            Util.log("Moving to destination " + destination.toString());
            nav.goTo(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
        }
    }

    private boolean isAllyBoundaryTile(MapInfo tile) throws GameActionException {
        // Make sure tile is ally.
        if (!tile.getPaint().isAlly()) {
            return false;
        }

        // Check for empty adjacent tiles.
        Direction[] directions = Direction.allDirections();
        for (Direction dir : directions) {
            MapLocation adjacent = tile.getMapLocation().add(dir);
            if (rc.canSenseLocation(adjacent)) {
                MapInfo adjacentTile = rc.senseMapInfo(adjacent);
                // Make sure is passable.
                if (adjacentTile.isPassable()) {
                    if (adjacentTile.getPaint() == PaintType.EMPTY) {
                        return true; // At least one adjacent tile is empty
                    }
                }
            }
        }
        return false;
    }
}