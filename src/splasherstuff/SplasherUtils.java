package splasherstuff;

import battlecode.common.*;

public class SplasherUtils {
    static Splasher splasher;
    static RobotController rc;

    static int[] repaintableIndices = {7,8,9,14,15,16,17,18,22,23,24,25,26,27,28,31,32,33,34,35,36,37,40,41,42,43,44,45,46,50,51,52,53,54,59,60,61};
    static int[] nonRepaintableIndices = {2,30,38,66};

    static int[] allIndices = {2,7,8,9,14,15,16,17,18,22,23,24,25,26,27,28,30,31,32,33,34,35,36,37,38,40,41,42,43,44,45,46,50,51,52,53,54,59,60,61,66};

    public static final byte[] shift_dx = {-4,-4,-4,-4,-4,-3,-3,-3,-3,-3,-3,-3,-2,-2,-2,-2,-2,-2,-2,-2,-2,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,4,4,4,4,4};
    public static final byte[] shift_dy = {-2,-1,0,1,2,-3,-2,-1,0,1,2,3,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-3,-2,-1,0,1,2,3,-2,-1,0,1,2};

    // 0 will indicate we didn't look at it yet
    // 1 indicates it's invalid
    // 2 indicates it's valid

//    boolean[] resourcePattern = {
//            true, true, false, true, true,
//            true, false, false, false, true,
//            false, false, true, false, false,
//            true, false, false, false, true,
//            true, true, false, true, true};



    public static int[] getRelevantScoreIndicesRestricted(int index) {
        // index is index into nearbyMapInfos
        // all adjacent ones that we can repaint

        switch (index) {
            case 7: return new int[]{0};
            case 8: return new int[]{0};
            case 9: return new int[]{0};
            case 14: return new int[]{1};
            case 15: return new int[]{0,1,2};
            case 16: return new int[]{0,1,2,3};
            case 17: return new int[]{0,2,3};
            case 18: return new int[]{3};
            case 22: return new int[]{4};
            case 23: return new int[]{1,4,5};
            case 24: return new int[]{0,1,2,4,5,6};
            case 25: return new int[]{0,1,2,3,5,6,7};
            case 26: return new int[]{0,2,3,6,7,8};
            case 27: return new int[]{8,3,7};
            case 28: return new int[]{8};
            case 31: return new int[]{4};
            case 32: return new int[]{1,4,5,9};
            case 33: return new int[]{1,2,4,5,6,9,10};
            case 34: return new int[]{1,2,3,5,6,7,9,10,11};
            case 35: return new int[]{2,3,6,7,8,10,11};
            case 36: return new int[]{8,11,3,7};
            case 37: return new int[]{8};
            case 40: return new int[]{4};
            case 41: return new int[]{9,4,5};
            case 42: return new int[]{4,5,6,9,10,12};
            case 43: return new int[]{5,6,7,9,10,11,12};
            case 44: return new int[]{6,7,8,10,11,12};
            case 45: return new int[]{8,11,7};
            case 46: return new int[]{8};
            case 50: return new int[]{9};
            case 51: return new int[]{9,10,12};
            case 52: return new int[]{9,10,11,12};
            case 53: return new int[]{10,11,12};
            case 54: return new int[]{11};
            case 59: return new int[]{12};
            case 60: return new int[]{12};
            case 61: return new int[]{12};
            default: return null;
        }
    }

    public static int[] getRelevantScoreIndicesUnrestricted(int index) {
        // index is index into nearbyMapInfos
        switch (index) {
            case 2: return new int[]{0};
            case 7: return new int[]{0,1};
            case 8: return new int[]{0,2};
            case 9: return new int[]{0,3};
            case 14: return new int[]{0,1,4};
            case 15: return new int[]{0,1,2,5};
            case 16: return new int[]{0,1,2,3,6};
            case 17: return new int[]{0,2,3,7};
            case 18: return new int[]{0,8,3};
            case 22: return new int[]{1,4};
            case 23: return new int[]{1,2,4,5};
            case 24: return new int[]{0,1,2,3,4,5,6,9};
            case 25: return new int[]{0,1,2,3,5,6,7,10};
            case 26: return new int[]{0,1,2,3,6,7,8,11};
            case 27: return new int[]{8,2,3,7};
            case 28: return new int[]{8,3};
            case 30: return new int[]{4};
            case 31: return new int[]{4,5};
            case 32: return new int[]{1,4,5,6,9};
            case 33: return new int[]{1,2,4,5,6,7,9,10};
            case 34: return new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12};
            case 35: return new int[]{2,3,5,6,7,8,10,11};
            case 36: return new int[]{3,6,7,8,11};
            case 37: return new int[]{8,7};
            case 38: return new int[]{8};
            case 40: return new int[]{9,4};
            case 41: return new int[]{9,10,4,5};
            case 42: return new int[]{1,4,5,6,9,10,11,12};
            case 43: return new int[]{2,5,6,7,9,10,11,12};
            case 44: return new int[]{3,6,7,8,9,10,11,12};
            case 45: return new int[]{8,10,11,7};
            case 46: return new int[]{8,11};
            case 50: return new int[]{9,4,12};
            case 51: return new int[]{9,10,12,5};
            case 52: return new int[]{6,9,10,11,12};
            case 53: return new int[]{10,11,12,7};
            case 54: return new int[]{8,11,12};
            case 59: return new int[]{9,12};
            case 60: return new int[]{10,12};
            case 61: return new int[]{11,12};
            case 66: return new int[]{12};
            default: return null;
        }
    }

    // want to be able to tell: given attack location and color type, a) how many of theirs are we repainting, b) how many of our own are we repainting, c) how many empties are we painting
    // all we really care about is the score tbh

    public static void addScore(int[] scores, int[] relevantScoreIndices, int score) {
        if (relevantScoreIndices == null || relevantScoreIndices.length == 0) {
            return;
        }
        for (int i : relevantScoreIndices) {
            scores[i] += score;
        }
    }


//    public static void updateScoresForRuin(int[] scores, MapInfo[] nearbyMapInfos, int ruinLoc, Team ruinType) {
//        for (5x5 grid around ruinLoc) {
//
//        }
//    }

    public static void addScore(int[] scores, int[] tilesPainted, int[] relevantScoreIndices, int score, boolean isPainting) {
        if (relevantScoreIndices == null || relevantScoreIndices.length == 0) {
            return;
        }

        if (isPainting) {
            for (int i : relevantScoreIndices) {
                scores[i] += score;
                tilesPainted[i] += 1;
            }
        } else {
            for (int i : relevantScoreIndices) {
                scores[i] += score;
            }
        }


    }

    public static void updateScoresForRuin(int ruinIndex, Team ruinRobotTeam, int[] scores, int[] tilesPainted, MapInfo[] nearbyMapInfos) throws GameActionException {
        // this paint is important
        for (int dx = shift_dx[ruinIndex]-2; dx <= shift_dx[ruinIndex]+2; dx++) {
            for (int dy = shift_dy[ruinIndex]-2; dx <= shift_dy[ruinIndex]+2; dy++) {
                if (dx*dx+dy*dy>20) {
                    continue;
                } else {
                    int index = Util.getMapInfoIndex(dx, dy);
                    if (index != -1) {
                        MapInfo curr = nearbyMapInfos[index];

                        if (ruinRobotTeam == null && curr.getPaint() == PaintType.EMPTY) {
                            int[] relevantScoreIndices = getRelevantScoreIndicesUnrestricted(index);
                            addScore(scores, tilesPainted, relevantScoreIndices, 200, true);
                        } else if (ruinRobotTeam == null && curr.getPaint().isEnemy()) {
                            int[] relevantScoreIndices = getRelevantScoreIndicesRestricted(index);
                            addScore(scores, tilesPainted, relevantScoreIndices, 500, true);
                        } else if (ruinRobotTeam == null) {
//                            int[] relevantScoreIndices = getRelevantScoreIndicesUnrestricted(index);
//                            addScore(scores, tilesPainted, relevantScoreIndices, -200, true);
                        } else if (ruinRobotTeam == splasher.myTeam && curr.getPaint().isEnemy()) {
                            int[] relevantScoreIndices = getRelevantScoreIndicesRestricted(index);
                            addScore(scores, tilesPainted, relevantScoreIndices, 700, true);
                        } else if (ruinRobotTeam == splasher.myTeam) {

                        } else if (curr.getPaint().isEnemy()) {
                            int[] relevantScoreIndices = getRelevantScoreIndicesRestricted(index);
                            addScore(scores, tilesPainted, relevantScoreIndices, 800, true);
                        } else if (curr.getPaint() == PaintType.EMPTY) {
                            int[] relevantScoreIndices = getRelevantScoreIndicesUnrestricted(index);
                            addScore(scores, tilesPainted, relevantScoreIndices, 600, true);
                        }

                    }
                }
            }
        }
    }

    public static void updateScores(int[] scores, MapInfo[] nearbyMapInfos) throws GameActionException {

        int[] relevantScoreIndices;
//        for (int i that are repaintable with enemy paint) {

        for (int i : allIndices) {
            MapInfo currInfo = nearbyMapInfos[i];
            if (currInfo == null || currInfo.isWall()) {
                continue;
            } else if (currInfo.hasRuin()) {
                RobotInfo ruinRobot = rc.senseRobotAtLocation(currInfo.getMapLocation());
//                if (ruinRobot == null) {
//                    relevantScoreIndices = getRelevantScoreIndicesUnrestricted(i);
//                    addScore(scores, relevantScoreIndices, 10);
                if (ruinRobot != null && ruinRobot.getTeam() == splasher.oppTeam) {
                    // TODO: should this be unrestricted?
                    relevantScoreIndices = getRelevantScoreIndicesRestricted(i);
                    addScore(scores, relevantScoreIndices, 1000);
                }
            } else if (currInfo.getPaint().isEnemy()) {
                // go through the ones that are repaintable, this will be more restricted
                relevantScoreIndices = getRelevantScoreIndicesUnrestricted(i);
                addScore(scores, relevantScoreIndices, 20);
            } else if (currInfo.getPaint() == PaintType.EMPTY) {
                relevantScoreIndices = getRelevantScoreIndicesUnrestricted(i);
                addScore(scores, relevantScoreIndices, 10);
            } else if (currInfo.getPaint() == PaintType.ALLY_PRIMARY){
                // this adds a penalty for repainting our own paint a different color
                relevantScoreIndices = getRelevantScoreIndicesUnrestricted(i);
                addScore(scores, relevantScoreIndices, -1);
            } else if (currInfo.getPaint() == PaintType.ALLY_SECONDARY){
                // this adds a penalty for repainting our own paint the same color
                relevantScoreIndices = getRelevantScoreIndicesUnrestricted(i);
                addScore(scores, relevantScoreIndices, -1);
            }
        }
//        for (int i that are not repaintable with enemy paint but can still be affected) {
//            // similar logic
//
//        }
    }

    public static int getAttackIndex(int dx, int dy) {
        if (dx == -2) {
            return 0;
        } else if (dx == -1) {
            return 2+dy;
        } else if (dx == 0) {
            return 6+dy;
        } else if (dx == 1) {
            return 10+dy;
        } else {
            return 12;
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

    public static boolean isValidAttack(int dx, int dy, int[] lookup, int[] lookupResourceCenters, MapInfo[] nearbyMapInfos) throws GameActionException {
        int index = getAttackIndex(dx, dy);

        if (lookup[index] != 0) {
            return lookup[index] == 2;
        }
        // based on dx, dy, and isSecondary, figure out which resource centers you actually need to look at

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

        return false;
    }

    public static int[] getRelevantIndices(int dx, int dy) {
        switch (dx) {
            case -6: switch (dy) {
                case -2: return new int[]{0,1,2};
                case -1: return new int[]{0,1,2,3};
                case 0: return new int[]{0,1,2,3,4};
                case 1: return new int[]{1,2,3,4};
                case 2: return new int[]{2,3,4};
            }
            case -5: switch (dy) {
                case -3: return new int[]{0,1,5,6,7};
                case -2: return new int[]{0,1,2,5,6,7,8};
                case -1: return new int[]{0,1,2,3,5,6,7,8,9};
                case 0: return new int[]{0,1,2,3,4,6,7,8,9,10};
                case 1: return new int[]{1,2,3,4,7,8,9,10,11};
                case 2: return new int[]{2,3,4,8,9,10,11};
                case 3: return new int[]{3,4,9,10,11};
            }
            case -4: switch (dy) {
                case -4: return new int[]{0,5,6,12,13,14};
                case -3: return new int[]{0,1,5,6,7,12,13,14,15};
                case 3: return new int[]{3,4,9,10,11,17,18,19,20};
                case 4: return new int[]{4,10,11,18,19,20};
            }
            case -3: switch (dy) {
                case -5: return new int[]{5,12,13,21,22};
                case -4: return new int[]{0,5,6,12,13,14,21,22,23};
                case 4: return new int[]{4,10,11,18,19,20,27,28,29};
                case 5: return new int[]{11,19,20,28,29};
            }
            case -2: switch (dy) {
                case -6: return new int[]{12,21,30};
                case -5: return new int[]{5,12,13,21,22,30,31};
                case 5: return new int[]{11,19,20,28,29,37,38};
                case 6: return new int[]{20,29,38};
            }
            case -1: switch (dy) {
                case -6: return new int[]{12,21,30,39};
                case -5: return new int[]{5,12,13,21,22,30,31,39,40};
                case 5: return new int[]{11,19,20,28,29,37,38,46,47};
                case 6: return new int[]{20,29,38,47};
            }
            case 0: switch (dy) {
                case -6: return new int[]{12,21,30,39,48};
                case -5: return new int[]{12,13,21,22,30,31,39,40,48,49};
                case 5: return new int[]{19,20,28,29,37,38,46,47,55,56};
                case 6: return new int[]{20,29,38,47,56};
            }
            case 1: switch (dy) {
                case -6: return new int[]{21,30,39,48};
                case -5: return new int[]{21,22,30,31,39,40,48,49,57};
                case 5: return new int[]{28,29,37,38,46,47,55,56,63};
                case 6: return new int[]{29,38,47,56};
            }
            case 2: switch (dy) {
                case -6: return new int[]{30,39,48};
                case -5: return new int[]{30,31,39,40,48,49,57};
                case 5: return new int[]{37,38,46,47,55,56,63};
                case 6: return new int[]{38,47,56};
            }
            case 3: switch (dy) {
                case -5: return new int[]{39,40,48,49,57};
                case -4: return new int[]{39,40,41,48,49,50,57,58,64};
                case 4: return new int[]{45,46,47,54,55,56,62,63,68};
                case 5: return new int[]{46,47,55,56,63};
            }
            case 4: switch (dy) {
                case -4: return new int[]{48,49,50,57,58,64};
                case -3: return new int[]{48,49,50,51,57,58,59,64,65};
                case 3: return new int[]{53,54,55,56,61,62,63,67,68};
                case 4: return new int[]{54,55,56,62,63,68};
            }
            case 5: switch (dy) {
                case -3: return new int[]{57,58,59,64,65};
                case -2: return new int[]{57,58,59,60,64,65,66};
                case -1: return new int[]{57,58,59,60,61,64,65,66,67};
                case 0: return new int[]{58,59,60,61,62,64,65,66,67,68};
                case 1: return new int[]{59,60,61,62,63,65,66,67,68};
                case 2: return new int[]{60,61,62,63,66,67,68};
                case 3: return new int[]{61,62,63,67,68};
            }
            case 6: switch (dy) {
                case -2: return new int[]{64,65,66};
                case -1: return new int[]{64,65,66,67};
                case 0: return new int[]{64,65,66,67,68};
                case 1: return new int[]{65,66,67,68};
                case 2: return new int[]{66,67,68};
            }
        }
        return null;
    }

    // 0 means we didn't look yet, 1 means it could be a resource center, and 2 means it can't be a resource center
    public static boolean isPossibleResourceCenters(int dx, int dy, int[] lookupResourceCenters, MapInfo[] nearbyMapInfos) {
        int index = getResourceCenterIndex(dx, dy);
        if (lookupResourceCenters[index] != 0) {
            return lookupResourceCenters[index] == 1;
        } else {
            boolean[][] pattern = rc.getResourcePattern();
            // now we need to look through and see whether this matches the information we have


            int[] relevantIndices = getRelevantIndices(dx, dy);
            for (int relevantIndex : relevantIndices) {
                MapInfo curr = nearbyMapInfos[relevantIndex];
                if (curr == null || !curr.isPassable()) {
                    lookupResourceCenters[index] = 2;
                    return false;
                }
                PaintType currPaint = curr.getPaint();
                if (!currPaint.isAlly()) {
                    lookupResourceCenters[index] = 2;
                    return false;
                }

                PaintType expectedPaint;
//                Util.log("Starting this logging");
//                Util.log("looking at index: "+relevantIndex);
//                Util.log("looking at loc: "+curr.getMapLocation().toString());
//                Util.log("my loc: "+splasher.myLoc.toString());
//                Util.log("dx,dy: "+dx + "," + dy);

                if (pattern[curr.getMapLocation().x-(splasher.myLoc.x+dx)+2][curr.getMapLocation().y-(splasher.myLoc.y+dy)+2]) {
                    expectedPaint = PaintType.ALLY_SECONDARY;
                } else {
                    expectedPaint = PaintType.ALLY_PRIMARY;
                }
                if (currPaint != expectedPaint) {
                    lookupResourceCenters[index] = 2;
                    return false;
                }
            }
            lookupResourceCenters[index] = 1;
            return true;

//            for (int ddx = dx-2; ddx <= dx+2; ddx++) {
//                for (int ddy = dy-2; ddy <= dy+2; ddy++) {
//                    if (ddx*ddx + ddy*ddy <= 20) {
//                        MapInfo curr = nearbyMapInfos[Util.getMapInfoIndex(ddx, ddy)];
//                        if (curr == null || !curr.isPassable()) {
//                            lookupResourceCenters[index] = 2;
//                            return false;
//                        }
//
//                        PaintType currPaint = curr.getPaint();
//                        PaintType expectedPaint;
//                        if (pattern[ddx+2-dx][ddy+2-dy]) {
//                            expectedPaint = PaintType.ALLY_SECONDARY;
//                        } else {
//                            expectedPaint = PaintType.ALLY_PRIMARY;
//                        }
//                        if (currPaint != expectedPaint) {
//                            lookupResourceCenters[index] = 2;
//                            return false;
//                        }
//
//                    }
//                }
//            }
//            lookupResourceCenters[index] = 1;
//            return true;
        }
    }

    public static void updateKnownResourceCenter(int[] lookup, int centerIndex) {
        switch (centerIndex) {
            case 0:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[4] = 1;
                return;
            case 1:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[5] = 1;
                return;
            case 2:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[6] = 1;
                return;
            case 3:
                lookup[0] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[7] = 1;
                return;
            case 4:
                lookup[0] = 1;
                lookup[8] = 1;
                lookup[3] = 1;
                return;
            case 5:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[4] = 1;
                return;
            case 6:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                return;
            case 7:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[9] = 1;
                return;
            case 8:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[10] = 1;
                return;
            case 9:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[11] = 1;
                return;
            case 10:
                lookup[0] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                return;
            case 11:
                lookup[0] = 1;
                lookup[8] = 1;
                lookup[3] = 1;
                return;
            case 12:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[4] = 1;
                return;
            case 13:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                return;
            case 14:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[9] = 1;
                return;
            case 15:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                return;
            case 16:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 17:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                return;
            case 18:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[11] = 1;
                return;
            case 19:
                lookup[0] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                return;
            case 20:
                lookup[0] = 1;
                lookup[8] = 1;
                lookup[3] = 1;
                return;
            case 21:
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                return;
            case 22:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[9] = 1;
                return;
            case 23:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                return;
            case 24:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 25:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 26:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 27:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                return;
            case 28:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[11] = 1;
                return;
            case 29:
                lookup[8] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[7] = 1;
                return;
            case 30:
                lookup[1] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[9] = 1;
                return;
            case 31:
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                return;
            case 32:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 33:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 34:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 35:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 36:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 37:
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                return;
            case 38:
                lookup[3] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[11] = 1;
                return;
            case 39:
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                return;
            case 40:
                lookup[1] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 41:
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 42:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 43:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 44:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 45:
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 46:
                lookup[3] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 47:
                lookup[8] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[7] = 1;
                return;
            case 48:
                lookup[9] = 1;
                lookup[4] = 1;
                lookup[12] = 1;
                return;
            case 49:
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[12] = 1;
                return;
            case 50:
                lookup[1] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 51:
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 52:
                lookup[0] = 1;
                lookup[1] = 1;
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 53:
                lookup[2] = 1;
                lookup[3] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 54:
                lookup[3] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 55:
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 56:
                lookup[8] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 57:
                lookup[9] = 1;
                lookup[4] = 1;
                lookup[12] = 1;
                return;
            case 58:
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[12] = 1;
                return;
            case 59:
                lookup[1] = 1;
                lookup[4] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 60:
                lookup[2] = 1;
                lookup[5] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 61:
                lookup[3] = 1;
                lookup[6] = 1;
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 62:
                lookup[7] = 1;
                lookup[8] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 63:
                lookup[8] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 64:
                lookup[9] = 1;
                lookup[4] = 1;
                lookup[12] = 1;
                return;
            case 65:
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[12] = 1;
                lookup[5] = 1;
                return;
            case 66:
                lookup[6] = 1;
                lookup[9] = 1;
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
            case 67:
                lookup[10] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                lookup[7] = 1;
                return;
            case 68:
                lookup[8] = 1;
                lookup[11] = 1;
                lookup[12] = 1;
                return;
        }
    }

    public static int initializeInfo(int[] lookup, int[] lookupResourceCenters, MapInfo[] nearbyMapInfos, MapLocation[] nearbyRuins, Team[] ruinTypes) throws GameActionException {
        int numRuins = 0;
        for (int i = 0; i < 69; i++) {
            if (nearbyMapInfos[i] != null) {
                if (nearbyMapInfos[i].isResourcePatternCenter() && nearbyMapInfos[i].getPaint().isAlly()) {
                    updateKnownResourceCenter(lookup, i);
                } else if (nearbyMapInfos[i].hasRuin()) {
                    nearbyRuins[numRuins] = nearbyMapInfos[i].getMapLocation();
                    RobotInfo ruinRobot = rc.senseRobotAtLocation(nearbyMapInfos[i].getMapLocation());
                    if (ruinRobot == null) {
                        ruinTypes[numRuins] = null;
                    } else {
                        ruinTypes[numRuins] = ruinRobot.getTeam();
                    }
                    numRuins++;
                }
            }
        }
        return numRuins;
    }
}
