package boostedboonyComms;

import battlecode.common.*;

public class BunnyComms extends Comms {

    public BunnyComms(RobotController rc, Robot robot) {
        super(rc);
    }

    /**
     * Processes received messages and updates the robot's local map or state.
     */
    public void receiveMessages() throws GameActionException {
        Message[] messageBuffer = rc.readMessages(-1);
        for (Message message : messageBuffer) {
            int sectorIndex = (message.getBytes() >> 8) & 0xFF; // Extract sector index (8 bits)
            int sectorData = message.getBytes() & 0xFF; // Extract sector data (8 bits)

            // Update the local map representation
            myWorld[sectorIndex] = sectorData;
            rc.setIndicatorString("Received message for sector " + sectorIndex);
        }
    }

    /**
     * Sends a request for updated map information to nearby towers.
     */
    public void requestMapUpdate() throws GameActionException {
        RobotInfo[] nearbyTowers = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo tower : nearbyTowers) {
            if (tower.getType().isTowerType() && rc.canSendMessage(tower.location)) {
                rc.sendMessage(tower.location, 0xFFFF); // Special code to request map update
                rc.setIndicatorString("Requested map update from tower at " + tower.location);
                return;
            }
        }
    }

    /**
     * Sends a specific message to a targeted robot.
     */
    public void sendMessageToRobot(MapLocation loc, int data) throws GameActionException {
        if (rc.canSendMessage(loc)) {
            rc.sendMessage(loc, data);
            rc.setIndicatorString("Sent message to robot at " + loc);
        }
    }

    /**
     * Processes map updates and acts on received data.
     */
    public void processMapUpdates() throws GameActionException {
        for (int sectorIndex = 0; sectorIndex < myWorld.length; sectorIndex++) {
            int sectorData = myWorld[sectorIndex];
            if (sectorData != 0) { // Non-zero indicates meaningful data
                rc.setIndicatorString("Processing sector " + sectorIndex + " with data: " + sectorData);
                // Implement any specific logic for acting on the map update
            }
        }
    }
}
