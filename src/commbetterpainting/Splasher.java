package commbetterpainting;

import battlecode.common.*;

public class Splasher extends Bunny {

    boolean[][] offlimits;
    boolean[][] updated;

    public Splasher(RobotController rc) throws GameActionException {
        super(rc);
        offlimits = new boolean[rc.getMapWidth()][rc.getMapHeight()];
        updated = new boolean[rc.getMapWidth()][rc.getMapHeight()];
    }

    public void run() throws GameActionException {
        super.run(); // Call shared logic for all bunnies

        updateDestinationIfNeeded();

//        Util.logBytecode("Ran super");
//        updateOffLimits();
//        Util.logBytecode("Updated off limits");

        // 1. Replenish or Perform Splash Attack
        if (tryingToReplenish) {
            replenishLogic();
        } else {
//            splashAttack();
//            Util.logBytecode("After first attack");
            // 2. Movement Logic
//            MapLocation currLoc = rc.getLocation();
            if (canMove()) {
                moveLogic();
//                Util.logBytecode("Move logic");
            }
//                if(!rc.getLocation().equals(currLoc) && rc.isActionReady()) {
            nearbyMapInfos = Util.getFilledInMapInfo(rc.senseNearbyMapInfos());
//            Util.logBytecode("Reclaculate infos");
            updateOffLimits();
//            Util.logBytecode("Reupdate off limits");
//                }
            splashAttack();
//            Util.logBytecode("Second attack");
        }

        // 4. End of Turn Logic
        sharedEndFunction();
    }


    public boolean checkIfIShouldStartReplenishing() throws GameActionException {
        return rc.getPaint() <= Constants.PAINT_THRESHOLD_TO_REPLENISH;
    }

    // 2k
    public void updateOffLimits() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        for(MapInfo info : nearbyMapInfos) {
            if(info == null){
                continue;
            }
            MapLocation loc = info.getMapLocation();
            if(info.getMark() == PaintType.ALLY_PRIMARY || (info.isResourcePatternCenter() && info.getPaint().isAlly()) || (info.hasRuin() && rc.senseRobotAtLocation(loc) == null)){
                if(updated[loc.x][loc.y]){
                    continue;
                }
                Util.log("Updating new location");
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
                    updated[loc.x][loc.y] = true;
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

            // Check if this square fucks w/ any ruin or resource center builds.
            if(offlimits[targetLocation.x][targetLocation.y]){
                continue;
            }

            int[] adjacencyCounts = calculateAdjacencyCounts(targetLocation);

            int emptyCount = adjacencyCounts[0];
            int enemyCount = adjacencyCounts[1];

            int score = 0;
            if(emptyCount + enemyCount > 4){
                score = 10 * emptyCount + 50 * enemyCount;
            }

            if (score > bestScore) {
                bestScore = score;
                bestTarget = tile.getMapLocation();
            }
        }

        if (bestTarget != null && rc.isActionReady()) {
            rc.attack(bestTarget);
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
