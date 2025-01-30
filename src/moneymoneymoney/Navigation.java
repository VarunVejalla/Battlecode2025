package moneymoneymoney;

import battlecode.common.*;

enum NavigationMode {
    FUZZYNAV, BUGNAV;
}

public class Navigation {

    RobotController rc;
    Robot robot;

    NavigationMode mode = NavigationMode.BUGNAV;

    // Bugnav variables
    int closestDistToTarget = Integer.MAX_VALUE;
    MapLocation lastWallFollowed = null;
    Direction lastDirectionMoved = null;
    int roundsSinceClosestDistReset = 0;
    MapLocation prevTarget = null;
    boolean bugFollowRight = true; // TODO: Figure out how to make this a smart decision.
    boolean fuzzyFailed = false;
    MapLocation lastGoToSmartLoc = null;

    final int ROUNDS_TO_RESET_BUG_CLOSEST = 15;

    public Navigation(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;
    }

    public boolean goToBug(MapLocation target, int minDistToSatisfy) throws GameActionException {
        if (mode != NavigationMode.BUGNAV) {
            mode = NavigationMode.BUGNAV;
            resetBugNav();
        }
        if (!target.equals(prevTarget)) {
            resetBugNav();
        }
        prevTarget = target;
        return goTo(target, minDistToSatisfy, false);
    }

    public boolean goToFuzzy(MapLocation target, int minDistToSatisfy) throws GameActionException {
        return goToFuzzy(target, minDistToSatisfy, false);
    }

    public boolean goToFuzzy(MapLocation target, int minDistToSatisfy, boolean allow_center) throws GameActionException {
        mode = NavigationMode.FUZZYNAV;
        return goTo(target, minDistToSatisfy, allow_center);
    }

    public void resetBugNav() {
        closestDistToTarget = Integer.MAX_VALUE;
        lastWallFollowed = null;
        lastDirectionMoved = null;
        roundsSinceClosestDistReset = 0;
    }

    public int getPaintLost(MapInfo newLocInfo, int numFriendlyRobots) {
        if(newLocInfo.getPaint() == PaintType.EMPTY){
            return 1 + numFriendlyRobots;
        } else if(newLocInfo.getPaint().isEnemy()){
            return 2 + 2*numFriendlyRobots;
        } else {
            return numFriendlyRobots;
        }
    }

    public Direction bugNav(MapLocation target) throws GameActionException {
        // Util.log("Running bugnav");
        // Every 20 turns reset the closest distance to target
        if (roundsSinceClosestDistReset >= ROUNDS_TO_RESET_BUG_CLOSEST) {
            closestDistToTarget = Integer.MAX_VALUE;
            roundsSinceClosestDistReset = 0;
        }
        roundsSinceClosestDistReset++;

        Direction closestDir = null;
        Direction wallDir = null;
        Direction dir = null;

        if (lastWallFollowed != null) {
            // If the wall no longer exists there, so note that.
            Direction toLastWallFollowed = robot.myLoc.directionTo(lastWallFollowed);
            if (toLastWallFollowed == Direction.CENTER
                    || (robot.myLoc.isAdjacentTo(lastWallFollowed) && rc.canMove(toLastWallFollowed))) {
                lastWallFollowed = null;
            } else {
                dir = robot.myLoc.directionTo(lastWallFollowed);
            }
        }
        if (dir == null) {
            dir = robot.myLoc.directionTo(target);
        }

        // This should never happen theoretically, but in case it does, just reset and
        // continue.
        if (dir == Direction.CENTER) {
            // System.out.println("ID: " + rc.getID());
            // rc.resign();
            // return null;
            resetBugNav();
            return Direction.CENTER;
        }

        for (int i = 0; i < 8; i++) {
            MapLocation newLoc = rc.adjacentLocation(dir);
            if (rc.canSenseLocation(newLoc) && rc.canMove(dir) && getPaintLost(rc.senseMapInfo(newLoc), rc.senseNearbyRobots(newLoc, 2, robot.myTeam).length) < rc.getPaint()) {
                // If we can get closer to the target than we've ever been before, do that.
                int dist = newLoc.distanceSquaredTo(target);
                if (dist < closestDistToTarget) {
                    closestDistToTarget = dist;
                    closestDir = dir;
//                    Util.addToIndicatorString("CLSR " + dist);
                }

                // Check if wall-following is viable
                if (wallDir == null) {
                    wallDir = dir;
                }
            } else {
                if (wallDir == null) {
                    // Hard check for if wall is outer boundary (don't count that as a wall).
                    if (rc.onTheMap(newLoc)) {
                        // Hard check for if wall is another robot (don't count that as a wall).
                        if (rc.canSenseLocation(newLoc) && rc.senseRobotAtLocation(newLoc) == null) {
                            lastWallFollowed = newLoc;
                        }
                    } else {
                        bugFollowRight = !bugFollowRight;
                    }
                }
            }
            if (bugFollowRight) {
                dir = dir.rotateRight();
            } else {
                dir = dir.rotateLeft();
            }
        }

//        Util.addToIndicatorString("BFR: " + bugFollowRight);
//        Util.addToIndicatorString("WD: " + wallDir);

        if (closestDir != null) {
            //            Direction bestDir = closestDir;
            //            int bestHeuristic = heuristic(closestDir);
            return closestDir;
        }
        return wallDir;
    }

    public Direction fuzzyNav(MapLocation target, boolean center_allowed) throws GameActionException {
        Util.addToIndicatorString("FZ" + target);
        Direction toTarget = robot.myLoc.directionTo(target);
        Direction[] moveOptions = {
                toTarget,
                toTarget.rotateLeft(),
                toTarget.rotateRight(),
                toTarget.rotateLeft().rotateLeft(),
                toTarget.rotateRight().rotateRight(),
                Direction.CENTER
        };
        if(!center_allowed) {
            moveOptions = new Direction[]{
                    toTarget,
                    toTarget.rotateLeft(),
                    toTarget.rotateRight(),
                    toTarget.rotateLeft().rotateLeft(),
                    toTarget.rotateRight().rotateRight(),
                    Direction.CENTER
            };
        }

        Direction bestDir = null;
        int leastNumMoves = Integer.MAX_VALUE;
        double leastHeuristic = Double.MAX_VALUE;

        for (int i = moveOptions.length; i-- > 0;) {
            Direction dir = moveOptions[i];
            MapLocation newLoc = robot.myLoc.add(dir);

            if (dir != Direction.CENTER && (!rc.canSenseLocation(newLoc) || !rc.canMove(dir))) {
                continue;
            }
            MapInfo info = rc.senseMapInfo(newLoc);
            if (!info.isPassable()) {
                continue;
            }

            int numMoves = Util.minMovesToReach(newLoc, target);
//            int distanceSquared = newLoc.distanceSquaredTo(target);
//            int distance = (int)Math.sqrt(distanceSquared);



            int numAllies = rc.senseNearbyRobots(newLoc, 2, robot.myTeam).length;
//            RobotInfo[] adjAllies = rc.senseNearbyRobots(newLoc, 2, robot.myTeam);
//            for(RobotInfo ally : adjAllies){
//                if(!Util.isTower(ally.getType())){
//                    numAllies++;
//                }
//            }


            int paintLost = getPaintLost(info, numAllies);

            if (paintLost >= rc.getPaint()) {
                // don't consider this direction if it would leave us at 0 paint
                continue;
            }


//            Util.addToIndicatorString("D" + distance + "," + paintHeuristic + "," + allyHeuristic + ";");
//            Util.log("D" + dir + "," + distance + "," + paintHeuristic + "," + allyHeuristic + ";");


            double heuristic = numMoves + (double) paintLost /rc.getPaint();

            if (heuristic < leastHeuristic || (heuristic == leastHeuristic && numMoves < leastNumMoves)) {
//            if(heuristic < leastHeuristic){
                leastNumMoves = numMoves;
                leastHeuristic = heuristic;
                bestDir = dir;
            }
        }
        return bestDir;
    }

    public boolean goTo(MapLocation target, int minDistToSatisfy, boolean center_allowed) throws GameActionException {
        // thy journey hath been completed
        if (robot.myLoc.distanceSquaredTo(target) <= minDistToSatisfy) {
            return true;
        }

        if (!rc.isMovementReady()) {
            return false;
        }

        while (rc.isMovementReady()) {
            Direction toGo = null;
            switch (mode) {
                case FUZZYNAV:
                    toGo = fuzzyNav(target, center_allowed);
                    break;
                case BUGNAV:
                    toGo = bugNav(target);
                    break;
            }
            if (toGo == null)
                return false;
            if(toGo == Direction.CENTER){
                return true;
            }
            Util.tryMove(toGo); // Should always return true since fuzzyNav checks if rc.canMove(dir)
            if (robot.myLoc.distanceSquaredTo(target) <= minDistToSatisfy) {
                return true;
            }
        }
        return true;
    }

    public void goToSmart(MapLocation target, int minDistToSatisfy) throws GameActionException {
        if(lastGoToSmartLoc == null || !target.isWithinDistanceSquared(lastGoToSmartLoc, 8)) {
//            Util.addToIndicatorString("RESETTING FUZZY " + target + ", " + lastGoToSmartLoc);
            lastGoToSmartLoc = target;
            fuzzyFailed = false;
            if(rc.getLocation().distanceSquaredTo(target) > 36){
                fuzzyFailed = true;
            }
        }

        if(rc.getLocation().isWithinDistanceSquared(target, minDistToSatisfy)){
            return;
        }

        if(fuzzyFailed){
//            Util.addToIndicatorString("FZ FAILED");
        }
        if(!fuzzyFailed){
            Direction bestDir = fuzzyNav(target, true);
            if(bestDir == null || bestDir == Direction.CENTER){
                fuzzyFailed = true;
            } else {
                Util.tryMove(bestDir);
            }
//            Util.addToIndicatorString("BDR: " + bestDir);
        }
        if(rc.isMovementReady() && fuzzyFailed){
            Direction bestDir = bugNav(target);
            if(bestDir != null && bestDir != Direction.CENTER){
                Util.tryMove(bestDir);
            }
        }
        if(fuzzyFailed){
//            Util.addToIndicatorString("FZ FAILED 2");
        }
    }
}