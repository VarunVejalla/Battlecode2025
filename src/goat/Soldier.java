package goat;

import battlecode.common.*;

public class Soldier extends Bunny {

    public static final int[] spiralOutwardIndices = {34,25,33,35,43,24,26,42,44,16,32,36,52,15,17,23,27,41,45,51,53,14,18,50,54,8,31,37,60,7,9,22,28,40,46,59,61,6,10,13,19,49,55,58,62,2,30,38,66,1,3,21,29,39,47,65,67,5,11,57,63,0,4,12,20,48,56,64,68};
    public static final int[] shift_dx = {-4,-4,-4,-4,-4,-3,-3,-3,-3,-3,-3,-3,-2,-2,-2,-2,-2,-2,-2,-2,-2,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,4,4,4,4,4};
    public static final int[] shift_dy = {-2,-1,0,1,2,-3,-2,-1,0,1,2,3,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-3,-2,-1,0,1,2,3,-2,-1,0,1,2};

    MapLocation currResourceCenterLoc = null;
    MapLocation currRuinLoc = null;
    UnitType currRuinType = null;
    boolean currRuinMarked = false;
    boolean currRuinMyResponsibility = false;
    int[] roundPaintedRuinsBySector = new int[144];

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
        PatternUtils.soldier = this;
        PatternUtils.rc = rc;
    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
        updateDestinationIfNeeded();

        // 1. If trying to replenish, go do that.
        // TODO: If nearestAlliedPaintTowerLoc == null, should we explore or smth?
        if(tryingToReplenish && nearestAlliedPaintTowerLoc != null){
            tryReplenish();
            // TODO: Don't stay adjacent to other robots.
            if (myLoc.distanceSquaredTo(nearestAlliedPaintTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {
                nav.goToBug(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
            }
        }
        else {
            RobotInfo attackInfo = getAttackTarget();
            if(attackInfo != null) {
                runAttackLogic(attackInfo);
            }
            else {
                // 3. If not attacking, run pattern painting logic.
                buildPattern();
            }
        }

        MarkingUtils.tryRuinPatternCompletion();
        MarkingUtils.tryResourcePatternCompletion();

        // 6. End of Turn Logic
        // Perform any shared cleanup or post-turn logic
        sharedEndFunction();
    }

    public RobotInfo getAttackTarget() throws GameActionException {
        // Get location of tower to attack.
        RobotInfo attackInfo = null;
        int minDist = Integer.MAX_VALUE;
        for(RobotInfo info : nearbyOpponents){
            if(!Util.isTower(info.getType())){
                continue;
            }
            int dist = rc.getLocation().distanceSquaredTo(info.getLocation());
            if(dist < minDist){
                minDist = dist;
                attackInfo = info;
            }
        }

        return attackInfo;
    }

    public void runAttackLogic(RobotInfo attackInfo) throws GameActionException {
        MapLocation attackTarget = attackInfo.getLocation();
        Util.log("Running attack strat on target at " + attackTarget);

        // Once we have a target, run the strat.
        Direction backoutDir = rc.getLocation().directionTo(attackTarget).opposite();
        MapLocation backoutLoc = rc.getLocation().add(backoutDir).add(backoutDir).add(backoutDir);
        int distToTarget = rc.getLocation().distanceSquaredTo(attackTarget);

        // 1. If you can attack him, attack him, then back out.
        if(rc.isActionReady() && rc.canAttack(attackTarget)){
            Util.log("Running attack and back out");
            rc.attack(attackTarget);
            nav.goToFuzzy(backoutLoc, 0);
        }
        // 2. If your action is ready but you're too far away, move towards and then attack.
        else if(rc.isActionReady()){
            Util.log("Running push");
            nav.goToFuzzy(attackTarget, 0);
            if(rc.canAttack(attackTarget)){
                rc.attack(attackTarget);
            }
        }
        // 3. If your action is not ready but you're within attack radius, back out.
        else if(!rc.isActionReady() && distToTarget <= attackInfo.getType().actionRadiusSquared){
            Util.log("Pulling out");
            nav.goToFuzzy(backoutLoc, 0);
        }
    }

    public void buildPattern() throws GameActionException {
        // If we're already building a ruin, check if it's been completed.
        Util.log("Beginning of method: " + currRuinLoc + ", " + currRuinType);
        Util.addToIndicatorString("R " + currRuinLoc);
        Util.addToIndicatorString("RC " + currResourceCenterLoc);
        boolean[][] resourcePattern = rc.getResourcePattern();

        // Find the best potential resource pattern center index.
        int resourceCenterIndex = PatternUtils.getPotentialResourcePatternCenterIndex(nearbyMapInfos);

        // Check if resource center complete or not completable.
        if(currResourceCenterLoc != null){
            // Check if there's a better resource center to build at.
            if (resourceCenterIndex != -1) {
                currResourceCenterLoc = nearbyMapInfos[resourceCenterIndex].getMapLocation();
                Util.addToIndicatorString("NRC " + currResourceCenterLoc);
            } else if(rc.canSenseLocation(currResourceCenterLoc)) {
                // If the resource center loc is done or invalid, skip it.
                currResourceCenterLoc = null;
            }
        }

        if(currResourceCenterLoc != null){
            if(PatternUtils.checkPatternCompleted(currResourceCenterLoc, resourcePattern) || PatternUtils.checkEnemyPaintInConsctructionArea(currResourceCenterLoc)){
                currResourceCenterLoc = null;
            }
        }

        // Check if ruin completed or is unable to be completed.
        if(currRuinLoc != null){
            if((rc.canSenseLocation(currRuinLoc) && rc.senseRobotAtLocation(currRuinLoc) != null) || PatternUtils.checkEnemyPaintInConsctructionArea(currRuinLoc)){
                roundPaintedRuinsBySector[comms.getSectorIndex(currRuinLoc)] = rc.getRoundNum();
                currRuinLoc = null;
                currRuinType = null;
                currRuinMarked = false;
                currRuinMyResponsibility = false;
            } else if(currRuinType != null && PatternUtils.checkRuinCompleted(currRuinLoc, currRuinType) && !currRuinMyResponsibility) {
                // If i'm not the closest guy to the ruin, and the ruin is completed, dip.
                boolean amClosest = true;
                int myDist = rc.getLocation().distanceSquaredTo(currRuinLoc);
                for(RobotInfo info : nearbyFriendlies){
                    if(info.getLocation().distanceSquaredTo(currRuinLoc) < myDist){
                        Util.log("I'm the not closest! Robot that's closer: " + info.getID() + ", " + info.getLocation());
                        amClosest = false;
                        break;
                    }
                    else if(info.getLocation().distanceSquaredTo(currRuinLoc) == myDist && rc.getID() > info.getID()){
                        // Tiebreaker is robot id. Lower id stays.
                        Util.log("I'm the not closest! Tiebreaker with robot: " + info.getID());
                        amClosest = false;
                        break;
                    }
                }
                if(amClosest) {
                    Util.log("I'm the closest! Staying behind.");
                    currRuinMyResponsibility = true;
                }
                else {
                    roundPaintedRuinsBySector[comms.getSectorIndex(currRuinLoc)] = rc.getRoundNum();
                    Direction oppDir = rc.getLocation().directionTo(currRuinLoc);
                    currRuinLoc = null;
                    currRuinType = null;
                    currRuinMarked = false;
                    currRuinMyResponsibility = false;
                    // Move away from the center.
                    nav.goToFuzzy(rc.getLocation().add(oppDir).add(oppDir).add(oppDir), 0);
                }
            }
        }

        // If not building ruin, or resource pattern, figure out what to do next.
        if(currRuinLoc == null && currResourceCenterLoc == null){
            // Find a ruin to build.
            checkForNewRuinToBuild();
            if(currRuinLoc != null) Util.addToIndicatorString("NR " + currRuinLoc);

            // If none found, find a resource pattern to build.
            if(currRuinLoc == null) {
                // 2650 bytecode.
                if (resourceCenterIndex != -1) {
                    currResourceCenterLoc = nearbyMapInfos[resourceCenterIndex].getMapLocation();
                    if(currRuinLoc != null) Util.addToIndicatorString("NRC " + currResourceCenterLoc);
                }
            }
        }

        // If we're already building a ruin, go with that.
        if(currRuinLoc != null){
            int deltaX = currRuinLoc.x - rc.getLocation().x;
            int deltaY = currRuinLoc.y - rc.getLocation().y;
            int index = Util.getMapInfoIndex(deltaX, deltaY);
            // Too far away, move towards pattern
            if(index == -1){
                Util.log("Moving towards: " + currRuinLoc + ", " + currRuinType);
                nav.goToFuzzy(currRuinLoc, 0);
                return;
            }

            // If you haven't marked it yet, redetermine it each iteration.
            if(currRuinType != null && !currRuinMarked){
                currRuinType = null;
            }

            if(currRuinType == null){
                if(!PatternUtils.closeEnoughToDetermineRuinType(currRuinLoc)){
                    Util.log("Moving towards 2: " + currRuinLoc + ", " + currRuinType);
                    nav.goToFuzzy(currRuinLoc, 0);
                    return;
                }
                currRuinType = PatternUtils.getRuinUnitType(currRuinLoc);
            }

            if(!currRuinMarked){
                boolean marked = PatternUtils.markRuinUnitType(currRuinLoc, currRuinType);
                currRuinMarked = marked;
                // Get closer to mark.
                if(!currRuinMarked){
                    nav.goToFuzzy(currRuinLoc, 0);
                    return;
                }
            }

            boolean[][] pattern = rc.getTowerPattern(currRuinType);
            PatternUtils.workOnRuin(index, pattern);
            if (rc.canCompleteTowerPattern(currRuinType, nearbyMapInfos[index].getMapLocation())) {
                rc.completeTowerPattern(currRuinType, nearbyMapInfos[index].getMapLocation());
            }
            return;
        }

        // If working on a resource center, continue doing so.
        else if(currResourceCenterLoc != null){
            if(!rc.canSenseLocation(currResourceCenterLoc)){
                nav.goToBug(currResourceCenterLoc, 0);
                return;
            }
            MapLocation myLoc = rc.getLocation();
            PatternUtils.workOnResourcePattern(currResourceCenterLoc.x - myLoc.x, currResourceCenterLoc.y - myLoc.y, resourcePattern);

            if (rc.isMovementReady()) {
                nav.goToFuzzy(currResourceCenterLoc, 0);
            }
            if (rc.canCompleteResourcePattern(currResourceCenterLoc)) {
                rc.completeResourcePattern(currResourceCenterLoc);
            }
            return;
        }

        Util.log("Running default!");

        PatternUtils.runDefaultBehavior();
    }

    public void checkForNewRuinToBuild() throws GameActionException {
        // Spirals outward up to vision radius.
        // 1500 bytecode.
        for(int index : spiralOutwardIndices) {
            if (nearbyMapInfos[index] == null || !nearbyMapInfos[index].hasRuin() || rc.canSenseRobotAtLocation(nearbyMapInfos[index].getMapLocation())) {
                continue;
            }

            MapLocation ruinLoc = nearbyMapInfos[index].getMapLocation();
            int sectorIdx = comms.getSectorIndex(ruinLoc);
            if(roundPaintedRuinsBySector[sectorIdx] != 0 && roundPaintedRuinsBySector[sectorIdx] + Constants.ROUNDS_TO_IGNORE_PAINTED_RUINS > rc.getRoundNum()){
                continue;
            }

            currRuinLoc = ruinLoc;
        }
    }

    /**
     * Returns a score evaluating how favorable it would be for this robot to move to this sector.
     */
    public int evaluateSector(int encodedSector) {
        ScanResult sr = comms.decodeSector(encodedSector);
        int sectorScore = 0;

        // If the region is mostly empty and there's an unbuilt ruin, go there first.
        if(sr.emptyPaintLevel >= 2 && sr.towerType == 1) {
            sectorScore += 1000;
        }

        // Prefer regions with 5-12 empty cells.
        if(sr.emptyPaintLevel == 2) {
            sectorScore += 500;
        }

        // Avoid regions with enemy cells.
        if(sr.enemyPaintLevel >= 1) {
            sectorScore -= 500;
        }

        return sectorScore;

    }
}