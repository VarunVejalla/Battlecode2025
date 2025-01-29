package explorepls;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class ExplorationUtils {
    static Bunny bunny;
    static BunnyComms comms;
    static RobotController rc;

    public static MapLocation getExplorationTarget() throws GameActionException {
        // this returns the center of the sector to explore
        double chargeAngle = bunny.getChargeAngle();

        if (chargeAngle > -4) {
            if (chargeAngle > 0) {
                chargeAngle -= Math.PI;
            } else {
                chargeAngle += Math.PI;
            }
        }

//        if (chargeAngle < -4) {
//            chargeAngle = Util.getAngle(bunny.myLoc, new MapLocation(bunny.mapWidth/2, bunny.mapHeight/2));
//        }

        MapLocation sectorCenter = Util.getSectorCenter(Util.getSectorIndex(bunny.myLoc));
        Util.log("My loc: " + bunny.myLoc);
        Util.log("Sector: " + sectorCenter);



        int distance = Integer.MAX_VALUE;
        double angleDiff = Double.MAX_VALUE;
        MapLocation target = null;

        int currDistance;
        double currAngleDiff, currAngle;
        MapLocation currTarget;
        boolean currExplored;

        int[] relevantIndices;
        if (sectorCenter.x < 5 || sectorCenter.y < 5  || sectorCenter.x > bunny.mapWidth - 4 || sectorCenter.y > bunny.mapHeight - 4) {
            relevantIndices = Util.getSectorAndNeighbors(sectorCenter, 3);
        } else {
            relevantIndices = Util.getSectorAndNeighbors(sectorCenter, 2);
        }


        Util.log("" + chargeAngle);
        Util.log("these are unexplored");


        for (int index : relevantIndices) {
            currExplored = comms.explored[index];
            if (currExplored) {
                continue;
            }

            Util.log(Util.getSectorCenter(index).toString());

//            if (!currExplored) {
//                Util.log("found unexplored");
//            }

            currTarget = Util.getSectorCenter(index);
            currDistance = Util.minMovesToReach(currTarget, sectorCenter);
            currAngle = Util.getAngle(currTarget, sectorCenter);

            currAngleDiff = Math.abs(currAngle - chargeAngle);

            if (currAngleDiff > Math.PI) {
                currAngleDiff = 2*Math.PI-currAngleDiff;
            }

            if (currDistance < distance) {
                distance = currDistance;
                angleDiff = currAngleDiff;
                target = currTarget;
            } else if (currDistance == distance && currAngleDiff < angleDiff) {
                target = currTarget;
            }
        }
        Util.log("Exploration target: " + target);
        return target;





    }



}
