package bytecodefixed;

import battlecode.common.MapLocation;

public class LocalView {
    MapLocation currentLocation;


//    // might want to make these more than one long each if we want to expand how far we allow the resource pattern center to be
//    long validResourceBitstring = -1;
//    long unfinishedResourceBitstring = 0;
//
//    MapInfo[] nearbyWorld = new MapInfo[169];
//
//    RobotController rc;
//    Robot robot;
//    public LocalView(RobotController rc, Robot robot) {
//        this.rc = rc;
//        this.robot = robot;
//
//        initializeAugmentedMapInfo(robot.myLoc);
//    }
//
//    public void initializeAugmentedMapInfo(MapLocation currentLocation) {
//
//
////        MapInfo[] nearbyMapInfos = Util.getFilledInMapInfo(rc.senseNearbyMapInfos());
//////         and then go through each one
////        for(int i = 0; i < 69; i++) {
////            nearbyWorld[someArray[i]] = nearbyMapInfos[i];
////        }
//
//        MapInfo[] nearbyMapInfos = rc.senseNearbyMapInfos();
//        for (int i = 0; i < nearbyMapInfos.length; i++) {
//            MapLocation loc = nearbyMapInfos[i].getMapLocation();
//            int dx = loc.x-robot.myLoc.x;
//            int dy = loc.y-robot.myLoc.y;
//
//            nearbyWorld[dx*13+dy+84] = nearbyMapInfos[i];
//        }
//        int index = 0;
//
//        for (int dx = -6; dx <= 6; dx++) {
//            for (int dy = -6; dy <= 6; dy++) {
//                int locX = robot.myLoc.x+dx;
//                int locY = robot.myLoc.y+dy;
//                if (nearbyWorld[index] == null) {
//                    if (0 <= locX && 0 <= locY && locX < rc.getMapWidth() && locY < rc.getMapHeight()) {
//                        nearbyWorld[index] = new MapInfo(new MapLocation(locX, locY), true, false, PaintType.EMPTY, PaintType.EMPTY, false);
//                    } else {
//                        nearbyWorld[index] = new MapInfo(new MapLocation(locX, locY), false, true, PaintType.EMPTY, PaintType.EMPTY, false);
//                    }
//                }
//                index++;
//            }
//        }
//
//        long validBitstring = -1;
//        long unfinishedBitstring = 0;
//
//        for(int i = 0; i < 69; i++) {
//            if (nearbyMapInfos[i].hasRuin() || nearbyMapInfos[i].isWall()) {
//                // if it's a ruin, we might actually want a bit bigger radius around it, but whatever
//                validBitstring &= Constants.invalidSquareForResource[i];
//            } else {
//                PaintType paint = nearbyMapInfos[i].getPaint();
//                if (paint.isEnemy()) {
//                    validBitstring &= Constants.invalidSquareForResource[i];
//                } else if (paint == PaintType.EMPTY) {
//                    unfinishedBitstring |= Constants.emptySquareForResource[i];
//                } else if (paint == PaintType.ALLY_PRIMARY) {
//                    validBitstring &= Constants.primaryColorMask[i];
//                } else {
//                    validBitstring &= Constants.secondaryColorMask[i];
//                }
//            }
//        }
//
//        this.validResourceBitstring = validBitstring;
//        this.unfinishedResourceBitstring = unfinishedBitstring;
//    }
//
////    validBitstring &= unfinishedBitstring;
////        if (validBitstring == 0) {
////        return -1;
////    }
////
////    int counter = 64; // c will be the number of zero bits on the right
////    validBitstring &= -validBitstring;
////    if (validBitstring != 0) counter--;
////    if ((validBitstring & 0x00000000FFFFFFFFL) != 0) counter -= 32;
////    if ((validBitstring & 0x0000FFFF0000FFFFL) != 0) counter -= 16;
////    if ((validBitstring & 0x00FF00FF00FF00FFL) != 0) counter -= 8;
////    if ((validBitstring & 0x0F0F0F0F0F0F0F0FL) != 0) counter -= 4;
////    if ((validBitstring & 0x3333333333333333L) != 0) counter -= 2;
////    if ((validBitstring & 0x5555555555555555L) != 0) counter -= 1;
////    return Constants.spiralOutwardIndices[counter];
//


}
