package commsTesting;

import battlecode.common.*;

public class BunnyComms extends Comms {

//    public final int MESSAGE_BUFFER_SIZE = 5;


//    public final int MAP_COOLDOWN = 100;
    public final int MAP_COOLDOWN = 50;
    public int lastMapUpdate = 0;

    public final int BUFFER_COOLDOWN = 10;
    public int lastBufferUpdate = 0;

    public final int NUM_ROUNDS_TO_WAIT_FOR_MAP_UPDATE = 4;


    // Kind of annoying way of handling larger maps.
    public int sectorStartIndex = 0;

    // -1 is used to represent empty messages.
    public int[] messageBuffer = {-1,-1,-1,-1,-1}; // Buffer fills up at 5 sectors. Hardcoded for bytecode.
    public int messageBufferIndex = 0; // Stores the first invalid index.
    public int messagesTransmitted = 0;

    boolean waitingForMap = false;
    public int mapRequestRound = -1;

    public BunnyComms(RobotController rc, Robot robot) {
        super(rc);
    }

    public void updateBunnyBuffer(int roundNum, int sectorID, int msg) throws GameActionException {
        assert roundNum <= 2001;
        assert sectorID < 144;
        messageBuffer[messageBufferIndex] = (roundNum << 16) + (sectorID << 8) + msg;
        Util.log("RoundNum = " + roundNum + ", sectorID = " + sectorID + ", msg = " + msg);
        Util.log("Bunny buffer updated to " + messageBuffer[messageBufferIndex]);
        messageBufferIndex++;
        messageBufferIndex %= messageBuffer.length;
        Util.log("New buffer index: " + messageBufferIndex);
    }

    /**
     * To be called when there is a friendly tower nearby. Chooses to send either buffer information or map request.
     */
    public void sendMessages(RobotInfo tower) throws GameActionException {

        // When the buffer cooldown expires, prioritize sending bunny memories.
        if(rc.getRoundNum() - lastBufferUpdate > BUFFER_COOLDOWN) {
            sendBufferUpdateMessage(tower);
        } else {
            Util.log("Robot " + rc.getID() + ": Buffer transmission is already complete. Skipping retransmission.");
        }

        // TODO: Large maps require two requests for the map. Right now this code only does one.
        // Otherwise, when the map cooldown expires, request a map.
        if(rc.getRoundNum() - lastMapUpdate > MAP_COOLDOWN) {
            // Map cooldown has expired, request a map.
            sendMapUpdateRequestMessage(tower);
        } else {
            Util.log("BunnyComms map is fresh enough! (no map request sent!)");
            Util.log("Last Map Update: " + lastMapUpdate + ", Current Round: " + rc.getRoundNum());
        }
    }


    /**
     * Bunny sends a tower the next entry in its buffer.
     */
    public void sendBufferUpdateMessage(RobotInfo tower) throws GameActionException {
        // Send messages until messagesTransmitted is the size of the buffer.
        // You can only send one message per round. If there's no messages left in the buffer, request a map!
        if(messagesTransmitted < messageBuffer.length && messageBuffer[messagesTransmitted] != -1) {
            if(rc.canSendMessage(tower.getLocation())) {

                // Send the next message in the buffer.
                rc.sendMessage(tower.getLocation(), messageBuffer[messagesTransmitted]);
                Util.log("BunnyComms sendMessages successful to " + tower.getLocation());

                // Shift to next index of the buffer to transmit.
                messagesTransmitted++;
            } else {
                Util.log("BunnyComms sendMessages failed for " + tower.getLocation());
            }
        }
        else {
            // Buffer transfer is complete and cooldown is reset.
            messagesTransmitted = 0;
            lastBufferUpdate = rc.getRoundNum();
        }
    }


    /**
     * Bunny sends a request to a tower for a map.
     */
    public void sendMapUpdateRequestMessage(RobotInfo tower) throws GameActionException {
        if(rc.canSendMessage(tower.getLocation())) {
            rc.sendMessage(tower.getLocation(), MAP_UPDATE_REQUEST_CODE);
            Util.log("BunnyComms requested map from " + tower.getLocation());

            // Map transfer is complete and cooldown is reset.
            mapRequestRound = rc.getRoundNum();

            // Boolean updated to constrain motion and wait for map.
            waitingForMap = true;

        } else {
            Util.log("BunnyComms couldn't request map from " + tower.getLocation());
        }
    }

    /**
     * Processes map updates and acts on received data.
     */
    // TODO: For large maps processMap needs to be called twice. Make sure that this is being done properly.
    // For large maps we need to run this twice. sectorStartIndex stores the shift.
    public void processMap() throws GameActionException {
        // Queue should be max 20 messages describing (first half of) the map.
        Message[] messages = rc.readMessages(rc.getRoundNum());
        Util.log("Bunny " + rc.getID() + " received " + messages.length + " map messages");

        if(messages.length == 0){
            return;
        }

        int sectorIndex = sectorStartIndex;
        int mapMessageCount = 0;
        for(Message message : messages) {
            int msgBytes = message.getBytes();
            for(int bitshift = 0; bitshift < 4; bitshift++) {
                // If there are still sectors to process, we should update the map.
                if(sectorIndex < sectorCount) {
                    // TODO: Keep track of notable sectors here
                    // Always take the towers map.
                    // sectorCount - 1 is the greatest index achieved
                    Util.log("Sector being updated: " + sectorIndex + "/" + (sectorCount-1));
                    myWorld[sectorIndex] = msgBytes & 0xFF;
                    msgBytes >>>= 8;
                    sectorIndex++;
                }
                else {
                    // Only map sectors are processed. Anything sent after is thrown away.
                    break;
                }
            }
            mapMessageCount++;
            Util.log("Bunny processed " + mapMessageCount+" map messages");
        }

        Util.log("Bunny has finished processing it's new map.");
        Util.logArray("Bunny's new world", myWorld);
        if(sectorIndex >= sectorCount) {
            sectorStartIndex = 0; // Map is complete, reset to start of map.
        }
        Util.log("BunnyComms successfully loaded new map!");

        lastMapUpdate = rc.getRoundNum(); // set the last map update to the current round.
        waitingForMap = false; // set waiting for map to false to unlock regular moveLogic behavior

    }
}
