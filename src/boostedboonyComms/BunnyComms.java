package boostedboonyComms;

import battlecode.common.*;

public class BunnyComms extends Comms {

//    public final int MESSAGE_BUFFER_SIZE = 5;
    public final int MAP_COOLDOWN = 0;
    public int lastMapUpdate = 0;

    // Kind of annoying way of handling larger maps.
    public int sectorStartIndex = 0;

    // -1 is used to represent empty messages.
    public int[] messageBuffer = {-1,-1,-1,-1,-1}; // Buffer fills up at 5 sectors. Hardcoded for bytecode.
    public int messageBufferIndex = 0; // Stores the first invalid index.
    public boolean bufferIsEmpty = true;


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

        bufferIsEmpty = false;
    }

    // If there's a friendly tower nearby, send all messages in the buffer.
    // Goes through the buffer backwards. Retracing steps because the current index is the first invalid.
    public void sendMessages(RobotInfo tower) throws GameActionException {
        // Go back one for the first valid index.
        messageBufferIndex--;
        // Remember that % is remainder, not modulus in Java.
        if(messageBufferIndex < 0) {
            messageBufferIndex += messageBuffer.length;
        }

        // You can only send one message per round. If there's no messages left in the buffer, request a map!
        if(messageBuffer[messageBufferIndex] != -1) {
            if(rc.canSendMessage(tower.getLocation())) {
                rc.sendMessage(tower.getLocation(), messageBuffer[messageBufferIndex]);
                messageBuffer[messageBufferIndex] = -1;
                messageBufferIndex--;
                if(messageBufferIndex < 0) {
                    messageBufferIndex += messageBuffer.length;
                }
                Util.log("BunnyComms sendMessages successful!! to " + tower.getLocation());

            } else {
                Util.log("BunnyComms sendMessages failed for " + tower.getLocation());
            }
        }
        else {
            // Add back one for the first invalid index.
            messageBufferIndex++;
            messageBufferIndex %= messageBuffer.length;
            bufferIsEmpty = true;
        }

        // When finished sending the buffer messages, request the map.
        // TODO: Large maps require two requests for the map. Right now this code only does one.
        // If you need a map update, request one.
        if((rc.getRoundNum() - lastMapUpdate < MAP_COOLDOWN)) {
            // Didn't need a map!
            Util.log("BunnyComms map is fresh enough! (no map request sent!)");
        } else {
            if(rc.canSendMessage(tower.getLocation())) {
                rc.sendMessage(tower.getLocation(), MAP_UPDATE_REQUEST_CODE);
                Util.log("BunnyComms requested map from " + tower.getLocation());
            } else {
                Util.log("BunnyComms couldn't request map from " + tower.getLocation());
            }
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

    }
}
