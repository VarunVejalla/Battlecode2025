package blitz;

import battlecode.common.RobotController;

public class TowerComms extends Comms {

RobotController rc;
    Robot robot;
    Constants constants;

    public TowerComms(RobotController rc, Robot robot) {
        super(rc, robot);
    }


}
