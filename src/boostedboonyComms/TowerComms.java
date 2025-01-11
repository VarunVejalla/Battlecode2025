package boostedboonyComms;

import battlecode.common.*;

public class TowerComms extends Comms {


    Tower tower;

    public TowerComms(RobotController rc, Robot robot, Tower tower) {
        super(rc, robot);
        this.tower = tower;


    }

    public void findMessageReceiver(){

    }

    public void processMessages() throws GameActionException{

    }

    public void sendMessages() throws GameActionException {
        for(RobotInfo ri: this.tower.friendliesToComm){
            MapLocation loc = ri.location;

            if(rc.canSendMessage(loc)) {
                rc.sendMessage(loc, 69);
            }
        }
    }

    public void sendMessageToRobot(){

    }


}
