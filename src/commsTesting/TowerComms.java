package commsTesting;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

import java.util.ArrayDeque;
import java.util.Queue;


public class TowerComms extends Comms {

    Tower tower;
    int[] roundLastSeen;


    // note, the queue contains the IDs of the robots we should send the map to
    Queue<Integer> robotsToSendMapTo = new ArrayDeque<>();

    int currentMapRecipientID = -1;
    int currSectorIndex = 0;


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

        // TODO: I think there's a chance that if multiple robots send a map request on the same round
        //  we won't send them all maps because of the line below (specifically rc.getRoundNum() - 1)

        Message[] messages = rc.readMessages(rc.getRoundNum()-1); // Read all messages from this round.
        Util.log("Tower " + rc.getID() + " received " + messages.length + " sector messages");

        for (Message message : messages) {
            if(message.getBytes() == MAP_UPDATE_REQUEST_CODE) {
                Util.log("Received a map request: " + message);

                int senderID = message.getSenderID();
                    robotsToSendMapTo.add(senderID);
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


        // try sending map messages to

        // if you are currently in progress of sending a message, finish sending that message
        if(currentMapRecipientID != -1) {
            sendMap(currentMapRecipientID);
        }

        else if(!robotsToSendMapTo.isEmpty()) {
            currentMapRecipientID = robotsToSendMapTo.poll();
            sendMap(currentMapRecipientID);
        }
        // otherwise, if the queue is not empty, start sending map messages to the next robot in the queue
    }

    /**
     * Sends messages representing the tower's entire map to a specific robot.
     */
    public void sendMap(int robotID) throws GameActionException {
        // TODO: Make this work for larger maps. Currently, this assumes that the whole map can be held in 20 messages.
        int shiftIndex = 0;
        int message = 0;
        // Sectors should be sent in decreasing ID order within each message.

        while(currSectorIndex < sectorCount){
            // Each 32-bit message includes 4 sectors.
            int encodedSector = myWorld[currSectorIndex];
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
            currSectorIndex++;
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
