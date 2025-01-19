package bettermoppers;

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
    int minMoneyTowers;
    int estimatedIncomePerRound;
    int roundSpawn;
    MapLocation spawnLoc;

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
        currentNumTotalChips = rc.getChips();
        currentNumTotalTowers = rc.getNumberTowers();
        spawnLoc = rc.getLocation();
    }

    public void run() throws GameActionException {
        currentNumTotalChips = rc.getChips();
        currentNumTotalTowers = rc.getNumberTowers();
        indicatorString = "";
        myLoc = rc.getLocation();

        int deltaChips = currentNumTotalChips - previousNumTotalChips;
        if (currentNumTotalTowers >= previousNumTotalTowers) {
            if (deltaChips/20 > minMoneyTowers) {
                minMoneyTowers = deltaChips/20;
            }
        } else {
            minMoneyTowers -= previousNumTotalTowers - currentNumTotalTowers;
            if (minMoneyTowers < 0) {
                minMoneyTowers = 0;
            }
        }
    }

    public double getMetric() {
        return (double) (rc.getRoundNum() * (2 + rc.getNumberTowers()))/(mapHeight * mapWidth);
    }

    public void sharedEndFunction() throws GameActionException {
        // this function is available to all robots, if they'd like to run it at the end
        // of their turn
        myLoc = rc.getLocation();
        rc.setIndicatorString(indicatorString);
        previousNumTotalChips = rc.getChips();
        previousNumTotalTowers = rc.getNumberTowers();
    }
}