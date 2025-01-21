package commbetterbenchmark;

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
    MapLocation rotationalDestination;
    boolean alreadyVisited = false;


    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
        invalidPotentialLocs = new boolean[3600];
        PatternUtils.soldier = this;
        PatternUtils.rc = rc;
        double metric = getMetric();
        rotationalDestination = Util.getRotationalReflection(spawnLoc);

        if (metric < Constants.RUIN_SEARCHING_THRESHOLD && rc.getRoundNum() < 100) {
            destination = Util.getRotationalReflection(spawnLoc);
        }

    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies

        if (myLoc.isWithinDistanceSquared(rotationalDestination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION)) {
            alreadyVisited = true;
        }


        double metric = getMetric();
        if (metric < Constants.RUIN_SEARCHING_THRESHOLD) {

            if ((nearestAlliedPaintTowerLoc != null || nearestAlliedTowerLoc != null) && (rc.getPaint() < 5)) {
                if(nearestAlliedPaintTowerLoc != null){
                    destination = nearestAlliedPaintTowerLoc;
                } else {
                    destination = nearestAlliedTowerLoc;
                }
                tryingToReplenish = true;
                Util.addToIndicatorString("REP");
            } else {

                // we are kamikazes
                if (rc.getLocation().distanceSquaredTo(destination) <= Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION) {
                    destination = Util.getRandomMapLocation();
                }
                tryReplenish();
                tryingToReplenish = false;
            }
        } else {
            updateDestinationIfNeeded();
        }

        // TODO: is this needed?
        if (!tryingToReplenish && !alreadyVisited && (rc.getNumberTowers() <= 3 && rc.getRoundNum() < 100)) {
            destination = Util.getRotationalReflection(spawnLoc);
        }



        // 1. If trying to replenish, go do that.
        // TODO: If nearestAlliedPaintTowerLoc == null, should we explore or smth?
        if(tryingToReplenish){
            replenishLogic();
        }
        else {
            RobotInfo attackInfo = getAttackTarget();
            Util.logBytecode("Get attack target");
            if(attackInfo != null) {
                runAttackLogic(attackInfo);
                Util.logBytecode("Running attack logic");
            }
            else {
                if(Constants.BLOCK_OFF_ENEMY_RUINS) {
                    blockEnemyRuins();
                }
                // 3. If not attacking, run pattern painting logic.
                if (metric < Constants.RUIN_SEARCHING_THRESHOLD) {
                    buildPatternHardExplore();
                } else if (metric < Constants.PATTERN_SEARCHING_THRESHOLD) {
                    buildPatternMediumExplore();
                } else {
                    buildPattern();
                }
                Util.logBytecode("Built pattern");
            }
        }

        MarkingUtils.tryRuinPatternCompletion();
        MarkingUtils.tryResourcePatternCompletion();

        Util.logBytecode("Tried completion");

        // 6. End of Turn Logic
        // Perform any shared cleanup or post-turn logic
        sharedEndFunction();
    }

    public void blockEnemyRuins() throws GameActionException {
        MapLocation[] nearbyRuins = rc.senseNearbyRuins(GameConstants.VISION_RADIUS_SQUARED);
        for(MapLocation nearbyRuin : nearbyRuins){
            // Only care about unfinished ruins here.
            if(rc.senseRobotAtLocation(nearbyRuin) != null){
                continue;
            }

            MapLocation closestEmpty = null;
            int closestDistance = Integer.MAX_VALUE;
            boolean isEnemyRuin = false;
            boolean isBlockedOff = false;
            for(int x = nearbyRuin.x - 2; x <= nearbyRuin.x + 2; x++) {
                for(int y = nearbyRuin.y - 2; y <= nearbyRuin.y + 2; y++) {
                    MapLocation loc = new MapLocation(x, y);
                    if(!rc.canSenseLocation(loc)){
                        continue;
                    }
                    PaintType paintType = rc.senseMapInfo(loc).getPaint();
                    if(paintType.isEnemy()) {
                        isEnemyRuin = true;
                    } else if(paintType.isAlly()){
                        isBlockedOff = true;
                    } else if(myLoc.distanceSquaredTo(loc) < closestDistance) {
                        closestEmpty = loc;
                        closestDistance = myLoc.distanceSquaredTo(loc);
                    }
                }
            }
            if(isEnemyRuin && !isBlockedOff && closestEmpty != null){
                Util.addToIndicatorString("BLK" + closestEmpty);
                if(rc.canAttack(closestEmpty)){
                    rc.attack(closestEmpty);
                } else {
                    nav.goToFuzzy(closestEmpty, 0);
                }
            }
        }
    }

    public boolean checkIfIShouldStartReplenishing() throws GameActionException {
        boolean[][] pattern = null;
        MapLocation center = null;
        if(currRuinLoc != null){
            center = currRuinLoc;
            pattern = rc.getTowerPattern(PatternUtils.decideRuinUnitType(currRuinLoc));
        }
        else if(currResourceCenterLoc != null){
            center = currResourceCenterLoc;
            pattern = rc.getResourcePattern();
        }
        if(center == null){
            return rc.getPaint() <= Constants.PAINT_THRESHOLD_TO_REPLENISH;
        }

        MapLocation myLoc = rc.getLocation();
        int needToComplete = 0;
        for(int x = center.x - 2; x <= center.x + 2; x++){
            for(int y = center.y - 2; y <= center.y + 2; y++){
                int index = Util.getMapInfoIndex(x - myLoc.x, y - myLoc.y);
                if(index == -1 || nearbyMapInfos[index] == null){
                    continue;
                }
                PaintType paintType = nearbyMapInfos[index].getPaint();
                if(!paintType.isAlly()){
                    needToComplete++;
                }
                else if(nearbyMapInfos[index].getPaint().isSecondary() != pattern[x - center.x + 2][y - center.y + 2]){
                    needToComplete++;
                }
            }
        }

        return rc.getPaint() - needToComplete * rc.getType().attackCost <= Constants.PAINT_THRESHOLD_TO_REPLENISH_WHEN_WORKING;
    }

    // Attacking logic.

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
            nav.goToFuzzy(backoutLoc, 0);
        }
        // 2. If your action is ready but you're too far away, move towards and then attack.
        else if(rc.isActionReady()){
            nav.goToFuzzy(attackTarget, 0);
            if(rc.canAttack(attackTarget)){
                rc.attack(attackTarget);
            }
        }
        // 3. If your action is not ready but you're within attack radius, back out.
        else if(!rc.isActionReady() && distToTarget <= attackInfo.getType().actionRadiusSquared){
            nav.goToFuzzy(backoutLoc, 0);
        }
    }

    // Pattern logic.

    public boolean checkRuinStillValid() throws GameActionException {
        // Check if ruin is invalid.
        if((rc.canSenseLocation(currRuinLoc) && rc.senseRobotAtLocation(currRuinLoc) != null) || PatternUtils.checkEnemyPaintInConsctructionArea(currRuinLoc)){
            roundPaintedRuinsBySector[comms.getSectorIndex(currRuinLoc)] = rc.getRoundNum();
            currRuinLoc = null;
            currRuinResponsibility = Responsibility.UNASSIGNED;
            return false;
        }

        // If it's still valid, check if its completed.
        UnitType currRuinType = PatternUtils.decideRuinUnitType(currRuinLoc);
        if(PatternUtils.checkRuinCompleted(currRuinLoc, currRuinType)) {
            // If i'm assigned to this ruin, just stay near it.
            Util.addToIndicatorString("??");
            if(currRuinResponsibility == Responsibility.SELF_RESPONSIBLE){
                Util.addToIndicatorString("SR");
                nav.goToFuzzy(currRuinLoc, 0);
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
                nav.goToFuzzy(currRuinLoc, 0);
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
        if((rc.canSenseLocation(currResourceCenterLoc) && rc.senseMapInfo(currResourceCenterLoc).isResourcePatternCenter()) || PatternUtils.checkEnemyPaintInConsctructionArea(currResourceCenterLoc)){
            currResourceCenterLoc = null;
            currResourceResponsibility = Responsibility.UNASSIGNED;
            return false;
        }

        boolean patternCompleted = PatternUtils.checkPatternCompleted(currResourceCenterLoc, rc.getResourcePattern());
        if(patternCompleted) {
            // If i'm assigned to this resource pattern, just stay near it.
            if (currResourceResponsibility == Responsibility.SELF_RESPONSIBLE) {
                Util.addToIndicatorString("SR");
                nav.goToFuzzy(currResourceCenterLoc, 0);
                return true;
            }
            // Otherwise, responsibility is still unknown.
            boolean responsiblityAssigned = PatternUtils.checkSomeoneResponsible(currResourceCenterLoc);
            // Someone else took responsibility. Lets dip.
            if (responsiblityAssigned) {
                currResourceCenterLoc = null;
                currResourceResponsibility = Responsibility.UNASSIGNED;
            }
            // No one's taken responsibility yet, lets try taking responsibility.
            else {
                nav.goToFuzzy(currResourceCenterLoc, 0);
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
            int overlap_x = Math.max(5 - abs_dx, 0);
            int overlap_y = Math.max(5 - abs_dy, 0);

            if(info.hasRuin()){
                boolean towerBuilt = rc.senseRobotAtLocation(info.getMapLocation()) != null;
                if(!towerBuilt && (abs_dx <= 4 && abs_dy <= 4)) {
                    // Failure! Ruin there.
                    Util.addToIndicatorString("IVD1");
                    PatternUtils.markPotentialRCInvalid();
                    return false;
                }
                if(towerBuilt && (abs_dx <= 2 && abs_dy <= 2)) {
                    // Failure! Ruin there.
                    Util.addToIndicatorString("IVD2");
                    PatternUtils.markPotentialRCInvalid();
                    return false;
                }
            }
            if((info.isResourcePatternCenter() || info.getMark() == PaintType.ALLY_PRIMARY) && abs_dx <= 4 && abs_dy <= 4){
                boolean valid_overlap = (overlap_x == 1 && overlap_y == 1) || (overlap_x == 1 && overlap_y == 2) || (overlap_x == 1 && overlap_y == 5) || (overlap_x == 2 && overlap_y == 1) || (overlap_x == 5 && overlap_y == 1);
                if(!valid_overlap){
                    // Failure! RC there.
                    Util.addToIndicatorString("IVD3");
                    PatternUtils.markPotentialRCInvalid();
                    return false;
                }
            }
        }

        MapLocation cornerLoc = getPotentialRCCornerLoc(closestUnchecked);
        if(!rc.getLocation().equals(cornerLoc)){
            nav.goToFuzzy(cornerLoc, 0);
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

    public void buildRuin(boolean paintEmpty) throws GameActionException {
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

        // Redetermine it each iteration.
        UnitType currRuinType = PatternUtils.decideRuinUnitType(currRuinLoc);

        boolean[][] pattern = rc.getTowerPattern(currRuinType);
        // TODO: Srikar, do you want the last argument to be true or false here?
        PatternUtils.workOnRuin(index, pattern, paintEmpty);
        Util.addToIndicatorString("RT" + TowerType.from(currRuinType).toString());
        if (rc.canCompleteTowerPattern(currRuinType, nearbyMapInfos[index].getMapLocation())) {
            rc.completeTowerPattern(currRuinType, nearbyMapInfos[index].getMapLocation());
        }
    }

    public void buildResourceCenter(boolean paintEmpty) throws GameActionException {
        if(!rc.canSenseLocation(currResourceCenterLoc)){
            nav.goToBug(currResourceCenterLoc, 0);
            return;
        }
        MapLocation myLoc = rc.getLocation();

        boolean[][] resourcePattern = rc.getResourcePattern();
        PatternUtils.workOnResourcePattern(currResourceCenterLoc.x - myLoc.x, currResourceCenterLoc.y - myLoc.y, resourcePattern, paintEmpty);

        nav.goToFuzzy(currResourceCenterLoc, 0);
        if (rc.canCompleteResourcePattern(currResourceCenterLoc)) {
            rc.completeResourcePattern(currResourceCenterLoc);
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

    public void buildPatternHardExplore() throws GameActionException {
        // If we're already building a ruin, check if it's been completed.
        Util.log("Beginning of hard: " + currRuinLoc);
        Util.addToIndicatorString("R " + currRuinLoc);
        if(currRuinResponsibility == Responsibility.SELF_RESPONSIBLE){
            Util.addToIndicatorString("RP");
        }

        // Check if ruin completed or is unable to be completed.
        if(currRuinLoc != null){
            if(checkRuinStillValid()){
                return;
            }
            Util.logBytecode("Checked ruin valid");
        }

        // If not building ruin, or resource pattern, or validating resource center location, figure out what to do next.
        if(currRuinLoc == null){
            // Find a ruin to build.
            checkForNewRuinToBuild();

            Util.logBytecode("Check new ruin to build");
        }

        // If we're already building a ruin, go with that.
        if(currRuinLoc != null){
            buildRuin(false);
            Util.logBytecode("Built ruin");
        } else {
            Util.log("Running default!");
            PatternUtils.runDefaultBehavior(false);
            Util.logBytecode("Default behavior");
        }
    }

    public void buildPatternMediumExplore() throws GameActionException {
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
            Util.logBytecode("Checked ruin valid");
        }

        if(currResourceCenterLoc != null){
            if(checkResourceCenterStillValid()){
                return;
            }
            Util.logBytecode("Checked resource valid");
        }

        if(potentialResourceCenterLoc != null){
            if(checkPotentialResourceCenterLocValid()){
                return;
            }
            Util.logBytecode("Checked potential valid");
        }

        // If not building ruin, or resource pattern, or validating resource center location, figure out what to do next.
        if(currRuinLoc == null && currResourceCenterLoc == null && potentialResourceCenterLoc == null){
            // Find a ruin to build.
            checkForNewRuinToBuild();

            Util.logBytecode("Check new ruin to build");

            // If none found, find a resource pattern to build.
            if(currRuinLoc == null && shouldSearchForResourceCenter()) {
                // 2650 bytecode.
                int resourceCenterIndex = PatternUtils.getPotentialResourcePatternCenterIndex(nearbyMapInfos);
                if (resourceCenterIndex != -1) {
                    potentialResourceCenterLoc = nearbyMapInfos[resourceCenterIndex].getMapLocation();
                    Util.addToIndicatorString("NRC " + currResourceCenterLoc);
                }
                Util.logBytecode("Got optential resource pattern");
            }
        }

        // If we're already building a ruin, go with that.
        if(currRuinLoc != null){
            buildRuin(false);
            Util.logBytecode("Built ruin");
        }
        // If working on a resource center, continue doing so.
        else if(currResourceCenterLoc != null){
            buildResourceCenter(false);
            Util.logBytecode("Worked on RC");
        }
        else if(potentialResourceCenterLoc != null){
            PatternUtils.workOnPotentialResourceCenter(false);
            Util.logBytecode("Worked on potential");
        } else {
            Util.log("Running default!");
            PatternUtils.runDefaultBehavior(false);
            Util.logBytecode("Default behavior");
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
            Util.logBytecode("Checked ruin valid");
        }

        if(currResourceCenterLoc != null){
            if(checkResourceCenterStillValid()){
                return;
            }
            Util.logBytecode("Checked resource valid");
        }

        if(potentialResourceCenterLoc != null){
            if(checkPotentialResourceCenterLocValid()){
                return;
            }
            Util.logBytecode("Checked potential valid");
        }

        // If not building ruin, or resource pattern, or validating resource center location, figure out what to do next.
        if(currRuinLoc == null && currResourceCenterLoc == null && potentialResourceCenterLoc == null){
            // Find a ruin to build.
            checkForNewRuinToBuild();

            Util.logBytecode("Check new ruin to build");

            // If none found, find a resource pattern to build.
            if(currRuinLoc == null && shouldSearchForResourceCenter()) {
                // 2650 bytecode.
                int resourceCenterIndex = PatternUtils.getPotentialResourcePatternCenterIndex(nearbyMapInfos);
                if (resourceCenterIndex != -1) {
                    potentialResourceCenterLoc = nearbyMapInfos[resourceCenterIndex].getMapLocation();
                    Util.addToIndicatorString("NRC " + currResourceCenterLoc);
                }
                Util.logBytecode("Got optential resource pattern");
            }
        }

        // If we're already building a ruin, go with that.
        if(currRuinLoc != null){
            buildRuin(true);
            Util.logBytecode("Built ruin");
        }
        // If working on a resource center, continue doing so.
        else if(currResourceCenterLoc != null){
            buildResourceCenter(true);
            Util.logBytecode("Worked on RC");
        }
        else if(potentialResourceCenterLoc != null){
            PatternUtils.workOnPotentialResourceCenter(true);
            Util.logBytecode("Worked on potential");
        } else {
            Util.log("Running default!");
            PatternUtils.runDefaultBehavior(true);
            Util.logBytecode("Default behavior");
        }
    }

    public boolean shouldSearchForResourceCenter() {
        return rc.getRoundNum() > 100;
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