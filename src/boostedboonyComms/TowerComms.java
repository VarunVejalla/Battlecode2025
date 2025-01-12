package boostedboonyComms;

import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class TowerComms extends Comms {

    Tower tower;
    int[] roundLastSeen;

    public TowerComms(RobotController rc, Robot robot, Tower tower) {
        super(rc);
        this.tower = tower;
        roundLastSeen = new int[sectorCount];
    }

    /**
     * Processes incoming messages and updates the map representation.
     */
    public void processSectorMessages() throws GameActionException {
        // The buffer makes sense: what if the tower is called after?
        // Don't want to check the whole queue because then elements in the buffer will be processed multiple times.
        Message[] messages = rc.readMessages(rc.getRoundNum()-1); // Read all messages from this round.
        Util.log("Tower " + rc.getID() + " received " + messages.length + " sector messages");

        for (Message message : messages) {
            if(message.getBytes() == MAP_UPDATE_REQUEST_CODE) {
                Util.log("Received a map request: " + message);
                sendMap(message.getSenderID());
            }

            // Otherwise, it's potential new sector info.
            else {
                // Decode the sector message.
                int roundNum = message.getBytes() >> 16;
                int sectorID = (message.getBytes() & 0xFFFF) >> 8;
                int msg = message.getBytes() & 0xFF;

                Util.log("Received a sector update from robot: " + message.getSenderID() + "\n");
                Util.log("Contents: " + roundNum + ", " + sectorID + ", " + msg + "\n");
                Util.log("Sector Center: " + getSectorCenter(sectorID));
                Util.log("-------------------------------------");
                Util.log(Util.getSectorDescription(sectorID));

                // If the last time I saw this sector is older, update my world.
                if (roundLastSeen[sectorID] < roundNum) {
                    myWorld[sectorID] = msg;
                    roundLastSeen[sectorID] = roundNum;
                }
                Util.logArray("Tower world", myWorld);
                Util.logArray("Last Round Seen", roundLastSeen);
            }

        }
    }

    /**
     * Sends messages representing the tower's entire map to a specific robot.
     */
    public void sendMap(int robotID) throws GameActionException {
        // TODO: Make this work for larger maps. Currently, this assumes that the whole map can be held in 20 messages.
        int shiftIndex = 0;
        int message = 0;
        // Sectors should be sent in decreasing ID order within each message.
        for (int encodedSector : myWorld) {
            // Each 32-bit message includes 4 sectors.
            message += encodedSector << shiftIndex;
            shiftIndex += 8;

            if (shiftIndex == 24) {
                // Send the message once 32-bits are filled.
                if (rc.canSenseRobot(robotID)) {
                    rc.sendMessage(rc.senseRobot(robotID).getLocation(), message);
                    Util.log("Sent a map message to robot: " + robotID);
                    shiftIndex = 0;
                    message = 0;
                } else {
                    Util.log("Tower couldn't find the robot that requested a map!!");
                }
            }
        }

        // TODO: Check to make that it's never intended to send 0
        // If there's a non-blank message left over, send it.
        if(message != 0) {
            if (rc.canSenseRobot(robotID)) {
                rc.sendMessage(rc.senseRobot(robotID).getLocation(), message);
            } else {
                Util.log("Tower couldn't find the robot that requested a map!!");
            }
        }

    }


}
