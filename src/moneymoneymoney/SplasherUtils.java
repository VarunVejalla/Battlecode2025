package moneymoneymoney;

import battlecode.common.*;

public class SplasherUtils {
    static RobotController rc;
    static Splasher splasher;

    /* 6 represents current location.
        x x 12 x x
        x 9 10 11 x
        4 5 6 7 8
        x 1 2 3 x
        x x 0 x x
    */

    public static MapLocation indexToLocation(int index, MapLocation currLoc) {
        switch (index) {
            case 0:
                return new MapLocation(currLoc.x, currLoc.y - 2);
            case 1:
                return new MapLocation(currLoc.x - 1, currLoc.y - 1);
            case 2:
                return new MapLocation(currLoc.x, currLoc.y - 1);
            case 3:
                return new MapLocation(currLoc.x + 1, currLoc.y - 1);
            case 4:
                return new MapLocation(currLoc.x - 2, currLoc.y);
            case 5:
                return new MapLocation(currLoc.x - 1, currLoc.y);
            case 6:
                return new MapLocation(currLoc.x, currLoc.y);
            case 7:
                return new MapLocation(currLoc.x + 1, currLoc.y);
            case 8:
                return new MapLocation(currLoc.x + 2, currLoc.y);
            case 9:
                return new MapLocation(currLoc.x - 1, currLoc.y + 1);
            case 10:
                return new MapLocation(currLoc.x, currLoc.y + 1);
            case 11:
                return new MapLocation(currLoc.x + 1, currLoc.y + 1);
            case 12:
                return new MapLocation(currLoc.x, currLoc.y + 2);
            default:
                throw new IndexOutOfBoundsException("Index out of bounds");
        }
    }


    public static int[] calculateAdjacencyCounts() throws GameActionException {
        int index;
        int[] counts_arr = new int[26];

        index = Util.getMapInfoIndex(-4, 0);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-3, -1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[1]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[17]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-3, 0);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[5]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[17]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-3, 1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[9]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[17]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-2, -2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[1]++;
                    counts_arr[0]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[14]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-2, -1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[1]++;
                    counts_arr[5]++;
                    counts_arr[2]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[17]++;
                    counts_arr[14]++;
                    counts_arr[18]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-2, 0);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[1]++;
                    counts_arr[5]++;
                    counts_arr[9]++;
                    counts_arr[6]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[17]++;
                    counts_arr[14]++;
                    counts_arr[18]++;
                    counts_arr[22]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-2, 1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[5]++;
                    counts_arr[9]++;
                    counts_arr[10]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[17]++;
                    counts_arr[18]++;
                    counts_arr[22]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-2, 2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[9]++;
                    counts_arr[12]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[22]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-1, -3);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[1]++;
                    counts_arr[0]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[13]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-1, -2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[1]++;
                    counts_arr[5]++;
                    counts_arr[0]++;
                    counts_arr[2]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[14]++;
                    counts_arr[13]++;
                    counts_arr[15]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-1, -1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[1]++;
                    counts_arr[5]++;
                    counts_arr[9]++;
                    counts_arr[0]++;
                    counts_arr[2]++;
                    counts_arr[6]++;
                    counts_arr[3]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[17]++;
                    counts_arr[14]++;
                    counts_arr[18]++;
                    counts_arr[13]++;
                    counts_arr[15]++;
                    counts_arr[19]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-1, 0);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[1]++;
                    counts_arr[5]++;
                    counts_arr[9]++;
                    counts_arr[2]++;
                    counts_arr[6]++;
                    counts_arr[10]++;
                    counts_arr[7]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[17]++;
                    counts_arr[14]++;
                    counts_arr[18]++;
                    counts_arr[22]++;
                    counts_arr[15]++;
                    counts_arr[19]++;
                    counts_arr[23]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-1, 1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[1]++;
                    counts_arr[5]++;
                    counts_arr[9]++;
                    counts_arr[6]++;
                    counts_arr[10]++;
                    counts_arr[12]++;
                    counts_arr[11]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[17]++;
                    counts_arr[18]++;
                    counts_arr[22]++;
                    counts_arr[19]++;
                    counts_arr[23]++;
                    counts_arr[25]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-1, 2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[5]++;
                    counts_arr[9]++;
                    counts_arr[10]++;
                    counts_arr[12]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[22]++;
                    counts_arr[23]++;
                    counts_arr[25]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(-1, 3);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[9]++;
                    counts_arr[12]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[25]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(0, -4);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[0]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(0, -3);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[0]++;
                    counts_arr[2]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[13]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(0, -2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[1]++;
                    counts_arr[0]++;
                    counts_arr[2]++;
                    counts_arr[6]++;
                    counts_arr[3]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[14]++;
                    counts_arr[13]++;
                    counts_arr[15]++;
                    counts_arr[16]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(0, -1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[1]++;
                    counts_arr[5]++;
                    counts_arr[0]++;
                    counts_arr[2]++;
                    counts_arr[6]++;
                    counts_arr[10]++;
                    counts_arr[3]++;
                    counts_arr[7]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[14]++;
                    counts_arr[18]++;
                    counts_arr[13]++;
                    counts_arr[15]++;
                    counts_arr[19]++;
                    counts_arr[16]++;
                    counts_arr[20]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(0, 0);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[4]++;
                    counts_arr[1]++;
                    counts_arr[5]++;
                    counts_arr[9]++;
                    counts_arr[0]++;
                    counts_arr[2]++;
                    counts_arr[6]++;
                    counts_arr[10]++;
                    counts_arr[12]++;
                    counts_arr[3]++;
                    counts_arr[7]++;
                    counts_arr[11]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[14]++;
                    counts_arr[18]++;
                    counts_arr[22]++;
                    counts_arr[15]++;
                    counts_arr[19]++;
                    counts_arr[23]++;
                    counts_arr[16]++;
                    counts_arr[20]++;
                    counts_arr[24]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(0, 1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[5]++;
                    counts_arr[9]++;
                    counts_arr[2]++;
                    counts_arr[6]++;
                    counts_arr[10]++;
                    counts_arr[12]++;
                    counts_arr[7]++;
                    counts_arr[11]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[18]++;
                    counts_arr[22]++;
                    counts_arr[19]++;
                    counts_arr[23]++;
                    counts_arr[25]++;
                    counts_arr[20]++;
                    counts_arr[24]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(0, 2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[9]++;
                    counts_arr[6]++;
                    counts_arr[10]++;
                    counts_arr[12]++;
                    counts_arr[11]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[22]++;
                    counts_arr[23]++;
                    counts_arr[25]++;
                    counts_arr[24]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(0, 3);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[10]++;
                    counts_arr[12]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[25]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(0, 4);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[12]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(1, -3);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[0]++;
                    counts_arr[3]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[13]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(1, -2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[0]++;
                    counts_arr[2]++;
                    counts_arr[3]++;
                    counts_arr[7]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[13]++;
                    counts_arr[15]++;
                    counts_arr[16]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(1, -1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[1]++;
                    counts_arr[0]++;
                    counts_arr[2]++;
                    counts_arr[6]++;
                    counts_arr[3]++;
                    counts_arr[7]++;
                    counts_arr[11]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[13]++;
                    counts_arr[15]++;
                    counts_arr[19]++;
                    counts_arr[16]++;
                    counts_arr[20]++;
                    counts_arr[21]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(1, 0);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[5]++;
                    counts_arr[2]++;
                    counts_arr[6]++;
                    counts_arr[10]++;
                    counts_arr[3]++;
                    counts_arr[7]++;
                    counts_arr[11]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[15]++;
                    counts_arr[19]++;
                    counts_arr[23]++;
                    counts_arr[16]++;
                    counts_arr[20]++;
                    counts_arr[24]++;
                    counts_arr[21]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(1, 1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[9]++;
                    counts_arr[6]++;
                    counts_arr[10]++;
                    counts_arr[12]++;
                    counts_arr[3]++;
                    counts_arr[7]++;
                    counts_arr[11]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[19]++;
                    counts_arr[23]++;
                    counts_arr[25]++;
                    counts_arr[20]++;
                    counts_arr[24]++;
                    counts_arr[21]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(1, 2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[10]++;
                    counts_arr[12]++;
                    counts_arr[7]++;
                    counts_arr[11]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[23]++;
                    counts_arr[25]++;
                    counts_arr[24]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(1, 3);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[12]++;
                    counts_arr[11]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[25]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(2, -2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[0]++;
                    counts_arr[3]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[16]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(2, -1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[2]++;
                    counts_arr[3]++;
                    counts_arr[7]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[16]++;
                    counts_arr[20]++;
                    counts_arr[21]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(2, 0);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[6]++;
                    counts_arr[3]++;
                    counts_arr[7]++;
                    counts_arr[11]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[16]++;
                    counts_arr[20]++;
                    counts_arr[24]++;
                    counts_arr[21]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(2, 1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[10]++;
                    counts_arr[7]++;
                    counts_arr[11]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[20]++;
                    counts_arr[24]++;
                    counts_arr[21]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(2, 2);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[12]++;
                    counts_arr[11]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[24]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(3, -1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[3]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[21]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(3, 0);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[7]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[21]++;
                    break;
            }
        }


        index = Util.getMapInfoIndex(3, 1);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[11]++;
                    counts_arr[8]++;
                    break;
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    counts_arr[21]++;
                    break;
            }
        }

        index = Util.getMapInfoIndex(4, 0);
        if(splasher.nearbyMapInfos[index] != null) {
            switch(splasher.nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    counts_arr[8]++;
                    break;
            }
        }

        return counts_arr;
    }


}
