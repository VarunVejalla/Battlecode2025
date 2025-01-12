package dummy;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Mopper extends Bunny {
    RobotInfo[] actionableOpponents;
    MapInfo[] actionableTiles;
    public Mopper(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
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
    }

}
