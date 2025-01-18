package commspawning;

import battlecode.common.*;

public class Tower extends Robot {

    TowerComms comms = new TowerComms(rc, this, this);
    MapInfo[] nearbyMapInfos;
//    RobotInfo[] friendliesToComm = null;
    RobotInfo[] friendliesToPaint = null;
    Direction dirToCenter;
    RobotInfo replenishingRobot = null;


    public Tower(RobotController rc) throws GameActionException {
        super(rc);
        dirToCenter = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
    }

    public void run() throws GameActionException {
        super.run();
//        Util.log("TOWER");
        Util.addToIndicatorString("RUN");
        scanSurroundings();
        replenishRobots();

        comms.updateKnowledge();
        runAttack();

        if (rc.getRoundNum() < Constants.SPAWN_OPENING_BOTS_ROUNDS) {
            openingBots();
        } else if (rc.getMoney() > Constants.SPAWN_BOTS_MIDGAME_COST_THRESHOLD) {
            midGameBots();
        }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            if(m.getBytes() != 65535) {
                Util.log("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
            }
        }

        if(rc.getRoundNum() == 236 && rc.getID() == Constants.DEBUG_ROBOT_ID) {
            comms.describeWorldConcise();
        }

    }

    public void scanSurroundings() throws GameActionException {
        nearbyMapInfos = rc.senseNearbyMapInfos();
//        friendliesToComm = rc.senseNearbyRobots(GameConstants.MESSAGE_RADIUS_SQUARED, rc.getTeam());
        friendliesToPaint = rc.senseNearbyRobots(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED, rc.getTeam());

        // processMessages
        comms.processSectorMessages();
    }

    public void replenishRobots() throws GameActionException {

        Util.logArray("Friends", friendliesToPaint);
        if(replenishingRobot != null)
            Util.log("Replenishing robot: " + replenishingRobot.getID());
        // No one needs paint.
        if(friendliesToPaint.length == 0) return;

        // If the replenishing robot is still here, serve them.
        if(replenishingRobot != null) {
            boolean robStillHere = false;
            for(RobotInfo r : friendliesToPaint) {
                if(r.getID() == replenishingRobot.getID()) {
                    robStillHere = true;
                    replenishingRobot = r;
                    break;
                }
            }

            // If they're gone, reset to null.
            if(!robStillHere) replenishingRobot = null;
        }

        // Not serving anyone yet, start with the first robot that needs replenishing.
        if(replenishingRobot == null) {
            for(RobotInfo r : friendliesToPaint) {
                if(r.getPaintAmount() < Constants.PAINT_THRESHOLD_TO_REPLENISH) {
                    Util.log("Robot needs paint"+ r.getID());
                    replenishingRobot = r;
                    break;
                }
            }
            // No one to serve? Return.
            return;
        }

        // Already serving someone, serve them, unless someone is going to die.
        for(RobotInfo rob : friendliesToPaint) {
            if(rob.getPaintAmount() <= 5) {
                replenishingRobot = rob;
                break;
            }
        }

        int paintToFillUp = Math.min(replenishingRobot.getType().paintCapacity - replenishingRobot.getPaintAmount(), rc.getPaint());

        if (rc.canTransferPaint(replenishingRobot.getLocation(), paintToFillUp)) {
            Util.log("Transferring paint: " + replenishingRobot.getID());
            rc.transferPaint(replenishingRobot.getLocation(), paintToFillUp);
        } else {
            Util.log("Couldn't tranfer to " + replenishingRobot.getLocation());
        }

        if(rc.getRoundNum() > 70) rc.resign();
    }

    public void openingBots() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        if(rc.getRoundNum() % 3 == 0) {
            if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                rc.buildRobot(UnitType.SOLDIER, nextLoc);
            }
        } else {
            if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                rc.buildRobot(UnitType.SOLDIER, nextLoc);
            }
        }
    }

    public void midGameBots() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        int robotType = rng.nextInt(3); // yes splashers
        if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
        } else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
            rc.buildRobot(UnitType.MOPPER, nextLoc);
        } else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
            rc.buildRobot(UnitType.SPLASHER, nextLoc);
        }
    }

    public void endGameBots() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        // every 10 rounds, make a mopper
        if(rc.getRoundNum() % 10 == 0 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
            rc.buildRobot(UnitType.MOPPER, nextLoc);
            return;
        }

        // rest of rounds, alternate between soldier and splasher
        int robotType = 2*rng.nextInt(2); // yes splashers
        if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
        } else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
            rc.buildRobot(UnitType.SPLASHER, nextLoc);
        }

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

        // run AoE attack
        if (rc.canAttack(null)) {
            rc.attack(null);
        }

        // see if there's an enemy to attack
        Util.addToIndicatorString("RA; ");
        MapLocation target = findBestAttackTarget();
//        Util.log("TGT: " + target);
        Util.addToIndicatorString("TGT: " + target);

        if (target != null && rc.canAttack(target)) {
            Util.log("Tower running attack");
            rc.attack(target);
        }
    }
}
