package boostedboonyComms;

import battlecode.common.*;

public class Tower extends Robot {

    TowerComms comms = new TowerComms(rc, this, this);
    MapInfo[] nearbyMapInfos;

    RobotInfo[] friendliesToComm = null;


    public Tower(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        scanSurroundings();
        comms.sendMessages();

        if (rc.getRoundNum() > 10){
            rc.resign();
        }

        if (rc.getRoundNum() < 50) {
            openingBots();
        } else if (rc.getMoney() > 1500) {
            midGameBots();
        }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }
        runAttack();
    }

    public void scanSurroundings() throws GameActionException {
        nearbyMapInfos = rc.senseNearbyMapInfos();
        friendliesToComm = rc.senseNearbyRobots(GameConstants.MESSAGE_RADIUS_SQUARED, rc.getTeam());
    }



    public void openingBots() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        }
    }

    public void midGameBots() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        int robotType = rng.nextInt(2); // no splashers

        if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        } else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
            rc.buildRobot(UnitType.MOPPER, nextLoc);
            System.out.println("BUILT A MOPPER");
        } else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
            rc.buildRobot(UnitType.SPLASHER, nextLoc);
            System.out.println("BUILT A SPLASHER");
        }
    }

    public boolean shouldRunAoEAttack() throws GameActionException {
        // check if there are enough enemies in the action radius
        RobotInfo[] bots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        return bots.length > 3;
    }

    /**
     * Find best enemy bot in action radiues to attack, based on health of bot
     */
    public MapLocation findBestAttackTarget() throws GameActionException {
        RobotInfo[] bots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        RobotInfo bestBot = null;
        for (RobotInfo bot : bots) {
            if (bestBot == null || bot.getHealth() < bestBot.getHealth()) {
                bestBot = bot;
            }
        }
        if (bestBot != null) {
            return bestBot.getLocation();
        }
        return null;
    }

    /**
     * Attack any enemies in your vision radius
     */
    public void runAttack() throws GameActionException {
        // see if there's an enemy to attack
        MapLocation target = findBestAttackTarget();
        if (target != null && rc.isActionReady() && rc.canAttack(target)) {
            Util.log("Tower running attack");
            rc.attack(target);
        }
        // run AoE attack if needed
        if (rc.isActionReady() && shouldRunAoEAttack()) {
            Util.log("Tower running AoE attack");
            rc.attack(null);
        }
    }
}
