package goat;

import battlecode.common.*;

enum PatternPriority {
    LOW, MEDIUM, HIGH
}



public class PatternUtils {
    static Soldier soldier;
    static RobotController rc;


    public static final byte[] shift_dx = {-4,-4,-4,-4,-4,-3,-3,-3,-3,-3,-3,-3,-2,-2,-2,-2,-2,-2,-2,-2,-2,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,4,4,4,4,4};
    public static final byte[] shift_dy = {-2,-1,0,1,2,-3,-2,-1,0,1,2,3,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-3,-2,-1,0,1,2,3,-2,-1,0,1,2};

    public static final long[] invalidSquareForResource = {0x5DFDDF5FDDDFDDFFL,0xDDF9DF5F9DDF9DFFL,0x9FF9DF9F9D9F9DFFL,0xBBF9DEBF9DBF9DFFL,0xBBFBDEBFBDBFBDFFL,0x5DF5FF5F5FDF5FDFL,0x5DF5DF5F5DDF5DDDL,0xDDF9DF5F1DDF1D9DL,0x9FF9DF9F9D9E1D9DL,0xBBF9DEBE9DBE9D9DL,0xBBEBDEBEBDBEBDBDL,0xBBEBFEBEBFBEBFBFL,0x5DF7BF5F7BDF7BFFL,0x5DF5BF5F5BDF5BDBL,0x5DF59F5F59DF59D8L,0xDDF9DF5F19DF1990L,0x9FF9DF9F9D9E1190L,0xBBF9DEBE95BE9590L,0xBBEB5EBEB5BEB5B4L,0xBBEB7EBEB7BEB7B7L,0xBBEF7EBEF7BEF7FFL,0x7DD7BF5D7BDD7BFFL,0x7DD7BF5D5BDD5B5BL,0x7DD7BF5D59DD5948L,0xFDFFFF5D19DD1800L,0xFFFFFF9F9D981000L,0xFBFFFEBA95BA9400L,0xFBAF7EBAB5BAB4A4L,0xFBAF7EBAB7BAB6B7L,0xFBAF7EBAF7BAF7FFL,0x7FD7BD7D7B5D7BFFL,0x7FD7BD7D7B555B5BL,0x7FD7BD7D7B554948L,0xFFFFFD7D7B450800L,0xFFFFFFFFFE000000L,0xFFFFFAFAF6A28400L,0xFFAF7AFAF6AAA4A4L,0xFFAF7AFAF6AAB6B7L,0xFFAF7AFAF6BAF7FFL,0xF7D7B5FD7B7D7BFFL,0xF7D7B5F57B757B5BL,0xF7D7B5F56B756B48L,0xF7FFF5E56B656A00L,0xFFFFE7E7EE606200L,0xEFFFEBE2E6E2E600L,0xEFAF6BEAE6EAE6A4L,0xEFAF6BEAF6EAF6B7L,0xEFAF6BFAF6FAF7FFL,0xF7DFB5FDFB7DFBFFL,0xF75FB5F5FB75FB7BL,0xF75EB5F5EB75EB6AL,0xF67EF5E5EB65EA62L,0xFE7EE7E7EE61E262L,0xEE7EEBE3E6E3E662L,0xEEBE6BEBE6EBE6E6L,0xEEBF6BEBF6EBF6F7L,0xEFBF6BFBF6FBF7FFL,0xF75FF5F5FF75FF7FL,0xF75EF5F5EF75EF6FL,0xF67EF5E5EF65EE6FL,0xFE7EE7E7EE61EE6FL,0xEE7EEBE3EEE3EE6FL,0xEEBEEBEBEEEBEEEFL,0xEEBFEBEBFEEBFEFFL,0xF77EF5F7EF77EFFFL,0xF67EF5E7EF67EFFFL,0xFE7EE7E7EE67EFFFL,0xEE7EEBE7EEE7EFFFL,0xEEFEEBEFEEEFEFFFL};
    public static final long[] emptySquareForResource = {0xA20220A022202200L,0x220620A062206200L,0x6006206062606200L,0x4406214062406200L,0x4404214042404200L,0xA20A00A0A020A020L,0xA20A20A0A220A222L,0x220620A0E220E262L,0x600620606261E262L,0x4406214162416262L,0x4414214142414242L,0x4414014140414040L,0xA20840A084208400L,0xA20A40A0A420A424L,0xA20A60A0A620A627L,0x220620A0E620E66FL,0x600620606261EE6FL,0x440621416A416A6FL,0x4414A1414A414A4BL,0x4414814148414848L,0x4410814108410800L,0x822840A284228400L,0x822840A2A422A4A4L,0x822840A2A622A6B7L,0x20000A2E622E7FFL,0x606267EFFFL,0x40001456A456BFFL,0x45081454A454B5BL,0x450814548454948L,0x450814508450800L,0x8028428284A28400L,0x8028428284AAA4A4L,0x8028428284AAB6B7L,0x28284BAF7FFL,0x1FFFFFFL,0x505095D7BFFL,0x50850509555B5BL,0x50850509554948L,0x50850509450800L,0x8284A0284828400L,0x8284A0A848A84A4L,0x8284A0A948A94B7L,0x8000A1A949A95FFL,0x1818119F9DFFL,0x1000141D191D19FFL,0x105094151915195BL,0x1050941509150948L,0x1050940509050800L,0x8204A0204820400L,0x8A04A0A048A0484L,0x8A14A0A148A1495L,0x9810A1A149A159DL,0x1811818119E1D9DL,0x1181141C191C199DL,0x1141941419141919L,0x1140941409140908L,0x1040940409040800L,0x8A00A0A008A0080L,0x8A10A0A108A1090L,0x9810A1A109A1190L,0x1811818119E1190L,0x1181141C111C1190L,0x1141141411141110L,0x1140141401140100L,0x8810A0810881000L,0x9810A1810981000L,0x181181811981000L,0x1181141811181000L,0x1101141011101000L};
    public static final long[] primaryColorMask = {0x7DFFDFFFDFDFFDFFL,0xFFFBFF5FFDFF9FFFL,0x9FFFFFFF9F9FFDFFL,0xFFFDFEBFFDFF9FFFL,0xFBFFDFFFBFBFFDFFL,0x5FFFFFFF5FDFFFDFL,0xFFF5FF7FFDFF5FFDL,0xDDFFDFFF3FDFFD9FL,0xFFF9FF9FFFFE1FFDL,0xBBFFDFFEDFBFFD9FL,0xFFEBFEFFFDFEBFFDL,0xBFFFFFFEBFBFFFBFL,0xDDFFBFFF7FDFFBFFL,0xFFF5FFDFFBFF5FFBL,0x5DFF9FFF5FFFF9DEL,0xFFF9FF5FF9FF3FF1L,0x9FFFDFFF9F9FF39EL,0xFFF9FEBFF5FEDFF1L,0xBBFF5FFEBFFFF5BEL,0xFFEBFFBFF7FEBFF7L,0xBBFF7FFEFFBFF7FFL,0xFFDFFF5FFBFD7FFFL,0x7DFFBFFDDFDFFB5FL,0xFFD7FF5FF9FDDFE9L,0xFDFFFFFD1FDFF83EL,0xFFFFFF9FFDF81FE3L,0xFBFFFFFA9FBFF45EL,0xFFAFFEBFF5FBBFE5L,0xFBFF7FFBBFBFF6BFL,0xFFBFFEBFF7FAFFFFL,0x7FFFFFFD7F5FFBFFL,0xFFD7FD7FFFF55FFBL,0x7FFFBFFD7F5FED5EL,0xFFFFFD7FFBE51FE5L,0xFFFFFFFFFE1FE01FL,0xFFFFFAFFF7E29FE9L,0xFFFF7FFAFEBFECBEL,0xFFAFFAFFFFEABFF7L,0xFFFFFFFAFEBFF7FFL,0xFFF7F5FFFBFD7FFFL,0xF7FFBFF77F7FFB5FL,0xFFD7F5FFEBF77FE9L,0xF7FFFFE57F7FEA9EL,0xFFFFE7FFEFE07FF1L,0xEFFFFFE2FEFFE71EL,0xFFAFEBFFE7EEFFE5L,0xEFFF7FEEFEFFF6BFL,0xFFEFEBFFF7FAFFFFL,0xF7FFBFFDFF7FFBFFL,0xFF5FF7FFFBF5FFFBL,0xF7FEBFF5FFFFEB7EL,0xFE7FF5FFEBEDFFE3L,0xFFFEFFE7FE7FF27EL,0xFE7FEBFFE7F3FFE3L,0xEFFE7FEBFFFFE6FEL,0xFEBFEFFFF7EBFFF7L,0xEFFF7FFBFEFFF7FFL,0xFFFFFFF5FF7FFF7FL,0xFF5FFDFFEFF5FFEFL,0xF7FEFFEDFF7FEE7FL,0xFE7FE7FFFFE1FFEFL,0xEFFEFFF3FEFFEE7FL,0xFEBFFBFFEFEBFFEFL,0xFFFFFFEBFEFFFEFFL,0xF7FEFFF7FF7FEFFFL,0xFEFFF5FFEFE7FFFFL,0xFFFFFFE7FE7FEFFFL,0xFF7FEBFFEFE7FFFFL,0xEFFEFFEFFEFFEFFFL};
    public static final long[] secondaryColorMask = {0xDFFDFF5FFDFFDFFFL,0xDDFDDFFF9FDFFDFFL,0xFFF9DF9FFDFF9FFFL,0xBBFBDFFF9FBFFDFFL,0xBFFBFEBFFDFFBFFFL,0xFDF5FF5FFFFF5FFFL,0x5DFFDFDF5FDFFDDFL,0xFFF9FF5FDDFF1FFDL,0x9FFFDFFF9D9FFD9FL,0xFFF9FEBFBDFE9FFDL,0xBBFFDFBEBFBFFDBFL,0xFBEBFEBFFFFEBFFFL,0x7FF7FF5FFBFF7FFFL,0x5DFFBF7F5FDFFBDFL,0xFFF5FF5FF9DF5FF9L,0xDDFFDFFF1FDFD99EL,0xFFF9FF9FFDFE1DF1L,0xBBFFDFFE9FBFB59EL,0xFFEBFEBFF5BEBFF5L,0xBBFF7EFEBFBFF7BFL,0xFFEFFEBFF7FEFFFFL,0x7DF7BFFD7FDFFBFFL,0xFFD7FF5F7BFD5FFBL,0x7DFFBFFD5FDF795EL,0xFFFFFF5FF9FD1FC1L,0xFFFFFFFF9F9FF01CL,0xFFFFFEBFF5FA9FA1L,0xFBFF7FFABFBEF4BEL,0xFFAFFEBEF7FABFF7L,0xFBEF7FFAFFBFF7FFL,0xFFD7BD7FFBFD7FFFL,0x7FFFBFFD7B5FFB5FL,0xFFD7FD7FFBF55BE9L,0xFFFFFFFD7F5FE81AL,0xFFFFFFFFFFE01FE0L,0xFFFFFFFAFEBFE416L,0xFFAFFAFFF7EAB7E5L,0xFFFF7FFAF6BFF6BFL,0xFFAF7AFFF7FAFFFFL,0xF7DFBFFD7F7FFBFFL,0xFFD7F5FDFBF57FFBL,0xF7FFBFF57F7DEB5EL,0xFFFFF5FFEBE57F61L,0xFFFFFFE7FE7FE20EL,0xFFFFEBFFE7E2FEE1L,0xEFFF7FEAFEFBE6BEL,0xFFAFEBFBF7EAFFF7L,0xEFBF7FFAFEFFF7FFL,0xFFDFF5FFFBFDFFFFL,0xF7FFBDF5FF7FFB7FL,0xFF5FF5FFEB75FFEBL,0xF7FEFFE5FF77EA7EL,0xFE7FE7FFEFE1EFE3L,0xEFFEFFE3FEEFE67EL,0xFEBFEBFFE6EBFFE7L,0xEFFF7BEBFEFFF6FFL,0xFFBFEBFFF7FBFFFFL,0xF75FF5FFFFF5FFFFL,0xF7FEF7F5FF7FEF7FL,0xFE7FF5F7EFE5FFEFL,0xFFFEFFE7EE7FEE7FL,0xFE7FEBEFEFE3FFEFL,0xEFFEEFEBFEFFEEFFL,0xEEBFEBFFFFEBFFFFL,0xFF7FF5FFEFF7FFFFL,0xF77EFFE7FF7FEFFFL,0xFE7EE7FFEFE7FFFFL,0xEEFEFFE7FEFFEFFFL,0xFEFFEBFFEFEFFFFFL};

    public static void runDefaultBehavior() throws GameActionException {
        // move destination to be on the line connecting what it currently is to right outside any overlap with vision radius
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
        if (isPaintReady) {
            for (int i = 0; i < 29; i++) {
                int index = soldier.spiralOutwardIndices[i];
                if (soldier.nearbyMapInfos[index] == null || soldier.nearbyMapInfos[index].hasRuin() || soldier.nearbyMapInfos[index].isWall()) {
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
            soldier.nav.goTo(soldier.destination, 9);//Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
        }
    }

    public static void workOnRuin(int index, boolean[][] paintPattern) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;

        if (isPaintReady) {
            byte dx = shift_dx[index];
            byte dy = shift_dy[index];

            byte[] ordering = ExcessConstants.orderFillingRuinCall(13*dx + dy + 84);

            for (byte attackIndex : ordering) {
                if (attackIndex == index) {
                    continue;
                }

                if (soldier.nearbyMapInfos[attackIndex] == null || soldier.nearbyMapInfos[attackIndex].isWall()) {
                    continue;
                }

                PaintType currentPaint = soldier.nearbyMapInfos[attackIndex].getPaint();
                if (currentPaint == PaintType.EMPTY) {
                    int offsetX = shift_dx[attackIndex] - dx;
                    int offsetY = shift_dy[attackIndex] - dy;
                    if (offsetX*offsetX + offsetY*offsetY <= 8) {
                        rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);

                    } else {
                        rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), (offsetX+dx+offsetY+dy)%2==0);
                    }
                    break;
                } else if (currentPaint.isEnemy()) {
                    continue;
                } else {
                    int offsetX = shift_dx[attackIndex] - dx;
                    int offsetY = shift_dy[attackIndex] - dy;

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
            byte[] ordering = ExcessConstants.orderFillingResourceCall(13*dx + dy + 84);
            MapLocation attackSquare;
            for (short attackIndex : ordering) {
                if (soldier.nearbyMapInfos[attackIndex] == null || soldier.nearbyMapInfos[attackIndex].hasRuin() || soldier.nearbyMapInfos[attackIndex].isWall()) {
                    continue;
                }
                PaintType currentPaint = soldier.nearbyMapInfos[attackIndex].getPaint();
                int offsetX = shift_dx[attackIndex] - dx;
                int offsetY = shift_dy[attackIndex] - dy;

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

        int dx = shift_dx[index];
        int dy = shift_dy[index];

        byte[] indexOrder;
        if (dx < -6 || dx > 6 || dy < -6 || dy > 6) {
            indexOrder = new byte[]{};
        }
        else {
            indexOrder = ExcessConstants.gridLookupIndicesPatternCall(13*dx + dy + 84);
        }

        for (int neighboringIndex : indexOrder) {
            if (neighboringIndex == index) {
                continue;
            }
            PaintType paintType = soldier.nearbyMapInfos[neighboringIndex].getPaint();
            if (paintType == PaintType.EMPTY) {
                return PatternPriority.HIGH;
            } else if (paintType.isEnemy()) {
                hasEnemy = true;
            } else {
                int lookupX = shift_dx[neighboringIndex] - shift_dx[index] + 2;
                int lookupY = shift_dy[neighboringIndex] - shift_dy[index] + 2;
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

    public static UnitType getPatternUnitType() {
        if(soldier.nearestAlliedTowerType == TowerType.PaintTower) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }
        else {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }
    }

    // TODO: Script to unroll created, but varun's gonna change some code so wait until that's done.
    public static int getPotentialResourcePatternCenterIndex(MapInfo[] nearbyMapInfos) throws GameActionException {
        long validBitstring = -1;
        long unfinishedBitstring = 0;

        for(int i = 0; i < 69; i++) {
            if (nearbyMapInfos[i] == null || nearbyMapInfos[i].hasRuin() || nearbyMapInfos[i].isWall()) {
                // if it's a ruin, we might actually want a bit bigger radius around it, but whatever
                validBitstring &= invalidSquareForResource[i];
            } else {
                PaintType paint = nearbyMapInfos[i].getPaint();
                switch(paint) {
                    case PaintType.ENEMY_PRIMARY:
                    case PaintType.ENEMY_SECONDARY:
                        validBitstring &= invalidSquareForResource[i];
                        break;
                    case PaintType.EMPTY:
                        unfinishedBitstring |= emptySquareForResource[i];
                        break;
                    case PaintType.ALLY_PRIMARY:
                        validBitstring &= primaryColorMask[i];
                        break;
                    default:
                        validBitstring &= secondaryColorMask[i];
                        break;
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
        return soldier.spiralOutwardIndices[counter];
    }

}
