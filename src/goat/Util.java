package goat;

import battlecode.common.*;

public class Util {

    static RobotController rc;
    static Robot robot;

    public static final byte[] deltaLookup = {-1,-1,0,1,2,3,4,-1,-1,-1,5,6,7,8,9,10,11,-1,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,-1,57,58,59,60,61,62,63,-1,-1,-1,64,65,66,67,68,-1,-1};

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
        int deltaX, deltaY, intendedIndex;
        int timeTakenShift = 0;
        int timeTakenFilling = 0;
        for (MapInfo mapInfo : nearbyMapInfo) {
            // get location of nearbyMapInfo[i]
            // figure out what index of filledInMapInfo it should go in
            int gettingShiftTime = Clock.getBytecodesLeft();
            location = mapInfo.getMapLocation();
            deltaX = location.x - robot.myLoc.x;
            deltaY = location.y - robot.myLoc.y;
            int endingShiftTime = Clock.getBytecodesLeft();
            timeTakenShift += gettingShiftTime-endingShiftTime;

            // need reverse lookup given Shifts.dx and Shifts.dy
            intendedIndex = getMapInfoIndex(deltaX, deltaY);
            filledInMapInfo[intendedIndex] = mapInfo;
            timeTakenFilling += endingShiftTime-Clock.getBytecodesLeft();
        }

        if (rc.getID() == 11435) {
            System.out.println(timeTakenShift);
            System.out.println(timeTakenFilling);
            Util.logBytecode("middle of filling in");
        }


//        for (int i = 0; i < 69; i++) {
//            if ( filledInMapInfo[i] == null ) {
//                filledInMapInfo[i] = new MapInfo(robot.myLoc.translate(Shifts.dx[i], Shifts.dy[i]), false, true, PaintType.EMPTY, PaintType.EMPTY, false);
//            }
//        }
        return filledInMapInfo;
    }

    public static int getMapInfoIndex(int deltaX, int deltaY) {
        return deltaLookup[deltaX*9+deltaY+40];
//        if (deltaX*deltaX + deltaY*deltaY > 20) {
//            return -1;
//        }
//        if (deltaX < 3) {
//            if (deltaX > -3) {
//                return deltaY+9*deltaX+34;
//            } else if (deltaX == -3) {
//                return deltaY+8;
//            } else {
//                return deltaY+2;
//            }
//        } else if (deltaX == 3) {
//            return deltaY+60;
//        } else if (deltaX == 4) {
//            return deltaY+66;
//        }
//        return -1;
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