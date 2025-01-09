package basebotv2;

import battlecode.common.*;

public class Soldier extends Bunny {


    MapLocation nearestAlliedTowerLoc;
    MapLocation destination; //long-term destination
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
        //   - Move toward the ruin if we are far away (distance > 2).
        //   - Once in range, try mark it with a tower pattern (so we can build a tower)
        MapInfo curRuin = findUnmarkedRuin();


        if (curRuin != null && !tryingToReplenish) {
            handleUnmarkedRuin(curRuin); // Move to and interact with the ruin if you're not trying to replenish
        }

        else if (!tryingToReplenish) {
            // 2. Attempt to Mark a Resource Pattern if you're not trying to replenish
            // If no ruins are found, check if we can mark a resource pattern at our current location.
            //   - Only do this if the location is not already marked by our team
            attemptMarkResourcePattern();
        }

        // 3. Replenish or Paint/Attack if you can
        // After handling ruins and resource marking, check if we can act:
        // If we're trying to replenish and we're in range of a tower, try to replenish
        // Otherwise
        //   - Paint or attack a nearby tile based on priority:
        //     - Ally-marked but unpainted tiles take precedence.
        //     - If no such tiles exist, attack an unpainted tile nearby.
        if (rc.isActionReady()) {
            if (tryingToReplenish){
                tryReplenish();
            }
            else {
                paintOrAttack();
            }
        }

        // 4. Movement Logic
        // If movement is possible:
        //   - Move toward the nearest tower if you're trying to replenish.

        //   - Otherwise Prioritize moving toward ally-marked tiles that are empty (unpainted).
        //   - If no such tiles are found, move to your destination
        if (rc.isMovementReady()) {
            moveLogic();
        }

        // 5. Recheck for Replenish or Painting/Attacking

        // After handling ruins and resource marking, check if we can act:
        // If we're trying to replenish and we're in range of a tower, try to replenish

        // Otherwise
        //   - Paint or attack a nearby tile based on priority:
        //     - Ally-marked but unpainted tiles take precedence.
        //     - If no such tiles exist, attack an unpainted tile nearby.
        if (rc.isActionReady()) {
            if (tryingToReplenish){
                tryReplenish();
            }
            else {
                paintOrAttack();
            }
        }

        tryPatternCompletion();

        // 6. End of Turn Logic
        // Perform any shared cleanup or post-turn logic
        sharedEndFunction();
    }


    /**
     * Scan stuff around you (this method is executed at the beginning of every turn)
     */
    public void scanSurroundings() throws GameActionException{
        nearbyMapInfos = rc.senseNearbyMapInfos();
        nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
        nearbyOpponents = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        updateNearestAlliedTowerLoc();

    }


    /**
    * Update the destination we're travelling to if we need to replenish, or we've reached our current destination
     */
    public void updateDestinationIfNeeded() throws GameActionException{
        if (nearestAlliedTowerLoc != null && (tryingToReplenish || checkIfIShouldReplenish())){
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
     * Update the nearest allied tower location to replenish paint from based on surroundings
    */
    public void updateNearestAlliedTowerLoc() throws GameActionException{
        for (RobotInfo bot : nearbyFriendlies) {
            if (Util.isTower(bot.getType())) {
                MapLocation currAlliedTowerLocation = bot.getLocation();
                if (nearestAlliedTowerLoc == null
                        || rc.getLocation().distanceSquaredTo(currAlliedTowerLocation) < rc.getLocation().distanceSquaredTo(nearestAlliedTowerLoc)) {
                    nearestAlliedTowerLoc = currAlliedTowerLocation;
                }
            }
        }
    }



    /**
     * Tries to replenish paint from the nearest allied tower if within range to transfer paint
     */
    public void tryReplenish() throws GameActionException{
        if (nearestAlliedTowerLoc != null){
            if (rc.getLocation().distanceSquaredTo(nearestAlliedTowerLoc) <= GameConstants.PAINT_TRANSFER_RADIUS_SQUARED){

                int towerPaintQuantity = rc.senseRobotAtLocation(nearestAlliedTowerLoc).getPaintAmount();
                int paintToFillUp = Math.min(
                        rc.getType().paintCapacity - rc.getPaint(), // amount of paint needed to fully top off
                        towerPaintQuantity); // amount of paint available in the tower


                if(rc.isActionReady() && rc.canTransferPaint(nearestAlliedTowerLoc, -paintToFillUp)) {
                    rc.transferPaint(nearestAlliedTowerLoc, -paintToFillUp);
                }


                if(!checkIfIShouldReplenish()){
                    tryingToReplenish = false;
                }
            }

        }
    }


    /**
     * Determine whether current you should go back to an ally tower to replenish on paint
     * based on current paint quantity and distance to nearest tower.
     */
    public boolean checkIfIShouldReplenish() throws GameActionException{
        //TODO: make this a more intelligent decision based on factors like:
        // - distance to nearest tower
        // - whether you're really close to finishing a pattern, in which case you should consider sacrificing yourself for the greater good
        // - how much paint you have left

        return rc.getPaint() <= Constants.PAINT_THRESHOLD_TO_REPLENISH;
    }


    /**
     * Finds a ruin that is not claimed by your team.
     */
    public MapInfo findUnmarkedRuin() throws GameActionException {
        for (MapInfo tile : nearbyMapInfos) { // nearbyMapInfos is populated in the scanSurroundings() method at the beginning of each round
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
            // Move towards hte ruin if we're too far to mark it (we need to be adjacent to it to mark it)
            if (ruinLoc.distanceSquaredTo(rc.getLocation()) > Constants.MAX_RUIN_DISTANCE_SQUARED) {
                nav.goTo(ruinLoc, Constants.MAX_RUIN_DISTANCE_SQUARED);
            }

            // Mark tower pattern on the ruin if in range
            if (ruinLoc.distanceSquaredTo(rc.getLocation()) <= Constants.MAX_RUIN_DISTANCE_SQUARED) {
                // TODO: Possibly pick the tower type you want to build (not sure if we need to select tower when marking though)
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
     * Attempt to mark a resource pattern at your current location if no ally mark is present that would conflict with it is present.
     *
     * This code checks the 5 x 5 area around the robot's current location (nearbyMapInfos(8) gets you this exact 5x5 grid)
     *      to see if any spots have already been marked).
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

        //TODO: make a getBestAttack() method that loops over all squares and chooses the best square to attack, and paint color
        // this method should also consider attacking enemy towers if there are any nearby
        for (MapInfo tile : actionableTiles) {

            // If tile is ally-marked but not painted, and we can attack, do it
            if (tile.getMark().isAlly() &&
                    (tile.getPaint() == PaintType.EMPTY || // the tile is currently not painted

                            // the tile is painted, but doesn't match the marking color
                            tile.getPaint()==PaintType.ALLY_SECONDARY  &&  !tile.getMark().isSecondary() ||
                            tile.getPaint()==PaintType.ALLY_PRIMARY &&  tile.getMark().isSecondary())

                    && rc.canAttack(tile.getMapLocation())) {

                if (tile.getMark().isSecondary()) {
                    rc.attack(tile.getMapLocation(), true);
                } else {
                    rc.attack(tile.getMapLocation(), false);
                }
                return;
            }

            // Otherwise remember the first empty tile we can paint.
            // If we don't find a better target, we'll just paint this one at the end.
            else if (firstEmptyPaintLoc == null && tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                firstEmptyPaintLoc = tile.getMapLocation();
            }
        }

        // If we found no ally-marked-but-unpainted tiles, attack an empty tile if we can
        if (firstEmptyPaintLoc != null && rc.isActionReady() && rc.canAttack(firstEmptyPaintLoc)) {
            rc.attack(firstEmptyPaintLoc);
        }
    }



    /**
     * Try to complete a tower pattern if we're near a ruin.
     */
    public void tryPatternCompletion() throws GameActionException {
        //TODO: handle resource pattern completion too, not just tower pattern completion

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
    }


    /**
     * Choose where to move:
     *  - if we're currently trying to replenish, move closer to nearest tower (where we can transfer paint from)
     *
     *
     *  - If there’s an ally-marked empty tile, move toward it to paint/attack.
     *  - Otherwise towards your destination (currently just a random spot on the map, to encourage exploration).
     */
    public void moveLogic() throws GameActionException {

        myLoc = rc.getLocation();
        // if we are trying to replenish, move towards the nearest tower if we're not close enough
        if (tryingToReplenish && nearestAlliedTowerLoc != null &&
                rc.getLocation().distanceSquaredTo(nearestAlliedTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {
            nav.goTo(nearestAlliedTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
        }

        // we don't use the nearbyMapInfos here because we might have moved since we last scanned
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
            nav.goTo(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
        }
    }

}