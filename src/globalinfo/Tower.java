package globalinfo;

import battlecode.common.*;

public class Tower extends Robot {

    TowerComms comms = new TowerComms(rc, this, this);
    MapInfo[] nearbyMapInfos;
    RobotInfo[] friendliesToComm = null;
    int numTotalSpawned = 0;
    int myTowerNumber;
    int numRoundsLessThan1400 = 5;
    int previousNumTowers;


    public Tower(RobotController rc) throws GameActionException {
        super(rc);
        myTowerNumber = rc.getNumberTowers();
        previousNumTowers = myTowerNumber;
    }

    public void run() throws GameActionException {
        super.run();

        Util.log("TOWER");
        Util.addToIndicatorString("RUN");
        scanSurroundings();
        runAttack();

        if (rc.getChips() < 1400) {
            numRoundsLessThan1400++;
        } else {
            numRoundsLessThan1400 = 0;
        }

        if (rc.getRoundNum() < 100 && rc.getNumberTowers() <= 3) {
            if (numTotalSpawned < 2) {
                soldierSpawning();
            }
        } else if (!isSaving()) {
            midGameBots();
        }
//            if(rc.getRoundNum() < Constants.SPAWN_MIDGAME_BOTS_ROUNDS) {
//                midGameBots();
//            }
//            else {
//                endGameBots();
//            }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
             Util.log("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

    }

    public void scanSurroundings() throws GameActionException {
        nearbyMapInfos = rc.senseNearbyMapInfos();
        friendliesToComm = rc.senseNearbyRobots(GameConstants.MESSAGE_RADIUS_SQUARED, rc.getTeam());

        // processMessages
        comms.processSectorMessages();
    }

    public void tryBuilding(UnitType unitType, MapLocation location) throws GameActionException {
        if (rc.canBuildRobot(unitType, location)) {
            numTotalSpawned++;
            rc.buildRobot(unitType, location);
        }
    }

    public boolean isSaving() {
        if (rc.getRoundNum() < 50 && rc.getNumberTowers() <= 3) {
            return true;
        }

        if (numRoundsLessThan1400 >= 4) {
            return true;
        }

        if (rc.getChips() >= 1400) {
            return false;
        } else if (rc.getNumberTowers() >= 25) {
            return false;
        } else if (rc.getNumberTowers() <= 1) {
            return true;
        } else {
            return false;
        }
    }

    public void soldierSpawning() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        tryBuilding(UnitType.SOLDIER, nextLoc);
    }

    public void midGameBots() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        int robotType = rng.nextInt(3); // yes splashers
        if (robotType == 0) {
            tryBuilding(UnitType.SOLDIER, nextLoc);
        } else if (robotType == 1) {
            tryBuilding(UnitType.MOPPER, nextLoc);
        } else if (robotType == 2) {
            tryBuilding(UnitType.SPLASHER, nextLoc);
        }
    }

    public void endGameBots() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        // every 10 rounds, make a mopper
        if(rc.getRoundNum() % 10 == 0) {
            tryBuilding(UnitType.MOPPER, nextLoc);
            return;
        }

        // rest of rounds, alternate between soldier and splasher
        int robotType = 2*rng.nextInt(2); // yes splashers
        if (robotType == 0) {
            tryBuilding(UnitType.SOLDIER, nextLoc);
        } else if (robotType == 2) {
            tryBuilding(UnitType.SPLASHER, nextLoc);
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
        Util.addToIndicatorString("RA; ");
        MapLocation target = findBestAttackTarget();
        Util.log("TGT: " + target);
        Util.addToIndicatorString("TGT: " + target);
        if (target != null && rc.isActionReady() && rc.canAttack(target)) {
            Util.log("Tower running attack");
            rc.attack(target);
        }
        // run AoE attack if needed
        if (rc.isActionReady() && shouldRunAoEAttack()) {
            // Util.log("Tower running AoE attack");
            rc.attack(null);
        }
    }
}
