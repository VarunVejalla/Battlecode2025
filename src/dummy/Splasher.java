package dummy;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Splasher extends Bunny {

    public Splasher(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run(); // Call shared logic for all bunnies
    }
}
