package goat;

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
    boolean[][] locsToIgnore;
    MapLocation[] recentlyVisited = new MapLocation[10];
    int recentlyVisitedIdx = 0;
    boolean bugFollowRight = true; // TODO: Figure out how to make this a smart decision.

    final int ROUNDS_TO_RESET_BUG_CLOSEST = 15;

    public Navigation(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;
        locsToIgnore = new boolean[rc.getMapWidth()][rc.getMapHeight()];
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
        return goTo(target, minDistToSatisfy);
    }

    public boolean goToFuzzy(MapLocation target, int minDistToSatisfy) throws GameActionException {
        mode = NavigationMode.FUZZYNAV;
        return goTo(target, minDistToSatisfy);
    }

    public void resetBugNav() {
        closestDistToTarget = Integer.MAX_VALUE;
        lastWallFollowed = null;
        lastDirectionMoved = null;
        roundsSinceClosestDistReset = 0;
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
            if (rc.canSenseLocation(newLoc) && rc.canMove(dir)) {
                // If we can get closer to the target than we've ever been before, do that.
                int dist = newLoc.distanceSquaredTo(target);
                if (dist < closestDistToTarget) {
                    closestDistToTarget = dist;
                    closestDir = dir;
                    Util.addToIndicatorString("CLSR " + dist);
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
                    }
                }
            }
            if (bugFollowRight) {
                dir = dir.rotateRight();
            } else {
                dir = dir.rotateLeft();
            }
        }

        if (closestDir != null) {
            return closestDir;
        }
        return wallDir;
    }

    public Direction fuzzyNav(MapLocation target, int minDistToSatisfy) throws GameActionException {
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
//        if(rc.getLocation().distanceSquaredTo(target) <= minDistToSatisfy) {
//            moveOptions = new Direction[]{
//                    toTarget,
//                    toTarget.rotateLeft(),
//                    toTarget.rotateRight(),
//                    toTarget.rotateLeft().rotateLeft(),
//                    toTarget.rotateRight().rotateRight(),
//                    Direction.CENTER
//            };
//        }

        Direction bestDir = null;
        int leastNumMoves = Integer.MAX_VALUE;
        int leastHeuristic = Integer.MAX_VALUE;

        for (int i = moveOptions.length; i-- > 0;) {
            Direction dir = moveOptions[i];
            MapLocation newLoc = robot.myLoc.add(dir);

            if (!rc.canSenseLocation(newLoc) || !rc.canMove(dir)) {
                continue;
            }

            if (!rc.sensePassability(newLoc)) {
                continue;
            }

            int numMoves = Util.minMovesToReach(newLoc, target);
            int distanceSquared = newLoc.distanceSquaredTo(target);
            int distance = (int)Math.sqrt(distanceSquared);
            MapInfo info = rc.senseMapInfo(newLoc);
            int paintHeuristic = 0;
            if(info.getPaint().isAlly()){
                paintHeuristic = -5;
            } else if(info.getPaint().isEnemy()){
                paintHeuristic = 5;
            }

            int numAllies = 0;
            RobotInfo[] adjAllies = rc.senseNearbyRobots(newLoc, 2, robot.myTeam);
            for(RobotInfo ally : adjAllies){
                if(!Util.isTower(ally.getType())){
                    numAllies++;
                }
            }
            int allyHeuristic = numAllies * 5;

            Util.addToIndicatorString("D" + distance + ",P" + paintHeuristic + ",A" + allyHeuristic + ";");

            int heuristic = distance + paintHeuristic + allyHeuristic;

            if (numMoves < leastNumMoves ||
                    (numMoves == leastNumMoves && heuristic < leastHeuristic)) {
                leastNumMoves = numMoves;
                leastHeuristic = heuristic;
                bestDir = dir;
            }
        }
        return bestDir;
    }

    public void moveRandom() throws GameActionException {
        int randomIdx = robot.rng.nextInt(8);
        for (int i = 0; i < Robot.directions.length; i++) {
            if (Util.tryMove(Robot.directions[(randomIdx + i) % Robot.directions.length])) {
                return;
            }
        }
    }

    public boolean goTo(MapLocation target, int minDistToSatisfy) throws GameActionException {
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
                    toGo = fuzzyNav(target, minDistToSatisfy);
                    break;
                case BUGNAV:
                    toGo = bugNav(target);
                    break;
            }
            if (toGo == null)
                return false;
            Util.tryMove(toGo); // Should always return true since fuzzyNav checks if rc.canMove(dir)
            if (robot.myLoc.distanceSquaredTo(target) <= minDistToSatisfy) {
                return true;
            }
        }
        return true;
    }

    public boolean circle(MapLocation center, int minDist, int maxDist) throws GameActionException {
        // Util.log("Tryna circle CCW");
        if (circle(center, minDist, maxDist, true)) {
            return true;
        }
        // Util.log("Tryna circle CW");
        return circle(center, minDist, maxDist, false);
    }

    // from:
    // https://github.com/srikarg89/Battlecode2022/blob/main/src/cracked4BuildOrder/Navigation.java
    public boolean circle(MapLocation center, int minDist, int maxDist, boolean ccw) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }
        MapLocation myLoc = robot.myLoc;
        if (Util.minMovesToReach(myLoc, center) > maxDist) {
            // Util.log("Moving closer!");
            return goTo(center, minDist);
        }
        if (Util.minMovesToReach(myLoc, center) < minDist) {
            // Util.log("Moving away!");
            Direction centerDir = myLoc.directionTo(center);
            MapLocation target = myLoc.subtract(centerDir).subtract(centerDir).subtract(centerDir).subtract(centerDir)
                    .subtract(centerDir);
            boolean moved = goToBug(target, minDist);
            if (moved) {
                return true;
            }
            moved = goToFuzzy(target, minDist);
            if (moved) {
                return true;
            }
            return false;
        }

        int dx = myLoc.x - center.x;
        int dy = myLoc.y - center.y;
        double theta = Math.atan2(dy, dx);
        theta += (ccw ? 0.5 : -0.5);
        int avgDist = (minDist + maxDist) / 2;

        int x = (int) ((double) avgDist * Math.cos(theta));
        int y = (int) ((double) avgDist * Math.sin(theta));
        MapLocation target = center.translate(x, y);
        Direction targetDir = myLoc.directionTo(target);

        Direction[] options = { targetDir, targetDir.rotateRight(), targetDir.rotateLeft(),
                targetDir.rotateRight().rotateRight(), targetDir.rotateLeft().rotateLeft() };
        Direction bestDirection = null;
        int bestHeuristic = Integer.MAX_VALUE;
        for (int i = 0; i < options.length; i++) {
            if (!rc.canMove(options[i])) {
                continue;
            }
            MapLocation newLoc = myLoc.add(options[i]);
            if (Util.checkIfItemInArray(newLoc, recentlyVisited)) {
                continue;
            }
            int heuristic = i * 10;
            heuristic += locsToIgnore[newLoc.x][newLoc.y] ? 1000 : 0;
            int centerDist = Util.minMovesToReach(center, newLoc);
            heuristic += Math.abs(centerDist - avgDist) * 15;
            if (Util.minMovesToReach(center, newLoc) < minDist) {
                continue;
            }
            if (Util.minMovesToReach(center, newLoc) > maxDist) {
                continue;
            }
            if (heuristic < bestHeuristic) {
                bestDirection = options[i];
                bestHeuristic = heuristic;
            }
        }
        if (bestDirection != null) {
            Util.move(bestDirection);
            recentlyVisited[recentlyVisitedIdx] = rc.getLocation();
            recentlyVisitedIdx = (recentlyVisitedIdx + 1) % recentlyVisited.length;
            return true;
        }
        return false;
    }
}