package basebotv2;

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
    RobotController rc;
    Navigation nav;
    Constants constants = new Constants();
    MapLocation myLoc; //current loc of robot
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

    MapLocation sharedOffensiveTarget;
    RobotInfo[] nearbyFriendlies; // friendly bots within vision radius of bot

    RobotInfo[] nearbyVisionEnemies; // enemy bots within vision radius of bot
    MapLocation[] spawnCenters;

    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc;
        Util.rc = rc;
        Util.robot = this;
        myTeam = rc.getTeam();
        oppTeam = rc.getTeam().opponent();
        this.mapWidth = rc.getMapWidth();
        this.mapHeight = rc.getMapHeight();
//        Util.logBytecode("After computing all spawn centers");

        this.nav = new Navigation(rc, this);
        rng = new Random(42);  // seed the random number generator with the id of the bot
    }

    public void run() throws GameActionException {
        indicatorString = "";
        myLoc = rc.getLocation();
    }

    public void sharedEndFunction() throws GameActionException {
        // this function is available to all robots, if they'd like to run it at the end of their turn
        myLoc = rc.getLocation();
        scanSurroundings();
        rc.setIndicatorString(indicatorString);
    }


    public void scanSurroundings() throws GameActionException {
        // this method scans the surroundings of the bot and updates comms if needed

        // TODO: maybe it would be more efficient to call rc.senseNearbyRobots once and generate the arrays ourselves?
        nearbyFriendlies = rc.senseNearbyRobots(GameConstants.VISION_RADIUS_SQUARED, myTeam);
//        nearbyActionFriendlies = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, myTeam);
        nearbyVisionEnemies = rc.senseNearbyRobots(GameConstants.VISION_RADIUS_SQUARED, oppTeam);
//        nearbyActionEnemies = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, oppTeam);
    }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for possible future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            // Occasionally try to tell nearby allies how many enemy robots we see.
            if (rc.getRoundNum() % 20 == 0){
                for (RobotInfo ally : allyRobots){
                    if (rc.canSendMessage(ally.location, enemyRobots.length)){
                        rc.sendMessage(ally.location, enemyRobots.length);
                    }
                }
            }
        }
    }

}