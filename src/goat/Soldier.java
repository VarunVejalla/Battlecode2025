package goat;

import battlecode.common.*;

public class Soldier extends Bunny {

    public static final int[] spiralOutwardIndices = {34,25,33,35,43,24,26,42,44,16,32,36,52,15,17,23,27,41,45,51,53,14,18,50,54,8,31,37,60,7,9,22,28,40,46,59,61,6,10,13,19,49,55,58,62,2,30,38,66,1,3,21,29,39,47,65,67,5,11,57,63,0,4,12,20,48,56,64,68};
    public static final int[] shift_dx = {-4,-4,-4,-4,-4,-3,-3,-3,-3,-3,-3,-3,-2,-2,-2,-2,-2,-2,-2,-2,-2,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,4,4,4,4,4};
    public static final int[] shift_dy = {-2,-1,0,1,2,-3,-2,-1,0,1,2,3,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-3,-2,-1,0,1,2,3,-2,-1,0,1,2};

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
                nav.goTo(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
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
        int highPriorityRuinIndex = -1;
        int mediumPriorityRuinIndex = -1;

        UnitType intendedType = PatternUtils.getPatternUnitType();
        boolean[][] pattern = rc.getTowerPattern(intendedType);

        // Spirals outward up to vision radius.
        // 1500 bytecode.
        for(int index : spiralOutwardIndices) {
            if (nearbyMapInfos[index] == null || !nearbyMapInfos[index].hasRuin() || rc.canSenseRobotAtLocation(nearbyMapInfos[index].getMapLocation())) {
                continue;
            }
            PatternPriority priority = PatternUtils.findPriority(index, pattern);
            if (priority == PatternPriority.HIGH) {
                highPriorityRuinIndex = index;
                break;
            } else if (mediumPriorityRuinIndex == -1 && priority == PatternPriority.MEDIUM) {
                mediumPriorityRuinIndex = index;
            }
        }

        if (highPriorityRuinIndex != -1) {
            PatternUtils.workOnRuin(highPriorityRuinIndex, pattern);
            if (rc.canCompleteTowerPattern(intendedType, nearbyMapInfos[highPriorityRuinIndex].getMapLocation())) {
                rc.completeTowerPattern(intendedType, nearbyMapInfos[highPriorityRuinIndex].getMapLocation());
            }
            return;
        }

        // 2650 bytecode.
        int resourceCenterIndex = PatternUtils.getPotentialResourcePatternCenterIndex(nearbyMapInfos);

        if (resourceCenterIndex != -1) {
            // 300 bytecode.
            pattern = rc.getResourcePattern();
            PatternUtils.workOnResourcePattern(shift_dx[resourceCenterIndex], shift_dy[resourceCenterIndex], pattern);

            if (rc.isMovementReady()) {
                nav.goTo(nearbyMapInfos[resourceCenterIndex].getMapLocation(), 0);
            }
            if (rc.canCompleteResourcePattern(nearbyMapInfos[resourceCenterIndex].getMapLocation())) {
                rc.completeResourcePattern(nearbyMapInfos[resourceCenterIndex].getMapLocation());
            }
            return;
        }
        if (mediumPriorityRuinIndex != -1) {
            PatternUtils.workOnRuin(mediumPriorityRuinIndex, pattern);
            if (rc.canCompleteTowerPattern(intendedType, nearbyMapInfos[mediumPriorityRuinIndex].getMapLocation())) {
                rc.completeTowerPattern(intendedType, nearbyMapInfos[mediumPriorityRuinIndex].getMapLocation());
            }
            return;
        }
        PatternUtils.runDefaultBehavior();
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