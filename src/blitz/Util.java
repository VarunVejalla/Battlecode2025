package blitz;

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
        if(Constants.MUTE){
            return;
        }
        System.out.println(str);
    }

    public static void logBytecode(String str) {
        Util.log(str + ": " + Clock.getBytecodesLeft());
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

    // TODO: make sure this method works
    public static boolean shouldSecondaryPaintTower(MapLocation tileToPaint, MapLocation center) {
        if (tileToPaint.equals(center)) {
            return false;
        }
        if (tileToPaint.x == center.x || tileToPaint.y == center.y) {
            return false;
        } else {
            int mod = tileToPaint.x-center.x + tileToPaint.y-center.y;
            return mod%2 == 0;
        }
    }

    public static MapInfo getMapInfo(int deltaX, int deltaY, MapInfo[] nearbyMapInfos) throws GameActionException {
        if (nearbyMapInfos.length == 69) {
            if (deltaX == -4) {
                return nearbyMapInfos[deltaY+2];
            } else if (deltaX == -3) {
                return nearbyMapInfos[deltaY+8];
            } else if (deltaX == -2) {
                return nearbyMapInfos[deltaY+16];
            } else if (deltaX == -1) {
                return nearbyMapInfos[deltaY+25];
            } else if (deltaX == 0) {
                return nearbyMapInfos[deltaY+34];
            } else if (deltaX == 1) {
                return nearbyMapInfos[deltaY+43];
            } else if (deltaX == 2) {
                return nearbyMapInfos[deltaY+52];
            } else if (deltaX == 3) {
                return nearbyMapInfos[deltaY+60];
            } else if (deltaX == 4) {
                return nearbyMapInfos[deltaY+66];
            }
        }
        return rc.senseMapInfo(rc.getLocation().translate(deltaX, deltaY));
    }
}