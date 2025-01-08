package basebot;

import battlecode.common.*;

public class Soldier extends Bunny {
    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();

        // TODO
//        if (low on paint) {
//            go towards nearest friendly tower
//        }

        // Sense information about all visible nearby tiles.
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        MapInfo curRuin = null;
        for (MapInfo tile : nearbyTiles){
            if (tile.hasRuin()){
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
        } else if (no marking present && rc.canMarkResourcePattern(rc.getLocation())){


            mark this area?
        }

                no marking present and we can mark this area) {

            mark resource pattern
        }

        if (marked squares nearby) {
            go towards where you want to paint and/or try to paint it
        } else {
            attack enemy tower and/or navigate to some area
        }



//        if (we can paint a marked tile) {
//            paint it
//        } else if (we can move toward a marked tile that we can paint) {
//            move towards it and then paint
//        } else if (we can mark a tower pattern) {
//            mark it
//        } else if (we can mark a resource pattern [that isnt already marked]) {
//            mark it
//        }


        // Search for a nearby ruin to complete.
        MapInfo curRuin = null;
        for (MapInfo tile : nearbyTiles){
            if (tile.hasRuin()){
                curRuin = tile;
            }
        }
        if (curRuin != null){
            MapLocation targetLoc = curRuin.getMapLocation();
            Direction dir = rc.getLocation().directionTo(targetLoc);
            if (rc.canMove(dir))
                rc.move(dir);
            // Mark the pattern we need to draw to build a tower here if we haven't already.
            MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
            if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
                rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                System.out.println("Trying to build a tower at " + targetLoc);
            }
            // Fill in any spots in the pattern with the appropriate paint.
            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                    if (rc.canAttack(patternTile.getMapLocation()))
                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                }
            }
            // Complete the ruin if we can.
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
                rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + targetLoc + "!");
            }
        }

        // Move and attack randomly if no objective.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)){
            rc.move(dir);
        }
        // Try to paint beneath us as we walk to avoid paint penalties.
        // Avoiding wasting paint by re-painting our own tiles.
        MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())){
            rc.attack(rc.getLocation());
        }

        sharedEndFunction();
    }
}
