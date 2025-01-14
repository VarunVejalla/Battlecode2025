package goat;

import battlecode.common.*;

enum PatternPriority {
    LOW, MEDIUM, HIGH
}

public class PatternUtils {
    static Soldier soldier;
    static RobotController rc;

    public static void runDefaultBehavior() throws GameActionException {
        // move destination to be on the line connecting what it currently is to right outside any overlap with vision radius
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
        if (isPaintReady) {
            for (int i = 0; i < 29; i++) {
                int index = Constants.spiralOutwardIndices[i];
                if (soldier.nearbyMapInfos[index].hasRuin() || soldier.nearbyMapInfos[index].isWall()) {
                    continue;
                } else {
                    if (soldier.nearbyMapInfos[index].getPaint() == PaintType.EMPTY) {
                        MapLocation location = soldier.nearbyMapInfos[index].getMapLocation();
                        rc.attack(location, (location.x+location.y)%2 == 0);
                        break;
                    }
                }
            }
        }

        if (rc.isMovementReady()) {
            soldier.nav.goTo(soldier.destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
        }
    }

    public static void workOnRuin(int index, boolean[][] paintPattern) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
        if (isPaintReady) {
            int dx = Constants.shift_dx[index];
            int dy = Constants.shift_dy[index];
            int[] ordering = Constants.orderFillingRuin[13*dx + dy + 84];

            for (int attackIndex : ordering) {
                if (attackIndex == index) {
                    continue;
                }

                if (soldier.nearbyMapInfos[attackIndex].isWall()) {
                    continue;
                }

                PaintType currentPaint = soldier.nearbyMapInfos[attackIndex].getPaint();
                if (currentPaint == PaintType.EMPTY) {
                    int offsetX = Constants.shift_dx[attackIndex] - dx;
                    int offsetY = Constants.shift_dy[attackIndex] - dy;
                    if (offsetX*offsetX + offsetY*offsetY <= 8) {
                        rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);

                    } else {
                        rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), (offsetX+dx+offsetY+dy)%2==0);
                    }
                    break;
                } else if (currentPaint.isEnemy()) {
                    continue;
                } else {
                    int offsetX = Constants.shift_dx[attackIndex] - dx;
                    int offsetY = Constants.shift_dy[attackIndex] - dy;

                    if (offsetX*offsetX + offsetY*offsetY <= 8 && paintPattern[offsetX+2][offsetY+2] != currentPaint.isSecondary()) {
                        rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);
                        break;
                    }
                }
            }
        }

        if (rc.isMovementReady()) {
            int r2 = soldier.myLoc.distanceSquaredTo(soldier.nearbyMapInfos[index].getMapLocation());

            if (r2 == 1) {
                // try going counter-clockwise around those r^2 = 1 squares
                Direction direction = soldier.myLoc.directionTo(soldier.nearbyMapInfos[index].getMapLocation());

                if (rc.canMove(direction.rotateRight())) {
                    rc.move(direction.rotateRight());
                }

            } else if (r2 == 2) {
                // diagonally adjacent to center
                Direction direction = soldier.myLoc.directionTo(soldier.nearbyMapInfos[index].getMapLocation());
                if (rc.canMove(direction.rotateRight())) {
                    rc.move(direction.rotateRight());
                } else if (rc.canMove(direction.rotateLeft())) {
                    rc.move(direction.rotateLeft());
                }
            } else if (r2 == 4) {
                Direction direction = soldier.myLoc.directionTo(soldier.nearbyMapInfos[index].getMapLocation());
                if (rc.canMove(direction)) {
                    rc.move(direction);
                } else if (rc.canMove(direction.rotateRight())) {
                    rc.move(direction.rotateRight());
                } else if (rc.canMove(direction.rotateLeft())) {
                    rc.move(direction.rotateLeft());
                }
            } else {
                soldier.nav.goTo(soldier.nearbyMapInfos[index].getMapLocation(), 0);
            }
        }
    }

    public static void workOnResourcePattern(int dx, int dy, boolean[][] paintPattern) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;

        if (isPaintReady) {
            int[] ordering = Constants.orderFillingResource[13*dx + dy + 84];
            MapLocation attackSquare;
            for (int attackIndex : ordering) {
                if (soldier.nearbyMapInfos[attackIndex].hasRuin() || soldier.nearbyMapInfos[attackIndex].isWall()) {
                    continue;
                }
                PaintType currentPaint = soldier.nearbyMapInfos[attackIndex].getPaint();
                int offsetX = Constants.shift_dx[attackIndex] - dx;
                int offsetY = Constants.shift_dy[attackIndex] - dy;

                if (currentPaint == PaintType.EMPTY) {
                    attackSquare = soldier.nearbyMapInfos[attackIndex].getMapLocation();
                    // this check is needed for when we're near the edges
                    if (0 <= attackSquare.x && 0 <= attackSquare.y && attackSquare.x < rc.getMapWidth() && attackSquare.y < rc.getMapHeight()) {
                        if (offsetX * offsetX + offsetY * offsetY <= 8) {
                            rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX + 2][offsetY + 2]);
                        } else {
                            rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), (offsetX + dx + offsetY + dy) % 2 == 0);
                        }
                        break;
                    }
                }
            }
        }
    }

    public static PatternPriority findPriority(int index, boolean[][] pattern) {
        // guaranteed that this is a ruin
        boolean hasEmpty = false;
        boolean hasEnemy = false;
        boolean hasConflictingPaint = false;

        for (int neighboringIndex : getIndicesForSquareSpiral(Constants.shift_dx[index], Constants.shift_dy[index])) {
            if (neighboringIndex == index) {
                continue;
            }
            PaintType paintType = soldier.nearbyMapInfos[neighboringIndex].getPaint();
            if (paintType == PaintType.EMPTY) {
                return PatternPriority.HIGH;
            } else if (paintType.isEnemy()) {
                hasEnemy = true;
            } else {
                int lookupX = Constants.shift_dx[neighboringIndex] - Constants.shift_dx[index] + 2;
                int lookupY = Constants.shift_dy[neighboringIndex] - Constants.shift_dy[index] + 2;
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

    public static int getPotentialResourcePatternCenterIndex(MapInfo[] nearbyMapInfos) throws GameActionException {
        long validBitstring = -1;
        long unfinishedBitstring = 0;

        for(int i = 0; i < 69; i++) {
            if (nearbyMapInfos[i].hasRuin() || nearbyMapInfos[i].isWall()) {
                // if it's a ruin, we might actually want a bit bigger radius around it, but whatever
                validBitstring &= Constants.invalidSquareForResource[i];
            } else {
                PaintType paint = nearbyMapInfos[i].getPaint();
                if (paint.isEnemy()) {
                    validBitstring &= Constants.invalidSquareForResource[i];
                } else if (paint == PaintType.EMPTY) {
                    unfinishedBitstring |= Constants.emptySquareForResource[i];
                } else if (paint == PaintType.ALLY_PRIMARY) {
                    validBitstring &= Constants.primaryColorMask[i];
                } else {
                    validBitstring &= Constants.secondaryColorMask[i];
                }
            }
        }

        validBitstring &= unfinishedBitstring;
        if (validBitstring == 0) {
            return -1;
        }

        int counter = 64; // c will be the number of zero bits on the right
        validBitstring &= -validBitstring;
        if (validBitstring != 0) counter--;
        if ((validBitstring & 0x00000000FFFFFFFFL) != 0) counter -= 32;
        if ((validBitstring & 0x0000FFFF0000FFFFL) != 0) counter -= 16;
        if ((validBitstring & 0x00FF00FF00FF00FFL) != 0) counter -= 8;
        if ((validBitstring & 0x0F0F0F0F0F0F0F0FL) != 0) counter -= 4;
        if ((validBitstring & 0x3333333333333333L) != 0) counter -= 2;
        if ((validBitstring & 0x5555555555555555L) != 0) counter -= 1;
        return Constants.spiralOutwardIndices[counter];
    }

    public static int[] getIndicesForSquareSpiral(int dx, int dy) {
        if (dx < -6 || dx > 6 || dy < -6 || dy > 6) {
            return new int[]{};
        }
        else {
            int index = 13*dx + dy + 84;
            return BigGridIterators.gridLookupIndicesPattern[index];
        }
    }

    public static UnitType getPatternUnitType() {
        if(soldier.nearestAlliedTowerType == TowerType.PaintTower) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }
        else {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }
    }

}
