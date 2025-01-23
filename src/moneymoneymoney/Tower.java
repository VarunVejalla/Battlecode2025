package moneymoneymoney;

import battlecode.common.*;

public class Tower extends Robot {

    TowerComms comms = new TowerComms(rc, this, this);
    MapInfo[] nearbyMapInfos;
    RobotInfo[] friendliesToComm = null;
    int numTotalSpawned = 0;
    int myTowerNumber;
    int numRoundsLessThanN = 5;

    RobotInfo[] nearbyFriendlies;

    int numTowersLastRound = 0;
    int towersUpdateRound = 0;

    int chipThreshold = 1100;

    UnitType lastSpawnedUnitType;

    int spawnedSoldiers = 0;
    int spawnedMoppers = 0;
    int spawnedSplashers = 0;
    RobotInfo[] enemiesInVision = null;


    public Tower(RobotController rc) throws GameActionException {
        super(rc);
        myTowerNumber = currentNumTotalTowers;
    }

    public void run() throws GameActionException {
        super.run();
        Util.log("TOWER");
        Util.addToIndicatorString("RUN");
        scanSurroundings();
        runAttack();

        enemiesInVision = rc.senseNearbyRobots(-1, oppTeam);

        if (rc.getChips() < chipThreshold) {
            numRoundsLessThanN++;
        } else {
            numRoundsLessThanN = 0;
        }

        if (enemiesInVision.length > 0 && !isSaving()) {
            boolean considerSpawningMopper = false;
            for (RobotInfo enemy : enemiesInVision) {
                if (enemy.getPaintAmount() > 0) {
                    considerSpawningMopper = true;
                    break;
                }
            }

            if (considerSpawningMopper && (spawnedMoppers == 0 || (spawnedMoppers == 1 && rc.getHealth() < 700) || (spawnedMoppers == 2 && rc.getHealth() < 300))) {
                Direction dir = directions[rng.nextInt(directions.length)];
                MapLocation nextLoc = rc.getLocation().add(dir);
                tryBuilding(UnitType.MOPPER, nextLoc);
            }
        }

        if (rc.getRoundNum() < 50 && rc.getNumberTowers() <= 3) {
            if (numTotalSpawned < 2) {
                soldierSpawning();
            }
        } else if (!isSaving()) {
            midGameBots();
        }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
             Util.log("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

        sharedEndFunction();

        // Don't take them all out.
        boolean towersStillUp = rc.getNumberTowers() >= numTowersLastRound;

        if (rc.getPaint() < 100 && rc.getChips() > 2500 && rc.getType() == UnitType.LEVEL_ONE_MONEY_TOWER && towersStillUp) {
            // Make it so that all towers don't go down round after round.
            // Only go down if there's a robot nearby, and you can afford to put the tower back up immediately.
            nearbyFriendlies = rc.senseNearbyRobots(2, rc.getTeam());
            if(nearbyFriendlies.length > 0 && rc.getChips() > 1500) {
                // Only go down if the ruin pattern is complete.
                int diff = Util.getPatternDifference(rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER));

                if (diff != -1) {
                    int paintingCapacity = 0;
                    for (RobotInfo friend : nearbyFriendlies) {
                        if (friend.getType() == UnitType.SOLDIER) {
                            paintingCapacity += friend.getPaintAmount()/6; // dividing by 6 instead of 5 just to be a bit conservative from the soldiers losing paint
                        }
                    }
                    if (paintingCapacity >= diff) {
                        rc.disintegrate();
                    }
                }

            }
        }

        if(rc.getRoundNum() != towersUpdateRound) {
            numTowersLastRound = rc.getNumberTowers();
            towersUpdateRound = rc.getRoundNum();
        }
    }

    public boolean tryBuilding(UnitType unitType, MapLocation location) throws GameActionException {
        if (rc.canBuildRobot(unitType, location)) {
            numTotalSpawned++;
            rc.buildRobot(unitType, location);

            lastSpawnedUnitType = unitType;

            if (unitType == UnitType.SOLDIER) {
                spawnedSoldiers += 1;
            } else if (unitType == UnitType.MOPPER) {
                spawnedMoppers += 1;
            } else if (unitType == UnitType.SPLASHER) {
                spawnedSplashers += 1;
            }


            return true;
        }
        return false;
    }

    public boolean isSaving() {
        if (rc.getRoundNum() < 50 && rc.getNumberTowers() <= 3) {
            return true;
        }
        if (numRoundsLessThanN >= 2) {
            return true;
        }

        if (rc.getChips() >= chipThreshold) {
            return false;
        } else if (rc.getNumberTowers() >= 25) {
            return false;
        } else if (rc.getNumberTowers() <= 1) {
            return true;
        } else {
            return false;
        }
    }

    public void scanSurroundings() throws GameActionException {
        nearbyMapInfos = rc.senseNearbyMapInfos();
        friendliesToComm = rc.senseNearbyRobots(GameConstants.MESSAGE_RADIUS_SQUARED, rc.getTeam());

        // processMessages
        comms.processSectorMessages();
    }

    public void soldierSpawning() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        tryBuilding(UnitType.SOLDIER, nextLoc);
    }

    public void openingBots() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
        }
    }

    public void midGameBots() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        boolean spawned = false;

        boolean enoughPaintForIntended = false;

        if (getMetric() < Constants.TOWER_SPAWNING_THRESHOLD) {
            if (rc.getPaint() >= UnitType.SOLDIER.paintCost) {
                enoughPaintForIntended = true;
            }
            spawned = tryBuilding(UnitType.SOLDIER, nextLoc);

        } else {
            if(rc.getRoundNum() % 3 == 0) {
                if (rc.getPaint() >= UnitType.SOLDIER.paintCost) {
                    enoughPaintForIntended = true;
                }
                spawned = tryBuilding(UnitType.SOLDIER, nextLoc);
            } else {
                if (rc.getPaint() >= UnitType.SPLASHER.paintCost) {
                    enoughPaintForIntended = true;
                }
                spawned = tryBuilding(UnitType.SPLASHER, nextLoc);
            }
        }

        if (!spawned && rc.getChips() >= UnitType.MOPPER.moneyCost && !enoughPaintForIntended && !Util.isPaintTower(rc.getType())) {
            tryBuilding(UnitType.MOPPER, nextLoc);
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
        Util.addToIndicatorString("RA");
        MapLocation target = findBestAttackTarget();
        Util.log("TGT: " + target);
        Util.addToIndicatorString("TGT: " + target);

        if (target != null && rc.canAttack(target)) {
            Util.log("Tower running attack");
            rc.attack(target);
        }
    }
}
