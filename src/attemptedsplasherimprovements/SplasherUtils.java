package attemptedsplasherimprovements;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.PaintType;
import battlecode.common.RobotController;

public class SplasherUtils {
    static Splasher splasher;
    static RobotController rc;

    // 0 will indicate we didn't look at it yet
    // 1 indicates it's invalid
    // 2 indicates it's valid

    boolean[] resourcePattern = {
            true, true, false, true, true,
            true, false, false, false, true,
            false, false, true, false, false,
            true, false, false, false, true,
            true, true, false, true, true};

    public static int getAttackIndex(int dx, int dy, boolean isSecondary) {
        int idx;
        if (dx == -2) {
            idx = 0;
        } else if (dx == -1) {
            idx = 2+dy;
        } else if (dx == 0) {
            idx = 6+dy;
        } else if (dx == 1) {
            idx = 10+dy;
        } else {
            idx = 12;
        }
        if (isSecondary) {
            return idx+13;
        } else {
            return idx;
        }
    }

    public static int getAttackXFromIndex(int idx) {
        switch (idx) {
            case 0: return -2;
            case 1: return -1;
            case 2: return -1;
            case 3: return -1;
            case 4: return 0;
            case 5: return 0;
            case 6: return 0;
            case 7: return 0;
            case 8: return 0;
            case 9: return 1;
            case 10: return 1;
            case 11: return 1;
            case 12: return 2;
        }
        return -10;
    }

    public static int getAttackYFromIndex(int idx) {
        switch (idx) {
            case 0: return 0;
            case 1: return -1;
            case 2: return 0;
            case 3: return 1;
            case 4: return -2;
            case 5: return -1;
            case 6: return 0;
            case 7: return 1;
            case 8: return 2;
            case 9: return -1;
            case 10: return 0;
            case 11: return 1;
            case 12: return 0;
        }
        return -10;
    }

    public static int getResourceCenterIndex(int dx, int dy) {
        return 13 * (dx+6) + (dy+6);
    }

    public static boolean isValidAttack(int dx, int dy, boolean isSecondary, int[] lookup, int[] lookupResourceCenters, MapInfo[] nearbyMapInfos) throws GameActionException {
        int index = getAttackIndex(dx, dy, isSecondary);

        if (lookup[index] != 0) {
            return lookup[index] == 2;
        }
        // based on dx, dy, and isSecondary, figure out which resource centers you actually need to look at

        if (isSecondary) {
            if (dx == -2 && dy == 0) {
                if (isPossibleResourceCenters(-5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-6, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == -1 && dy == -1) {
                if (isPossibleResourceCenters(-1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == -1 && dy == 0) {
                if (isPossibleResourceCenters(-5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == -1 && dy == 1) {
                if (isPossibleResourceCenters(-5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == -2) {
                if (isPossibleResourceCenters(0, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(0, -6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == -1) {
                if (isPossibleResourceCenters(0, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == 0) {
                {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == 1) {
                if (isPossibleResourceCenters(0, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == 2) {
                if (isPossibleResourceCenters(1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(0, 6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(0, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 1 && dy == -1) {
                if (isPossibleResourceCenters(1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 1 && dy == 0) {
                if (isPossibleResourceCenters(5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 1 && dy == 1) {
                if (isPossibleResourceCenters(5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 2 && dy == 0) {
                if (isPossibleResourceCenters(5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(6, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            }
        }
        else {
            if (dx == -2 && dy == 0) {
                if (isPossibleResourceCenters(-5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-6, -2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-6, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-6, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, 2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-6, 2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == -1 && dy == -1) {
                if (isPossibleResourceCenters(-5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-2, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(0, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == -1 && dy == 0) {
                if (isPossibleResourceCenters(-5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, 2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == -1 && dy == 1) {
                if (isPossibleResourceCenters(-4, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, 2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(0, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-2, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == -2) {
                if (isPossibleResourceCenters(3, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-2, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(2, -6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-2, -6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, -6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, -6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(0, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(2, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == -1) {
                if (isPossibleResourceCenters(-1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-2, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(2, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == 0) {
                {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == 1) {
                if (isPossibleResourceCenters(-3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-2, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(2, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 0 && dy == 2) {
                if (isPossibleResourceCenters(4, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, 6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(2, 6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(0, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-2, 6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(1, 6, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(2, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-3, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-2, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 1 && dy == -1) {
                if (isPossibleResourceCenters(3, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, -2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(0, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(2, -5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 1 && dy == 0) {
                if (isPossibleResourceCenters(3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, -2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 1 && dy == 1) {
                if (isPossibleResourceCenters(4, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(0, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(2, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(-1, 5, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            } else if (dx == 2 && dy == 0) {
                if (isPossibleResourceCenters(4, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(6, 2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, 4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, -2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(6, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(6, -2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(4, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(6, -1, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, -3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 0, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 3, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(3, -4, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else if (isPossibleResourceCenters(5, 2, lookupResourceCenters, nearbyMapInfos)) {
                    lookup[index] = 1;
                    return false;
                } else {
                    lookup[index] = 2;
                    return true;
                }
            }
        }

        return false;
    }

    // 0 means we didn't look yet, 1 means it could be a resource center, and 2 means it can't be a resource center
    public static boolean isPossibleResourceCenters(int dx, int dy, int[] lookupResourceCenters, MapInfo[] nearbyMapInfos) {
        int index = getResourceCenterIndex(dx, dy);
        if (lookupResourceCenters[index] != 0) {
            return lookupResourceCenters[index] == 1;
        } else {

            // now we need to look through and see whether this matches the information we have
            for (int ddx = dx-2; ddx <= dx+2; ddx++) {
                for (int ddy = dy-2; ddy <= dy+2; ddy++) {
                    if (ddx*ddx + ddy*ddy <= 20) {
                        MapInfo curr = nearbyMapInfos[Util.getMapInfoIndex(ddx, ddy)];
                        if (curr == null || !curr.isPassable()) {
                            lookupResourceCenters[index] = 2;
                            return false;
                        }

                        PaintType currPaint = curr.getPaint();
                        PaintType expectedPaint;
                        if (rc.getResourcePattern()[ddx+2-dx][ddy+2-dy]) {
                            expectedPaint = PaintType.ALLY_SECONDARY;
                        } else {
                            expectedPaint = PaintType.ALLY_PRIMARY;
                        }
                        if (currPaint != expectedPaint) {
                            lookupResourceCenters[index] = 2;
                            return false;
                        }

                    }
                }
            }
            lookupResourceCenters[index] = 1;
            return true;
        }
    }

    public static void initializeInfo(int[] lookup, int[] lookupResourceCenters, MapInfo[] nearbyMapInfos) throws GameActionException {
        for (int i = 0; i < 69; i++) {
            if (nearbyMapInfos[i] != null && nearbyMapInfos[i].isResourcePatternCenter() && nearbyMapInfos[i].getPaint().isAlly()) {
                for (int index = 0; index < 13; index++) {
                    for (int shift_idx = 0; shift_idx < 13; shift_idx++) {
                        if (nearbyMapInfos[i].getMapLocation().isWithinDistanceSquared(splasher.myLoc.translate(getAttackXFromIndex(index) + getAttackXFromIndex(shift_idx), getAttackYFromIndex(index) + getAttackYFromIndex(shift_idx)), 8)) {
                            // TODO: make this smarter, instead of making both invalid
                            // also we can speed this loop up
                            lookup[index] = 1;
                            lookup[index+13] = 1;
                            break;
                        }
                    }
                }
            }
        }
    }
}
