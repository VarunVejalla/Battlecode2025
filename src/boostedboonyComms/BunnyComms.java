package boostedboonyComms;

import battlecode.common.*;

public class BunnyComms extends Comms {
    public BunnyComms(RobotController rc, Robot robot) {
        super(rc, robot);
    }



    public void receiveMessages(){
        Message[] messageBuffer = rc.readMessages(-1);
        for(Message message : messageBuffer){
            System.out.println(message);
        }
    }
}
