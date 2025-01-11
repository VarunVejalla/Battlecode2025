package blitz;

import battlecode.common.*;

public class Soldier extends Bunny {

    MapLocation blitzDestination = null;
    SymmetryType blitzSymmetry = null;

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);

    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
        scanSurroundings();
//        updateDestinationIfNeeded();

//        Util.log("KNOWN SPAWN TOWERS " + knownSpawnTowersIdx);
//        for(int i = 0; i < knownSpawnTowers.length; i++){
//            if(knownSpawnTowers[i] == null) continue;
//            Util.log("Spawn tower: " + knownSpawnTowers[i]);
//        }
//        Util.log("KNOWN ENEMY TOWERS");
//        for(int i = 0; i < knownEnemyTowerLocs.length; i++){
//            if(knownEnemyTowerLocs[i] == null) continue;
//            Util.log("Enemy tower: " + knownEnemyTowerLocs[i]);
//        }
//        Util.log("POSSIBLE SYMMETRIES");
//        for(int i = 0; i < possibleSymmetries.length; i++){
//            if(possibleSymmetries[i] == null) continue;
//            Util.log("Symmetry: " + possibleSymmetries[i]);
//        }
//        Util.log("KNOWN EMPTY LOCS");
//        for(int i = 0; i < emptyPotentialEnemyTowersLocs.length; i++){
//            if(emptyPotentialEnemyTowersLocs[i] == null) continue;
//            Util.log("Empty loc: " + emptyPotentialEnemyTowersLocs[i]);
//        }
//

        updateBlitzDestination();

        // 1. Find opponent towers.
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

        // 6. End of Turn Logic
        // Perform any shared cleanup or post-turn logic
        sharedEndFunction();
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
//                rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc);

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

        if (rc.getPaint() < Constants.MIN_PAINT_NEEDED_FOR_SOLDIER_ATTACK) {
            return; // Not enough paint to do anything in this method
        }

        // Prioritize attacking enemy towers.
        for(RobotInfo info : rc.senseNearbyRobots(GameConstants.VISION_RADIUS_SQUARED, oppTeam)) {
            if (info != null && info.getTeam() == oppTeam && Util.isTower(info.getType())) {
                if (rc.isActionReady() && rc.canAttack(info.getLocation())) {
                    rc.attack(info.getLocation());
                    Util.log("SQUARE ATTACKED: " + info.getLocation());
                    return;
                }
            }
        }


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

            // Paint towers close to towers very first.
//            tileScore -= 4*nearestAlliedTowerLoc.distanceSquaredTo(tile.getMapLocation());

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
        nearbyMapInfos = rc.senseNearbyMapInfos();
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

    public void updateBlitzDestination() throws GameActionException {
        if(blitzDestination != null){
            // Check if the blitz destination is still valid.
            if(blitzSymmetry == null){
                for(int i = 0; i < knownEnemyTowerLocs.length; i++){
                    if(blitzDestination.equals(knownEnemyTowerLocs[i])){
                        return;
                    }
                }
            } else {
                // Check if the blitz destination is in the empty tower locs.
                boolean locIsInvalid = false;
                for (int i = 0; i < emptyPotentialEnemyTowersLocs.length; i++) {
                    if (blitzDestination.equals(emptyPotentialEnemyTowersLocs[i])) {
                        locIsInvalid = true;
                        break;
                    }
                }

                if(!locIsInvalid) {
                    for (int i = 0; i < possibleSymmetries.length; i++) {
                        if (possibleSymmetries[i] == blitzSymmetry) {
                            return;
                        }
                    }
                }
            }

            // If not, calculate a new one.
            blitzDestination = null;
            blitzSymmetry = null;
        }

        for(int i = 0; i < knownEnemyTowerLocs.length; i++){
            if(knownEnemyTowerLocs[i] != null){
                blitzDestination = knownEnemyTowerLocs[i];
                blitzSymmetry = null;
                return;
            }
        }

        for(int s = 0; s < possibleSymmetries.length; s++){
            SymmetryType symmetry = possibleSymmetries[s];
            if(symmetry == null){
                continue;
            }
            for(int i = 0; i < knownSpawnTowers.length; i++){
                if(knownSpawnTowers[i] == null){
                    continue;
                }
                MapLocation potentialEnemyLoc = Util.applySymmetry(knownSpawnTowers[i], symmetry);
                boolean validLoc = true;
                for(int j = 0; j < emptyPotentialEnemyTowersLocs.length; j++){
                    if(potentialEnemyLoc.equals(emptyPotentialEnemyTowersLocs[j])){
                        validLoc = false;
                        break;
                    }
                }
                if(validLoc){
                    blitzDestination = potentialEnemyLoc;
                    blitzSymmetry = symmetry;
                    Util.log("GOT BLITZ DESTINATION: " + blitzDestination + " WITH SYMMETRY " + symmetry);
                    return;
                }
            }
        }
    }

    /**
     * Choose where to move:
     * - If there’s an ally-marked empty tile, move toward it to paint/attack.
     * - Otherwise move randomly.
     */
    public void moveLogic() throws GameActionException {
        Util.log("Blitz destination: " + blitzDestination);
        myLoc = rc.getLocation();

        // If trying to replenish, go to nearest tower immediately.
        if (tryingToReplenish &&
                nearestAlliedPaintTowerLoc != null
                && myLoc.distanceSquaredTo(nearestAlliedPaintTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {

            nav.goTo(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
            return;
        }

        if(blitzDestination != null){
            nav.goTo(blitzDestination, rc.getType().actionRadiusSquared);
            return;
        }

        MapLocation bestDirection = null;
        int bestScore = 0;

        for (Direction dir : Direction.allDirections()) {
            if (!rc.canMove(dir)) {
                continue;
            }
            MapInfo tile = rc.senseMapInfo(myLoc.add(dir));
            int tileScore = 0;

            // Strongly favor tiles with ally color on the boundary.
            if (isAllyBoundaryTile(tile)) {
                tileScore += 1000;
                // nav.goTo(tile.getMapLocation(), 0);
                // Util.log("My next tile is a boundary!");
                // return;
                // // Favor staying on your color
                if (tile.getPaint().isAlly()) {
                    tileScore += 5;
                }
            }

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
            Util.log("Moving in direction: " + bestDirection.toString());
            nav.goTo(bestDirection, 0);
        } else {
            // Move in a pre-determined global direction.
            Util.log("Moving to destination " + destination.toString());
            nav.goTo(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
        }
    }

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
}