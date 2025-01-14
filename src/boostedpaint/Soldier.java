package boostedpaint;

import battlecode.common.*;

enum PatternPriority {
    LOW, MEDIUM, HIGH;
}

enum PatternType {
    RESOURCE,
    PAINT_TOWER,
    MONEY_TOWER,
    DEFENSE_TOWER;

    public boolean isTower() {
        return this != RESOURCE;
    }

    public UnitType getUnitType() {
        if (this == PAINT_TOWER) {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        } else if (this == MONEY_TOWER) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        } else if (this == DEFENSE_TOWER) {
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        } else {
            return null;
        }
    }
}

enum PatternStatus {
    INVALID,
    FINISHED,
    POSSIBLY_FINISHED,
    BLOCKED_BY_ENEMY,
    BLOCKED_BY_SELF,
    BLOCKED_BY_NOTHING;
}

public class Soldier extends Bunny {
//    MapLocation patternCenter;
//    PatternType patternType;
//    boolean definiteCenter = false;

    // only looking at the ruins without a tower on them already
//    int[] nearbyRuinIndices = new int[4]; // can have at most 4 ruins in vision radius
//    int numEmptyRuins = 0;
//    PatternPriority[] ruinPriorities = new PatternPriority[4];

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
//        if (rc.getRoundNum() > 15) {
//            rc.resign();
//        }
        super.run(); // Call the shared logic for all bunnies

        scanSurroundings();
        updateDestinationIfNeeded();

        if (tryingToReplenish) {
            if (rc.isActionReady()) {
                tryReplenish();
            }
            boolean moved = false;
            if (rc.isMovementReady()) {
                moveLogic();
                moved = !myLoc.equals(rc.getLocation());
                myLoc = rc.getLocation();
            }
            if (moved && rc.isActionReady()) {
                tryReplenish();
            }

            // TODO: should we be doing this?
            tryTowerPatternCompletion();
            tryResourcePatternCompletion();

            sharedEndFunction();
            return;
        }

        int highPriorityRuinIndex = -1;
        int mediumPriorityRuinIndex = -1;

        UnitType intendedType = getPatternUnitType();
        boolean[][] pattern = rc.getTowerPattern(intendedType);

        for(int index : Shifts.spiralOutwardIndices) {
            if (!nearbyMapInfos[index].hasRuin() || rc.senseRobotAtLocation(nearbyMapInfos[index].getMapLocation()) != null) {
                continue;
            }
            PatternPriority priority = findPriority(index, pattern);
            if (priority == PatternPriority.HIGH) {
                highPriorityRuinIndex = index;
                break;
            } else if (mediumPriorityRuinIndex == -1 && priority == PatternPriority.MEDIUM) {
                mediumPriorityRuinIndex = index;
            }
        }

        if (highPriorityRuinIndex != -1) {
            workOnRuin(highPriorityRuinIndex, pattern);
            if (rc.canCompleteTowerPattern(intendedType, nearbyMapInfos[highPriorityRuinIndex].getMapLocation())) {
                rc.completeTowerPattern(intendedType, nearbyMapInfos[highPriorityRuinIndex].getMapLocation());
            }
            sharedEndFunction();
            return;
        }

        int resourceCenterIndex = Util.getPotentialResourcePatternCenterIndex(nearbyMapInfos);

        if (resourceCenterIndex != -1) {
            pattern = rc.getResourcePattern();
            workOnResourcePattern(resourceCenterIndex, pattern);
            if (rc.canCompleteResourcePattern(nearbyMapInfos[resourceCenterIndex].getMapLocation())) {
                rc.completeResourcePattern(nearbyMapInfos[resourceCenterIndex].getMapLocation());
            }
            sharedEndFunction();
            return;
        }
        if (mediumPriorityRuinIndex != -1) {
            workOnRuin(mediumPriorityRuinIndex, pattern);
            if (rc.canCompleteTowerPattern(intendedType, nearbyMapInfos[mediumPriorityRuinIndex].getMapLocation())) {
                rc.completeTowerPattern(intendedType, nearbyMapInfos[mediumPriorityRuinIndex].getMapLocation());
            }
            sharedEndFunction();
            return;
        }

        // TODO: default behavior

        sharedEndFunction();
        return;
//
//
//
//
//
////        if (potentialResourceCenter != null) {
////            Util.log(potentialResourceCenter.toString());
////        } else {
////            Util.log("null");
////        }
//
////        Util.logBytecode("after finding resource pattern");
//
//        // if we see a definitely unfinished ruin that we could possibly finish, go for that one
//            // means
//        //
//
////        updatePatternCenterInfo();
//
////        if (rc.getRoundNum() > 10) {
////            rc.resign();
////        }
//
//
//        // TODO: when are we allowing repainting??
//        // i think it should be never for resource patterns
//        // but when for tower patterns?
//        // for now, it's not allowed at all
//        // need to change other things depending on how generous we are with repainting
//
//        // NOTE: I HAD IT ORGANIZED LIKE THIS SPECIFICALLY IN ORDER TO CHANGE MACRO MORE EASILY IF WE WANTED TO LATER
//        if (definiteCenter && patternType.isTower()) {
//            // this is guaranteed to be in line with what has been put down so far, since we are not repainting
//            boolean canPaint = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
//            boolean paintedPattern = false;
//            boolean moved = false;
//
//            MapLocation newLoc = myLoc;
//
//
//
//            if (rc.isMovementReady()) {
//                nav.goTo(patternCenter, 2);
//                newLoc = rc.getLocation();
//                moved = !newLoc.equals(myLoc);
//            }
//            if (moved) {
//                myLoc = newLoc;
//
//                // TODO: Could probably do this, but maybe bytecode issues
////                if (canPaint) {
////                scanSurroundings();
////                updatePatternCenterInfo();
////                }
//            }
//            if (canPaint) {
//                paintedPattern = fillInPattern();
//            }
//
//            if (rc.canCompleteTowerPattern(patternType.getUnitType(), patternCenter)) {
//                rc.completeTowerPattern(patternType.getUnitType(), patternCenter);
//                patternCenter = null;
//            }
//
//            // TODO: do we care about other ruins right here?
////            else if (rc.getChips() >= patternType.getUnitType().moneyCost) {
////                // try completing other ones?
////            }
//
////            if () {
////                tryTowerPatternCompletion();
////            }
//        } else if (definiteCenter) {
//            // guaranteed that we see all 5x5 of these tiles and that any tower we see doesn't conflict with this pattern
//            // want to a) end turn on our tile, b) move towards the emptier tiles, and c)
//
//            boolean canPaint = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
//            boolean paintedPattern = false;
//            boolean moved = false;
//
//            MapLocation newLoc = myLoc;
//            if (rc.isMovementReady()) {
//                nav.goTo(patternCenter, 0);
//                newLoc = rc.getLocation();
//                moved = !newLoc.equals(myLoc);
//            }
//            if (moved) {
//                myLoc = newLoc;
//                // TODO: Could probably do this, but maybe bytecode issues
////                if (canPaint) {
////                scanSurroundings();
////                updatePatternCenterInfo();
////                }
//            }
//            if (canPaint) {
//                paintedPattern = fillInPattern();
//            }
//
//            if (paintedPattern && myLoc.isWithinDistanceSquared(patternCenter, 8) && rc.canCompleteResourcePattern(patternCenter)) {
//                rc.completeResourcePattern(patternCenter);
//                patternCenter = null;
//            }
//
////            // TODO: do we care about completing towers?
////            if (rc.getChips() >= patternType.getUnitType().moneyCost) {
////                tryTowerPatternCompletion();
////            }
//        } else {
//            // if it's not a definite center, that means it's a) an unfinished resource pattern and b) not fully contained within vision radius
//            // it's a resource pattern, because if it were a tower pattern, we'd be able to see the ruin and mark it as definite
//
//            boolean canPaint = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
//            boolean paintedPattern = false;
//            if (canPaint) {
//                paintedPattern = fillInTowardPattern();
//            }
//            if (rc.isMovementReady()) {
//                nav.goTo(patternCenter, 0);
//            }
//
//        }
//        sharedEndFunction();
    }

    public int getTowerAsInt(UnitType type) {
        if (type == UnitType.LEVEL_ONE_PAINT_TOWER) {
            return 0;
        } else if (type == UnitType.LEVEL_ONE_MONEY_TOWER) {
            return 1;
        } else { //(type == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
            return 2;
        }
    }

    public void workOnRuin(int index, boolean[][] paintPattern) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
        if (isPaintReady) {
            int dx = Shifts.dx[index];
            int dy = Shifts.dy[index];
            int[] ordering = PatternFillingIterators.orderFillingRuin[13*dx + dy + 84];

            for (int attackIndex : ordering) {
                if (attackIndex == index) {
                    continue;
                }
                if (nearbyMapInfos[attackIndex].hasRuin() || nearbyMapInfos[attackIndex].isWall()) {
                    rc.resign();
                } else if (nearbyMapInfos[attackIndex].isWall()) {
                    rc.resign();
                }

                PaintType currentPaint = nearbyMapInfos[attackIndex].getPaint();
                if (currentPaint == PaintType.EMPTY) {

                    int offsetX = Shifts.dx[attackIndex] - dx;
                    int offsetY = Shifts.dy[attackIndex] - dy;
                    if (offsetX*offsetX + offsetY*offsetY <= 8) {
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);

                    } else {
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), (offsetX+dx+offsetY+dy)%2==0);
                    }
                    break;
                } else if (currentPaint.isEnemy()) {
                    continue;
                } else {
                    int offsetX = Shifts.dx[attackIndex] - dx;
                    int offsetY = Shifts.dy[attackIndex] - dy;

                    if (offsetX*offsetX + offsetY*offsetY <= 8 && paintPattern[offsetX+2][offsetY+2] != currentPaint.isSecondary()) {
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);
                        break;
                    }
                }
            }
        }

        if (rc.isMovementReady()) {
            nav.circle(nearbyMapInfos[index].getMapLocation(), 1, 2);
        }
    }

    public void workOnResourcePattern(int index, boolean[][] paintPattern) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;

        if (isPaintReady) {
            int dx = Shifts.dx[index];
            int dy = Shifts.dy[index];
            int[] ordering = PatternFillingIterators.orderFillingResource[13*dx + dy + 84];

            for (int attackIndex : ordering) {
                PaintType currentPaint = nearbyMapInfos[attackIndex].getPaint();
                int offsetX = Shifts.dx[attackIndex] - dx;
                int offsetY = Shifts.dy[attackIndex] - dy;

                if (currentPaint == PaintType.EMPTY) {
                    if (offsetX*offsetX + offsetY*offsetY <= 8) {
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);
                    } else {
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), (offsetX+dx+offsetY+dy)%2==0);
                    }
                    break;
                }
            }
        }

        if (rc.isMovementReady()) {
            nav.goTo(nearbyMapInfos[index].getMapLocation(), 0);
        }
    }

    public PatternPriority findPriority(int index, boolean[][] pattern) throws GameActionException {
        // guaranteed that this is a ruin

        boolean hasEmpty = false;
        boolean hasEnemy = false;
        boolean hasConflictingPaint = false;

        for (int neighboringIndex : Util.getIndicesForSquareSpiral(Shifts.dx[index], Shifts.dy[index])) {
            if (neighboringIndex == index) {
                continue;
            }
            PaintType paintType = nearbyMapInfos[neighboringIndex].getPaint();
            if (paintType == PaintType.EMPTY) {
                return PatternPriority.HIGH;
            } else if (paintType.isEnemy()) {
                hasEnemy = true;
            } else {
                int lookupX = Shifts.dx[neighboringIndex] - Shifts.dx[index] + 2;
                int lookupY = Shifts.dy[neighboringIndex] - Shifts.dy[index] + 2;
                if (paintType.isSecondary() != pattern[lookupX][lookupY]) {
                    hasConflictingPaint = true;
                }
            }
        }

        //by end of this loop, we see no empty

        if (hasEnemy) {
            // has enemy and no empty
            return PatternPriority.LOW;
        } else if (hasConflictingPaint) {
            return PatternPriority.HIGH;
        } else {
            return PatternPriority.MEDIUM;
        }
    }


    public UnitType getPatternUnitType() throws GameActionException {
        if(nearestAlliedTowerLoc.equals(nearestAlliedPaintTowerLoc)) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }
        else {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }
    }

    /**
     * Perform the attack, and if we have a ruin to complete, do it.
     */
    public void tryTowerPatternCompletion() throws GameActionException {

        // TODO: handle resource pattern completion too, not just tower pattern
        // completion

        // Possibly complete tower pattern near a ruin if it exists
        nearbyMapInfos = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyMapInfos) {
            if (tile.hasRuin()) {
                // We might want to check if we can complete the tower
                MapLocation ruinLoc = tile.getMapLocation();

                // Check if you can complete a tower pattern.
                if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc)) {
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc);
                } else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc)) {
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc);
                }
//                else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruinLoc)) {
//                    rc.completeTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruinLoc);
//                }

            }
        }
    }

    public void tryResourcePatternCompletion() throws GameActionException {
        // this assumes that the whole pattern is within vision radius (and so the
        // possible centers are closer in)

        // TODO: optimize this for bytecode
        MapInfo[] possibleCenters = rc.senseNearbyMapInfos(8);

        for (MapInfo center : possibleCenters) {
            if (center.getPaint().isAlly() && !center.getPaint().isSecondary() &&
                    rc.canCompleteResourcePattern(center.getMapLocation())) {
                rc.completeResourcePattern(center.getMapLocation());
            }
        }
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
                nearestAlliedPaintTowerLoc != null
                && myLoc.distanceSquaredTo(nearestAlliedPaintTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {

            nav.goTo(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
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