package goat;

import battlecode.common.*;

public class Soldier extends Bunny {

    public static final int[] spiralOutwardIndices = {34,25,33,35,43,24,26,42,44,16,32,36,52,15,17,23,27,41,45,51,53,14,18,50,54,8,31,37,60,7,9,22,28,40,46,59,61,6,10,13,19,49,55,58,62,2,30,38,66,1,3,21,29,39,47,65,67,5,11,57,63,0,4,12,20,48,56,64,68};
    public static final int[] shift_dx = {-4,-4,-4,-4,-4,-3,-3,-3,-3,-3,-3,-3,-2,-2,-2,-2,-2,-2,-2,-2,-2,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,4,4,4,4,4};
    public static final int[] shift_dy = {-2,-1,0,1,2,-3,-2,-1,0,1,2,3,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-3,-2,-1,0,1,2,3,-2,-1,0,1,2};

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
        Util.logBytecode("Soldier constructor");
        PatternUtils.soldier = this;
        PatternUtils.rc = rc;
        Util.logBytecode("After soldier constructor");
    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
        updateDestinationIfNeeded();

        // 1. If trying to replenish, go do that.
        // TODO: If nearestAlliedPaintTowerLoc == null, should we explore or smth?
        if(tryingToReplenish && nearestAlliedPaintTowerLoc != null){
            tryReplenish();
            if (myLoc.distanceSquaredTo(nearestAlliedPaintTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {
                nav.goTo(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
            }
        }

        else if(isAttacking()){
            // 2. TODO: Attacking logic.
            runAttackLogic();
        }
        else {
            // 3. If not attacking, run pattern painting logic.
            buildPattern();
        }

        MarkingUtils.tryRuinPatternCompletion();
        MarkingUtils.tryResourcePatternCompletion();

        if(canMove()) {
            moveLogic();
        }

        // 6. End of Turn Logic
        // Perform any shared cleanup or post-turn logic
        sharedEndFunction();
    }

    // TODO implement this
    public boolean isAttacking() throws GameActionException {
        return false;
    }

    // TODO implement this
    public void runAttackLogic() throws GameActionException {
        return;
    }

    public void buildPattern() throws GameActionException {
        int highPriorityRuinIndex = -1;
        int mediumPriorityRuinIndex = -1;

        UnitType intendedType = PatternUtils.getPatternUnitType();
        boolean[][] pattern = rc.getTowerPattern(intendedType);

        // Spirals outward up to vision radius.
        // 1500 bytecode.
        for(int index : spiralOutwardIndices) {
            if (nearbyMapInfos[index] == null || !nearbyMapInfos[index].hasRuin() || rc.canSenseRobotAtLocation(nearbyMapInfos[index].getMapLocation())) {
                continue;
            }
            PatternPriority priority = PatternUtils.findPriority(index, pattern);
            if (priority == PatternPriority.HIGH) {
                highPriorityRuinIndex = index;
                break;
            } else if (mediumPriorityRuinIndex == -1 && priority == PatternPriority.MEDIUM) {
                mediumPriorityRuinIndex = index;
            }
        }

        if (highPriorityRuinIndex != -1) {
            PatternUtils.workOnRuin(highPriorityRuinIndex, pattern);
            if (rc.canCompleteTowerPattern(intendedType, nearbyMapInfos[highPriorityRuinIndex].getMapLocation())) {
                rc.completeTowerPattern(intendedType, nearbyMapInfos[highPriorityRuinIndex].getMapLocation());
            }
            return;
        }

        // 2650 bytecode.
        int resourceCenterIndex = PatternUtils.getPotentialResourcePatternCenterIndex(nearbyMapInfos);

        if (resourceCenterIndex != -1) {
            // 300 bytecode.
            pattern = rc.getResourcePattern();
            PatternUtils.workOnResourcePattern(shift_dx[resourceCenterIndex], shift_dy[resourceCenterIndex], pattern);

            if (rc.isMovementReady()) {
                nav.goTo(nearbyMapInfos[resourceCenterIndex].getMapLocation(), 0);
            }
            if (rc.canCompleteResourcePattern(nearbyMapInfos[resourceCenterIndex].getMapLocation())) {
                rc.completeResourcePattern(nearbyMapInfos[resourceCenterIndex].getMapLocation());
            }
            return;
        }
        if (mediumPriorityRuinIndex != -1) {
            PatternUtils.workOnRuin(mediumPriorityRuinIndex, pattern);
            if (rc.canCompleteTowerPattern(intendedType, nearbyMapInfos[mediumPriorityRuinIndex].getMapLocation())) {
                rc.completeTowerPattern(intendedType, nearbyMapInfos[mediumPriorityRuinIndex].getMapLocation());
            }
            return;
        }
        PatternUtils.runDefaultBehavior();
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
                // // Favor staying on your color
                if (tile.getPaint().isAlly()) {
                    tileScore += 5;
                }
            }

            if (tileScore > bestScore) {
                bestScore = tileScore;
                bestDirection = tile.getMapLocation();
            }
        }

        if (bestDirection != null) {
            nav.goTo(bestDirection, 0);
        } else {

//            // Make a macro informed movement.
//            macroMove();

            // Move in a pre-determined global direction.
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