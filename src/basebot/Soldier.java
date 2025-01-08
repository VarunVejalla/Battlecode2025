package basebot;

import battlecode.common.*;

public class Soldier extends Bunny {
    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        // TODO
        // if (low on paint) {
        // go towards nearest friendly tower
        // }

        // Sense information about all visible nearby tiles.
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        MapInfo curRuin = null;
        for (MapInfo tile : nearbyTiles) {
            if (tile.hasRuin()) {
                curRuin = tile;
                break;
            }
        }

        if (curRuin != null) {
            // TODO: optimize this for bytecode, since we are only sensing four squares
            MapInfo[] tilesNearRuin = rc.senseNearbyMapInfos(curRuin.getMapLocation(), 1);
            boolean markedRuin = false;
            for (MapInfo tile : tilesNearRuin) {
                if (tile.getMark() == PaintType.ALLY_PRIMARY) {
                    markedRuin = true;
                }
            }
            if (!markedRuin) {
                if (curRuin.getMapLocation().distanceSquaredTo(rc.getLocation()) > 2) {
                    nav.goTo(curRuin.getMapLocation(), 2);
                }

                if (curRuin.getMapLocation().distanceSquaredTo(rc.getLocation()) <= 2) {
                    // TODO: should the first argument be different depending on what we want here?
                    rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, curRuin.getMapLocation());
                }
            }
        } else if ( rc.canMarkResourcePattern(rc.getLocation())) {
            // if no marking present and the marking we would do doesnt conflict with already painted

            boolean shouldMark = true;
            MapInfo[] tilesNearMe = rc.senseNearbyMapInfos(8);
            for (MapInfo tile : tilesNearMe ) {
                if (tile.getMark().isAlly()) {
                    shouldMark = false;
                    break;
                }
            }
            if (shouldMark) {
                rc.markResourcePattern(rc.getLocation());
            }
        }

        // no marking present and we can mark this area) {

        // mark resource pattern
        // }

        MapInfo[] actionableTiles = rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared);

        if (!rc.isMovementReady() && !rc.isActionReady()) {
            sharedEndFunction();
            return;
        } else if (rc.isActionReady()) {
            // TODO: prioritize the tower pattern and/or almost finished patterns
            // TODO: paint if conflicting pattern was painted?
            for (MapInfo tile : actionableTiles) {
                if (tile.getMark().isAlly() && tile.getPaint() == PaintType.EMPTY) {
                    if (rc.canAttack(tile.getMapLocation())) {
                        rc.attack(tile.getMapLocation(), tile.getMark() == PaintType.ALLY_SECONDARY);
                        break;
                    }
                }
            }
        }

        if (!rc.isMovementReady() && !rc.isActionReady()) {
            sharedEndFunction();
            return;
        } else if (!rc.isMovementReady()) {
            // TODO: prioritize the tower pattern and/or almost finished patterns
            // TODO: paint if conflicting pattern was painted?

            MapLocation toPaint = null;

            for (MapInfo tile : actionableTiles) {
                if (tile.getMark().isAlly() && tile.getPaint() == PaintType.EMPTY) {
                    if (rc.canAttack(tile.getMapLocation())) {
                        rc.attack(tile.getMapLocation(), tile.getMark() == PaintType.ALLY_SECONDARY);
                        break;
                    }
                }  else if (toPaint == null && tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                    toPaint = tile.getMapLocation();
                }
            }

            if (rc.isActionReady() && toPaint != null) {
                rc.attack(toPaint);
            }

            sharedEndFunction();
            return;
        }


        MapInfo[] visionTiles = rc.senseNearbyMapInfos();
        int bestDistance = Integer.MAX_VALUE;
        MapLocation bestLocation = null;
        for (MapInfo tile : visionTiles) {
            if (tile.getMark().isAlly() && tile.getPaint() == PaintType.EMPTY) {
                int newDistance = Math.max(Math.abs(tile.getMapLocation().x - rc.getLocation().x),
                        Math.abs(tile.getMapLocation().y - rc.getLocation().y));
                if (newDistance < bestDistance) {
                    bestDistance = newDistance;
                    bestLocation = tile.getMapLocation();
                }

            }
        }
        if (bestLocation != null) {
            nav.goTo(bestLocation, UnitType.SOLDIER.actionRadiusSquared);
        } else {
            nav.moveRandom();
        }

        if (!rc.isActionReady()) {
            sharedEndFunction();
            return;
        } else {
            actionableTiles = rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared);

            // TODO: prioritize the tower pattern and/or almost finished patterns
            // TODO: paint if conflicting pattern was painted?
            MapLocation toPaint = null;
            for (MapInfo tile : actionableTiles) {
                if (tile.getMark().isAlly() && tile.getPaint() == PaintType.EMPTY) {
                    if (rc.canAttack(tile.getMapLocation())) {
                        rc.attack(tile.getMapLocation(), tile.getMark() == PaintType.ALLY_SECONDARY);
                        break;
                    }
                }  else if (toPaint == null && tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                    toPaint = tile.getMapLocation();
                }
            }

            if (rc.isActionReady() && toPaint != null) {
                rc.attack(toPaint);
            }

            sharedEndFunction();
            return;
        }
    }
}
