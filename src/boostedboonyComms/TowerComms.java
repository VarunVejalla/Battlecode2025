package boostedboonyComms;

import battlecode.common.*;

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
        Message[] messages = rc.readMessages(-1); // Read all messages from the queue
        for (Message message : messages) {
            if(message.getBytes() == MAP_UPDATE_REQUEST_CODE) {
                sendMap(message.getSenderID());
            }


            // Otherwise, it's potential new sector info.
            else {
                // Decode the sector message.
                int roundNum = message.getBytes() >> 16;
                int sectorID = (message.getBytes() & 0xFFFF) >> 8;
                int msg = message.getBytes() & 0xFF;

                // If the last time I saw this sector is older, update my world.
                if (roundLastSeen[sectorID] < roundNum) {
                    myWorld[sectorID] = msg;
                    roundLastSeen[sectorID] = roundNum;
                }
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
