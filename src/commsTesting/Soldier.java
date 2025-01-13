package commsTesting;

import battlecode.common.*;

public class Soldier extends Bunny {

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);

    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
        moveLogic();
    }

    /**
     * Attempt to paint or attack nearby tiles if possible.
     */
    public void paintOrAttack() throws GameActionException {

    }

    /**
     * Choose where to move:
     */
    public void moveLogic() throws GameActionException {
        // move up.
        if(comms.waitingForMap){ // don't move if we're waiting to receive a map from a tower
            Util.log("Soldier @ " + rc.getLocation() + ". Pausing movement because I'm waiting for a map!");
            return;
        }


        if(comms.lastMapUpdate > 70){
            nav.goTo(new MapLocation(40, 40), 0);
        }

        else if(rc.getRoundNum() < 40) {
            nav.goTo(new MapLocation(0, 40), 0);
        } else if(rc.getRoundNum() < 100) {
            nav.goTo(new MapLocation(7, 5), 0);
        }
        else if(rc.getRoundNum() >= 74) {
            Util.log("Bunny " + rc.getID() + " World: \n");
            comms.describeWorld();
        }
    }

}