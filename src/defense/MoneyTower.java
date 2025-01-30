package defense;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class MoneyTower extends Tower {
    public MoneyTower(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        Util.log("MONEY TOWER");
    }
}
