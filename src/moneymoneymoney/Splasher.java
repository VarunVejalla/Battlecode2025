package moneymoneymoney;

import battlecode.common.*;

public class Splasher extends Bunny {

    boolean[][] offlimits;

    public Splasher(RobotController rc) throws GameActionException {
        super(rc);
        SplasherUtils.rc = rc;
        SplasherUtils.splasher = this;
        offlimits = new boolean[rc.getMapWidth()][rc.getMapHeight()];
    }

    public void run() throws GameActionException {
        super.run(); // Call shared logic for all bunnies

        updateOffLimits();
        splashAttack();

        updateDestinationIfNeeded();

        // 1. Replenish or Perform Splash Attack
        if (tryingToReplenish) {
            replenishLogic();
        } else {
            // 2. Movement Logic
            if (canMove()) {
                // 2k bytecode.
                moveLogic();
            }
        }

        MarkingUtils.tryRuinPatternCompletion();
        MarkingUtils.tryResourcePatternCompletion();
    }

    public boolean checkIfIShouldStartReplenishing() throws GameActionException {
        return rc.getPaint() <= Constants.PAINT_THRESHOLD_TO_REPLENISH;
    }

    // 2k
    // TODO: Fix this for painting on regions with ruins
    public void updateOffLimits() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        for(MapInfo info : nearbyMapInfos) {
            if(info == null){
                continue;
            }
            // Already handled this one.
            if(info.getMark() == PaintType.ALLY_PRIMARY || (info.isResourcePatternCenter() && info.getPaint().isAlly()) || (info.hasRuin() && rc.senseRobotAtLocation(info.getMapLocation()) == null)){
                MapLocation loc = info.getMapLocation();
                if(offlimits[loc.x][loc.y]) {
                    continue;
                }
                Util.log("Updating new location");
                // Don't touch 5x5 square if no enemy paint in area.
                for(int x = loc.x - 2; x <= loc.x + 2; x++) {
                    for(int y = loc.y - 2; y <= loc.y + 2; y++) {
                        int index = Util.getMapInfoIndex(x - myLoc.x, y - myLoc.y);
                        if(index != -1 && nearbyMapInfos[index] != null && nearbyMapInfos[index].getPaint().isEnemy()){
                            for(int x2 = loc.x - 2; x2 <= loc.x + 2; x2++) {
                                for(int y2 = loc.y - 2; y2 <= loc.y + 2; y2++) {
                                    if(x2 >= 0 && y2 >= 0 && x2 < offlimits.length && y2 < offlimits[0].length) {
                                        offlimits[x2][y2] = true;
                                    }
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Perform a splash attack, prioritizing clusters of enemy paint or robots.
     */
    // ~4.5k bytecode
    public void splashAttack() throws GameActionException {
        MapLocation bestTarget = null;
        int bestScore = 0;
        MapLocation myLoc = rc.getLocation();

        // 1k bytecode
        int[] adjacencyCounts = SplasherUtils.calculateAdjacencyCounts();
        for(int i = 0; i < 13; i++){
            int emptyCount = adjacencyCounts[i];
            int enemyCount = adjacencyCounts[i + 13];
            MapLocation targetLocation = SplasherUtils.indexToLocation(i, myLoc);

            if (!rc.canAttack(targetLocation)) continue;

            // Check if this square fucks w/ any ruin or resource center builds.
            if(offlimits[targetLocation.x][targetLocation.y]){
                continue;
            }

            int score = 0;
            if(emptyCount + enemyCount > 4){
                score = 10 * emptyCount + 50 * enemyCount;
            }

            // Unit at location.
            // TODO: Update this to check for adjacency to enemy tower.
            RobotInfo rob = rc.senseRobotAtLocation(targetLocation);
            if(rob != null && rob.getType().isTowerType() && rob.getTeam() == oppTeam) {
                score += 10000;
            }

            if (score > bestScore) {
                bestScore = score;
                bestTarget = targetLocation;
            }
        }

        if (bestTarget != null && rc.isActionReady()) {
            rc.attack(bestTarget);
        }
    }

    /**
     * Returns a score evaluating how favorable it would be for this robot to move to this sector.
     */
    public int evaluateSector(int encodedSector) {
        ScanResult sr = comms.decodeSector(encodedSector);
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

//        if (tryingToReplenish && nearestAlliedPaintTowerLoc != null &&
//                myLoc.distanceSquaredTo(nearestAlliedPaintTowerLoc) > GameConstants.PAINT_TRANSFER_RADIUS_SQUARED) {
//            nav.goToBug(nearestAlliedPaintTowerLoc, GameConstants.PAINT_TRANSFER_RADIUS_SQUARED);
//            return;
//        }

        macroMove(0);
    }

}
