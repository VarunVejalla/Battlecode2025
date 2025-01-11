package boostedboony;

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

    public static void printBytecode(String prefix) {
        Util.log(prefix + ": " + Clock.getBytecodesLeft());
    }

    public static int countBotsOfTeam(Team team, RobotInfo[] bots) {
        int count = 0;
        for (RobotInfo bot : bots) {
            if (bot.getTeam() == team) {
                count++;
            }
        }
        return count;
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
        System.out.println(out);
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
        System.out.println(out);
    }

    public static void fillTrue(boolean[][] arr, MapLocation center, int radiusSquared) {
        int ceiledRadius = (int) Math.ceil(Math.sqrt(radiusSquared)) + 1; // add +1 just to be safe
        int minX = Math.max(center.x - ceiledRadius, 0);
        int minY = Math.max(center.y - ceiledRadius, 0);
        int maxX = Math.min(center.x + ceiledRadius, rc.getMapWidth() - 1);
        int maxY = Math.min(center.y + ceiledRadius, rc.getMapHeight() - 1);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                MapLocation newLocation = new MapLocation(x, y);
                if (center.isWithinDistanceSquared(newLocation, radiusSquared)) {
                    arr[x][y] = true;
                }
            }
        }
    }

    // public static MapLocation getNearestHomeSpawnLoc(MapLocation loc) throws
    // GameActionException{
    //// MapLocation[] homeSpawnLocs = rc.getAllySpawnLocations();
    // int minDist = Integer.MAX_VALUE;
    // MapLocation nearestHomeSpawnLoc = null;
    // for(MapLocation homeSpawnLoc : homeSpawnLocs){
    // int dist = loc.distanceSquaredTo(homeSpawnLoc);
    // if(dist < minDist){
    // minDist = dist;
    // nearestHomeSpawnLoc = homeSpawnLoc;
    // }
    // }
    // return nearestHomeSpawnLoc;
    // }

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

    public static boolean locIsASpawnLoc(MapLocation loc) throws GameActionException {
        // this method checks if the robot is on a spawn location
        for (MapLocation spawnCenter : robot.spawnCenters) {
            if (Util.minMovesToReach(loc, spawnCenter) <= 1) {
                return true;
            }
        }
        return false;
    }

    public static int encodeMapLocation(MapLocation loc) {
        return loc.x * (robot.mapHeight + 1) + loc.y;
    }

    public static int encodeMapLocation(int x, int y) {
        return x * (robot.mapHeight + 1) + y;
    }

    public static MapLocation decodeMapLocation(int code) {
        return new MapLocation(code / (robot.mapHeight + 1), code % (robot.mapHeight + 1));
    }

    public static void log(String str) {
        System.out.println(str);
    }

    public static void logBytecode(String str) {
        System.out.println(str + ": " + Clock.getBytecodesLeft());
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

    // TODO: make sure this method works
    public static boolean shouldSecondaryPaintResource(MapLocation tileToPaint, MapLocation center) {
        if (tileToPaint.equals(center)) {
            return false;
        }
        int mod = tileToPaint.x-center.x + tileToPaint.y-center.y;
        return mod%2 == 0;
    }

    public static boolean shouldSecondaryPaintResource(int dx, int dy) {
        if (dx == 0 && dy == 0) {
            return false;
        }
        int mod = dx + dy;
        return mod%2 == 0;
    }

    // TODO: make sure this method works
    public static boolean shouldSecondaryPaintTower(UnitType towerType, MapLocation tileToPaint, MapLocation center) {
        if (towerType == UnitType.LEVEL_ONE_PAINT_TOWER) {
            if (tileToPaint.equals(center)) {
                return false;
            }
            if (tileToPaint.x == center.x || tileToPaint.y == center.y) {
                return false;
            } else {
                int mod = tileToPaint.x - center.x + tileToPaint.y - center.y;
                return mod % 2 == 0;
            }
        } else if (towerType == UnitType.LEVEL_ONE_MONEY_TOWER) {
            if (tileToPaint.equals(center)) {
                return false;
            }
            int dx = tileToPaint.x - center.x;
            int dy = tileToPaint.y - center.y;
            int s = dx*dx + dy*dy;
            if (s == 1 || s == 8) {
                return false;
            } else {
                return true;
            }
        } else {
            // TODO: this is definitely wrong, just a placeholder
            return false;
        }
    }


    public static int getMapInfoIndex(int deltaX, int deltaY, MapInfo[] nearbyMapInfos) {
        if (nearbyMapInfos.length == 69) {
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
        }
        return -1;
    }

    public static int[] getMapInfoIndicesWithinRadiusSquared(int radius_squared, MapLocation currentLocation) {


        if (4 <= currentLocation.x && currentLocation.x <= rc.getMapWidth() - 5 && 4 <= currentLocation.y && currentLocation.y <= rc.getMapHeight() - 5) {
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

        } else {
            return null;
        }
    }

    public static MapInfo[] getMapInfosWithinRadiusSquared(int radius_squared, MapInfo[] nearbyMapInfos) throws GameActionException {
        if (radius_squared < 0 || radius_squared >= 20) {
            return nearbyMapInfos;
        } else {
            int[] indices = getMapInfoIndicesWithinRadiusSquared(radius_squared, rc.getLocation());
            if (indices != null) {
                MapInfo[] mapInfos = new MapInfo[indices.length];
                for (int i = 0; i < indices.length; i++) {
                    mapInfos[i] = nearbyMapInfos[indices[i]];
                }
                return mapInfos;
            } else {
                return rc.senseNearbyMapInfos(radius_squared);
            }
        }
    }

    public static MapInfo getMapInfo(int deltaX, int deltaY, MapInfo[] nearbyMapInfos) throws GameActionException {
        if (nearbyMapInfos.length == 69) {
            int index = getMapInfoIndex(deltaX, deltaY, nearbyMapInfos);
            if (index != -1) {
                return nearbyMapInfos[index];
            }

        }
        return rc.senseMapInfo(rc.getLocation().translate(deltaX, deltaY));
    }

    public static PaintType getPaintType(int dx, int dy, MapInfo[] nearbyMapInfos) throws GameActionException {

        MapInfo mapInfo = getMapInfo(dx, dy, nearbyMapInfos);
        if (mapInfo.hasRuin() || mapInfo.isWall()) {
            return null;
        }
        return mapInfo.getPaint();
    }

    public static MapLocation getMapLocationForResourcePattern(MapInfo[] nearbyMapInfos) throws GameActionException {
        // this avoids repainting squares no matter what

        if (nearbyMapInfos.length == 69) {
            int[] possibleCentersX = {0, 1, 0, -1, 0, 1, -1, -1, 1, 2, 0, -2, 0};
            int[] possibleCentersY = {0, 0, 1, 0, -1, 1, -1, 1, -1, 0, 2, 0, -2};


            // invalid tile or enemy paint tile or empty tile with incorrect mark
            // empty tile with no mark or with correct mark
            // our paint tile with no mark and agrees with what we're painting
            // our paint tile with no mark and doesn't agree with what we're painting
            // our paint tile with mark and agrees with what we're painting
            // our paint tile with mark and doesn't agree with what we're painting


            // 0 represents unknown, 1 is no paint (and not invalid and not marked), 2 is our paint (and not invalid and not marked), 3 is invalid square or marked or their paint
            int[] validSquares = new int[nearbyMapInfos.length];


            for (int i = 0; i < possibleCentersX.length; i++) {
                boolean alreadyFinished = true;
                int dx = possibleCentersX[i];
                int dy = possibleCentersY[i];
                int ddx;
                int ddy;
                boolean invalid = false;
                for (int dx_shift = -2; dx_shift <= 2; dx_shift++) {
                    for(int dy_shift = -2; dy_shift <= 2; dy_shift++) {
                        ddx = dx+dx_shift;
                        ddy = dy+dy_shift;

                        int index = getMapInfoIndex(ddx, ddy, nearbyMapInfos);
                        MapInfo mapInfo = getMapInfo(ddx, ddy, nearbyMapInfos);
                        PaintType paintType = mapInfo.getPaint();

                        if (validSquares[index] == 0) {
                            if (mapInfo.hasRuin() || mapInfo.isWall() || paintType.isEnemy()) {
                                validSquares[index] = 3;
                            } else if (mapInfo.getMark().isAlly()){
                                validSquares[index] = 3;
                            } else if (paintType.isAlly()) {
                                validSquares[index] = 2;
                            } else {
                                validSquares[index] = 1;
                            }

                        }

                        if (validSquares[index] == 1) {
                            // unpainted and unmarked
                            alreadyFinished = false;
                        } else if (validSquares[index] == 2) {
                            // painted in our color but unmarked
                            boolean shouldBeSecondaryPaint = shouldSecondaryPaintResource(dx_shift, dy_shift);
                            boolean isSecondaryPaint = mapInfo.getPaint().isSecondary();
                            if (isSecondaryPaint == shouldBeSecondaryPaint) {
                                continue;
                            } else {
                                invalid = true;
                                break;
                            }
                        }  else if (validSquares[index] == 3) {
                            invalid = true;
                            break;
                        }
                    }
                    if (invalid) {
                        break;
                    }
                }
                if (!invalid && !alreadyFinished) {
                    return rc.getLocation().translate(dx, dy);
                }
            }
        }

        return null;

    }
}