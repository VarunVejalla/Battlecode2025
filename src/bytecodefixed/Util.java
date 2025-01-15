package bytecodefixed;

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
            rc.move(dir);
            robot.myLoc = rc.getLocation();
            robot.myLocInfo = rc.senseMapInfo(robot.myLoc);
            return true;
        }
        return false;
    }

    public static MapInfo[] getFilledInMapInfo(MapInfo[] nearbyMapInfo) {
        // assuming that we are the center of the nearbyMapInfo, and we are still at the center
        if (nearbyMapInfo.length == 69) {
            return nearbyMapInfo;
        }

        MapInfo[] filledInMapInfo = new MapInfo[69];
        MapLocation location;
        int intendedIndex;
        for (MapInfo mapInfo : nearbyMapInfo) {
            // get location of nearbyMapInfo[i]
            // figure out what index of filledInMapInfo it should go in
            location = mapInfo.getMapLocation();
            // need reverse lookup given Shifts.dx and Shifts.dy
            intendedIndex = getMapInfoIndex(location.x - robot.myLoc.x, location.y - robot.myLoc.y);
            filledInMapInfo[intendedIndex] = mapInfo;
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

    public static void addToIndicatorString(String str) {
        robot.indicatorString += str + ";";
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
        if(Constants.MUTE){
            return;
        }
        System.out.println(str);
    }

    public static void logBytecode(String prefix) {
        Util.log(prefix + ": " + Clock.getBytecodesLeft());
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

}