package boony;

import battlecode.common.*;

public class Soldier extends Bunny {

    MapLocation nearestAlliedTowerLoc;
    MapLocation destination; // long-term destination
    MapInfo[] nearbyMapInfos;
    RobotInfo[] nearbyFriendlies;
    RobotInfo[] nearbyOpponents;
    boolean tryingToReplenish = false;

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);

        destination = Util.getRandomMapLocation();

    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
        scanSurroundings();
        updateDestinationIfNeeded();

        // 1. Handle Ruins
        // Check if there are any unmakred ruins nearby. If a ruin is found:
        // - Move toward the ruin if we are far away (distance > 2).
        // - Once in range, try mark it with a tower pattern (so we can build a tower)
        MapInfo curRuin = findUnmarkedRuin();

        if (curRuin != null && !tryingToReplenish) {
            handleUnmarkedRuin(curRuin); // Move to and interact with the ruin if you're not trying to replenish
        }

        else if (!tryingToReplenish) {
            // 2. Attempt to Mark a Resource Pattern if you're not trying to replenish
            // If no ruins are found, check if we can mark a resource pattern at our current
            // location.
            // - Only do this if the location is not already marked by our team
            attemptMarkResourcePattern();
        }

        // 3. Replenish or Paint/Attack if you can
        // After handling ruins and resource marking, check if we can act:
        // If we're trying to replenish and we're in range of a tower, try to replenish
        // Otherwise
        // - Paint or attack a nearby tile based on priority:
        // - Ally-marked but unpainted tiles take precedence.
        // - If no such tiles exist, attack an unpainted tile nearby.
        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                paintOrAttack();
            }
        }

        // 4. Movement Logic
        // If movement is possible:
        // - Move toward the nearest tower if you're trying to replenish.

        // - Otherwise Prioritize moving toward ally-marked tiles that are empty
        // (unpainted).
        // - If no such tiles are found, move to your destination
        if (rc.isMovementReady()) {
            moveLogic();
        }

        // 5. Recheck for Replenish or Painting/Attacking

        // After handling ruins and resource marking, check if we can act:
        // If we're trying to replenish and we're in range of a tower, try to replenish

        // Otherwise
        // - Paint or attack a nearby tile based on priority:
        // - Ally-marked but unpainted tiles take precedence.
        // - If no such tiles exist, attack an unpainted tile nearby.
        if (rc.isActionReady()) {
            if (tryingToReplenish) {
                tryReplenish();
            } else {
                paintOrAttack();
            }
        }

        tryPatternCompletion();

        // 6. End of Turn Logic
        // Perform any shared cleanup or post-turn logic
        sharedEndFunction();
    }

    /**
     * Scan stuff around you (this method is executed at the beginning of every
     * turn)
     */
    public void scanSurroundings() throws GameActionException {
        nearbyMapInfos = rc.senseNearbyMapInfos();
        nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
        nearbyOpponents = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        updateNearestAlliedTowerLoc();

    }

    /**
     * Update the destination we're travelling to if we need to replenish, or we've
     * reached our current destination
     */
    public void updateDestinationIfNeeded() throws GameActionException {
        if (nearestAlliedTowerLoc != null && (tryingToReplenish || checkIfIShouldReplenish())) {
            destination = nearestAlliedTowerLoc;
            tryingToReplenish = true;
            Util.addToIndicatorString("REP");
        }

        else if (destination == null ||
                rc.getLocation().distanceSquaredTo(destination) <= Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION) {
            destination = Util.getRandomMapLocation();
        }
    }

    /**
     * Update the nearest allied tower location to replenish paint from based on
     * surroundings
     */
    public void updateNearestAlliedTowerLoc() throws GameActionException {
        for (RobotInfo bot : nearbyFriendlies) {
            if (Util.isTower(bot.getType())) {
                MapLocation currAlliedTowerLocation = bot.getLocation();
                if (nearestAlliedTowerLoc == null
                        || rc.getLocation().distanceSquaredTo(currAlliedTowerLocation) < rc.getLocation()
                                .distanceSquaredTo(nearestAlliedTowerLoc)) {
                    nearestAlliedTowerLoc = currAlliedTowerLocation;
                }
            }
        }
    }

    /**
     * Tries to replenish paint from the nearest allied tower if within range to
     * transfer paint
     */
    public void tryReplenish() throws GameActionException {
        if (nearestAlliedTowerLoc != null) {
            if (rc.getLocation()
                    .distanceSquaredTo(nearestAlliedTowerLoc) <= GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {

                int towerPaintQuantity = rc.senseRobotAtLocation(nearestAlliedTowerLoc).getPaintAmount();
                int paintToFillUp = Math.min(
                        rc.getType().paintCapacity - rc.getPaint(), // amount of paint needed to fully top off
                        towerPaintQuantity); // amount of paint available in the tower

                if (rc.isActionReady() && rc.canTransferPaint(nearestAlliedTowerLoc, -paintToFillUp)) {
                    rc.transferPaint(nearestAlliedTowerLoc, -paintToFillUp);
                }

                if (!checkIfIShouldReplenish()) {
                    tryingToReplenish = false;
                }
            }

        }
    }

    /**
     * Determine whether current you should go back to an ally tower to replenish on
     * paint
     * based on current paint quantity and distance to nearest tower.
     */
    public boolean checkIfIShouldReplenish() throws GameActionException {
        // TODO: make this a more intelligent decision based on factors like:
        // - distance to nearest tower
        // - whether you're really close to finishing a pattern, in which case you
        // should consider sacrificing yourself for the greater good
        // - how much paint you have left

        return rc.getPaint() <= Constants.PAINT_THRESHOLD_TO_REPLENISH;
    }

    /**
     * Finds a ruin that is not claimed by your team.
     */
    public MapInfo findUnmarkedRuin() throws GameActionException {
        for (MapInfo tile : nearbyMapInfos) {
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
        MapLocation myLoc = rc.getLocation();

        MapLocation bestPaintLoc = null;
        int bestScore = 0;
        boolean secondaryPaint = false;

        for (MapInfo tile : actionableTiles) {
            // Make sure tile can be painted.
            if (!rc.canPaint(tile.getMapLocation())) {
                continue;
            }

            int tileScore = 0;
            // Prioritize painting marked tiles.
            if (tile.getMark().isAlly()) {
                // Tile is empty or wrong color.
                boolean tileEmpty = tile.getPaint() == PaintType.EMPTY;
                boolean wrongColor = tile.getMark().isSecondary() != tile.getPaint().isSecondary();
                if (tileEmpty || wrongColor) {
                    // Make sure soldier is ready and tile can be painted.
                    if (rc.isActionReady() && rc.canPaint(tile.getMapLocation())) {
                        // tileScore += 1000;
                        // Attack immediately to save bytecode.
                        rc.attack(tile.getMapLocation(), tile.getMark().isSecondary());
                        Util.log("Square Attacked: " + tile.getMapLocation().toString());
                        return;
                    }
                }
            }

            // If there are no marked tiles, paint an empty one.
            else if (tile.getPaint() == PaintType.EMPTY) {
                // Reward for adjacency.
                tileScore += 50 * adjacencyToAllyPaint(tile.getMapLocation()) + 50;
            }

            // Paint closer tiles first.
            tileScore -= myLoc.distanceSquaredTo(tile.getMapLocation());

            if (tileScore > bestScore) {
                bestScore = tileScore;
                bestPaintLoc = tile.getMapLocation();
                secondaryPaint = tile.getMark().isSecondary();
            }
        }

        // Paint the best tile that was found.
        if (bestPaintLoc != null && rc.isActionReady()) {
            rc.attack(bestPaintLoc, secondaryPaint);
            Util.log("Square Attacked: " + bestPaintLoc.toString());
        }
    }

    public int adjacencyToAllyPaint(MapLocation loc) throws GameActionException {
        Direction[] directions = Direction.allDirections();
        int adjacentAllyTiles = 0;
        for (Direction dir : directions) {
            MapLocation adjacent = loc.add(dir);

            // Check if the adjacent tile is within bounds and has allied paint
            if (rc.canSenseLocation(adjacent)) {
                MapInfo adjacentTile = rc.senseMapInfo(adjacent);
                if (adjacentTile.getPaint().isAlly()) {
                    adjacentAllyTiles++;
                }
            }
        }
        return adjacentAllyTiles;
    }

    /**
     * Perform the attack, and if we have a ruin to complete, do it.
     */
    public void tryPatternCompletion() throws GameActionException {

        // TODO: handle resource pattern completion too, not just tower pattern
        // completion

        // Possibly complete tower pattern near a ruin if it exists
        // nearbyMapInfos = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyMapInfos) {
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
        myLoc = rc.getLocation();

        // If trying to replenish, go to nearest tower immediately.
        boolean tooFar = myLoc.distanceSquaredTo(nearestAlliedTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED;
        if (tryingToReplenish && nearestAlliedTowerLoc != null && tooFar) {
            nav.goTo(nearestAlliedTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
            return;
        }

        MapLocation bestDirection = null;
        int bestScore = 0;

        for (Direction dir : Direction.allDirections()) {

            MapInfo tile = rc.senseMapInfo(myLoc.add(dir));

            int tileScore = 0;

            // Strongly favor tiles with ally color on the boundary.
            if (isAllyBoundaryTile(tile)) {
                // tileScore += 1000;
                nav.goTo(tile.getMapLocation(), 0);
                Util.log("My next tile is a boundary!");
                return;
            }
            // // Favor staying on your color
            // if (tile.getPaint().isAlly()) {
            // tileScore += 5;
            // }

            // // If there's a mark and it's unpainted, favor that too.
            // if (tile.getMark().isAlly() && tile.getPaint() == PaintType.EMPTY) {
            // tileScore += 100;
            // }

            if (tileScore > bestScore) {
                bestScore = tileScore;
                bestDirection = tile.getMapLocation();
            }
        }

        if (bestDirection != null) {
            nav.goTo(bestDirection, UnitType.SOLDIER.actionRadiusSquared);
        } else {
            // Move in a pre-determined global direction.
            Util.log("Moving to a destination");
            nav.goTo(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
        }
    }

    /**
     * Determines if a tile is on the boundary of the allied-painted region.
     */
    private boolean isAllyBoundaryTile(MapInfo tile) throws GameActionException {
        // Make sure tile is ally.
        if (!tile.getPaint().isAlly()) {
            return false;
        }

        // Check for empty adjacent tiles.
        Direction[] directions = Direction.allDirections();
        for (Direction dir : directions) {
            MapLocation adjacent = tile.getMapLocation().add(dir);
            if (rc.canSenseLocation(adjacent)) {
                MapInfo adjacentTile = rc.senseMapInfo(adjacent);
                // Make sure is passable.
                if (adjacentTile.isPassable()) {
                    if (adjacentTile.getPaint() == PaintType.EMPTY) {
                        return true; // At least one adjacent tile is empty
                    }
                }
            }
        }
        return false;
    }

    public MapLocation findEmptyTiles() throws GameActionException {
        int emptyX = 0;
        int emptyY = 0;
        int emptyCount = 0;
        for (MapInfo tile : nearbyMapInfos) {
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