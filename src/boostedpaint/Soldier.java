package boostedpaint;

import battlecode.common.*;

enum PatternPriority {
    LOW, MEDIUM, HIGH;
}

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
//    MapLocation patternCenter;
//    PatternType patternType;
//    boolean definiteCenter = false;

    // only looking at the ruins without a tower on them already
//    int[] nearbyRuinIndices = new int[4]; // can have at most 4 ruins in vision radius
//    int numEmptyRuins = 0;
//    PatternPriority[] ruinPriorities = new PatternPriority[4];

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
//        nearbyRuinIndices[0] = -1;
//        nearbyRuinIndices[1] = -1;
//        nearbyRuinIndices[2] = -1;
//        nearbyRuinIndices[3] = -1;
//        numEmptyRuins = 0;
//        if (rc.getRoundNum() > 100) {
//            rc.resign();
//        }

        scanSurroundings();
        updateDestinationIfNeeded();


        if (tryingToReplenish) {
            if (rc.isActionReady()) {
                tryReplenish();
            }
            boolean moved = false;
            if (rc.isMovementReady()) {
                moveLogic();
                moved = !myLoc.equals(rc.getLocation());
                myLoc = rc.getLocation();
            }
            if (moved && rc.isActionReady()) {
                tryReplenish();
            }

            // TODO: should we be doing this?
            tryTowerPatternCompletion();
            tryResourcePatternCompletion();

            sharedEndFunction();
            return;
        }

        int highPriorityRuinIndex = -1;
        int mediumPriorityRuinIndex = -1;

        UnitType intendedType = getPatternUnitType();
        boolean[][] pattern = rc.getTowerPattern(intendedType);

        for(int index : Masks2.spiralOutwardIndices) {
            if (!nearbyMapInfos[index].hasRuin() || rc.senseRobotAtLocation(nearbyMapInfos[index].getMapLocation()) != null) {
                continue;
            }
            PatternPriority priority = findPriority(index, pattern);
            if (priority == PatternPriority.HIGH) {
                highPriorityRuinIndex = index;
                break;
            } else if (mediumPriorityRuinIndex == -1 && priority == PatternPriority.MEDIUM) {
                mediumPriorityRuinIndex = index;
            }
        }

        if (highPriorityRuinIndex != -1) {
            workOnRuin(highPriorityRuinIndex, pattern);
            sharedEndFunction();
            return;
        }

        int resourceCenterIndex = Util.getPotentialResourcePatternCenterIndex(nearbyMapInfos);
        if (resourceCenterIndex != -1) {
            workOnResourcePattern(resourceCenterIndex, pattern);
            sharedEndFunction();
            return;
        }
        if (mediumPriorityRuinIndex != -1) {
            workOnRuin(mediumPriorityRuinIndex, pattern);
            sharedEndFunction();
            return;
        }

        // TODO: default behavior

        sharedEndFunction();
        return;



//
//
//
//
//
////        if (potentialResourceCenter != null) {
////            Util.log(potentialResourceCenter.toString());
////        } else {
////            Util.log("null");
////        }
//
////        Util.logBytecode("after finding resource pattern");
//
//        // if we see a definitely unfinished ruin that we could possibly finish, go for that one
//            // means
//        //
//
////        updatePatternCenterInfo();
//
////        if (rc.getRoundNum() > 10) {
////            rc.resign();
////        }
//
//
//        // TODO: when are we allowing repainting??
//        // i think it should be never for resource patterns
//        // but when for tower patterns?
//        // for now, it's not allowed at all
//        // need to change other things depending on how generous we are with repainting
//
//        // NOTE: I HAD IT ORGANIZED LIKE THIS SPECIFICALLY IN ORDER TO CHANGE MACRO MORE EASILY IF WE WANTED TO LATER
//        if (definiteCenter && patternType.isTower()) {
//            // this is guaranteed to be in line with what has been put down so far, since we are not repainting
//            boolean canPaint = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
//            boolean paintedPattern = false;
//            boolean moved = false;
//
//            MapLocation newLoc = myLoc;
//
//
//
//            if (rc.isMovementReady()) {
//                nav.goTo(patternCenter, 2);
//                newLoc = rc.getLocation();
//                moved = !newLoc.equals(myLoc);
//            }
//            if (moved) {
//                myLoc = newLoc;
//
//                // TODO: Could probably do this, but maybe bytecode issues
////                if (canPaint) {
////                scanSurroundings();
////                updatePatternCenterInfo();
////                }
//            }
//            if (canPaint) {
//                paintedPattern = fillInPattern();
//            }
//
//            if (rc.canCompleteTowerPattern(patternType.getUnitType(), patternCenter)) {
//                rc.completeTowerPattern(patternType.getUnitType(), patternCenter);
//                patternCenter = null;
//            }
//
//            // TODO: do we care about other ruins right here?
////            else if (rc.getChips() >= patternType.getUnitType().moneyCost) {
////                // try completing other ones?
////            }
//
////            if () {
////                tryTowerPatternCompletion();
////            }
//        } else if (definiteCenter) {
//            // guaranteed that we see all 5x5 of these tiles and that any tower we see doesn't conflict with this pattern
//            // want to a) end turn on our tile, b) move towards the emptier tiles, and c)
//
//            boolean canPaint = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
//            boolean paintedPattern = false;
//            boolean moved = false;
//
//            MapLocation newLoc = myLoc;
//            if (rc.isMovementReady()) {
//                nav.goTo(patternCenter, 0);
//                newLoc = rc.getLocation();
//                moved = !newLoc.equals(myLoc);
//            }
//            if (moved) {
//                myLoc = newLoc;
//                // TODO: Could probably do this, but maybe bytecode issues
////                if (canPaint) {
////                scanSurroundings();
////                updatePatternCenterInfo();
////                }
//            }
//            if (canPaint) {
//                paintedPattern = fillInPattern();
//            }
//
//            if (paintedPattern && myLoc.isWithinDistanceSquared(patternCenter, 8) && rc.canCompleteResourcePattern(patternCenter)) {
//                rc.completeResourcePattern(patternCenter);
//                patternCenter = null;
//            }
//
////            // TODO: do we care about completing towers?
////            if (rc.getChips() >= patternType.getUnitType().moneyCost) {
////                tryTowerPatternCompletion();
////            }
//        } else {
//            // if it's not a definite center, that means it's a) an unfinished resource pattern and b) not fully contained within vision radius
//            // it's a resource pattern, because if it were a tower pattern, we'd be able to see the ruin and mark it as definite
//
//            boolean canPaint = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;
//            boolean paintedPattern = false;
//            if (canPaint) {
//                paintedPattern = fillInTowardPattern();
//            }
//            if (rc.isMovementReady()) {
//                nav.goTo(patternCenter, 0);
//            }
//
//        }
//        sharedEndFunction();
    }

    public int getTowerAsInt(UnitType type) {
        if (type == UnitType.LEVEL_ONE_PAINT_TOWER) {
            return 0;
        } else if (type == UnitType.LEVEL_ONE_MONEY_TOWER) {
            return 1;
        } else { //(type == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
            return 2;
        }
    }

    public void workOnRuin(int index, boolean[][] paintPattern) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;

        if (isPaintReady) {
            int dx = Constants.dx[index];
            int dy = Constants.dy[index];
            int[] ordering = Masks2.orderFillingRuin[13*dx + dy + 84];

            for (int attackIndex : ordering) {
                if (nearbyMapInfos[attackIndex].hasRuin()) {
                    continue;
                }

                PaintType currentPaint = nearbyMapInfos[attackIndex].getPaint();
                if (currentPaint == PaintType.EMPTY) {

                    int offsetX = Constants.dx[attackIndex] - dx;
                    int offsetY = Constants.dy[attackIndex] - dy;
                    if (rc.getID() == 13775) {
                        Util.log("filling in empty");
                        Util.log("Offset:" + offsetX+" " + offsetY);
                        Util.log("d:" + dx+" " + dy);
                        Util.log("sec:" + currentPaint.isSecondary());
                        Util.log("actual attacking square:" + nearbyMapInfos[attackIndex].getMapLocation());
                    }
                    if (offsetX*offsetX + offsetY*offsetY <= 8) {
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);
                    } else {
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), (offsetX+dx+offsetY+dy)%2==0);
                    }
                    break;
                } else if (currentPaint.isEnemy()) {
                    continue;
                } else {
                    int offsetX = Constants.dx[attackIndex] - dx;
                    int offsetY = Constants.dy[attackIndex] - dy;

                    if (offsetX*offsetX + offsetY*offsetY <= 8 && paintPattern[offsetX+2][offsetY+2] != currentPaint.isSecondary()) {
                        if (rc.getID() == 13775) {
                            Util.log("Offset:" + offsetX+" " + offsetY);
                            Util.log("d:" + dx+" " + dy);
                            Util.log("sec:" + currentPaint.isSecondary());
                        }
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);
                        break;
                    }
                }
            }
        }

        if (rc.isMovementReady()) {
            nav.circle(nearbyMapInfos[index].getMapLocation(), 1, 2);
        }
    }

    public void workOnResourcePattern(int index, boolean[][] paintPattern) throws GameActionException {
        boolean isPaintReady = rc.isActionReady() && rc.getPaint() >= UnitType.SOLDIER.attackCost;

        if (isPaintReady) {
            int dx = Constants.dx[index];
            int dy = Constants.dy[index];
            int[] ordering = Masks2.orderFillingResource[13*dx + dy + 84];

            for (int attackIndex : ordering) {
                PaintType currentPaint = nearbyMapInfos[attackIndex].getPaint();
                int offsetX = Constants.dx[attackIndex] - dx;
                int offsetY = Constants.dy[attackIndex] - dy;

                if (currentPaint == PaintType.EMPTY) {
                    if (offsetX*offsetX + offsetY*offsetY <= 8) {
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), paintPattern[offsetX+2][offsetY+2]);
                        if (rc.canCompleteResourcePattern(nearbyMapInfos[index].getMapLocation())) {
                            rc.completeResourcePattern(nearbyMapInfos[index].getMapLocation());
                        }
                    } else {
                        rc.attack(nearbyMapInfos[attackIndex].getMapLocation(), (offsetX+dx+offsetY+dy)%2==0);
                    }
                    break;
                }
            }
        }

        if (rc.isMovementReady()) {
            nav.goTo(nearbyMapInfos[index].getMapLocation(), 0);
        }
    }

    public PatternPriority findPriority(int index, boolean[][] pattern) throws GameActionException {
        // guaranteed that this is a ruin

        boolean hasEmpty = false;
        boolean hasEnemy = false;
        boolean hasConflictingPaint = false;

        for (int neighboringIndex : Util.getIndicesForSquareSpiral(Constants.dx[index], Constants.dy[index])) {
            if (neighboringIndex == index) {
                continue;
            }
            PaintType paintType = nearbyMapInfos[neighboringIndex].getPaint();
            if (paintType == PaintType.EMPTY) {
                return PatternPriority.HIGH;
            } else if (paintType.isEnemy()) {
                hasEnemy = true;
            } else {
                int lookupX = Constants.dx[neighboringIndex] - Constants.dx[index] + 2;
                int lookupY = Constants.dy[neighboringIndex] - Constants.dy[index] + 2;
                if (paintType.isSecondary() != pattern[lookupX][lookupY]) {
                    hasConflictingPaint = true;
                }
            }
        }

        //by end of this loop, we see no empty

        if (hasEnemy) {
            // has enemy and no empty
            return PatternPriority.LOW;
        } else if (hasConflictingPaint) {
            return PatternPriority.HIGH;
        } else {
            return PatternPriority.MEDIUM;
        }
    }

//    public int findPriorityAndType(int index, UnitType defaultType) {
//        // guaranteed that this is a ruin
//
//        boolean hasEmpty = false;
//        boolean hasEnemy = false;
//
//
//
//
//        for (int neighboringIndex : Util.getIndicesForSquareSpiral(Constants.dx[index], Constants.dy[index])) {
//            if (neighboringIndex == index) {
//                continue;
//            }
//            PaintType paintType = nearbyMapInfos[neighboringIndex].getPaint();
//            if (paintType.isEnemy()) {
//                hasEnemy = true;
//            }
//            // if this is enemy paint
//
//        }
//        return -1;
//
//    }

//    public void updatePatternCenterInfo() throws GameActionException {
//        // if we see ruin with no friendly or unfriendly tower on it and that is known to be unfinished, we want to beeline for the closest one
//        // if we see ruin with no friendly or unfriendly tower on it and that may or may not be unfinished, we treat it as just a normal resource pattern
//        // - may or may not be unfinished means that the visible tiles
//        // if we see ruin with friendly tower, we ignore that
//        // if we see ruin with
//
//        if (rc.getID() == 13775) {
//            Util.logBytecode("before pattern removal");
//        }
//
//        if (patternCenter != null) {
//
//
//
//            PatternStatus patternStatus = Util.checkPattern(patternCenter.x-myLoc.x, patternCenter.y-myLoc.y, nearbyMapInfos, patternType);
//
//            if (rc.getID() == 13775) {
//                Util.logBytecode("after pattern check");
//            }
//
//            if (patternStatus == PatternStatus.INVALID) {
//                patternCenter = null;
//            } else if (patternStatus == PatternStatus.POSSIBLY_FINISHED) {
//                boolean inCompletableRange = myLoc.isWithinDistanceSquared(patternCenter, 8);
//
//                if (patternType.isTower() && inCompletableRange) {
//                    if (rc.getChips() > patternType.getUnitType().moneyCost) {
//                        if (rc.canCompleteTowerPattern(patternType.getUnitType(), patternCenter)) {
//                            rc.completeTowerPattern(patternType.getUnitType(), patternCenter);
//                            patternCenter = null;
//                        }
//                    }
//                } else if (inCompletableRange) {
//                    if (rc.canCompleteResourcePattern(patternCenter)) {
//                        rc.completeResourcePattern(patternCenter);
//                        patternCenter = null;
//                    }
//
//                }
//            } else if (patternStatus == PatternStatus.FINISHED) {
//                if (patternType.isTower() && rc.getChips() > patternType.getUnitType().moneyCost) {
//                    rc.completeTowerPattern(patternType.getUnitType(), patternCenter);
//                    patternCenter = null;
//                } else if (patternType == PatternType.RESOURCE) {
//                    rc.completeResourcePattern(patternCenter);
//                    patternCenter = null;
//                }
//            } else if (patternStatus == PatternStatus.BLOCKED_BY_NOTHING) {
//
//            } else {
//                // we give up if we blocked ourselves or we are blocked by enemy paint
//                patternCenter = null;
//            }
//        }
//
//        if (rc.getID() == 13775) {
//            Util.logBytecode("after pattern removal, before pattern update");
//        }
//
//        if (patternCenter != null) {
//            return;
//        }
//
//        UnitType unitType = getPatternUnitType();
//        PatternType intendedTowerPattern;
//
//        if (unitType == UnitType.LEVEL_ONE_PAINT_TOWER) {
//            intendedTowerPattern = PatternType.PAINT_TOWER;
//        } else {
//            intendedTowerPattern = PatternType.MONEY_TOWER;
//        }
//
//        boolean[] notPossibleCenters = new boolean[69];
//        int numPossibleCenters = 69;
//
//
//        if (rc.getID() == 13775) {
//            Util.logBytecode("before wall/ruin scanning");
//        }
//
//
//        int index_indexer = 0;
//        int index;
//        while (index_indexer < nearbyMapInfos.length && numPossibleCenters > 0) {
//            index = Constants.spiralOutwardIndices[index_indexer];
//            int dx = Constants.dx[index];
//            int dy = Constants.dy[index];
//
//            if (notPossibleCenters[index]) {
//                // this is to avoid updating too much (basically saying that if we already know this can't be a valid center, don't update the squares around it)
//                index_indexer += 1;
//                continue;
//            }
//
//            if (nearbyMapInfos[index].isWall()) {
//                for (int small_index : Util.getIndicesForSquareSpiral(dx, dy)) {
//                    if (!notPossibleCenters[small_index]) {
//                        numPossibleCenters -= 1;
//                        notPossibleCenters[small_index] = true;
//                        if (numPossibleCenters == 0) {
//                            break;
//                        }
//                    }
//                }
//                index_indexer += 3;
//            } else if (nearbyMapInfos[index].hasRuin()) {
//                for (int small_index : Util.getIndicesForSquareSpiral(dx, dy)) {
//                    if (small_index == index) {
//                        continue;
//                    }
//
//                    if (!notPossibleCenters[small_index]) {
//                        numPossibleCenters -= 1;
//                        notPossibleCenters[small_index] = true;
//                        if (numPossibleCenters == 0) {
//                            break;
//                        }
//                    }
//                }
//                index_indexer += 3;
//            }
//
//            if (numPossibleCenters == 0) {
//                break;
//            }
//            index_indexer += 1;
//
//            // if this is a wall, mark all patterns that would overlap with this as invalid
//            // i.e. squares in that 5x5 grid centered at wall
//
//            // if this is a ruin, mark all pattern that would overlap with this as invalid
//            // except for possibly that one itself
//        }
//
//        if (rc.getID() == 13775) {
//            Util.logBytecode("after wall/ruin scanning");
//        }
//
//        if (numPossibleCenters == 0) {
//            patternCenter = Util.getRandomMapLocation();
//            definiteCenter = false;
//            patternType = PatternType.RESOURCE;
//            if (rc.getID() == 13775) {
//                Util.logBytecode("after pattern removal, after pattern update");
//            }
//            return;
//        }
//
//        for (int center_index : Constants.spiralOutwardIndices) {
//            if (notPossibleCenters[center_index]) {
//                continue;
//            }
//            int dx = Constants.dx[center_index];
//            int dy = Constants.dy[center_index];
//
//            if (nearbyMapInfos[center_index].hasRuin()) {
//                RobotInfo robotAtRuin = rc.senseRobotAtLocation(nearbyMapInfos[center_index].getMapLocation());
//                if (robotAtRuin == null) {
//                    if (Util.checkPattern(dx, dy, nearbyMapInfos, intendedTowerPattern) == PatternStatus.BLOCKED_BY_NOTHING) {
//                        // this is our destination!
//                        patternCenter = nearbyMapInfos[center_index].getMapLocation();
//                        definiteCenter = true;
//                        patternType = intendedTowerPattern;
//                        return;
//                    }
//                }
//            } else if (patternCenter == null && Util.checkPattern(dx, dy, nearbyMapInfos, PatternType.RESOURCE) == PatternStatus.POSSIBLY_FINISHED) {
//                patternCenter = nearbyMapInfos[center_index].getMapLocation();
//                definiteCenter = false;
//                patternType = PatternType.RESOURCE;
//            }
//        }
//
//        if (patternCenter == null) {
//            patternCenter = Util.getRandomMapLocation();
//            definiteCenter = false;
//            patternType = PatternType.RESOURCE;
//        }
//
//        if (rc.getID() == 13775) {
//            Util.logBytecode("after pattern removal, after pattern update");
//        }
//
//    }
//
    public UnitType getPatternUnitType() throws GameActionException {
        if(nearestAlliedTowerLoc.equals(nearestAlliedPaintTowerLoc)) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        }
        else {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        }
    }

//    public boolean fillInPattern() throws GameActionException {
//
//        // in the pattern, favor the squares based on how far they are from the center (farther away is better). as tiebreaker, use distance from you
//        // if we couldn't paint in the pattern, paint closest empty tile that is on the path from me to the center of the pattern
//
//        // return whether we actually painted in the pattern (vs outside of it)
//        // TODO;
//        return false;
//    }
//
//    public boolean fillInTowardPattern() throws GameActionException {
//        // basically same as above, but we will prioritize expanding our path
//        // paint closest (to me) empty tile that is on the path from me to the center of the pattern
//        // TODO
//        return false;
//    }

    /**
     * Perform the attack, and if we have a ruin to complete, do it.
     */
    public void tryTowerPatternCompletion() throws GameActionException {

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
//                else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruinLoc)) {
//                    rc.completeTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, ruinLoc);
//                }

            }
        }
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

        if (tryingToReplenish &&
                nearestAlliedPaintTowerLoc != null
                && myLoc.distanceSquaredTo(nearestAlliedPaintTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {

            nav.goTo(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
            Util.log("Trying to replenish paint");
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