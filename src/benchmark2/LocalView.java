package benchmark2;

import battlecode.common.*;

public class LocalView {
    MapLocation currentLocation;

//    //    // might want to make these more than one long each if we want to expand how far we allow the resource pattern center to be
    long validResourceBitstring = -1;
    long unfinishedResourceBitstring = 0;


    MapInfo[] nearbyWorld = new MapInfo[169];

    RobotController rc;
    Robot robot;

    public LocalView(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;
        initializeAugmentedMapInfo(rc.getLocation());

    }

    public MapInfo getInfo(int dx, int dy) {
        return nearbyWorld[getIndex(dx, dy)];
    }

    public int getIndex(int dx, int dy) {
        return (13 * ((currentLocation.x + dx+13) % 13)) + ((currentLocation.y + dy + 13) % 13);
    }

    public void paint(int dx, int dy, boolean isSecondary) throws GameActionException {
        // TODO: we don't technically need to resense again (can just update based on old information)
        nearbyWorld[getIndex(dx, dy)] = rc.senseMapInfo(new MapLocation(currentLocation.x+dx, currentLocation.y+dy));

        // TODO: update allowed bitstring? ik how to do this when it was empty, not sure about when it wasn't empty though
        // just assume it was empty for now
    }

    public void shift(Direction direction) {
        currentLocation = currentLocation.add(direction);
        if (direction == Direction.EAST) {
            int firstIndex = 13 * ((currentLocation.x + 6) % 13);
            int lastIndex = firstIndex+13;

            for (int index = firstIndex; index < lastIndex; index++) {
                // make these all null
                nearbyWorld[index] = null;
            }
        } else if (direction == Direction.WEST) {
            int firstIndex = 13 * ((currentLocation.x + 7) % 13); // this is 7 = -6 + 13
            int lastIndex = firstIndex+13;
            for (int index = firstIndex; index < lastIndex; index++) {
                // make these all null
                nearbyWorld[index] = null;
            }
        } else if (direction == Direction.NORTH) {
            int firstIndex = (currentLocation.y + 6) % 13;
            for(int index = firstIndex; index < 169; index += 13) {
                nearbyWorld[index] = null;
            }
        } else if (direction == Direction.SOUTH) {
            int firstIndex = (currentLocation.y + 7) % 13;
            for(int index = firstIndex; index < 169; index += 13) {
                nearbyWorld[index] = null;
            }
        } else if (direction == Direction.NORTHEAST) {
            shift(Direction.EAST);
            shift(Direction.NORTH);
        } else if (direction == Direction.NORTHWEST) {
            shift(Direction.WEST);
            shift(Direction.NORTH);
        } else if (direction == Direction.SOUTHEAST) {
            shift(Direction.SOUTH);
            shift(Direction.EAST);
        } else if (direction == Direction.SOUTHWEST) {
            shift(Direction.SOUTH);
            shift(Direction.WEST);
        }
    }

    public void shiftAndResense(Direction direction) throws GameActionException {
        // assumes that we've already called move in this direction before this
        shift(direction);

        Util.log(currentLocation.toString());

        int[] dx;
        int[] dy;

        if (direction == Direction.EAST) {
            dx = new int[]{2, 2, 3, 3, 4, 4, 4, 4, 4};
            dy = new int[]{-4, 4, -3, 3, -2, -1, 0, 1, 2};
        } else if (direction == Direction.WEST) {
            dx = new int[]{-4, -4, -4, -4, -4, -3, -3, -2, -2};
            dy = new int[]{-2, -1, 0, 1, 2, -3, 3, -4, 4};
        } else if (direction == Direction.NORTH) {
            dx = new int[]{-4, -3, -2, -1, 0, 1, 2, 3, 4};
            dy = new int[]{2, 3, 4, 4, 4, 4, 4, 3, 2};
        } else if (direction == Direction.SOUTH) {
            dx = new int[]{-4, -3, -2, -1, 0, 1, 2, 3, 4};
            dy = new int[]{-2, -3, -4, -4, -4, -4, -4, -3, -2};
        } else if (direction == Direction.NORTHEAST) {
            dx = new int[]{-2, -1, 0, 1, 2, 2, 3, 3, 4, 4, 4, 4, 4};
            dy = new int[]{4, 4, 4, 4, 3, 4, 2, 3, -2, -1, 0, 1, 2};
        } else if (direction == Direction.NORTHWEST) {
            dx = new int[]{-4, -4, -4, -4, -4, -3, -3, -2, -2, -1, 0, 1, 2};
            dy = new int[]{-2, -1, 0, 1, 2, 2, 3, 3, 4, 4, 4, 4, 4};
        } else if (direction == Direction.SOUTHEAST) {
            dx = new int[]{-2, -1, 0, 1, 2, 2, 3, 3, 4, 4, 4, 4, 4};
            dy = new int[]{-4, -4, -4, -4, -4, -3, -3, -2, -2, -1, 0, 1, 2};
        } else if (direction == Direction.SOUTHWEST) {
            dx = new int[]{-4, -4, -4, -4, -4, -3, -3, -2, -2, -1, 0, 1, 2};
            dy = new int[]{-2, -1, 0, 1, 2, -3, -2, -4, -3, -4, -4, -4, -4};
        } else {
            return;
        }

        for (int i = 0; i < dx.length; i++) {
            int ddx = dx[i];
            int ddy = dy[i];
            if (0 <= currentLocation.x+ddx && currentLocation.x+ddx < rc.getMapWidth() && 0 <= currentLocation.y+ddy && currentLocation.y+ddy < rc.getMapHeight()) {
                nearbyWorld[getIndex(ddx, ddy)] = rc.senseMapInfo(currentLocation.translate(ddx, ddy));
            } else {
                nearbyWorld[getIndex(ddx, ddy)] = new MapInfo(currentLocation.translate(ddx, ddy), false, true, null, null, false, false);
            }
        }
    }

    public void resense() {
        MapInfo[] nearbyMapInfos = rc.senseNearbyMapInfos();
        for (int i = 0; i < nearbyMapInfos.length; i++) {
            MapLocation loc = nearbyMapInfos[i].getMapLocation();
            nearbyWorld[13 * ((loc.x)%13) + ((loc.y)%13)] = nearbyMapInfos[i];
        }
    }

    public void initializeAugmentedMapInfo(MapLocation currentLocation) {
        // TODO: this could probably be optimized

        this.currentLocation = currentLocation;

        resense();

        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = -6; dy <= 6; dy++) {
                int locX = (currentLocation.x + dx);
                int locY = (currentLocation.y + dy);
                int index = (13*((locX+13)%13)) + ((locY+13)%13);
                if (nearbyWorld[index] == null) {
                    if (0 <= locX && 0 <= locY && locX < rc.getMapWidth() && locY < rc.getMapHeight()) {
                        nearbyWorld[index] = new MapInfo(new MapLocation(locX, locY), true, false, PaintType.EMPTY, PaintType.EMPTY, false, false);
                    } else {
                        // can we just leave this as null?
                        nearbyWorld[index] = new MapInfo(new MapLocation(locX, locY), false, true, PaintType.EMPTY, PaintType.EMPTY, false, false);
                    }
                }
            }
        }
    }

    public long getValidResourceBitstring() {
        return -1;
    }

    public String toString() {
        StringBuilder output = new StringBuilder(currentLocation.toString() + "\n");
        for (MapInfo mapInfo : nearbyWorld) {
            if (mapInfo != null) {
                output.append(mapInfo.toString()).append(",");
            } else {
                output.append("null,");
            }
        }
        return output.toString();
    }




}
////
////        long validBitstring = -1;
////        long unfinishedBitstring = 0;
////
////        for(int i = 0; i < 69; i++) {
////            if (nearbyMapInfos[i].hasRuin() || nearbyMapInfos[i].isWall()) {
////                // if it's a ruin, we might actually want a bit bigger radius around it, but whatever
////                validBitstring &= Constants.invalidSquareForResource[i];
////            } else {
////                PaintType paint = nearbyMapInfos[i].getPaint();
////                if (paint.isEnemy()) {
////                    validBitstring &= Constants.invalidSquareForResource[i];
////                } else if (paint == PaintType.EMPTY) {
////                    unfinishedBitstring |= Constants.emptySquareForResource[i];
////                } else if (paint == PaintType.ALLY_PRIMARY) {
////                    validBitstring &= Constants.primaryColorMask[i];
////                } else {
////                    validBitstring &= Constants.secondaryColorMask[i];
////                }
////            }
////        }
////
////        this.validResourceBitstring = validBitstring;
////        this.unfinishedResourceBitstring = unfinishedBitstring;
////    }
////
//////    validBitstring &= unfinishedBitstring;
//////        if (validBitstring == 0) {
//////        return -1;
//////    }
//////
//////    int counter = 64; // c will be the number of zero bits on the right
//////    validBitstring &= -validBitstring;
//////    if (validBitstring != 0) counter--;
//////    if ((validBitstring & 0x00000000FFFFFFFFL) != 0) counter -= 32;
//////    if ((validBitstring & 0x0000FFFF0000FFFFL) != 0) counter -= 16;
//////    if ((validBitstring & 0x00FF00FF00FF00FFL) != 0) counter -= 8;
//////    if ((validBitstring & 0x0F0F0F0F0F0F0F0FL) != 0) counter -= 4;
//////    if ((validBitstring & 0x3333333333333333L) != 0) counter -= 2;
//////    if ((validBitstring & 0x5555555555555555L) != 0) counter -= 1;
//////    return Constants.spiralOutwardIndices[counter];
////
//
//
//}
