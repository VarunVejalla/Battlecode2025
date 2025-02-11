package defense;

import battlecode.common.*;

public class MarkingUtils {

    static Bunny bunny;
    static RobotController rc;

    public static void tryRemoveResponsibilityMark(MapLocation ruinLoc) throws GameActionException {
        MapLocation markLoc = new MapLocation(ruinLoc.x - 1, ruinLoc.y);
        if(rc.canRemoveMark(markLoc)){
            rc.removeMark(markLoc);
        }
        markLoc = new MapLocation(ruinLoc.x + 1, ruinLoc.y);
        if(rc.canRemoveMark(markLoc)){
            rc.removeMark(markLoc);
        }
        markLoc = new MapLocation(ruinLoc.x, ruinLoc.y - 1);
        if(rc.canRemoveMark(markLoc)){
            rc.removeMark(markLoc);
        }
        markLoc = new MapLocation(ruinLoc.x, ruinLoc.y + 1);
        if(rc.canRemoveMark(markLoc)){
            rc.removeMark(markLoc);
        }
    }

    public static void tryRemoveResponsibilityMarks() throws GameActionException {
        MapLocation[] ruins = rc.senseNearbyRuins(-1);
        for(MapLocation ruinLoc : ruins){
            if(rc.canSenseRobotAtLocation(ruinLoc)){
                tryRemoveResponsibilityMark(ruinLoc);
            }
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
