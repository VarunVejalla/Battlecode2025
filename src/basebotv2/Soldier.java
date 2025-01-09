package basebotv2;

import battlecode.common.*;

public class Soldier extends Bunny {

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);

    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
        // 1. Handle Ruins
        // Check if there are any unmakred ruins nearby. If a ruin is found:
        // - Move toward the ruin if we are far away (distance > 2).
        // - Once in range, try mark it with a tower pattern (so we can build a tower)
        MapInfo curRuin = findUnmarkedRuin();

        if (curRuin != null) {
            handleUnmarkedRuin(curRuin); // Move to and interact with the ruin.
        }

        else {
            // 2. Attempt to Mark a Resource Pattern
            // If no ruins are found, check if we can mark a resource pattern at our current
            // location.
            // - Only do this if the location is not already marked by our team
            attemptMarkResourcePattern();
        }

        // 3. Paint/Attack if you can
        // After handling ruins and resource marking, check if we can act:
        // - Paint or attack a nearby tile based on priority:
        // - Ally-marked but unpainted tiles take precedence.
        // - If no such tiles exist, attack an unpainted tile nearby.
        if (rc.isActionReady()) {
            paintOrAttack();
            tryPatternCompletion();
        }

        // 4. Movement Logic
        // If movement is possible:
        // - Prioritize moving toward ally-marked tiles that are empty (unpainted).
        // - If no such tiles are found, move randomly as a fallback to explore new
        // areas.
        if (rc.isMovementReady()) {
            moveLogic();
            tryPatternCompletion();
        }

        // 5. Recheck for Painting/Attacking
        // After movement, check if we can paint/attack again:
        // - Reattempt painting/attacking in case a new opportunity is available after
        // movement.
        if (rc.isActionReady()) {
            paintOrAttack();
            tryPatternCompletion();
        }

        // 6. End of Turn Logic
        // Perform any shared cleanup or post-turn logic
        sharedEndFunction();
    }

    /**
     * Finds a ruin that is not claimed by your team.
     */
    public MapInfo findUnmarkedRuin() throws GameActionException {
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyTiles) {
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
    public void handleUnmarkedRuin(MapInfo ruinInfo) throws GameActionException {
        MapLocation ruinLoc = ruinInfo.getMapLocation();
        if (!isRuinMarked(ruinLoc)) {
            // Move towards hte ruin if we're too far to mark it (we need to be adjacent to
            // it to mark it)
            if (ruinLoc.distanceSquaredTo(rc.getLocation()) > Constants.MAX_RUIN_DISTANCE_SQUARED) {
                nav.goTo(ruinLoc, Constants.MAX_RUIN_DISTANCE_SQUARED);
            }

            // Mark tower pattern on the ruin if in range
            if (ruinLoc.distanceSquaredTo(rc.getLocation()) <= Constants.MAX_RUIN_DISTANCE_SQUARED) {
                // TODO: Possibly pick the tower type you want to build (not sure if we need to
                // select tower when marking though)
                rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc);
            }
        }
    }

    /**
     * Check if the ruin is already marked by an ally tower pattern.
     */
    public boolean isRuinMarked(MapLocation ruinLoc) throws GameActionException {
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
    public void attemptMarkResourcePattern() throws GameActionException {
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

    /**
     * Attempt to paint or attack nearby tiles if possible.
     */
    public void paintOrAttack() throws GameActionException {
        MapInfo[] actionableTiles = rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared);
        MapLocation firstEmptyPaintLoc = null;

        // TODO: make a getBestAttack() method that loops over all squares and chooses
        // the best square to attack, and paint color
        // this method should also consider attacking enemy towers if there are any
        // nearby
        for (MapInfo tile : actionableTiles) {

            // If tile is ally-marked but not painted, and we can attack, do it
            // if (tile.getMark().isAlly()) {
            // if (rc.canAttack(tile.getMapLocation())) {
            // // don't re-color if it's already right
            // if (tile.getPaint().isAlly() && (tile.getPaint().isSecondary() ==
            // tile.getMark().isSecondary())) {
            // continue;
            // } else {
            // rc.attack(tile.getMapLocation(), tile.getMark().isSecondary());
            // return;
            // }
            // }
            // }

            if (tile.getMark().isAlly() &&
                    (tile.getPaint() == PaintType.EMPTY || // the tile is currently not painted

                    // the tile is painted, but doesn't match the marking color
                            (tile.getPaint().isAlly()
                                    && (tile.getPaint().isSecondary() != tile.getMark().isSecondary())))

                    && rc.canAttack(tile.getMapLocation())) {

                // Add in check to make sure it's not a wall, log the square that you're
                // attacking.
                rc.attack(tile.getMapLocation(), tile.getMark().isSecondary());

                return;
            }

            // Otherwise remember the first empty tile we can paint.
            // If we don't find a better target, we'll just paint this one at the end.
            else if (firstEmptyPaintLoc == null && tile.getPaint() == PaintType.EMPTY
                    && rc.canAttack(tile.getMapLocation())) {
                firstEmptyPaintLoc = tile.getMapLocation();
            }
        }

        // If we found no ally-marked-but-unpainted tiles, attack an empty tile if we
        // can
        if (firstEmptyPaintLoc != null && rc.isActionReady() && rc.canAttack(firstEmptyPaintLoc)) {
            rc.attack(firstEmptyPaintLoc);
        }
    }

    /**
     * Perform the attack, and if we have a ruin to complete, do it.
     */
    public void tryPatternCompletion() throws GameActionException {

        // TODO: handle resource pattern completion too, not just tower pattern
        // completion

        // Possibly complete tower pattern near a ruin if it exists
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyTiles) {
            if (tile.hasRuin()) {
                // We might want to check if we can complete the tower
                MapLocation ruinLoc = tile.getMapLocation();

                if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc)) {
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc);
                }

                else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc)) {
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc);
                }

            }
        }

        tryResourcePatternCompletion();
    }

    public void tryResourcePatternCompletion() throws GameActionException {
        // this assumes that the whole pattern is within vision radius (and so the
        // possible centers are closer in)

        // TODO: optimize this for bytecode
        MapInfo[] possibleCenters = rc.senseNearbyMapInfos(8);

        for (MapInfo center : possibleCenters) {
            if (center.getPaint().isAlly() && !center.getPaint().isSecondary() &&
                    rc.canCompleteResourcePattern(center.getMapLocation())) {
                rc.completeResourcePattern(center.getMapLocation());
            }
        }
    }

    /**
     * Choose where to move:
     * - If there’s an ally-marked empty tile, move toward it to paint/attack.
     * - Otherwise move randomly.
     */
    public void moveLogic() throws GameActionException {
        MapInfo[] visionTiles = rc.senseNearbyMapInfos();
        int bestDistance = Integer.MAX_VALUE;
        MapLocation bestLocation = null;

        for (MapInfo tile : visionTiles) {
            if (tile.getMark().isAlly() && tile.getPaint() == PaintType.EMPTY) {

                int newDistance = Math.max(Math.abs(tile.getMapLocation().x - rc.getLocation().x),
                        Math.abs(tile.getMapLocation().y - rc.getLocation().y));
                if (newDistance < bestDistance) {
                    bestDistance = newDistance;
                    bestLocation = tile.getMapLocation();
                }
            }
        }

        if (bestLocation != null) {
            nav.goTo(bestLocation, UnitType.SOLDIER.actionRadiusSquared);
        } else {
            // Move in the direction
            MapLocation empty = findEmptyTiles();
            if (empty == null) {
                nav.moveRandom();
            } else {
                nav.goTo(empty, 0);
            }
        }
    }

    public MapLocation findEmptyTiles() throws GameActionException {
        MapInfo[] visionTiles = rc.senseNearbyMapInfos();
        int emptyX = 0;
        int emptyY = 0;
        int emptyCount = 0;
        for (MapInfo tile : visionTiles) {
            if (tile.getPaint() == PaintType.EMPTY) {
                emptyX += tile.getMapLocation().x;
                emptyY += tile.getMapLocation().y;
                emptyCount++;
            }
        }

        if (emptyCount == 0) {
            return null;
        }

        emptyX /= emptyCount;
        emptyY /= emptyCount;

        return new MapLocation(emptyX, emptyY);

    }

}