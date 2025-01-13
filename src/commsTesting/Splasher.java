package commsTesting;

import battlecode.common.*;

public class Splasher extends Bunny {

    public Splasher(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run(); // Call shared logic for all bunnies
    }
}
