package boostedboonyComms;

import battlecode.common.*;

public class TowerComms extends Comms {

    Tower tower;

    public TowerComms(RobotController rc, Robot robot, Tower tower) {
        super(rc);
        this.tower = tower;
    }

    /**
     * Finds a robot to receive the message by scanning for nearby friendlies.
     */
    public void findMessageReceiver() throws GameActionException {
        RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo ri : nearbyFriendlies) {
            if (rc.canSendMessage(ri.location)) {
                rc.setIndicatorString("Found receiver: " + ri.location);
                return; // Found a receiver
            }
        }
    }

    /**
     * Processes incoming messages and updates the map representation.
     */
    public void processMessages() throws GameActionException {
        Message[] messages = rc.readMessages(-1); // Read all messages from the queue
        for (Message message : messages) {
            int sectorIndex = (message.getBytes() >> 8) & 0xFF; // Extract sector index (8 bits)
            int sectorData = message.getBytes() & 0xFF; // Extract sector data (8 bits)

            // Update the map representation
            myWorld[sectorIndex] = sectorData;
            rc.setIndicatorString("Processed message for sector " + sectorIndex);
        }
    }

    /**
     * Sends messages representing the map state for the tower's controlled area.
     */
    public void sendMessages() throws GameActionException {
        for (int sectorIndex = 0; sectorIndex < myWorld.length; sectorIndex++) {
            int sectorData = myWorld[sectorIndex];

            // Combine sector index and data into a 32-bit message
            int message = (sectorIndex << 8) | (sectorData & 0xFF);

            // Find a receiver for the message
            RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
            for (RobotInfo ri : nearbyFriendlies) {
                if (rc.canSendMessage(ri.location)) {
                    rc.sendMessage(ri.location, message);
                    rc.setIndicatorString("Sent message for sector " + sectorIndex + " to " + ri.location);
                    break; // Move to the next sector after sending
                }
            }
        }
    }

    /**
     * Sends a specific message to a robot.
     */
    public void sendMessageToRobot(MapLocation loc, int data) throws GameActionException {
        if (rc.canSendMessage(loc)) {
            rc.sendMessage(loc, data);
            rc.setIndicatorString("Sent message to robot at " + loc);
        }
    }
}
