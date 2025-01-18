package goat;

import battlecode.common.*;

enum Responsibility {
    SELF_RESPONSIBLE, UNASSIGNED
}

public class Soldier extends Bunny {

    public static final int[] spiralOutwardIndices = {34,25,33,35,43,24,26,42,44,16,32,36,52,15,17,23,27,41,45,51,53,14,18,50,54,8,31,37,60,7,9,22,28,40,46,59,61,6,10,13,19,49,55,58,62,2,30,38,66,1,3,21,29,39,47,65,67,5,11,57,63,0,4,12,20,48,56,64,68};
    public static final int[] invSpiralOutwardIndices = {61,49,45,50,62,57,37,29,25,30,38,58,63,39,21,13,9,14,22,40,64,51,31,15,5,1,6,16,32,52,46,26,10,2,0,3,11,27,47,53,33,17,7,4,8,18,34,54,65,41,23,19,12,20,24,42,66,59,43,35,28,36,44,60,67,55,48,56,68};

    MapLocation potentialResourceCenterLoc = null;
    boolean[] potentialRCCornersChecked = new boolean[4];
    boolean[] invalidPotentialLocs;
    MapLocation currResourceCenterLoc = null;
    Responsibility currResourceResponsibility = Responsibility.UNASSIGNED;
    MapLocation currRuinLoc = null;
    Responsibility currRuinResponsibility = Responsibility.UNASSIGNED;
    int[] roundPaintedRuinsBySector = new int[144];

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
        invalidPotentialLocs = new boolean[3600];
        PatternUtils.soldier = this;
        PatternUtils.rc = rc;
    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies

        // 1. If trying to replenish, go do that.
        // TODO: If nearestAlliedPaintTowerLoc == null, should we explore or smth?
        if(tryingToReplenish){
            replenishLogic();
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

        // Once we have a target, run the strat.
        Direction backoutDir = rc.getLocation().directionTo(attackTarget).opposite();
        MapLocation backoutLoc = rc.getLocation().add(backoutDir).add(backoutDir).add(backoutDir);
        int distToTarget = rc.getLocation().distanceSquaredTo(attackTarget);

        // 1. If you can attack him, attack him, then back out.
        if(rc.isActionReady() && rc.canAttack(attackTarget)){
            rc.attack(attackTarget);
            nav.goToFuzzy(backoutLoc, 0, false, false);
        }
        // 2. If your action is ready but you're too far away, move towards and then attack.
        else if(rc.isActionReady()){
            nav.goToFuzzy(attackTarget, 0, true, false);
            if(rc.canAttack(attackTarget)){
                rc.attack(attackTarget);
            }
        }
        // 3. If your action is not ready but you're within attack radius, back out.
        else if(!rc.isActionReady() && distToTarget <= attackInfo.getType().actionRadiusSquared){
            nav.goToFuzzy(backoutLoc, 0, false, false);
        }
    }

    public boolean checkRuinStillValid() throws GameActionException {
        // Check if ruin is invalid.
        if((rc.canSenseLocation(currRuinLoc) && rc.senseRobotAtLocation(currRuinLoc) != null) || PatternUtils.checkEnemyPaintInConstructionArea(currRuinLoc)){
            roundPaintedRuinsBySector[comms.getSectorIndex(currRuinLoc)] = rc.getRoundNum();
            currRuinLoc = null;
            currRuinResponsibility = Responsibility.UNASSIGNED;
            return false;
        }

        // If it's still valid, check if its completed.
        UnitType currRuinType = PatternUtils.decideRuinUnitType(currRuinLoc);
        if(PatternUtils.checkRuinCompleted(currRuinLoc, currRuinType) == PatternCompleted.COMPLETE) {
            // If i'm assigned to this ruin, just stay near it.
            Util.addToIndicatorString("??");
            if(currRuinResponsibility == Responsibility.SELF_RESPONSIBLE){
                Util.addToIndicatorString("SR");
                nav.goToFuzzy(currRuinLoc, 0, false, true);
                return true;
            }
            // Otherwise, responsibility is still unknown.
            boolean responsiblityAssigned = PatternUtils.checkSomeoneResponsible(currRuinLoc);
            Util.addToIndicatorString("RA" + responsiblityAssigned);
            // Someone else took responsibility. Lets dip.
            if(responsiblityAssigned){
                roundPaintedRuinsBySector[comms.getSectorIndex(currRuinLoc)] = rc.getRoundNum();
                currRuinLoc = null;
                currRuinResponsibility = Responsibility.UNASSIGNED;
            }
            // No one's taken responsibility yet, lets try taking responsibility.
            else {
                nav.goToFuzzy(currRuinLoc, 0, true, false);
                boolean markedResponsibility = PatternUtils.markResponsibility(currRuinLoc);
                if(markedResponsibility){
                    currRuinResponsibility = Responsibility.SELF_RESPONSIBLE;
                }
                return true;
            }
        }
        return false;
    }

    public boolean checkResourceCenterStillValid() throws GameActionException {
        // Check if resource center is completed or can still be completed.
        if((rc.canSenseLocation(currResourceCenterLoc) && rc.senseMapInfo(currResourceCenterLoc).isResourcePatternCenter()) || PatternUtils.checkEnemyPaintInConstructionArea(currResourceCenterLoc)){
            currResourceCenterLoc = null;
            currResourceResponsibility = Responsibility.UNASSIGNED;
            return false;
        }

        PatternCompleted patternCompleted = PatternUtils.checkPatternCompleted(currResourceCenterLoc, rc.getResourcePattern());
        if(patternCompleted == PatternCompleted.COMPLETE) {
            // If i'm assigned to this resource pattern, just stay near it.
            if (currResourceResponsibility == Responsibility.SELF_RESPONSIBLE) {
                Util.addToIndicatorString("SR");
                nav.goToFuzzy(currResourceCenterLoc, 0, false, true);
                return true;
            }
            // Otherwise, responsibility is still unknown.
            boolean responsiblityAssigned = PatternUtils.checkSomeoneResponsible(currResourceCenterLoc);
            // Someone else took responsibility. Lets dip.
            if (responsiblityAssigned) {
//                roundPaintedRuinsBySector[comms.getSectorIndex(currRuinLoc)] = rc.getRoundNum();
                currResourceCenterLoc = null;
                currResourceResponsibility = Responsibility.UNASSIGNED;
            }
            // No one's taken responsibility yet, lets try taking responsibility.
            else {
                nav.goToFuzzy(currResourceCenterLoc, 0, true, false);
                boolean markedResponsibility = PatternUtils.markResponsibility(currResourceCenterLoc);
                if (markedResponsibility) {
                    currResourceResponsibility = Responsibility.SELF_RESPONSIBLE;
                }
                return true;
            }
        }
        return false;
    }

    public MapLocation getPotentialRCCornerLoc(int index){
        switch(index){
            case 0:
                return new MapLocation(potentialResourceCenterLoc.x - 1, potentialResourceCenterLoc.y - 1);
            case 1:
                return new MapLocation(potentialResourceCenterLoc.x + 1, potentialResourceCenterLoc.y - 1);
            case 2:
                return new MapLocation(potentialResourceCenterLoc.x - 1, potentialResourceCenterLoc.y + 1);
            case 3:
                return new MapLocation(potentialResourceCenterLoc.x + 1, potentialResourceCenterLoc.y + 1);
        }
        return null;
    }

    public MapLocation[] getPotentialRCCornerLocsToCheck(int index){
        MapLocation[] locations = new MapLocation[3];
        switch(index){
            case 0:
                locations[0] = new MapLocation(potentialResourceCenterLoc.x - 4, potentialResourceCenterLoc.y - 3);
                locations[1] = new MapLocation(potentialResourceCenterLoc.x - 4, potentialResourceCenterLoc.y - 4);
                locations[2] = new MapLocation(potentialResourceCenterLoc.x - 3, potentialResourceCenterLoc.y - 4);
                return locations;
            case 1:
                locations[0] = new MapLocation(potentialResourceCenterLoc.x + 4, potentialResourceCenterLoc.y - 3);
                locations[1] = new MapLocation(potentialResourceCenterLoc.x + 4, potentialResourceCenterLoc.y - 4);
                locations[2] = new MapLocation(potentialResourceCenterLoc.x + 3, potentialResourceCenterLoc.y - 4);
                return locations;
            case 2:
                locations[0] = new MapLocation(potentialResourceCenterLoc.x - 4, potentialResourceCenterLoc.y + 3);
                locations[1] = new MapLocation(potentialResourceCenterLoc.x - 4, potentialResourceCenterLoc.y + 4);
                locations[2] = new MapLocation(potentialResourceCenterLoc.x - 3, potentialResourceCenterLoc.y + 4);
                return locations;
            case 3:
                locations[0] = new MapLocation(potentialResourceCenterLoc.x + 4, potentialResourceCenterLoc.y + 3);
                locations[1] = new MapLocation(potentialResourceCenterLoc.x + 4, potentialResourceCenterLoc.y + 4);
                locations[2] = new MapLocation(potentialResourceCenterLoc.x + 3, potentialResourceCenterLoc.y + 4);
                return locations;
        }
        return null;
    }

    public boolean checkPotentialResourceCenterLocValid() throws GameActionException {
        // If someone's already marked it as valid, consider it valid!
        if(rc.canSenseLocation(potentialResourceCenterLoc) && rc.senseMapInfo(potentialResourceCenterLoc).getMark() == PaintType.ALLY_PRIMARY){
            PatternUtils.setPotentialRCAsRC();
            return false;
        }

        // Find the index of the closest unchecked corner.
        // Check if we've already looked at all 4 corners.
        int closestUnchecked = 0;
        int closestDist = Integer.MAX_VALUE;
        int cornersLeftToCheck = 0;
        for(int i = 0; i < potentialRCCornersChecked.length; i++){
            if(!potentialRCCornersChecked[i]){
                cornersLeftToCheck++;
                int dist = rc.getLocation().distanceSquaredTo(getPotentialRCCornerLoc(i));
                if(dist < closestDist){
                    closestDist = dist;
                    closestUnchecked = i;
                }
            }
        }
        if(cornersLeftToCheck == 0){
            PatternUtils.markPotentialRCValid();
            PatternUtils.setPotentialRCAsRC();
            return false;
        }

        // Criteria for valid
        // 1. no towers / ruins in 5x5 area.
        // 2. no uncreated ruins in vision
        // 3. no resource pattern centers in vision?? TODO: Make this smarter by allowing it if it overlaps.
        for(MapInfo info : nearbyMapInfos){
            if(info == null) continue;
            MapLocation infoLoc = info.getMapLocation();
            int abs_dx = Math.abs(infoLoc.x - potentialResourceCenterLoc.x);
            int abs_dy = Math.abs(infoLoc.y - potentialResourceCenterLoc.y);


//            overlap_x = max(5 - abs(new_dx-dx), 0)
//            overlap_y = max(5 - abs(new_dy-dy), 0)
//
//            if abs_dx <= 4 and abs_dy <= 4:
//            if (overlap_x == 1 and overlap_y in {1, 2, 5}) or (overlap_y == 1 and overlap_x in {1, 2, 5}):


            if((abs_dx <= 4 && abs_dy <= 4) && info.hasRuin() && rc.senseRobotAtLocation(info.getMapLocation()) == null){
                // Failure! Ruin there.
                Util.addToIndicatorString("IVD1");
                PatternUtils.markPotentialRCInvalid();
                return false;
            }
            if(info.isResourcePatternCenter() || info.getMark() == PaintType.ALLY_PRIMARY){
                // Failure! RC there.
                Util.addToIndicatorString("IVD2");
                PatternUtils.markPotentialRCInvalid();
                return false;
            }
        }

        MapLocation cornerLoc = getPotentialRCCornerLoc(closestUnchecked);
        if(!rc.getLocation().equals(cornerLoc)){
            nav.goToFuzzy(cornerLoc, 0, true, false);
            return true;
        }

        potentialRCCornersChecked[closestUnchecked] = true;
        // We just checked off the last corner, mark it!
        if(cornersLeftToCheck == 1){
            PatternUtils.markPotentialRCValid();
            PatternUtils.setPotentialRCAsRC();
            return false;
        }
        return false;
    }

    public void buildRuin() throws GameActionException {
        int deltaX = currRuinLoc.x - rc.getLocation().x;
        int deltaY = currRuinLoc.y - rc.getLocation().y;
        int index = Util.getMapInfoIndex(deltaX, deltaY);
        // Too far away, move towards pattern
        if(index == -1){
            Util.addToIndicatorString("MT1");
            Util.log("Moving towards: " + currRuinLoc);
            nav.goToBug(currRuinLoc, 0);
            return;
        }

        // If you haven't marked it yet, redetermine it each iteration.
        UnitType currRuinType = PatternUtils.decideRuinUnitType(currRuinLoc);

        boolean[][] pattern = rc.getTowerPattern(currRuinType);
        if(PatternUtils.checkRuinCompleted(currRuinLoc, currRuinType) == PatternCompleted.COMPLETE){
            Util.addToIndicatorString("RPY");
            nav.goToFuzzy(currRuinLoc, 0, false, true);
        }
        else {
            PatternUtils.workOnRuin(index, pattern);
            Util.addToIndicatorString("WRN");
        }

        if (rc.canCompleteTowerPattern(currRuinType, nearbyMapInfos[index].getMapLocation())) {
            rc.completeTowerPattern(currRuinType, nearbyMapInfos[index].getMapLocation());
        }
    }

    public void buildResourceCenter() throws GameActionException {
        if(!rc.canSenseLocation(currResourceCenterLoc)){
            nav.goToBug(currResourceCenterLoc, 0);
            return;
        }
        MapLocation myLoc = rc.getLocation();

        boolean[][] resourcePattern = rc.getResourcePattern();
        PatternUtils.workOnResourcePattern(currResourceCenterLoc.x - myLoc.x, currResourceCenterLoc.y - myLoc.y, resourcePattern);

        if (rc.isMovementReady()) {
            nav.goToFuzzy(currResourceCenterLoc, 0, false, false);
        }
        if (rc.canCompleteResourcePattern(currResourceCenterLoc)) {
            rc.completeResourcePattern(currResourceCenterLoc);
        }
    }

    public void buildPattern() throws GameActionException {
        // If we're already building a ruin, check if it's been completed.
        Util.log("Beginning of method: " + currRuinLoc);
        Util.addToIndicatorString("R " + currRuinLoc);
        Util.addToIndicatorString("RC " + currResourceCenterLoc);
        Util.addToIndicatorString("PRC " + potentialResourceCenterLoc);
        if(currRuinResponsibility == Responsibility.SELF_RESPONSIBLE){
            Util.addToIndicatorString("RP");
        }
        if(currResourceResponsibility == Responsibility.SELF_RESPONSIBLE){
            Util.addToIndicatorString("CP");
        }
        if(potentialResourceCenterLoc != null){
            Util.addToIndicatorString(potentialRCCornersChecked[0] + "," + potentialRCCornersChecked[1] + "," + potentialRCCornersChecked[2] + "," + potentialRCCornersChecked[3]);
        }

        // Check if ruin completed or is unable to be completed.
        if(currRuinLoc != null){
            if(checkRuinStillValid()){
                return;
            }
        }

        if(currResourceCenterLoc != null){
            if(checkResourceCenterStillValid()){
                return;
            }
        }

        if(potentialResourceCenterLoc != null){
            if(checkPotentialResourceCenterLocValid()){
                return;
            }
        }

        // If not building ruin, or resource pattern, or validating resource center location, figure out what to do next.
        if(currRuinLoc == null && currResourceCenterLoc == null && potentialResourceCenterLoc == null){
            // Find a ruin to build.
            checkForNewRuinToBuild();

            // If none found, find a resource pattern to build.
            if(currRuinLoc == null) {
                // 2650 bytecode.
                int resourceCenterIndex = PatternUtils.getPotentialResourcePatternCenterIndex(nearbyMapInfos);
                if (resourceCenterIndex != -1) {
                    potentialResourceCenterLoc = nearbyMapInfos[resourceCenterIndex].getMapLocation();
                    Util.addToIndicatorString("NRC " + currResourceCenterLoc);
                }
            }
        }

        // If we're already building a ruin, go with that.
        if(currRuinLoc != null){
            buildRuin();
        }
        // If working on a resource center, continue doing so.
        else if(currResourceCenterLoc != null){
            buildResourceCenter();
        }
        else if(potentialResourceCenterLoc == null){
            Util.log("Running default!");
            PatternUtils.runDefaultBehavior();
        }
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
            Util.addToIndicatorString("NR " + currRuinLoc);
            return;
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