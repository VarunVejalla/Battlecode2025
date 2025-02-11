package moneybenchmark6;

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

    public void decryptSectorMessage(int message) {
        int roundNum = rc.getRoundNum()-(message >> 12)*10;
        int sectorID = (message & 0xFFF) >> 4;
        int info = message & 0xF;

        // If the last time I saw this sector is older, update my world.
        if (roundLastSeen[sectorID] < roundNum) {
            myWorld[sectorID] = (myWorld[sectorID] & 0xF0) + info;
            roundLastSeen[sectorID] = roundNum;
        }
    }

    /**
     * Processes incoming messages and updates the map representation.
     */
    public void processSectorMessages() throws GameActionException {

        // Don't want to check the whole queue because then elements in the buffer will be processed multiple times.
        Message[] messages = rc.readMessages(rc.getRoundNum()-1); // Read all messages from last round.

        if(messages.length > 0) {
            // Util.log("Tower " + rc.getID() + " received " + messages.length + " sector messages");
        }

        boolean hasSentMap = false;
        for (Message message : messages) {
            if (message.getBytes() == MAP_UPDATE_REQUEST_CODE) {
                // Util.log("Received a map request: " + message);
                if(!hasSentMap){
                    sendMap1(message.getSenderID());
                    hasSentMap = true;
                    Util.addToIndicatorString("sent map");
                }
            }
            else if (message.getBytes() == MAP2_UPDATE_REQUEST_CODE) {
                // Util.log("Received a map 2 request: " + message);
                if(!hasSentMap) {
                    sendMap2(message.getSenderID());
                    hasSentMap = true;
                }
            }

            // Otherwise, it's potential new sector info.
            else {
                int message1 = message.getBytes() >> 16;
                int message2 = message.getBytes() & 0xFFFF;

                // Decode the first message.
                if(message1 != NULL_MESSAGE) {
                    decryptSectorMessage(message1);
                }

                // Decode the second message.
                if(message2 != NULL_MESSAGE) {
                    decryptSectorMessage(message2);
                }


            }

        }
    }

    /**
     * Sends messages representing the tower's entire map to a specific robot.
     */
    public void sendMap(int robotID, int startSector, int endSector) throws GameActionException {
        if (!rc.canSenseRobot(robotID)) {
            // Util.log("Tower couldn't find Robot " + robotID + " who requested a map!!");
            return;
        }

        if(!rc.canSendMessage(rc.senseRobot(robotID).getLocation())) {
            // Util.log("Tower couldn't send Robot " + robotID + " their message!! (bad connection)");
            return;
        }

        int shiftIndex = 0;
        int message = 0;

        for (int sectorIndex = startSector; sectorIndex < endSector && sectorIndex < sectorCount; sectorIndex++) {
            message |= (myWorld[sectorIndex] << shiftIndex);
            shiftIndex += 8;

            if (shiftIndex == 32) {
                rc.sendMessage(rc.senseRobot(robotID).getLocation(), message);
                // Util.log("Sent a map message to robot: " + robotID);
                shiftIndex = 0;
                message = 0;
            }
        }

        // Send any remaining message
        if (message != 0) {
            rc.sendMessage(rc.senseRobot(robotID).getLocation(), message);
            // Util.log("Sent a map message to robot: " + robotID);
        }
    }

    // Usage examples
    public void sendMap1(int robotID) throws GameActionException {
        sendMap(robotID, 0, MAX_MAP_SECTORS_SENT_PER_ROUND);
    }

    public void sendMap2(int robotID) throws GameActionException {
        assert sectorCount >= MAX_MAP_SECTORS_SENT_PER_ROUND;
        sendMap(robotID, MAX_MAP_SECTORS_SENT_PER_ROUND, sectorCount);
    }



}