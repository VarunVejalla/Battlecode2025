package moneymoneymoney;

import battlecode.common.*;

public class Util {
    static RobotController rc;
    static Robot robot;


    public static int minMovesToReach(MapLocation a, MapLocation b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        return Math.max(Math.abs(dx), Math.abs(dy));
    }

    public static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            Util.move(dir);
            return true;
        }
        return false;
    }
    public static double getAngle(MapLocation a, MapLocation b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        boolean zero_x = -0.01 < dx && dx < 0.01;
        boolean zero_y = -0.01 < dy && dy < 0.01;
        if (zero_x && zero_y) {
            return -5;
        } else {
            return Math.atan2(dy, dx);
        }
    }

    public static MapLocation getRotationalReflection(MapLocation location) {
        double vx = rc.getMapWidth()/2.0 - location.x;
        double vy = rc.getMapHeight()/2.0 - location.y;

        double t = Double.MAX_VALUE;

        if (vx > 0) {
            t = Math.min(t, (rc.getMapWidth()-2-location.x)/vx);
        }
        if (vx < 0) {
            t = Math.min(t, (1-location.x)/vx);
        }
        if (vy > 0) {
            t = Math.min(t, (rc.getMapHeight()-2-location.y)/vy);
        }
        if (vy < 0) {
            t = Math.min(t, (1-location.y)/vy);
        }
        return new MapLocation((int)(location.x + vx * t), (int)(location.y + vy * t));
    }

    public static MapLocation getVerticalReflection(MapLocation current) {
        return new MapLocation(current.x, rc.getMapHeight()-1-current.y);
    }

    public static MapLocation getHorizontalReflection(MapLocation current) {
        return new MapLocation(rc.getMapWidth()-1-current.x, current.y);
    }

    public static int getPatternDifference(boolean[][] pattern) throws GameActionException {
        int paintsNeeded = 0;
        for(int x = robot.myLoc.x - 2; x <= robot.myLoc.x + 2; x++) {
            for(int y = robot.myLoc.y - 2; y <= robot.myLoc.y + 2; y++) {
                if(x == robot.myLoc.x && y == robot.myLoc.y) {
                    continue;
                }
                MapLocation loc = new MapLocation(x, y);
                boolean shouldBeSecondary = pattern[x - robot.myLoc.x + 2][y - robot.myLoc.y + 2];
                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {
                    return -1;
                } else if (shouldBeSecondary && paint != PaintType.ALLY_SECONDARY){
                    paintsNeeded++;
                } else if (!shouldBeSecondary && paint != PaintType.ALLY_PRIMARY){
                    paintsNeeded++;
                }
            }
        }
        return paintsNeeded;
    }

    public static MapInfo[] getFilledInMapInfo(MapInfo[] nearbyMapInfo) {
        // assuming that we are the center of the nearbyMapInfo, and we are still at the center
        if (nearbyMapInfo.length == 69) {
            return nearbyMapInfo;
        }

        MapInfo[] filledInMapInfo = new MapInfo[69];
        int leftOffset = Math.min(rc.getLocation().x, 4);
        int bottomOffset = Math.min(rc.getLocation().y, 4);
        int rightOffset = Math.min(rc.getMapWidth() - 1 - rc.getLocation().x, 4);
        int topOffset = Math.min(rc.getMapHeight() - 1 - rc.getLocation().y, 4);
        if(leftOffset < 4){
            rightOffset = 100;
        }
        if(rightOffset < 4){
            leftOffset = 100;
        }
        if(bottomOffset < 4){
            topOffset = 100;
        }
        if(topOffset < 4){
            bottomOffset = 100;
        }
        // TODO: Can optimize more by directly filling it in instead of returning an array.
        byte[] order = ExcessConstants.getFilledInMapInfoOrder(leftOffset, bottomOffset, rightOffset, topOffset);
        for(int i = 0; i < nearbyMapInfo.length; i++){
            filledInMapInfo[order[i]] = nearbyMapInfo[i];
        }

        return filledInMapInfo;
    }

    public static int getMapInfoIndex(int deltaX, int deltaY) {
        switch(deltaX){
            case -4:
                switch(deltaY){
                    case -2: return 0;
                    case -1: return 1;
                    case 0: return 2;
                    case 1: return 3;
                    case 2: return 4;
                }
            case -3:
                switch(deltaY){
                    case -3: return 5;
                    case -2: return 6;
                    case -1: return 7;
                    case 0: return 8;
                    case 1: return 9;
                    case 2: return 10;
                    case 3: return 11;
                }
            case -2:
                switch(deltaY){
                    case -4: return 12;
                    case -3: return 13;
                    case -2: return 14;
                    case -1: return 15;
                    case 0: return 16;
                    case 1: return 17;
                    case 2: return 18;
                    case 3: return 19;
                    case 4: return 20;
                }
            case -1:
                switch(deltaY){
                    case -4: return 21;
                    case -3: return 22;
                    case -2: return 23;
                    case -1: return 24;
                    case 0: return 25;
                    case 1: return 26;
                    case 2: return 27;
                    case 3: return 28;
                    case 4: return 29;
                }
            case 0:
                switch(deltaY){
                    case -4: return 30;
                    case -3: return 31;
                    case -2: return 32;
                    case -1: return 33;
                    case 0: return 34;
                    case 1: return 35;
                    case 2: return 36;
                    case 3: return 37;
                    case 4: return 38;
                }
            case 1:
                switch(deltaY){
                    case -4: return 39;
                    case -3: return 40;
                    case -2: return 41;
                    case -1: return 42;
                    case 0: return 43;
                    case 1: return 44;
                    case 2: return 45;
                    case 3: return 46;
                    case 4: return 47;
                }
            case 2:
                switch(deltaY){
                    case -4: return 48;
                    case -3: return 49;
                    case -2: return 50;
                    case -1: return 51;
                    case 0: return 52;
                    case 1: return 53;
                    case 2: return 54;
                    case 3: return 55;
                    case 4: return 56;
                }
            case 3:
                switch(deltaY){
                    case -3: return 57;
                    case -2: return 58;
                    case -1: return 59;
                    case 0: return 60;
                    case 1: return 61;
                    case 2: return 62;
                    case 3: return 63;
                }
            case 4:
                switch(deltaY){
                    case -2: return 64;
                    case -1: return 65;
                    case 0: return 66;
                    case 1: return 67;
                    case 2: return 68;
                }
        }
        return -1;
    }

    public static void move(Direction dir) throws GameActionException{
        rc.move(dir);
        robot.myLoc = rc.getLocation();
        robot.myLocInfo = rc.senseMapInfo(robot.myLoc);
    }

    public static int[] getNewVisionIndicesAfterMove(Direction direction){
        int[] dx = null;
        int[] dy = null;
        switch(direction) {
            case null:
                return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68};
            case Direction.EAST:
                dx = new int[]{2, 2, 3, 3, 4, 4, 4, 4, 4};
                dy = new int[]{-4, 4, -3, 3, -2, -1, 0, 1, 2};
                break;
            case Direction.WEST:
                dx = new int[]{-4, -4, -4, -4, -4, -3, -3, -2, -2};
                dy = new int[]{-2, -1, 0, 1, 2, -3, 3, -4, 4};
                break;
            case Direction.NORTH:
                dx = new int[]{-4, -3, -2, -1, 0, 1, 2, 3, 4};
                dy = new int[]{2, 3, 4, 4, 4, 4, 4, 3, 2};
                break;
            case Direction.SOUTH:
                dx = new int[]{-4, -3, -2, -1, 0, 1, 2, 3, 4};
                dy = new int[]{-2, -3, -4, -4, -4, -4, -4, -3, -2};
                break;
            case Direction.NORTHEAST:
                dx = new int[]{-2, -1, 0, 1, 2, 2, 3, 3, 4, 4, 4, 4, 4};
                dy = new int[]{4, 4, 4, 4, 3, 4, 2, 3, -2, -1, 0, 1, 2};
                break;
            case Direction.NORTHWEST:
                dx = new int[]{-4, -4, -4, -4, -4, -3, -3, -2, -2, -1, 0, 1, 2};
                dy = new int[]{-2, -1, 0, 1, 2, 2, 3, 3, 4, 4, 4, 4, 4};
                break;
            case Direction.SOUTHEAST:
                dx = new int[]{-2, -1, 0, 1, 2, 2, 3, 3, 4, 4, 4, 4, 4};
                dy = new int[]{-4, -4, -4, -4, -4, -3, -3, -2, -2, -1, 0, 1, 2};
                break;
            case Direction.SOUTHWEST:
                dx = new int[]{-4, -4, -4, -4, -4, -3, -3, -2, -2, -1, 0, 1, 2};
                dy = new int[]{-2, -1, 0, 1, 2, -3, -2, -4, -3, -4, -4, -4, -4};
                break;
            case Direction.CENTER:
                // No movement.
                return new int[]{};
        }
        int[] indices = new int[dx.length];
        for(int i = 0; i < indices.length; i++){
            indices[i] = getMapInfoIndex(dx[i], dy[i]);
        }
        return indices;
    }

    public static void addToIndicatorString(String str) {
        robot.indicatorString += str + ";";
        rc.setIndicatorString(robot.indicatorString);
    }

    public static <T> int getItemIndexInArray(T item, T[] array) {
        // helper method to get the index of an item in an array
        for (int i = 0; i < array.length; i++) {
            T arrayItem = array[i];
            if (arrayItem != null && arrayItem.equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public static MapLocation getRandomMapLocation() {
        return new MapLocation((int) (Math.random() * rc.getMapWidth()), (int) (Math.random() * rc.getMapHeight()));
    }

    public static boolean isTower(UnitType unitType) {
        // List all the tower unit types
        switch (unitType) {
            case LEVEL_ONE_DEFENSE_TOWER:
            case LEVEL_ONE_PAINT_TOWER:
            case LEVEL_ONE_MONEY_TOWER:
            case LEVEL_TWO_DEFENSE_TOWER:
            case LEVEL_TWO_PAINT_TOWER:
            case LEVEL_TWO_MONEY_TOWER:
            case LEVEL_THREE_DEFENSE_TOWER:
            case LEVEL_THREE_PAINT_TOWER:
            case LEVEL_THREE_MONEY_TOWER:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPaintTower(UnitType unitType) {
        // List all the tower unit types
        switch (unitType) {
            case LEVEL_ONE_PAINT_TOWER:
            case LEVEL_TWO_PAINT_TOWER:
            case LEVEL_THREE_PAINT_TOWER:
                return true;
            default:
                return false;
        }
    }

    public static boolean isMoneyTower(UnitType unitType) {
        // List all the tower unit types
        switch (unitType) {
            case LEVEL_ONE_MONEY_TOWER:
            case LEVEL_TWO_MONEY_TOWER:
            case LEVEL_THREE_MONEY_TOWER:
                return true;
            default:
                return false;
        }
    }

    public static boolean isDefenseTower(UnitType unitType) {
        // List all the tower unit types
        switch (unitType) {
            case LEVEL_ONE_DEFENSE_TOWER:
            case LEVEL_TWO_DEFENSE_TOWER:
            case LEVEL_THREE_DEFENSE_TOWER:
                return true;
            default:
                return false;
        }
    }

    public static <T> boolean checkIfItemInArray(T item, T[] array) {
        return getItemIndexInArray(item, array) != -1;
    }

    public static void logArray(String name, int[] array) {
        // helper method to display array of ints to the logs
        String out = "";
        out += name + ": ";
        for (int i = 0; i < array.length; i++) {
            if (i == 0) { // first element
                out += "[" + array[i] + ", ";
            } else if (i == array.length - 1) { // last element
                out += array[i] + "]";
            } else { // other elements
                out += array[i] + ", ";
            }
        }
        Util.log(out);
    }

    public static <T> void logArray(String name, T[] array) {
        // helper method to display array of any type to the logs
        String out = "";
        out += name + ": ";
        for (int i = 0; i < array.length; i++) {
            if (i == 0) { // first element
                out += "[" + array[i] + ", ";
            } else if (i == array.length - 1) { // last element
                out += array[i] + "]";
            } else { // other elements
                out += array[i] + ", ";
            }
        }
        Util.log(out);
    }

    public static void log(String str) {
        if(Constants.MUTE || (rc.getID() != Constants.DEBUG_BOT_ID)){
//        if(Constants.MUTE){
//        if(true){
            return;
        }
        System.out.println(str);
    }

    public static void logBytecode(String prefix) {
//        Util.log(prefix + ": " + Clock.getBytecodesLeft());
    }

    public static Direction[] closeDirections(Direction dir) {
        return new Direction[] {
                dir,
                dir.rotateLeft(),
                dir.rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight().rotateRight(),
                dir.opposite()
        };
    }

    public static MapLocation applySymmetry(MapLocation loc, SymmetryType type) {
        int width = rc.getMapWidth();
        int height = rc.getMapHeight();
        switch (type) {
            case HORIZONTAL:
                return new MapLocation(width - loc.x - 1, loc.y);
            case VERTICAL:
                return new MapLocation(loc.x, height - loc.y - 1);
            case ROTATIONAL:
                return new MapLocation(width - loc.x - 1, height - loc.y - 1);
        }
        return null;
    }

    public static String getSectorDescription(int sectorValue) {
        int ruinCondition = (sectorValue >> 1) & 0b111;
        int emptyCount = (sectorValue >> 4) & 0b11;
        int enemyPaintCount = (sectorValue >> 6) & 0b11;

        String ruinConditionDescription;
        switch (ruinCondition) {
            case 0:
                ruinConditionDescription = "No Ruin";
                break;
            case 1:
                ruinConditionDescription = "Unbuilt Ruin";
                break;
            case 2:
                ruinConditionDescription = "Friendly Paint Tower";
                break;
            case 3:
                ruinConditionDescription = "Friendly Money Tower";
                break;
            case 4:
                ruinConditionDescription = "Friendly Defense Tower";
                break;
            case 5:
                ruinConditionDescription = "Enemy Paint Tower";
                break;
            case 6:
                ruinConditionDescription = "Enemy Money Tower";
                break;
            case 7:
                ruinConditionDescription = "Enemy Defense Tower";
                break;
            default:
                ruinConditionDescription = "Unknown";
                break;
        }

        String enemyPaintCountDescription;
        switch (enemyPaintCount) {
            case 0:
                enemyPaintCountDescription = "0-1 Tiles";
                break;
            case 1:
                enemyPaintCountDescription = "2-4 Tiles";
                break;
            case 2:
                enemyPaintCountDescription = "5-12 Tiles";
                break;
            case 3:
                enemyPaintCountDescription = "13+ Tiles";
                break;
            default:
                enemyPaintCountDescription = "Unknown";
                break;
        }

        String emptyCountDescription;
        switch (emptyCount) {
            case 0:
                emptyCountDescription = "0-1 Tiles";
                break;
            case 1:
                emptyCountDescription = "2-4 Tiles";
                break;
            case 2:
                emptyCountDescription = "5-12 Tiles";
                break;
            case 3:
                emptyCountDescription = "13+ Tiles";
                break;
            default:
                emptyCountDescription = "Unknown";
                break;
        }

        return String.format(
                "Sector:\n" +
                        "  Enemy Paint Count: %d (%s)\n" +
                        "  Empty Count: %d (%s) \n" +
                        "  Ruin Condition: %d (%s)\n",
                enemyPaintCount, enemyPaintCountDescription,
                emptyCount, emptyCountDescription,
                ruinCondition, ruinConditionDescription
        );
    }

    /**
     * Returns the sector index for a given MapLocation.
     */
    public static int getSectorIndex(MapLocation loc) {
        int sectorRows = (rc.getMapWidth() + 4) / 5;

        int col = loc.x / 5;
        int row = loc.y / 5;
        return row * sectorRows + col;
    }

    /**
     * Decodes the sector data and returns an array of values:
     * [enemyPaintCount, emptyCount, ruinCondition, staleBit]
     */
    public static ScanResult decodeSector(int encodedSector) {

        int ruinCondition = (encodedSector >> 1) & 0b111;
        int emptyCount = (encodedSector >> 4) & 0b11;
        int enemyPaintCount = (encodedSector >> 6) & 0b11;

        // Put in a fake sector and a fake round number.
        return new ScanResult(-1, ruinCondition, enemyPaintCount, emptyCount, -1);
    }


    /**
     * Returns the index of the sector that is fully contained within a vision radius from center.
     * Returns -1 if there is no such sector.
     */
    public static int getFullyEnclosedSectorID(MapLocation center) {
        int sectorRows = (rc.getMapWidth() + 4) / 5;
        int sectorCols = (rc.getMapHeight() + 4) / 5;
        int sectorCount = sectorCols * sectorRows;


        // There is only one sector that could be fully enclosed. It must contain the center.
        int sectorIndex = getSectorIndex(center);
        assert sectorIndex < sectorCount;
        // If center is within radius squared 4 of the sector center, the sector is fully visible, even if the sector is cutoff!
        if (center.isWithinDistanceSquared(getSectorCenter(sectorIndex), 4)) {
            return sectorIndex;
        }

        return -1;
    }

    /**
     * Returns the center of a sector given its index.
     */
    public static MapLocation getSectorCenter(int sectorIndex) {
        int sectorRows = (rc.getMapWidth() + 4) / 5;
        int sectorCols = (rc.getMapHeight() + 4) / 5;


        int row = sectorIndex / sectorRows;
        int col = sectorIndex % sectorRows;

        int centerX = col * 5 + 2; // Center of the 5x5 grid
        int centerY = row * 5 + 2;

        return new MapLocation(centerX, centerY);
    }



    /**
     * Returns an array containing the index of the sector that contains the given MapLocation
     * as well as the indices of its neighboring sectors.
     */
    public static int[] getSectorAndNeighbors(MapLocation loc, int sectorsAway) {
        int sectorRows = (rc.getMapWidth() + 4) / 5;
        int sectorCols = (rc.getMapHeight() + 4) / 5;

        int col = loc.x / 5;
        int row = loc.y / 5;

        // Precompute bounds to avoid repeated checks
        int minRow = Math.max(0, row - sectorsAway);
        int maxRow = Math.min(sectorCols - 1, row + sectorsAway);
        int minCol = Math.max(0, col - sectorsAway);
        int maxCol = Math.min(sectorRows - 1, col + sectorsAway);

        // Validate bounds
        if (maxRow < minRow || maxCol < minCol) {
            throw new IllegalStateException("Invalid sector bounds");
        }

        // Calculate the number of valid neighbors
        int neighborCount = (maxRow - minRow + 1) * (maxCol - minCol + 1);

        int[] neighbors = new int[neighborCount];
        int index = 0;

        // Iterate only over valid rows and columns
        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                neighbors[index++] = r * sectorRows + c;
            }
        }

        return neighbors;
    }

}