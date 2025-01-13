package boostedboonyComms;

import battlecode.common.*;
import scala.collection.Map;

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
        if(rc.getRoundNum() < 30) {
            nav.goTo(new MapLocation(0, 30), 0);
        } else if(rc.getRoundNum() < 60) {
            nav.goTo(new MapLocation(0, 0), 0);
        }
    }

}