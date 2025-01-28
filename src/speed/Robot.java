package speed;

import battlecode.common.*;

import java.util.Random;

public class Robot {
//    Constants constants = new Constants();
    RobotController rc;
    Navigation nav;
    MapLocation myLoc; // current loc of robot
    MapInfo myLocInfo;
    int mapWidth, mapHeight;
    static Random rng;
    String indicatorString = "";
    Team myTeam;
    Team oppTeam;
    int previousNumTotalChips;
    int previousNumTotalTowers;
    int currentNumTotalChips;
    int currentNumTotalTowers;
    int estimatedChipsPerRound;
    int currentPaint;
    int previousPaint;
    int roundSpawn;
    MapLocation spawnLoc;
    UnitType myType;

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
        this.nav = new Navigation(rc, this);
        rng = new Random(42); // seed the random number generator with the id of the bot
        roundSpawn = rc.getRoundNum();
        this.myLoc = rc.getLocation();
        previousNumTotalChips = rc.getChips();
        previousNumTotalTowers = rc.getNumberTowers();
        spawnLoc = rc.getLocation();
        estimatedChipsPerRound = 0;
        myType = rc.getType();
    }

    public void run() throws GameActionException {
        myType = rc.getType();
        indicatorString = "";
        myLoc = rc.getLocation();
        currentNumTotalChips = rc.getChips();
        currentNumTotalTowers = rc.getNumberTowers();
        currentPaint = rc.getPaint();

        // TODO: this is a stupid way of doing it because resource patterns could go down. improve this
        int deltaChips = currentNumTotalChips - previousNumTotalChips;
        if(currentNumTotalTowers > previousNumTotalTowers){
            deltaChips += 1000 * (currentNumTotalTowers - previousNumTotalTowers);
        }
        if (deltaChips > 0) {
            estimatedChipsPerRound = deltaChips;
        }

        Util.addToIndicatorString("CPR: " + estimatedChipsPerRound);
    }

    public double getMetric() {
        return (double) (rc.getRoundNum() * (2 + rc.getNumberTowers()))/(mapHeight + mapWidth);
    }

    public void sharedEndFunction() throws GameActionException {
        // this function is available to all robots, if they'd like to run it at the end
        // of their turn
        myLoc = rc.getLocation();
        rc.setIndicatorString(indicatorString);
        previousNumTotalChips = rc.getChips();
        previousNumTotalTowers = rc.getNumberTowers();
        previousPaint = rc.getPaint();
    }
}