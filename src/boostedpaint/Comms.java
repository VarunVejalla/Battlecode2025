package boostedpaint;

import battlecode.common.RobotController;

public class Comms {

    RobotController rc;
    Robot robot;
    Constants constants;


    public Comms(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;
    }
}