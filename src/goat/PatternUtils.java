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
    public static final long[] confirmedFinishedMasks = {0x5DF19F1F199F1D98L,0x19F1DE1F199E1994L,0x19F9DE1E1D9E1191L,0x99E9DE1E959E1598L,0xBBE95E9E959E9D94L,0x5DD19F5F59DD1918L,0x1DD19F5D199E1900L,0x1DF19E1C199C1080L,0x19E1DE1E11981010L,0x9BE95E1A159A1100L,0x9BA95EBA959E1480L,0xBBA95EBEB5BA9494L,0x5DD59D5D595D5B58L,0x5DD19F5D19555900L,0x1DD19D1D19D40000L,0x19D19D1C11080000L,0x99E11E1810001000L,0x99A95A9A10900000L,0x9BA95A9A95AA0000L,0xBBA95EBA94AAB480L,0xBBAB5ABAB4BAB6B4L,0x55D5B55D5955594AL,0x5DD5955519550840L,0x55D19D1509010000L,0x11D1940900000000L,0x1999000010000000L,0x89A94A1080000000L,0xABA95A8A84808000L,0xBBAB4AAA94AA8420L,0xABAB6ABAB4AAB4A6L,0x55D7B5555B554949L,0x5555B55549450808L,0x7554954508000800L,0x54D2800008000000L,0x1E00000000000L,0xAB2C400004000000L,0xEAAA4AA284000400L,0xAAAB6AAAA4A28404L,0xABAF6AAAB6AAA4A5L,0x7557B5556B554B58L,0x7756B5454B454900L,0x7456A56548440000L,0x6456A14420000000L,0xE666200002000000L,0xE22E60A240000000L,0xEA2E62E2A4220000L,0xEEAE6AA2A6A2A480L,0xEAAF6AAAE6AAA6B4L,0xF756B5756B557B4AL,0x7656B5E56B554A40L,0x7656A5656B614000L,0xE656A56162402000L,0xE61E21E062000200L,0xE62E62E0E2204000L,0xEE2E62E2E6E0A000L,0xEE2E6BE2E6AAA620L,0xEEAE6AEAE6AAF6A6L,0xF656B5F5EB656A4AL,0xF656B5E56A61EA40L,0xF65EA1E16A616220L,0xE61EE1E1E2606202L,0xEE3E61E0E660E240L,0xEE2E6BE2E661E620L,0xEE2E6BEBE6E2E626L,0xF65EA5E5EA65EE6AL,0xE65EE1E5EA61EA66L,0xE67EE1E1EE61E263L,0xE63EE1E3E661E66AL,0xEE3E63E3E663EE66L};
    public static final long[] primaryColorMask = {0x5FFFFF7FFDFFDDFFL,0xFDFDFF7FBFDFBDFFL,0xFFFFDF9FFF9F9FFFL,0xFBFBFEFFDFBFDDFFL,0xBFFFFEFFFDFFBDFFL,0xFDF5FFFFFFFF5FDFL,0x7FF7DFDF7FFFFDDDL,0xFFFBFF7F5FFF3FBDL,0x9FFFFFFFFD9E7F9FL,0xFFFDFEFEBFFEDFDDL,0xFFEFDFBEFFFFFDBDL,0xFBEBFFFFFFFEBFBFL,0x5FFFFFDFFBFF7BFFL,0xDFFDBF7FDFFFFBDBL,0xFFF59FFFF9DFFFF8L,0xDDFBDFFF3BFFDBB6L,0x9FF9FF9FFFFE71F3L,0xBBFDDFFED7FFB7DAL,0xFFEB5FFFF5BFFFF4L,0xBFFB7EFFBFFFF7B7L,0xBFFFFFBFF7FEF7FFL,0xFDF7FFDDFFDDFBFFL,0xFFDFFFDF5FFDDF7BL,0x7DDFBFFDDDFF7D6EL,0xFDFFFF5DB9FDBEC7L,0xFFFFFF9F9F99F27DL,0xFBFFFEBBD5FBDF2BL,0xFBBF7FFBBDFEFCEEL,0xFFBFFFBEBFFBBEF7L,0xFBEFFFBBFFBBF7FFL,0xFFFFBD7FFF5D7FFFL,0x7FFFFFFFFB57DF5FL,0x7FD7FD7FFFF7C9EDL,0xFFFFFD7D7F4FACBBL,0xFFFFFFFFFE001FFEL,0xFFFFFAFAFEB7CD57L,0xFFAFFAFFFFEFA5EDL,0xFFFFFFFFF6AFBEBFL,0xFFFF7AFFFEBAFFFFL,0xF7DFF7FF7F7F7BFFL,0xFFF7F7F5FFF77FDBL,0xF7F7BFF76FFDEFDCL,0xF7FFF5EF6BEF7F35L,0xFFFFE7E7FE7E738FL,0xEFFFEBF6E7F6FED9L,0xEFEF7FEEEFFBEFBCL,0xFFEFEFEBFFEEFFB7L,0xEFBFEFFEFEFEF7FFL,0xFFFFF7FFFBFDFBFFL,0xFF7FBDF7FFFFFB7BL,0xFF5EBFFFEB7FFFEAL,0xF6FEFFEDFBF7FAF6L,0xFE7FE7FFFFF9E3F3L,0xEF7EFFF3F7EFF77AL,0xFEBE7FFFE6FFFFE6L,0xFEFF7BEFFFFFF6F7L,0xFFFFEFFFF7FBF7FFL,0xF75FFFFFFFF5FF7FL,0xFFDEF7FDFFFFEF6FL,0xFEFFFDF5FFEDFEEFL,0xFFFFFFFFEE79FE7FL,0xFF7FFBEBFFF3FF6FL,0xFFBEEFFBFFFFEEEFL,0xEEBFFFFFFFEBFEFFL,0xFFFFFDFFEFF7EFFFL,0xF77FFDEFFF6FEFFFL,0xFFFEE7FFFE67FFFFL,0xEEFFFBF7FEF7EFFFL,0xFFFFFBFFEFEFEFFFL};
    public static final long[] secondaryColorMask = {0xFDFDDFDFDFDFFFFFL,0xDFFBDFDFDDFFDFFFL,0x9FF9FFFF9DFFFDFFL,0xBFFDDFBFBDFFBFFFL,0xFBFBDFBFBFBFFFFFL,0x5FFFFF5F5FDFFFFFL,0xDDFDFF7FDDDF5FFFL,0xDDFDDFDFBDDFDDDFL,0xFFF9DF9F9FFF9DFDL,0xBBFBDFBFDDBFBDBFL,0xBBFBFEFFBDBEBFFFL,0xBFFFFEBEBFBFFFFFL,0xFDF7BF7F7FDFFFFFL,0x7DF7FFDF7BDF5FFFL,0x5DFFFF5F5FFF59DFL,0xFFFDFF5FDDDF3DD9L,0xFFFFDFFF9D9F9F9CL,0xFFFBFEBFBDBEDDB5L,0xBBFFFEBEBFFEB5BFL,0xFBEFFFBEF7BEBFFFL,0xFBEF7EFEFFBFFFFFL,0x7FDFBF7F7BFF7FFFL,0x7DF7BF7DFBDF7BDFL,0xFFF7FF5F7BDDDBD9L,0xFFFFFFFF5FDF5938L,0xFFFFFFFFFDFE1D82L,0xFFFFFFFEBFBEB4D4L,0xFFEFFEBEF7BBB7B5L,0xFBEF7EFBF7BEF7BFL,0xFFBF7EFEF7FEFFFFL,0x7FD7FFFD7BFFFBFFL,0xFFD7BD7D7FFD7BFBL,0xFFFFBFFD7B5D7F5AL,0xFFFFFFFFFBF55B44L,0xFFFFFFFFFFFFE001L,0xFFFFFFFFF7EAB6A8L,0xFFFF7FFAF6BAFEB6L,0xFFAF7AFAFFFAF7F7L,0xFFAFFFFAF7FFF7FFL,0xFFF7BDFDFBFDFFFFL,0xF7DFBDFF7B7DFB7FL,0xFFDFF5FDFB777B6BL,0xFFFFFFF5FF75EACAL,0xFFFFFFFFEFE1EE70L,0xFFFFFFEBFEEBE726L,0xFFBFEBFBF6EEF6E7L,0xEFBF7BFEF6FBF6FFL,0xFFEF7BFBF7FBFFFFL,0xF7DFBDFDFF7FFFFFL,0xF7DFF7FDFB75FFFFL,0xF7FFF5F5FFF5EB7FL,0xFF7FF5F7EF6DEF6BL,0xFFFEFFE7EE67FE6EL,0xFEFFEBEFEEF3EEE7L,0xEFFFEBEBFFEBE6FFL,0xEFBFEFFBF6EBFFFFL,0xEFBF7BFBFEFFFFFFL,0xFFFFF5F5FF7FFFFFL,0xF77FFDF7EF75FFFFL,0xF77EF7EFEF77EF7FL,0xFE7EE7E7FFE7EFEFL,0xEEFEEFF7EEEFEEFFL,0xEEFFFBEFEEEBFFFFL,0xFFFFEBEBFEFFFFFFL,0xF77EF7F7FF7FFFFFL,0xFEFEF7F7EFF7FFFFL,0xFE7FFFE7EFFFEFFFL,0xFF7EEFEFEFEFFFFFL,0xEEFEEFEFFEFFFFFFL};

    public static void runDefaultBehavior() throws GameActionException {
        Util.addToIndicatorString("DFL;");
        // move destination to be on the line connecting what it currently is to right outside any overlap with vision radius
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
        if (isPaintReady) {
            for (int i = 0; i < 29; i++) {
                int index = soldier.spiralOutwardIndices[i];
                if (soldier.nearbyMapInfos[index] == null || soldier.nearbyMapInfos[index].hasRuin() || soldier.nearbyMapInfos[index].isWall()) {
                    continue;
                }
                MapLocation location = soldier.nearbyMapInfos[index].getMapLocation();
                if (rc.canAttack(location) && soldier.nearbyMapInfos[index].getPaint() == PaintType.EMPTY) {
                    rc.attack(location, (location.x+location.y)%2 == 0);
                    break;
                }
            }
        }

        if (rc.isMovementReady()) {
            Util.addToIndicatorString("DEST " + soldier.destination  + ";");
            soldier.nav.goToBug(soldier.destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
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
                    Util.move(direction.rotateRight());
                }

            } else if (r2 == 2) {
                // diagonally adjacent to center
                Direction direction = soldier.myLoc.directionTo(soldier.nearbyMapInfos[index].getMapLocation());
                if (rc.canMove(direction.rotateRight())) {
                    Util.move(direction.rotateRight());
                } else if (rc.canMove(direction.rotateLeft())) {
                    Util.move(direction.rotateLeft());
                }
            } else if (r2 == 4) {
                Direction direction = soldier.myLoc.directionTo(soldier.nearbyMapInfos[index].getMapLocation());
                if (rc.canMove(direction)) {
                    Util.move(direction);
                } else if (rc.canMove(direction.rotateRight())) {
                    Util.move(direction.rotateRight());
                } else if (rc.canMove(direction.rotateLeft())) {
                    Util.move(direction.rotateLeft());
                }
            } else {
                soldier.nav.goToFuzzy(soldier.nearbyMapInfos[index].getMapLocation(), 0);
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

    public static void workOnDefiniteResourcePattern(int dx, int dy, boolean[][] paintPattern) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;

        if (isPaintReady) {
            byte[] ordering = ExcessConstants.orderFillingRuinCall(13*dx + dy + 84);
            MapLocation attackSquare;
            for (short attackIndex : ordering) {
                if (soldier.nearbyMapInfos[attackIndex] == null || soldier.nearbyMapInfos[attackIndex].hasRuin() || soldier.nearbyMapInfos[attackIndex].isWall()) {
                    continue;
                }
                PaintType currentPaint = soldier.nearbyMapInfos[attackIndex].getPaint();
                int offsetX = shift_dx[attackIndex] - dx;
                int offsetY = shift_dy[attackIndex] - dy;
                if (offsetX < -2 || offsetX > 2 || offsetY < -2 || offsetY > 2) {
                    continue;
                }

                if (currentPaint == PaintType.EMPTY || (currentPaint.isAlly() && currentPaint.isSecondary() != paintPattern[offsetX + 2][offsetY + 2])) {
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

    public static boolean closeEnoughToDetermineRuinType(MapLocation ruinLoc) throws GameActionException {
        for(int x = ruinLoc.x - 1; x <= ruinLoc.x + 1; x++) {
            for(int y = ruinLoc.y - 1; y <= ruinLoc.y + 1; y++) {
                MapLocation loc = new MapLocation(x, y);
                if(!rc.canSenseLocation(loc)){
                    return false;
                }
            }
        }
        return true;
    }

    public static UnitType getRuinUnitType(MapLocation ruinLoc) throws GameActionException {
        // Check for any existing markings on what tower type to build.
        for(int x = ruinLoc.x - 1; x <= ruinLoc.x + 1; x++) {
            for(int y = ruinLoc.y - 1; y <= ruinLoc.y + 1; y++) {
                MapLocation loc = new MapLocation(x, y);
                MapInfo info = rc.senseMapInfo(loc);
                if(info.getMark().isAlly()) {
                    soldier.currRuinMarked = true;
                    if ((x+y)%2 == 0) {
                        return UnitType.LEVEL_ONE_PAINT_TOWER;
                    } else {
                        if(info.getMark().isSecondary()) {
                            return UnitType.LEVEL_ONE_MONEY_TOWER;
                        }
                        return UnitType.LEVEL_ONE_DEFENSE_TOWER;
                    }
                }
            }
        }

        // If we got here, then no type has been assigned to this guy yet, so make one rn.
        UnitType buildingType = decideRuinUnitType(ruinLoc);
        // Mark it so that other people are aware of that.
        soldier.currRuinMarked = markRuinUnitType(ruinLoc, buildingType);
        return buildingType;
    }

    public static boolean markRuinUnitType(MapLocation ruinLoc, UnitType buildingType) throws GameActionException {
        boolean intendedMod = false;
        if(buildingType == UnitType.LEVEL_ONE_PAINT_TOWER) {
            intendedMod = true;
        }
        for(int x = ruinLoc.x - 1; x <= ruinLoc.x + 1; x++) {
            for(int y = ruinLoc.y - 1; y <= ruinLoc.y + 1; y++) {
                if (((x+y)%2 == 0) != intendedMod) {
                    continue;
                }
                MapLocation loc = new MapLocation(x, y);
                if(rc.canMark(loc)) {
                    rc.mark(loc, buildingType == UnitType.LEVEL_ONE_MONEY_TOWER);
                    return true;
                }
            }
        }
        return false;
    }

    public static UnitType decideRuinUnitType(MapLocation ruinLoc) {
        if(ruinLoc.distanceSquaredTo(soldier.center) < 100) {
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        }
        if(soldier.nearestAlliedTowerType == TowerType.PaintTower) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }
        else {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }
    }

    public static boolean checkRuinCompleted(MapLocation ruinLoc, UnitType ruinType) throws GameActionException {
        boolean[][] pattern = rc.getTowerPattern(ruinType);
        return checkPatternCompleted(ruinLoc, pattern);
    }

    public static boolean checkPatternCompleted(MapLocation centerLoc, boolean[][] pattern) throws GameActionException {
        for(int x = centerLoc.x - 2; x <= centerLoc.x + 2; x++) {
            for(int y = centerLoc.y - 2; y <= centerLoc.y + 2; y++) {
                if(x == centerLoc.x && y == centerLoc.y) {
                    continue;
                }
                MapLocation loc = new MapLocation(x, y);
                if(!rc.canSenseLocation(loc)){
                    return false;
                }
                boolean shouldBeSecondary = pattern[x - centerLoc.x + 2][y - centerLoc.y + 2];
                if(shouldBeSecondary && rc.senseMapInfo(loc).getPaint() != PaintType.ALLY_SECONDARY){
                    return false;
                }
                if(!shouldBeSecondary && rc.senseMapInfo(loc).getPaint() != PaintType.ALLY_PRIMARY){
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkEnemyPaintInConsctructionArea(MapLocation centerLoc) throws GameActionException {
        for(int x = centerLoc.x - 2; x <= centerLoc.x + 2; x++) {
            for(int y = centerLoc.y - 2; y <= centerLoc.y + 2; y++) {
                MapLocation loc = new MapLocation(x, y);
                if(!rc.canSenseLocation(loc)){
                    continue;
                }
                if(rc.senseMapInfo(loc).getPaint().isEnemy()){
                    return true;
                }
            }
        }
        return false;
    }

    // TODO: Script to unroll created, but varun's gonna change some code so wait until that's done.
    public static int getPotentialResourcePatternCenterIndex(MapInfo[] nearbyMapInfos) throws GameActionException {
        long validBitstring = -1;
        long unfinishedBitstring = 0;

        for(int i = 0; i < 69; i++) {
            if (nearbyMapInfos[i] == null || nearbyMapInfos[i].hasRuin() || nearbyMapInfos[i].isWall()) {
                // if it's a ruin, we might actually want a bit bigger radius around it, but whatever
                validBitstring &= invalidSquareForResource[i];
            } else if (nearbyMapInfos[i].isResourcePatternCenter()) {
                // do stuff
                validBitstring &= confirmedFinishedMasks[i];
            } else {
                PaintType paint = nearbyMapInfos[i].getPaint();
                switch(paint) {
                    case PaintType.ENEMY_PRIMARY:
                    case PaintType.ENEMY_SECONDARY:
                        validBitstring &= invalidSquareForResource[i];
                        break;
                    case PaintType.EMPTY:
                        // we still need this because it's possible that a pattern was finished, but not completed (if not enough tiles)
                        // we wouldn't want to go toward these
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
