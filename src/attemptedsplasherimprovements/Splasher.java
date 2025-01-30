package attemptedsplasherimprovements;

import battlecode.common.*;

enum SplasherMode {
    CLAIMING, BASE
}

public class Splasher extends Bunny {

    boolean[][] offlimits;
    SplasherMode mode;
    boolean ruinNearby;
    int[] allowedAttacks;
    int[] lookupResourceCenters;


    public Splasher(RobotController rc) throws GameActionException {
        super(rc);
        Util.logBytecode("Start of splasher constructor");
        offlimits = new boolean[rc.getMapWidth()][rc.getMapHeight()];
        SplasherUtils.rc = rc;
        SplasherUtils.splasher = this;
    }

    public void run() throws GameActionException {
        super.run(); // Call shared logic for all bunnies

        if (getMetric() < Constants.RUIN_SEARCHING_THRESHOLD) {
            mode = SplasherMode.CLAIMING;
        } else {
            mode = SplasherMode.BASE;
        }

        updateDestinationIfNeeded();

        replenishLogic();

        Util.logBytecode("Ran super");

        setAllowedAttacks();

//        if (mode == SplasherMode.BASE) {
//            updateOffLimits();
//        }

        Util.logBytecode("Updated off limits");

//        if (rc.getPaint() >= UnitType.SPLASHER.attackCost) {
//            if (mode == SplasherMode.CLAIMING) {
//                claimingLogic();
//            } else {
                splashAttack();
//            }
//        }

        Util.logBytecode("After attacking");

        // 1. Replenish or Perform Splash Attack
//            splashAttack();
//            Util.logBytecode("After first attack");
        // 2. Movement Logic
//            MapLocation currLoc = rc.getLocation();
        if (rc.isMovementReady()) {
            moveLogic();
            Util.logBytecode("Move logic");
        }

        MarkingUtils.tryRuinPatternCompletion();
        MarkingUtils.tryResourcePatternCompletion();

        tryReplenish();

        Util.logBytecode("Tried completion");
    }

    public void setAllowedAttacks()  throws GameActionException {
        allowedAttacks = new int[26];
        lookupResourceCenters = new int[169];
        SplasherUtils.initializeInfo(allowedAttacks, lookupResourceCenters, nearbyMapInfos);

    }


    public boolean checkIfIShouldStartReplenishing() throws GameActionException {
        return rc.getPaint() <= Constants.PAINT_THRESHOLD_TO_REPLENISH;
    }

    public void claimingLogic() throws GameActionException {
        // if we see empty ruin:
        //      if there is enemy paint that we can get rid of, splash it
        //      if there is no enemy paint that we can get rid of, and there is no friendly paint at all (on the whole pattern), splash it
        // if we see enemy ruin:
        //      if there is
        // with any enemy paint around it, splash it (whether or not it has friendly paint)
        // if we see empty ruin with no enemy paint and no friendly paint around it, splash it
        // if we see enemy tower with any enemy paint around it, splash it
        // if we see enemy tower with no enemy paint and no friendly paint, splash it
        // if we see friendly tower with any enemy paint around it, splash it

        // if ruin has enemy paint, splash it
        // if ruin does not have enemy paint and ruin is either empty or enemy, splash it

        MapLocation myLoc = rc.getLocation();

        for(MapInfo info : nearbyMapInfos) {
            if(info == null){
                continue;
            }
            MapLocation loc = info.getMapLocation();
            if(info.hasRuin()){
                boolean enemyPaintPresent = false;
                boolean friendlyPaintPresent = false;
                for(int x = loc.x - 2; x <= loc.x + 2; x++) {
                    for(int y = loc.y - 2; y <= loc.y + 2; y++) {
                        int deltaX = loc.x - myLoc.x;
                        int deltaY = loc.y - myLoc.y;


                        if (deltaX*deltaX + deltaY*deltaY <= 10) {
                            // we could repaint these even if they are enemy


                            int index = Util.getMapInfoIndex(myLoc.x - x, myLoc.y - y);
                            if(index != -1 && nearbyMapInfos[index] != null) {
                                PaintType currPaint = nearbyMapInfos[index].getPaint();
                                if (currPaint.isEnemy()) {
                                    enemyPaintPresent = true;
                                } else if (currPaint.isAlly()) {
                                    friendlyPaintPresent = true;
                                }
                            }
                        } else {


                            int index = Util.getMapInfoIndex(myLoc.x - x, myLoc.y - y);
                            if(index != -1 && nearbyMapInfos[index] != null) {
                                PaintType currPaint = nearbyMapInfos[index].getPaint();
                                if (currPaint.isAlly()) {
                                    friendlyPaintPresent = true;
                                }
                            }
                        }

                    }
                }

                MapLocation target = null;
                if (enemyPaintPresent) {
                    // just attack it right now and return
                    target = info.getMapLocation();
                } else if (!friendlyPaintPresent) {
                    RobotInfo ruinRobot = rc.senseRobotAtLocation(info.getMapLocation());
                    if (ruinRobot == null || ruinRobot.getTeam() == oppTeam) {
                        target = info.getMapLocation();
                    }
                }

                if (target != null) {
                    Direction dir = myLoc.directionTo(target);
                    if (dir.dx == 0 || dir.dy == 0) {
                        if (rc.canAttack(myLoc.add(dir).add(dir))) {
                            rc.attack(myLoc.add(dir).add(dir));
                        } else if (rc.canAttack(myLoc.add(dir))) {
                            rc.attack(myLoc.add(dir));
                        }
                    }
                    return;
                }
            }
        }

    }

    // 2k
    // TODO: Fix this for painting on regions with ruins
    public void updateOffLimits() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        for(MapInfo info : nearbyMapInfos) {
            if(info == null){
                continue;
            }
            MapLocation loc = info.getMapLocation();
            if(info.getMark() == PaintType.ALLY_PRIMARY || info.isResourcePatternCenter() || (info.hasRuin() && !rc.canSenseRobotAtLocation(loc))){
                Util.logBytecode("Updating new location");
                // Don't touch 5x5 square if no enemy paint in area.
                boolean enemyPaintPresent = false;
                for(int x = loc.x - 2; x <= loc.x + 2; x++) {
                    for(int y = loc.y - 2; y <= loc.y + 2; y++) {
                        int index = Util.getMapInfoIndex(myLoc.x - x, myLoc.y - y);
                        if(index != -1 && nearbyMapInfos[index] != null && nearbyMapInfos[index].getPaint().isEnemy()){
                            enemyPaintPresent = true;
                        }
                    }
                }

                if(!enemyPaintPresent){
                    for(int dx = -4; dx <= 4; dx++) {
                        for(int dy = -4; dy <= 4; dy++) {
                            if(dx*dx + dy*dy > 20){
                                continue;
                            }
                            int x = loc.x + dx;
                            int y = loc.y + dy;
                            if(x >= 0 && y >= 0 && x < offlimits.length && y < offlimits[0].length) {
                                offlimits[x][y] = true;
                            }
                        }
                    }
                }
                Util.logBytecode("Updated new location");
            }
        }
    }

    /**
     * Perform a splash attack, prioritizing clusters of enemy paint or robots.
     */
    // ~4.5k bytecode
    public void splashAttack() throws GameActionException {
        MapInfo[] actionableTiles = rc.senseNearbyMapInfos(UnitType.SPLASHER.actionRadiusSquared);

        MapLocation bestTarget = null;
        int bestScore = 0;

        for (MapInfo tile : actionableTiles) {

            MapLocation targetLocation = tile.getMapLocation();
            if (!rc.canAttack(targetLocation)) continue;

            if (!SplasherUtils.isValidAttack(targetLocation.x-myLoc.x, targetLocation.y-myLoc.y, true, allowedAttacks, lookupResourceCenters, nearbyMapInfos)) {
                continue;
            }

//            // Check if this square fucks w/ any ruin or resource center builds.
//            if(offlimits[targetLocation.x][targetLocation.y]){
//                continue;
//            }

            int[] adjacencyCounts = calculateAdjacencyCounts(targetLocation);

            int emptyCount = adjacencyCounts[0];
            int enemyCount = adjacencyCounts[1];

            int score = 0;
            if(emptyCount + enemyCount > 4){
                score = 10 * emptyCount + 50 * enemyCount;
            }

//            // Unit at location.
            // TODO: Update this to check for adjacency to enemy tower.
            RobotInfo rob= rc.senseRobotAtLocation(targetLocation);
            if(rob != null && rob.getType().isTowerType() && rob.getTeam() == oppTeam) {
                score += 10000;
            }

            if (score > bestScore) {
                bestScore = score;
                bestTarget = tile.getMapLocation();
            }
        }

        if (bestTarget != null && rc.isActionReady()) {
            rc.attack(bestTarget, true);
            //Util.log("Splasher attacked: " + bestTarget);
        }
    }

    /**
     * Calculate adjacency to empty and enemy-painted tiles.
     */
    public int[] calculateAdjacencyCounts(MapLocation loc) throws GameActionException {
        int index;
        int emptyCount = 0;
        int enemyCount = 0;

        int deltaX = loc.x - rc.getLocation().x;
        int deltaY = loc.y - rc.getLocation().y;

        index = Util.getMapInfoIndex(deltaX - 2, deltaY);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX - 1, deltaY - 1);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;

                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX - 1, deltaY);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;

                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX - 1, deltaY + 1);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;

                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX, deltaY - 2);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX, deltaY - 1);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;

                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX, deltaY);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;

                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;

            }
        }

        index = Util.getMapInfoIndex(deltaX, deltaY + 1);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;

                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX, deltaY + 2);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX + 1, deltaY - 1);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;

                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX + 1, deltaY);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;

                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX + 1, deltaY + 1);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;

                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(deltaX + 2, deltaY);
        if(index != -1 && nearbyMapInfos[index] != null && !nearbyMapInfos[index].isWall()) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;
            }
        }

        int[] ret = new int[2];
        ret[0] = emptyCount;
        ret[1] = enemyCount;
        return ret;
    }

    /**
     * Returns a score evaluating how favorable it would be for this robot to move to this sector.
     */
    public int evaluateSector(int encodedSector) {
        ScanResult sr = Util.decodeSector(encodedSector);
        int tileScore = 0;
        // Move towards areas with high enemy paint.
//        if(sr.enemyPaintLevel >= 2) {
//            tileScore+= 50;
//        }

//        // Tiebreak by going to unexperienced places.
//        if((encodedSector & 1) == 0) {
//            tileScore++;
//        }

        return tileScore;
    }

    /**
     * Movement logic for the splasher:
     * - Move toward clusters of enemy-painted tiles or robots.
     * - If no meaningful targets are found, move to the destination.
     */
    public void moveLogic() throws GameActionException {
        myLoc = rc.getLocation();
        adjustDestination();
        nav.goToBug(destination, Constants.MIN_DIST_TO_SATISFY_RANDOM_DESTINATION);
    }

}
