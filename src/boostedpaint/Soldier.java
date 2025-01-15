package boostedpaint;

import battlecode.common.*;

enum PatternPriority {
    LOW, MEDIUM, HIGH
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
    BLOCKED_BY_NOTHING
}

public class Soldier extends Bunny {
    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
//        if (rc.getRoundNum() > 150) {
//            rc.resign();
//        }

        super.run(); // Call the shared logic for all bunnies

        if (rc.getID() == 11065 && rc.getRoundNum() > 125 && rc.getRoundNum() < 150) {
            Util.logBytecode("Before scanning");
        }

        scanSurroundings();
        updateDestinationIfNeeded();

        if (rc.getID() == 11065 && rc.getRoundNum() > 125 && rc.getRoundNum() < 150) {
            Util.logBytecode("after scanning");
        }

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

        if (rc.getID() == 11065 && rc.getRoundNum() > 125 && rc.getRoundNum() < 150) {
            Util.logBytecode("after identifying ruins");
        }

        if (highPriorityRuinIndex != -1) {
            workOnRuin(highPriorityRuinIndex, pattern);
            if (rc.canCompleteTowerPattern(intendedType, nearbyMapInfos[highPriorityRuinIndex].getMapLocation())) {
                rc.completeTowerPattern(intendedType, nearbyMapInfos[highPriorityRuinIndex].getMapLocation());
            }
            sharedEndFunction();
            if (rc.getID() == 11065 && rc.getRoundNum() > 125 && rc.getRoundNum() < 150) {
                Util.logBytecode("after doing high priority ruin");
            }
            return;
        }



        int resourceCenterIndex = Util.getPotentialResourcePatternCenterIndex(nearbyMapInfos);

        if (rc.getID() == 11065 && rc.getRoundNum() > 125 && rc.getRoundNum() < 150) {
            Util.logBytecode("after finding resource pattern center, " + rc.getRoundNum());
        }

        if (resourceCenterIndex != -1) {



            pattern = rc.getResourcePattern();

            if (rc.getID() == 11065 && rc.getRoundNum() > 125 && rc.getRoundNum() < 150) {
                Util.logBytecode("before starting resource pattern processing, " + rc.getRoundNum());
            }


            workOnResourcePattern(Shifts.dx[resourceCenterIndex], Shifts.dy[resourceCenterIndex], pattern);

            if (rc.getID() == 11065 && rc.getRoundNum() > 125 && rc.getRoundNum() < 150) {
                Util.logBytecode("after working on resource pattern, " + rc.getRoundNum());
            }

            if (rc.isMovementReady()) {
                nav.goTo(nearbyMapInfos[resourceCenterIndex].getMapLocation(), 0);
            }
            // TODO do this pattern completion efficiently
//            tryResourcePatternCompletion();
            if (rc.canCompleteResourcePattern(nearbyMapInfos[resourceCenterIndex].getMapLocation())) {
                rc.completeResourcePattern(nearbyMapInfos[resourceCenterIndex].getMapLocation());
            }
            sharedEndFunction();

            if (rc.getID() == 11065 && rc.getRoundNum() > 125 && rc.getRoundNum() < 150) {
                Util.logBytecode("after resource pattern processing, " + rc.getRoundNum());
            }


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

        runDefaultBehavior();



        sharedEndFunction();
        return;
    }

    private void runDefaultBehavior() throws GameActionException {
        // move destination to be on the line connecting what it currently is to right outside any overlap with vision radius
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
        if (isPaintReady) {
            for (int i = 0; i < 29; i++) {
                int index = Shifts.spiralOutwardIndices[i];
                if (nearbyMapInfos[index].hasRuin() || nearbyMapInfos[index].isWall()) {
                    continue;
                } else {
                    if (nearbyMapInfos[index].getPaint() == PaintType.EMPTY) {
                        MapLocation location = nearbyMapInfos[index].getMapLocation();
                        rc.attack(location, (location.x+location.y)%2 == 0);
                        break;
                    }
                }
            }
        }

        if (rc.isMovementReady()) {
            nav.goTo(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
        }




        // paint around us
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

                if (nearbyMapInfos[attackIndex].isWall()) {
                    continue;
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
            int r2 = myLoc.distanceSquaredTo(nearbyMapInfos[index].getMapLocation());

            if (r2 == 1) {
                // try going counter-clockwise around those r^2 = 1 squares
                Direction direction = myLoc.directionTo(nearbyMapInfos[index].getMapLocation());

                if (rc.canMove(direction.rotateRight())) {
                    rc.move(direction.rotateRight());
                }

            } else if (r2 == 2) {
                // diagonally adjacent to center
                Direction direction = myLoc.directionTo(nearbyMapInfos[index].getMapLocation());
                if (rc.canMove(direction.rotateRight())) {
                    rc.move(direction.rotateRight());
                } else if (rc.canMove(direction.rotateLeft())) {
                    rc.move(direction.rotateLeft());
                }
            } else if (r2 == 4) {
                Direction direction = myLoc.directionTo(nearbyMapInfos[index].getMapLocation());
                if (rc.canMove(direction)) {
                    rc.move(direction);
                } else if (rc.canMove(direction.rotateRight())) {
                    rc.move(direction.rotateRight());
                } else if (rc.canMove(direction.rotateLeft())) {
                    rc.move(direction.rotateLeft());
                }
            } else {
                nav.goTo(nearbyMapInfos[index].getMapLocation(), 0);
            }
        }
    }

    public void workOnResourcePattern(int dx, int dy, boolean[][] paintPattern) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;

        if (isPaintReady) {
//            int dx = Shifts.dx[index];
//            int dy = Shifts.dy[index];
            int[] ordering = PatternFillingIterators.orderFillingResource[13*dx + dy + 84];
            MapLocation attackSquare;
            for (int attackIndex : ordering) {
                if (nearbyMapInfos[attackIndex].hasRuin() || nearbyMapInfos[attackIndex].isWall()) {
                    continue;
                }
                PaintType currentPaint = nearbyMapInfos[attackIndex].getPaint();
                int offsetX = Shifts.dx[attackIndex] - dx;
                int offsetY = Shifts.dy[attackIndex] - dy;

                if (currentPaint == PaintType.EMPTY) {
                    attackSquare = nearbyMapInfos[attackIndex].getMapLocation();
                    // this check is needed for when we're near the edges
                    if (0 <= attackSquare.x && 0 <= attackSquare.y && attackSquare.x < rc.getMapWidth() && attackSquare.y < rc.getMapHeight()) {
                        if (offsetX * offsetX + offsetY * offsetY <= 8) {
                            rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX + 2][offsetY + 2]);
                        } else {
                            rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), (offsetX + dx + offsetY + dy) % 2 == 0);
                        }
                        break;
                    }
                }
            }
        }
    }

    public PatternPriority findPriority(int index, boolean[][] pattern) {
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


    public UnitType getPatternUnitType() {
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
            if (center.getPaint() == PaintType.ALLY_PRIMARY &&
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

            // If there's a mark and it's unpainted, favor that too.
            // if (tile.getMark().isAlly() && tile.getPaint() == PaintType.EMPTY) {
            // tileScore += 100;
            // }

            if (tileScore > bestScore) {
                bestScore = tileScore;
                bestDirection = tile.getMapLocation();
            }
        }

        if (bestDirection != null) {
            nav.goTo(bestDirection, 0);
        } else {
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