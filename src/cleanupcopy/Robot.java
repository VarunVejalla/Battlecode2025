package cleanupcopy;

import battlecode.common.*;

import java.util.Random;

public class Robot {
    RobotController rc;
    Navigation nav;
    MapLocation myLoc; // current loc of robot
    MapInfo myLocInfo;
    int mapWidth, mapHeight;
    static Random rng;
    String indicatorString = "";
    Team myTeam;
    Team oppTeam;

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

    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc;
        Util.rc = rc;
        Util.robot = this;
        myTeam = rc.getTeam();
        oppTeam = rc.getTeam().opponent();
        this.mapWidth = rc.getMapWidth();
        this.mapHeight = rc.getMapHeight();
        // Util.logBytecode("After computing all spawn centers");

        this.nav = new Navigation(rc, this);
        rng = new Random(42); // seed the random number generator with the id of the bot
    }

    public void run() throws GameActionException {
        indicatorString = "";
        myLoc = rc.getLocation();
    }

    public void sharedEndFunction() throws GameActionException {
        // this function is available to all robots, if they'd like to run it at the end
        // of their turn
        myLoc = rc.getLocation();
        rc.setIndicatorString(indicatorString);
    }
}