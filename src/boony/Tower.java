package boony;

import battlecode.common.*;

public class Tower extends Robot {
    private static final int EARLY_GAME_ROUND = 50;
    private static final int MID_GAME_ROUND = 200;

    public Tower(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        if (rc.getRoundNum() < EARLY_GAME_ROUND) {
            openingStrategy();
        } else if (rc.getRoundNum() < MID_GAME_ROUND) {
            midGameStrategy();
        } else {
            lateGameStrategy();
        }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }
        runAttack();

    }
    private void buildRobot(UnitType type) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        if (rc.canBuildRobot(type, nextLoc)) {
            rc.buildRobot(type, nextLoc);
            System.out.println("BUILT A " + type);
        }
    }

    public void openingStrategy() throws GameActionException {
        buildRobot(UnitType.SOLDIER);
    }

    public void midGameStrategy() throws GameActionException {

        if(rc.getMoney() < 1500) return; // Save until at least 15.

        int robotType = rng.nextInt(2);
        if(robotType == 0) {
            buildRobot(UnitType.SOLDIER);
        } else {
            buildRobot(UnitType.MOPPER);
        }
    }

    public void lateGameStrategy() throws GameActionException {
        if(rc.getMoney() < 1500) return;
//        buildRobot(UnitType.SPLASHER);
        int robotType = rng.nextInt(2);
        if(robotType == 0) {
            buildRobot(UnitType.SOLDIER);
        } else {
            buildRobot(UnitType.MOPPER);
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
