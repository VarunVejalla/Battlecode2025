package moneymoneymoney;

import battlecode.common.*;

public class PatternUtils {
    static Soldier soldier;
    static RobotController rc;

    public static boolean getDefaultColor(int x, int y) {
//        return (x+y)%2 == 0;
        int lookup = 4*(x&3)+(y&3);

        switch (lookup) {
            case 0:
                return true;
            case 1:
                return true;
            case 2:
                return false;
            case 3:
                return true;
            case 4:
                return true;
            case 5:
                return false;
            case 6:
                return false;
            case 7:
                return false;
            case 8:
                return false;
            case 9:
                return false;
            case 10:
                return true;
            case 11:
                return false;
            case 12:
                return true;
            case 13:
                return false;
            case 14:
                return false;
            case 15:
                return false;
            default:
                assert false;
                return false;
        }
    }

    public static boolean getDefaultColor(MapLocation location) {
        return getDefaultColor(location.x, location.y);
    }

    public static void runDefaultBehavior(boolean paint) throws GameActionException {
        Util.addToIndicatorString("DFL");
        // move destination to be on the line connecting what it currently is to right outside any overlap with vision radius
        boolean isPaintReady = paint && rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
        if (isPaintReady) {
            for (int i = 0; i < 29; i++) {
                int index = soldier.spiralOutwardIndices[i];
                if (soldier.nearbyMapInfos[index] == null || soldier.nearbyMapInfos[index].hasRuin() || soldier.nearbyMapInfos[index].isWall()) {
                    continue;
                }
                MapLocation location = soldier.nearbyMapInfos[index].getMapLocation();
                if (rc.canAttack(location) && soldier.nearbyMapInfos[index].getPaint() == PaintType.EMPTY) {
                    rc.attack(location, getDefaultColor(location));
                    break;
                }
            }
        }

        if (rc.isMovementReady()) {
            soldier.adjustDestination();
            MapLocation myLoc = rc.getLocation();

            Util.addToIndicatorString("DEST: " + soldier.destination);
            soldier.nav.goToBug(soldier.destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);

//            // Select array of unexplored sectors
//            int[] unexploredSectors = {-1,-1,-1,-1,-1,-1,-1,-1, -1};
//            int unexploredSectorCount = 0;
//            int[] neighborSectorIndexes = Util.getSectorAndNeighbors(myLoc, 1);
//
//            for (int neighorSectorIndex : neighborSectorIndexes) {
//                // Check the last bit for exploration
//                if((soldier.comms.myWorld[neighorSectorIndex] & 1) == 0) {
//                    unexploredSectors[unexploredSectorCount] = neighorSectorIndex;
//                    unexploredSectorCount++;
//                }
//            }
//
//            if(unexploredSectorCount == 0) {
//                Util.addToIndicatorString("RAND DEST " + soldier.destination  + ";");
//                soldier.nav.goToBug(soldier.destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
//            }
//            else {
//                // Random select from the unexplored sectors.
//                int randomSector = unexploredSectors[soldier.rng.nextInt(unexploredSectorCount)];
//                Util.addToIndicatorString("EXPLORE DEST " + soldier.destination  + ";");
//                soldier.nav.goToBug(Util.getSectorCenter(randomSector), Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
//            }


        }
    }

    public static void workOnRuin(int index, boolean[][] paintPattern, boolean paintEmpty) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;

        if (isPaintReady) {
            byte dx = UnrolledConstants.getShiftDx(index);
            byte dy = UnrolledConstants.getShiftDy(index);

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
                    int offsetX = UnrolledConstants.getShiftDx(attackIndex) - dx;
                    int offsetY = UnrolledConstants.getShiftDy(attackIndex) - dy;
                    if (offsetX*offsetX + offsetY*offsetY <= 8) {
                        rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);
                        break;
                    } else if (paintEmpty) {
                        rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), getDefaultColor(offsetX+dx, offsetY+dy));
                        break;
                    }
                } else if (currentPaint.isEnemy()) {
                    continue;
                } else {
                    int offsetX = UnrolledConstants.getShiftDx(attackIndex) - dx;
                    int offsetY = UnrolledConstants.getShiftDy(attackIndex) - dy;

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
                soldier.nav.goToSmart(soldier.nearbyMapInfos[index].getMapLocation(), 0);
            }
        }
    }

    public static void workOnResourcePattern(int dx, int dy, boolean[][] paintPattern, boolean paintOutside) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;

        if (isPaintReady) {
            byte[] ordering = ExcessConstants.orderFillingResourceCall(13*dx + dy + 84);
            MapLocation attackSquare;
            for (byte attackIndex : ordering) {
                if (soldier.nearbyMapInfos[attackIndex] == null || soldier.nearbyMapInfos[attackIndex].hasRuin() || soldier.nearbyMapInfos[attackIndex].isWall() || !rc.canAttack(soldier.nearbyMapInfos[attackIndex].getMapLocation())) {
                    continue;
                }
                PaintType currentPaint = soldier.nearbyMapInfos[attackIndex].getPaint();
                int offsetX = UnrolledConstants.getShiftDx(attackIndex) - dx;
                int offsetY = UnrolledConstants.getShiftDy(attackIndex) - dy;

                attackSquare = soldier.nearbyMapInfos[attackIndex].getMapLocation();
                // this check is needed for when we're near the edges
                if (0 <= attackSquare.x && 0 <= attackSquare.y && attackSquare.x < rc.getMapWidth() && attackSquare.y < rc.getMapHeight()) {
                    if (offsetX * offsetX + offsetY * offsetY <= 8) {
                        // If it's already painted correctly, skip.
                        if(paintPattern[offsetX + 2][offsetY + 2] && (currentPaint == PaintType.ALLY_SECONDARY)){
                            continue;
                        }
                        if(!paintPattern[offsetX + 2][offsetY + 2] && (currentPaint == PaintType.ALLY_PRIMARY)){
                            continue;
                        }
                        Util.addToIndicatorString("PNT:" + attackSquare);
                        rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX + 2][offsetY + 2]);
                        return;
                    } else if (paintOutside) {
                        if(currentPaint != PaintType.EMPTY){
                            continue;
                        }
                        Util.addToIndicatorString("PNTR:" + attackSquare);
                        rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), getDefaultColor(offsetX+dx, offsetY+dy));
                        return;
                    }
                }
            }
        }
    }

    public static void workOnPotentialResourceCenter(boolean paintOutside) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
        if(!isPaintReady){
            return;
        }

        MapLocation myLoc = rc.getLocation();
        boolean[][] paintPattern = rc.getResourcePattern();
        int dx = soldier.potentialResourceCenterLoc.x - myLoc.x;
        int dy = soldier.potentialResourceCenterLoc.y - myLoc.y;

        // Just try default painting, but DONT OVERRIDE ANY EXISTING PAINT.
        byte[] ordering = ExcessConstants.orderFillingResourceCall(13*dx + dy + 84);
        MapLocation attackSquare;
        for (short attackIndex : ordering) {
            if (soldier.nearbyMapInfos[attackIndex] == null || soldier.nearbyMapInfos[attackIndex].hasRuin() || soldier.nearbyMapInfos[attackIndex].isWall() || !rc.canAttack(soldier.nearbyMapInfos[attackIndex].getMapLocation())) {
                continue;
            }
            PaintType currentPaint = soldier.nearbyMapInfos[attackIndex].getPaint();
            int offsetX = UnrolledConstants.getShiftDx(attackIndex) - dx;
            int offsetY = UnrolledConstants.getShiftDy(attackIndex) - dy;

            attackSquare = soldier.nearbyMapInfos[attackIndex].getMapLocation();
            if(currentPaint != PaintType.EMPTY){
                continue;
            }

            // this check is needed for when we're near the edges
            if (0 <= attackSquare.x && 0 <= attackSquare.y && attackSquare.x < rc.getMapWidth() && attackSquare.y < rc.getMapHeight()) {
                if (offsetX * offsetX + offsetY * offsetY <= 8) {
                    rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX + 2][offsetY + 2]);
                    Util.addToIndicatorString("PRNT:" + attackSquare);
                    return;
                } else if (paintOutside){
                    rc.attack(soldier.nearbyMapInfos[attackIndex].getMapLocation(), getDefaultColor(soldier.nearbyMapInfos[attackIndex].getMapLocation()));
                }
            }
        }
    }


    public static boolean markResponsibility(MapLocation ruinLoc) throws GameActionException {
        for(int x = ruinLoc.x - 1; x <= ruinLoc.x + 1; x++) {
            for(int y = ruinLoc.y - 1; y <= ruinLoc.y + 1; y++) {
                MapLocation loc = new MapLocation(x, y);
                if(!rc.canSenseLocation(loc)) {
                    continue;
                }
                if(rc.senseMapInfo(loc).getMark().isAlly()){
                    continue;
                }
                if(rc.canMark(loc)) {
                    rc.mark(loc, true);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkSomeoneResponsible(MapLocation centerLoc) throws GameActionException {
        for(int x = centerLoc.x - 1; x <= centerLoc.x + 1; x++) {
            for(int y = centerLoc.y - 1; y <= centerLoc.y + 1; y++) {
                MapLocation loc = new MapLocation(x, y);
                if(!rc.canSenseLocation(loc)) {
                    continue;
                }
                if(rc.senseMapInfo(loc).getMark() == PaintType.ALLY_SECONDARY){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isDefenseAllowed(MapLocation centerLoc) throws GameActionException {
        return false;//soldier.mapWidth * 0.375 <= centerLoc.x && centerLoc.x <= soldier.mapWidth*0.625 && soldier.mapHeight * 0.375 <= centerLoc.y && centerLoc.y <= soldier.mapHeight * 0.625;
    }

    public static UnitType decideRuinUnitType(MapLocation ruinLoc) throws GameActionException {
        // TODO: Come back and add defense towers.
        // Always build money towers.
            return UnitType.LEVEL_ONE_MONEY_TOWER;
    }

    public static boolean checkRuinCompleted(MapLocation ruinLoc, UnitType ruinType) throws GameActionException {
        boolean[][] pattern = rc.getTowerPattern(ruinType);
        return checkPatternCompleted(ruinLoc, pattern, false);
    }

    public static boolean checkPatternCompleted(MapLocation centerLoc, boolean[][] pattern, boolean checkCenter) throws GameActionException {
        for(int x = centerLoc.x - 2; x <= centerLoc.x + 2; x++) {
            for(int y = centerLoc.y - 2; y <= centerLoc.y + 2; y++) {
                if(!checkCenter && x == centerLoc.x && y == centerLoc.y) {
                    continue;
                }
                MapLocation loc = new MapLocation(x, y);
                if(!rc.canSenseLocation(loc)){
                    return false;
                }
                boolean shouldBeSecondary = pattern[x - centerLoc.x + 2][y - centerLoc.y + 2];
                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if(shouldBeSecondary && paint != PaintType.ALLY_SECONDARY){
                    return false;
                }
                if(!shouldBeSecondary && paint != PaintType.ALLY_PRIMARY){
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

    public static void markPotentialRCInvalid() throws GameActionException {
        soldier.invalidPotentialLocs[soldier.potentialResourceCenterLoc.x / 3][soldier.potentialResourceCenterLoc.y / 3] = true;
        soldier.potentialResourceCenterLoc = null;
        soldier.potentialRCCornersChecked = new boolean[4];
    }

    public static void markPotentialRCValid() throws GameActionException {
        rc.mark(soldier.potentialResourceCenterLoc, false);
    }

    public static void setPotentialRCAsRC() {
        soldier.currResourceCenterLoc = soldier.potentialResourceCenterLoc;
        soldier.currResourceResponsibility = Responsibility.UNASSIGNED;
        soldier.potentialResourceCenterLoc = null;
        soldier.potentialRCCornersChecked = new boolean[4];
    }

    public static long getPotentialResourcePatternCenterValidBitstring(MapInfo[] nearbyMapInfos) throws GameActionException {
        long validBitstring = -1;

        for(int i = 0; i < 69; i++) {
            if (nearbyMapInfos[i] == null || !nearbyMapInfos[i].isPassable()) {
                validBitstring &= UnrolledConstants.getInvalidSquareForResource(i);
                if(nearbyMapInfos[i] != null) {
                    Util.addToIndicatorString("NPSB " + nearbyMapInfos[i].getMapLocation());
//                    if(nearbyMapInfos[i].hasRuin() && rc.senseRobotAtLocation(nearbyMapInfos[i].getMapLocation()) == null) {
//                        validBitstring &= UnrolledConstants.getSquareHasUnfinishedRuin(i);
//                    }
                }
            }
            else {
                int x = nearbyMapInfos[i].getMapLocation().x;
                int y = nearbyMapInfos[i].getMapLocation().y;
                if(x <= 2 || y <= 2 || x >= soldier.mapWidth - 3 || y >= soldier.mapHeight - 3) {
                    validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                } else if (nearbyMapInfos[i].isResourcePatternCenter() || nearbyMapInfos[i].getMark() == PaintType.ALLY_PRIMARY) {
                    validBitstring &= UnrolledConstants.getSquareHasResourceCenter(i);
                } else if(nearbyMapInfos[i].hasRuin() && rc.senseRobotAtLocation(nearbyMapInfos[i].getMapLocation()) == null) {
                    validBitstring &= UnrolledConstants.getSquareHasUnfinishedRuin(i);
                } else if(nearbyMapInfos[i].getPaint().isEnemy()) {
                    validBitstring &= UnrolledConstants.getInvalidSquareForResource(i);
                }
            }

//            if (nearbyMapInfos[i] != null) {
//                MapLocation loc = nearbyMapInfos[i].getMapLocation();
//                if(soldier.invalidPotentialLocs[loc.x / 3][loc.y / 3]){
//                    int index = soldier.invSpiralOutwardIndices[i];
//                    long invalidBit = ~(1L << (long)index);
//                    validBitstring &= invalidBit;
//                }
//            }
        }

        MapLocation currLoc = rc.getLocation();
        int minX = Math.max((currLoc.x - 4) / 3, 0);
        int minY = Math.max((currLoc.y - 4) / 3, 0);
        int maxX = Math.min((currLoc.x + 4) / 3, soldier.mapWidth / 3 - 1);
        int maxY = Math.min((currLoc.y + 4) / 3, soldier.mapHeight / 3 - 1);
        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                if(soldier.invalidPotentialLocs[x][y]){
                    int i = Util.getMapInfoIndex(x * 3 - currLoc.x, y * 3 - currLoc.y);
                    if(i != -1){
                        validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                    }
                    i = Util.getMapInfoIndex(x * 3 + 1 - currLoc.x, y * 3 - currLoc.y);
                    if(i != -1){
                        validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                    }
                    i = Util.getMapInfoIndex(x * 3 + 2 - currLoc.x, y * 3 - currLoc.y);
                    if(i != -1){
                        validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                    }
                    i = Util.getMapInfoIndex(x * 3 - currLoc.x, y * 3 + 1 - currLoc.y);
                    if(i != -1){
                        validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                    }
                    i = Util.getMapInfoIndex(x * 3 + 1 - currLoc.x, y * 3 + 1 - currLoc.y);
                    if(i != -1){
                        validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                    }
                    i = Util.getMapInfoIndex(x * 3 + 2 - currLoc.x, y * 3 + 1- currLoc.y);
                    if(i != -1){
                        validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                    }
                    i = Util.getMapInfoIndex(x * 3 - currLoc.x, y * 3 + 2 - currLoc.y);
                    if(i != -1){
                        validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                    }
                    i = Util.getMapInfoIndex(x * 3 + 1 - currLoc.x, y * 3 + 2 - currLoc.y);
                    if(i != -1){
                        validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                    }
                    i = Util.getMapInfoIndex(x * 3 + 2 - currLoc.x, y * 3 + 2 - currLoc.y);
                    if(i != -1){
                        validBitstring &= UnrolledConstants.getPotentialRCAlreadyMarkedInvalid(i);
                    }
                }
            }
        }

        return validBitstring;
    }


    public static int getPotentialResourcePatternCenterIndex(long validBitstring) throws GameActionException {
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
//        Util.log("Returning counter: " + counter);
//        Util.log("Returning index: " + soldier.spiralOutwardIndices[counter]);
//        Util.log("Returning: " + nearbyMapInfos[soldier.spiralOutwardIndices[counter]].getMapLocation());
        return soldier.spiralOutwardIndices[counter];
    }

}
