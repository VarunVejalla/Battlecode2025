package moneybenchmark3;

import battlecode.common.*;

public class MarkingUtils {

    static Bunny bunny;
    static RobotController rc;

    /**
     * Finds a ruin that is not claimed by your team.
     */
    public static MapInfo findUnclaimedRuin() throws GameActionException {
        for (MapInfo tile : bunny.nearbyMapInfos) {
            if (tile.hasRuin()) {
                RobotInfo robotAtRuin = rc.senseRobotAtLocation(tile.getMapLocation());
                // We want a ruin either unoccupied or not controlled by our team
                if (robotAtRuin == null || robotAtRuin.team != rc.getTeam()) {
                    return tile;
                }
            }
        }
        return null;
    }

    /**
     * Tries to move towards an unmarked ruin and mark it if in range.
     */
    public static void handleUnmarkedRuin(MapLocation ruinLoc) throws GameActionException {
        // Move towards the ruin if we're too far to mark it (we need to be adjacent to
        // it to mark it)
        if (ruinLoc.distanceSquaredTo(rc.getLocation()) > Constants.MAX_RUIN_DISTANCE_SQUARED) {
            bunny.nav.goToBug(ruinLoc, Constants.MAX_RUIN_DISTANCE_SQUARED);
        }

        // Mark tower pattern on the ruin if in range
        if (ruinLoc.distanceSquaredTo(rc.getLocation()) <= Constants.MAX_RUIN_DISTANCE_SQUARED) {
            // Mark to build the kind of tower that is the opposite of the nearest allied tower.
            if(bunny.nearestAlliedTowerType == TowerType.PaintTower) {
                rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc);
            }
            else {
                rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc);
            }
        }
    }


    /**
     * Check if the ruin is already marked by an ally tower pattern.
     */
    public static boolean isRuinMarked(MapLocation ruinLoc) throws GameActionException {
        MapInfo[] tilesNearRuin = rc.senseNearbyMapInfos(ruinLoc, 1);
        for (MapInfo tile : tilesNearRuin) {
            if (tile.getMark() == PaintType.ALLY_PRIMARY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempt to mark a resource pattern at your current location if no ally mark
     * that would conflict with it is present.
     *
     * This code checks the 5 x 5 area around the robot's current location
     * (nearbyMapInfos(8) gets you this exact 5x5 grid)
     * to see if any spots have already been marked).
     * If we haven't marked anything in this area yet, we mark a resource pattern.
     */
    public static void attemptMarkResourcePattern() throws GameActionException {
        MapLocation currLoc = rc.getLocation(); // current location of the soldier is the center of the mark
        if (rc.canMarkResourcePattern(currLoc)) {
            MapInfo[] tilesNearMe = rc.senseNearbyMapInfos(8);
            for (MapInfo tile : tilesNearMe) {
                if (tile.getMark().isAlly()) {
                    return; // we skip if there's an ally marking
                }
            }
            rc.markResourcePattern(currLoc);
        }
    }

    public static void tryRuinPatternCompletion() throws GameActionException {
        // TODO: iterate only through the nearby ones
        if (rc.getChips() < Constants.TOWER_COST) {
            return;
        }
        for (MapInfo tile : bunny.nearbyMapInfos) {
            if(tile == null){
                continue;
            }
            if (tile.hasRuin() && bunny.myLoc.distanceSquaredTo(tile.getMapLocation()) <= 2) {
                // We might want to check if we can complete the tower
                MapLocation ruinLoc = tile.getMapLocation();

                // Check if you can complete a tower pattern.
                if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc)) {
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc);
                } else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc)) {
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc);
                } else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruinLoc)) {
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruinLoc);
                }
                return;
            }
        }
    }

    public static void tryResourcePatternCompletion() throws GameActionException {
        if (rc.getChips() < Constants.PATTERN_COST) {
            return;
        }
        // TODO: optimize this for bytecode
        MapInfo[] possibleCenters = rc.senseNearbyMapInfos(8);

        for (MapInfo center : possibleCenters) {
            if (center.getPaint() == PaintType.ALLY_SECONDARY &&
                rc.canCompleteResourcePattern(center.getMapLocation())) {
                rc.completeResourcePattern(center.getMapLocation());
            }
        }
    }



}
