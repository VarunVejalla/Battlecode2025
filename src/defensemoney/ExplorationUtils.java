package defensemoney;

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

        MapLocation sectorCenter = Util.getSectorCenter(Util.getSectorIndex(bunny.myLoc));


        boolean isExplored = true;
        int distance = Integer.MAX_VALUE;
        double angleDiff = Double.MAX_VALUE;
        MapLocation target = null;

        int currDistance;
        double currAngleDiff, currAngle;
        MapLocation currTarget;
        boolean currExplored;
        for (int index : Util.getSectorAndNeighbors(sectorCenter, 10)) {
//            comms.myWorld[index]
//            comms.myWorld[index]
            currExplored = (comms.myWorld[index] & 1) != 0; // TODO: how do i get whether i explored this sector?
            if (!isExplored && currExplored) {
                continue;
            }

            if (!currExplored) {
                Util.log("found unexplored");
            }

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
                isExplored = currExplored;
                target = currTarget;
            } else if (currDistance == distance && chargeAngle > -4 && currAngleDiff < angleDiff) {
                isExplored = currExplored;
                target = currTarget;
            }
        }
        return target;





    }



}
