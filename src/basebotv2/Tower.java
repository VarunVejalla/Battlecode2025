package basebotv2;

import battlecode.common.*;

public class Tower extends Robot {
    public Tower(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        // Pick a random robot type to build.
        // TODO: Select Mopper and Splasher generation.
        int robotType = 0;
        rng.nextInt(3);

        // Only make a bot if you will have over 1000 chips after making it.
        // The idea is to always maintain enough money to build a tower.
        if (rc.getMoney() > 1500) {
            if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                rc.buildRobot(UnitType.SOLDIER, nextLoc);
                System.out.println("BUILT A SOLDIER");
            } else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
                rc.buildRobot(UnitType.MOPPER, nextLoc);
                System.out.println("BUILT A MOPPER");
            } else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
                // rc.buildRobot(UnitType.SPLASHER, nextLoc);
                // System.out.println("BUILT A SPLASHER");
                rc.setIndicatorString("SPLASHER NOT IMPLEMENTED YET");
            }
        }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }
        runAttack();

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
        for(RobotInfo bot : bots){
            if(bestBot == null || bot.getHealth() < bestBot.getHealth()){
                bestBot = bot;
            }
        }
        if(bestBot != null){
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
            rc.attack(myLoc);
        }
        // run AoE attack if needed
        if (rc.isActionReady() && shouldRunAoEAttack()) {
            Util.log("Tower running AoE attack");
            rc.attack(null);
        }
    }
}
