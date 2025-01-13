package blitz2;

import battlecode.common.*;

enum PatternType {
    RESOURCE,
    PAINT_TOWER,
    MONEY_TOWER,
    DEFENSE_TOWER;

    public boolean isTower() {
        return this != RESOURCE;
    }

    public UnitType getUnitType() {
        if (this == PAINT_TOWER) {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        } else if (this == MONEY_TOWER) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        } else if (this == DEFENSE_TOWER) {
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        } else {
            return null;
        }
    }
}

enum PatternStatus {
    INVALID,
    FINISHED,
    POSSIBLY_FINISHED,
    BLOCKED_BY_ENEMY,
    BLOCKED_BY_SELF,
    BLOCKED_BY_NOTHING;
}

public class Soldier extends Bunny {

    int knownSpawnTowersIdx = 0;
    MapLocation[] knownSpawnTowers = new MapLocation[5];
    MapLocation[] knownEnemyTowerLocs = new MapLocation[10];
    MapLocation[] emptyPotentialEnemyTowersLocs = new MapLocation[20];
    MapLocation blitzDestination = null;
    SymmetryType blitzSymmetry = null;

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
        scanSurroundings();
        for(RobotInfo info : nearbyFriendlies){
            Util.log("Friendly: " + info);
            if(info.getTeam() == myTeam && Util.isTower(info.getType())){
                knownSpawnTowers[knownSpawnTowersIdx] = info.getLocation();
                knownSpawnTowersIdx++;
            }
        }
    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
        scanSurroundings(); // TODO: Can delete this?
        updateEnemyTowerLocs();
        updateSymmetries();
        updateDestinationIfNeeded();
        updateBlitzDestination();

        // 1. Handle Ruins
        // Check if there are any unmarked ruins nearby. If a ruin is found:
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
     * Tries to move towards an unmarked ruin and mark it if in range.
     */
    public void handleUnmarkedRuin(MapInfo ruinInfo) throws GameActionException {
        MapLocation ruinLoc = ruinInfo.getMapLocation();
        if (!Util.isRuinMarked(ruinLoc)) {
            // Move towards hte ruin if we're too far to mark it (we need to be adjacent to
            // it to mark it)
            if (ruinLoc.distanceSquaredTo(rc.getLocation()) > Constants.MAX_RUIN_DISTANCE_SQUARED) {
                nav.goTo(ruinLoc, Constants.MAX_RUIN_DISTANCE_SQUARED);
            }

            // Mark tower pattern on the ruin if in range
            if (ruinLoc.distanceSquaredTo(rc.getLocation()) <= Constants.MAX_RUIN_DISTANCE_SQUARED) {
                // TODO: Possibly pick the tower type you want to build (not sure if we need to
                // Mark to build the kind of tower that is the opposite of the nearest allied tower.
                if(nearestAlliedTowerLoc.equals(nearestAlliedPaintTowerLoc)) {
                    rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc);
                }
                else {
                    rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc);
                }
            }
        }
    }

    public void updateEnemyTowerLocs() throws GameActionException {
        // Clear existing slots
        for(int i = 0; i < knownEnemyTowerLocs.length; i++){
            if(knownEnemyTowerLocs[i] != null && rc.canSenseLocation(knownEnemyTowerLocs[i])){
                RobotInfo info = rc.senseRobotAtLocation(knownEnemyTowerLocs[i]);
                if(info == null || info.getTeam() != oppTeam || !Util.isTower(info.getType())){
                    knownEnemyTowerLocs[i] = null;
                }
            }
        }
        for(int i = 0; i < emptyPotentialEnemyTowersLocs.length; i++){
            if(emptyPotentialEnemyTowersLocs[i] == null){
                continue;
            }
            if(rc.canSenseRobotAtLocation(emptyPotentialEnemyTowersLocs[i])){
                RobotInfo info = rc.senseRobotAtLocation(emptyPotentialEnemyTowersLocs[i]);
                if(info != null && info.getTeam() != oppTeam && Util.isTower(info.getType())){
                    emptyPotentialEnemyTowersLocs[i] = null;
                }
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
                if(rc.canSenseLocation(potentialEnemyLoc)){
                    RobotInfo info = rc.senseRobotAtLocation(potentialEnemyLoc);
                    if(info == null || info.getTeam() != oppTeam || !Util.isTower(info.getType())){
                        boolean alreadyIn = false;
                        for(int j = 0; j < emptyPotentialEnemyTowersLocs.length; j++){
                            if(potentialEnemyLoc.equals(emptyPotentialEnemyTowersLocs[j])){
                                alreadyIn = true;
                                break;
                            }
                        }
                        if(!alreadyIn){
                            for(int j = 0; j < emptyPotentialEnemyTowersLocs.length; j++){
                                if(emptyPotentialEnemyTowersLocs[j] == null){
                                    emptyPotentialEnemyTowersLocs[j] = potentialEnemyLoc;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        for(RobotInfo info : nearbyOpponents){
//            Util.log("Nearby opponent: " + info);
            if(Util.isTower(info.getType())){
                MapLocation enemyTowerLoc = info.getLocation();
                boolean alreadyIn = false;
                for(int i = 0; i < 10; i++){
                    if(enemyTowerLoc.equals(knownEnemyTowerLocs[i])){
                        alreadyIn = true;
                        break;
                    }
                }
                if(!alreadyIn){
                    for(int i = 0; i < 10; i ++){
                        if(knownEnemyTowerLocs[i] == null){
                            knownEnemyTowerLocs[i] = enemyTowerLoc;
                            break;
                        }
                    }
                }
            }
        }
    }
    void updateSymmetries() throws GameActionException {
        for(int i = 0; i < possibleSymmetries.length; i++){
            SymmetryType symmetry = possibleSymmetries[i];
            if(symmetry == null){
                continue;
            }
            for(int j = 0; j < knownSpawnTowersIdx; j++){
                MapLocation symmetryLoc = Util.applySymmetry(knownSpawnTowers[j], symmetry);
                if(rc.canSenseLocation(symmetryLoc)){
                    // Check if symmetry is violated.
                    RobotInfo info = rc.senseRobotAtLocation(symmetryLoc);
                    MapInfo tile = rc.senseMapInfo(symmetryLoc);
                    boolean symmetryViolated = true;
                    if(tile.hasRuin()) {
                        symmetryViolated = false;
                    } else if(info != null && Util.isTower(info.getType())){
                        symmetryViolated = false;
                    }
                    if(symmetryViolated){
                        possibleSymmetries[i] = null;
                        break;
                    }
                }
            }
        }
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

        // loop over actionable enemies and try to attack them if they're a tower
        for (RobotInfo robot : rc.senseNearbyRobots(UnitType.SOLDIER.actionRadiusSquared, rc.getTeam().opponent())) {
            if (Util.isPaintTower(robot.getType())) {
                if (rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
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

            // Tile score is intentionally set to 0 and not min_value.
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
                tileScore += 500 * adjacencyToAllyPaint(tile.getMapLocation()) + 500;
            }

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

                // Check if you can complete a tower pattern.
                if(rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc)) {
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLoc);
                } else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLoc)) {
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
        if (tryingToReplenish && nearestAlliedPaintTowerLoc != null) {
            nav.goTo(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
            Util.log("Trying to replenish paint");
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