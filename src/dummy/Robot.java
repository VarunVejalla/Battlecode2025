package dummy;

import battlecode.common.*;

import java.util.Random;

enum SymmetryType {
    HORIZONTAL,
    VERTICAL,
    ROTATIONAL,
    DIAGONAL_RIGHT,
    DIAGONAL_LEFT
}

public class Robot {


    /**
     * Array containing all the possible movement directions.
     */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

//    MapLocation sharedOffensiveTarget;
//    RobotInfo[] nearbyFriendlies; // friendly bots within vision radius of bot
//
//    RobotInfo[] nearbyVisionEnemies; // enemy bots within vision radius of bot
//    MapLocation[] spawnCenters;

    public Robot(RobotController rc) throws GameActionException {

    }

    public void run() throws GameActionException {

    }


}