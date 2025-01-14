package goat;

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

    // TODO: fix the right and left diagonal symmetry cases
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
            case DIAGONAL_RIGHT:
                int newY = Math.min(loc.x, height - 1);
                int newX = Math.min(loc.y, width - 1);
                return new MapLocation(newX, newY);
            case DIAGONAL_LEFT:
                return new MapLocation(
                        Math.min(height - loc.y - 1, width - 1),
                        Math.min(width - loc.x - 1, height - 1));
        }
        return null;
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

    public static int[] getIndicesForSquareSpiral(int dx, int dy) {
        if (dx < -6 || dx > 6 || dy < -6 || dy > 6) {
            return new int[]{};
        }
        else {
            int index = 13*dx + dy + 84;
            return Constants.gridLookupIndicesSpiral[index];
        }
    }

    public static boolean shouldSecondaryPaintResource(int dx, int dy) {
        if (dx == 0 && dy == 0) {
            return false;
        }
        int mod = dx + dy;
        return mod%2 == 0;
    }

    public static int getMapInfoIndex(int deltaX, int deltaY) {
        if (deltaX*deltaX + deltaY*deltaY > 20) {
            return -1;
        }
        if (deltaX < 3) {
            if (deltaX > -3) {
                return deltaY+9*deltaX+34;
            } else if (deltaX == -3) {
                return deltaY+8;
            } else {
                return deltaY+2;
            }
        } else if (deltaX == 3) {
            return deltaY+60;
        } else if (deltaX == 4) {
            return deltaY+66;
        }
        return -1;
    }

    public static int[] getMapInfoIndicesWithinRadiusSquared(int radius_squared, MapLocation currentLocation) {
        if (radius_squared < 0 || radius_squared >= 20) {
            return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68};
        }

        if (radius_squared < 10) {
            if (radius_squared <= 4) {
                if (radius_squared < 2) {
                    if (radius_squared == 1) {
                        return new int[] {25, 33, 34, 35, 43};
                    } else {
                        return new int[] {34};
                    }
                } else if (radius_squared < 4) {
                    // r2 = 2 or 3
                    return new int[] {24, 25, 26, 33, 34, 35, 42, 43, 44};
                } else {
                    // r2 = 4
                    return new int[] {16, 24, 25, 26, 32, 33, 34, 35, 36, 42, 43, 44, 52};
                }
            } else if (radius_squared < 8) {
                // 5, 6, or 7. all work the same
                return new int[] {15, 16, 17, 23, 24, 25, 26, 27, 32, 33, 34, 35, 36, 41, 42, 43, 44, 45, 51, 52, 53};
            } else if (radius_squared == 8) {
                return new int[] {14, 15, 16, 17, 18, 23, 24, 25, 26, 27, 32, 33, 34, 35, 36, 41, 42, 43, 44, 45, 50, 51, 52, 53, 54};
            } else {
                // r2 = 9
                return new int[] {8, 14, 15, 16, 17, 18, 23, 24, 25, 26, 27, 31, 32, 33, 34, 35, 36, 37, 41, 42, 43, 44, 45, 50, 51, 52, 53, 54, 60};
            }
        } else if (radius_squared < 17) {
            if (radius_squared < 13) {
                // r2 = 10 or 11 or 12
                return new int[] {7, 8, 9, 14, 15, 16, 17, 18, 22, 23, 24, 25, 26, 27, 28, 31, 32, 33, 34, 35, 36, 37, 40, 41, 42, 43, 44, 45, 46, 50, 51, 52, 53, 54, 59, 60, 61};
            } else if (radius_squared < 16) {
                // r2 = 13 or 14 or 15
                return new int[] {6, 7, 8, 9, 10, 13, 14, 15, 16, 17, 18, 19, 22, 23, 24, 25, 26, 27, 28, 31, 32, 33, 34, 35, 36, 37, 40, 41, 42, 43, 44, 45, 46, 49, 50, 51, 52, 53, 54, 55, 58, 59, 60, 61, 62};
            } else {
                // r2 = 16
                return new int[] {2, 6, 7, 8, 9, 10, 13, 14, 15, 16, 17, 18, 19, 22, 23, 24, 25, 26, 27, 28, 30, 31, 32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 44, 45, 46, 49, 50, 51, 52, 53, 54, 55, 58, 59, 60, 61, 62, 66};
            }
        } else if (radius_squared == 17) {
            return new int[] {1, 2, 3, 6, 7, 8, 9, 10, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 49, 50, 51, 52, 53, 54, 55, 58, 59, 60, 61, 62, 65, 66, 67};
        } else {
            // 18 or 19
            return new int[] {1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 49, 50, 51, 52, 53, 54, 55, 57, 58, 59, 60, 61, 62, 63, 65, 66, 67};
        }
    }

    public static MapInfo getMapInfo(int deltaX, int deltaY, MapInfo[] nearbyMapInfos) throws GameActionException {
        if (nearbyMapInfos.length == 69) {
            int index = getMapInfoIndex(deltaX, deltaY);
            if (index != -1) {
                return nearbyMapInfos[index];
            }

        }
        return rc.senseMapInfo(rc.getLocation().translate(deltaX, deltaY));
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