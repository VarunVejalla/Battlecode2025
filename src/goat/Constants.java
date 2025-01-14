package goat;

public class Constants {

    public static final boolean MUTE = true;
    public static final int MAX_RUIN_DISTANCE_SQUARED = 2;
    public static final int PAINT_THRESHOLD_TO_REPLENISH = 30;
    public static final int MIN_DIST_TO_SATISFY_RANDOM_DESTINATION = 9;
    public static final int MIN_PAINT_NEEDED_FOR_SOLDIER_ATTACK = 5;
    public static final int SPAWN_OPENING_BOTS_ROUNDS = 50;
    public static final int SPAWN_BOTS_MIDGAME_COST_THRESHOLD = 1500;

    public static final int[] dx = {-4,-4,-4,-4,-4,-3,-3,-3,-3,-3,-3,-3,-2,-2,-2,-2,-2,-2,-2,-2,-2,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,4,4,4,4,4};
    public static final int[] dy = {-2,-1,0,1,2,-3,-2,-1,0,1,2,3,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-3,-2,-1,0,1,2,3,-2,-1,0,1,2};
    public static final int[][] gridLookupIndicesSpiral = {{},{},{0},{0,1},{0,1,2},{0,1,2,3},{0,1,2,3,4},{1,2,3,4},{2,3,4},{3,4},{4},{},{},{},{5},{5,6,0},{5,6,7,0,1},{5,6,7,8,0,1,2},{5,6,7,8,9,0,1,2,3},{6,7,8,9,10,0,1,2,3,4},{7,8,9,10,11,1,2,3,4},{2,3,4,8,9,10,11},{3,4,9,10,11},{4,10,11},{11},{},{12},{12,13,5},{12,13,14,5,6,0},{12,13,14,15,5,6,7,0,1},{12,13,14,15,16,5,6,7,8,0,1,2},{13,14,15,16,17,5,6,7,8,9,0,1,2,3},{14,15,16,17,18,6,7,8,9,10,0,1,2,3,4},{7,8,9,10,11,15,16,17,18,19,1,2,3,4},{8,9,10,11,2,3,4,16,17,18,19,20},{3,4,9,10,11,17,18,19,20},{4,10,11,18,19,20},{11,19,20},{20},{21,12},{21,22,12,13,5},{21,22,23,12,13,14,5,6,0},{21,22,23,24,12,13,14,15,5,6,7,0,1},{21,22,23,24,25,12,13,14,15,16,5,6,7,8,0,1,2},{22,23,24,25,26,13,14,15,16,17,5,6,7,8,9,0,1,2,3},{14,15,16,17,18,23,24,25,26,27,6,7,8,9,10,0,1,2,3,4},{15,16,17,18,19,7,8,9,10,11,24,25,26,27,28,1,2,3,4},{8,9,10,11,16,17,18,19,20,2,3,4,25,26,27,28,29},{9,10,11,3,4,17,18,19,20,26,27,28,29},{4,10,11,18,19,20,27,28,29},{11,19,20,28,29},{20,29},{30,21,12},{30,31,21,22,12,13,5},{30,31,32,21,22,23,12,13,14,5,6,0},{30,31,32,33,21,22,23,24,12,13,14,15,5,6,7,0,1},{30,31,32,33,34,21,22,23,24,25,12,13,14,15,16,5,6,7,8,0,1,2},{22,23,24,25,26,31,32,33,34,35,13,14,15,16,17,5,6,7,8,9,0,1,2,3},{23,24,25,26,27,14,15,16,17,18,32,33,34,35,36,6,7,8,9,10,0,1,2,3,4},{15,16,17,18,19,24,25,26,27,28,7,8,9,10,11,33,34,35,36,37,1,2,3,4},{16,17,18,19,20,8,9,10,11,25,26,27,28,29,2,3,4,34,35,36,37,38},{9,10,11,17,18,19,20,3,4,26,27,28,29,35,36,37,38},{10,11,4,18,19,20,27,28,29,36,37,38},{11,19,20,28,29,37,38},{20,29,38},{39,30,21,12},{39,40,30,31,21,22,12,13,5},{39,40,41,30,31,32,21,22,23,12,13,14,5,6},{39,40,41,42,30,31,32,33,21,22,23,24,12,13,14,15,5,6,7},{30,31,32,33,34,39,40,41,42,43,21,22,23,24,25,12,13,14,15,16,5,6,7,8},{31,32,33,34,35,22,23,24,25,26,40,41,42,43,44,13,14,15,16,17,5,6,7,8,9},{23,24,25,26,27,32,33,34,35,36,14,15,16,17,18,41,42,43,44,45,6,7,8,9,10},{24,25,26,27,28,15,16,17,18,19,33,34,35,36,37,7,8,9,10,11,42,43,44,45,46},{16,17,18,19,20,25,26,27,28,29,8,9,10,11,34,35,36,37,38,43,44,45,46,47},{17,18,19,20,9,10,11,26,27,28,29,35,36,37,38,44,45,46,47},{10,11,18,19,20,27,28,29,36,37,38,45,46,47},{11,19,20,28,29,37,38,46,47},{20,29,38,47},{48,39,30,21,12},{48,49,39,40,30,31,21,22,12,13},{48,49,50,39,40,41,30,31,32,21,22,23,12,13,14},{39,40,41,42,48,49,50,51,30,31,32,33,21,22,23,24,12,13,14,15},{39,40,41,42,43,30,31,32,33,34,48,49,50,51,52,21,22,23,24,25,12,13,14,15,16},{31,32,33,34,35,40,41,42,43,44,22,23,24,25,26,49,50,51,52,53,13,14,15,16,17},{32,33,34,35,36,23,24,25,26,27,41,42,43,44,45,14,15,16,17,18,50,51,52,53,54},{24,25,26,27,28,33,34,35,36,37,15,16,17,18,19,42,43,44,45,46,51,52,53,54,55},{25,26,27,28,29,16,17,18,19,20,34,35,36,37,38,43,44,45,46,47,52,53,54,55,56},{17,18,19,20,26,27,28,29,35,36,37,38,44,45,46,47,53,54,55,56},{18,19,20,27,28,29,36,37,38,45,46,47,54,55,56},{19,20,28,29,37,38,46,47,55,56},{20,29,38,47,56},{48,39,30,21},{57,48,49,39,40,30,31,21,22},{48,49,50,57,58,39,40,41,30,31,32,21,22,23},{48,49,50,51,39,40,41,42,57,58,59,30,31,32,33,21,22,23,24},{39,40,41,42,43,48,49,50,51,52,30,31,32,33,34,57,58,59,60,21,22,23,24,25},{40,41,42,43,44,31,32,33,34,35,49,50,51,52,53,22,23,24,25,26,57,58,59,60,61},{32,33,34,35,36,41,42,43,44,45,23,24,25,26,27,50,51,52,53,54,58,59,60,61,62},{33,34,35,36,37,24,25,26,27,28,42,43,44,45,46,51,52,53,54,55,59,60,61,62,63},{25,26,27,28,29,34,35,36,37,38,43,44,45,46,47,52,53,54,55,56,60,61,62,63},{26,27,28,29,35,36,37,38,44,45,46,47,53,54,55,56,61,62,63},{27,28,29,36,37,38,45,46,47,54,55,56,62,63},{28,29,37,38,46,47,55,56,63},{29,38,47,56},{48,39,30},{57,48,49,39,40,30,31},{57,58,48,49,50,64,39,40,41,30,31,32},{48,49,50,51,57,58,59,39,40,41,42,64,65,30,31,32,33},{48,49,50,51,52,39,40,41,42,43,57,58,59,60,30,31,32,33,34,64,65,66},{40,41,42,43,44,49,50,51,52,53,31,32,33,34,35,57,58,59,60,61,64,65,66,67},{41,42,43,44,45,32,33,34,35,36,50,51,52,53,54,58,59,60,61,62,64,65,66,67,68},{33,34,35,36,37,42,43,44,45,46,51,52,53,54,55,59,60,61,62,63,65,66,67,68},{34,35,36,37,38,43,44,45,46,47,52,53,54,55,56,60,61,62,63,66,67,68},{35,36,37,38,44,45,46,47,53,54,55,56,61,62,63,67,68},{36,37,38,45,46,47,54,55,56,62,63,68},{37,38,46,47,55,56,63},{38,47,56},{48,39},{57,48,49,39,40},{57,58,64,48,49,50,39,40,41},{57,58,59,48,49,50,51,64,65,39,40,41,42},{48,49,50,51,52,57,58,59,60,39,40,41,42,43,64,65,66},{49,50,51,52,53,40,41,42,43,44,57,58,59,60,61,64,65,66,67},{41,42,43,44,45,50,51,52,53,54,58,59,60,61,62,64,65,66,67,68},{42,43,44,45,46,51,52,53,54,55,59,60,61,62,63,65,66,67,68},{43,44,45,46,47,52,53,54,55,56,60,61,62,63,66,67,68},{44,45,46,47,53,54,55,56,61,62,63,67,68},{45,46,47,54,55,56,62,63,68},{46,47,55,56,63},{47,56},{48},{57,48,49},{64,57,58,48,49,50},{57,58,59,64,65,48,49,50,51},{57,58,59,60,48,49,50,51,52,64,65,66},{49,50,51,52,53,57,58,59,60,61,64,65,66,67},{50,51,52,53,54,58,59,60,61,62,64,65,66,67,68},{51,52,53,54,55,59,60,61,62,63,65,66,67,68},{52,53,54,55,56,60,61,62,63,66,67,68},{53,54,55,56,61,62,63,67,68},{54,55,56,62,63,68},{55,56,63},{56},{},{57},{64,57,58},{64,65,57,58,59},{57,58,59,60,64,65,66},{57,58,59,60,61,64,65,66,67},{58,59,60,61,62,64,65,66,67,68},{59,60,61,62,63,65,66,67,68},{60,61,62,63,66,67,68},{61,62,63,67,68},{62,63,68},{63},{},{},{},{64},{64,65},{64,65,66},{64,65,66,67},{64,65,66,67,68},{65,66,67,68},{66,67,68},{67,68},{68},{},{}};
    public static final int[][] gridLookupIndicesPattern = {{}, {}, {0}, {0, 1}, {0, 1, 2}, {0, 1, 2, 3}, {0, 1, 2, 3, 4}, {1, 2, 3, 4}, {2, 3, 4}, {3, 4}, {4}, {}, {}, {}, {5}, {5, 6, 0}, {5, 6, 7, 0, 1}, {5, 6, 7, 8, 0, 1, 2}, {5, 6, 7, 8, 9, 0, 1, 2, 3}, {6, 7, 8, 9, 10, 0, 1, 2, 3, 4}, {7, 8, 9, 10, 11, 1, 2, 3, 4}, {2, 3, 4, 8, 9, 10, 11}, {3, 4, 9, 10, 11}, {4, 10, 11}, {11}, {}, {12}, {12, 13, 5}, {12, 13, 14, 5, 6, 0}, {12, 13, 14, 15, 5, 6, 7, 0, 1}, {12, 13, 14, 15, 16, 5, 6, 7, 8, 0, 1, 2}, {13, 14, 15, 16, 17, 5, 6, 7, 8, 9, 0, 1, 2, 3}, {14, 15, 16, 17, 18, 6, 7, 8, 9, 10, 0, 1, 2, 3, 4}, {7, 8, 9, 10, 11, 15, 16, 17, 18, 19, 1, 2, 3, 4}, {8, 9, 10, 11, 2, 3, 4, 16, 17, 18, 19, 20}, {3, 4, 9, 10, 11, 17, 18, 19, 20}, {4, 10, 11, 18, 19, 20}, {11, 19, 20}, {20}, {21, 12}, {21, 22, 12, 13, 5}, {21, 22, 23, 12, 13, 14, 5, 6, 0}, {21, 22, 23, 24, 12, 13, 14, 15, 5, 6, 7, 0, 1}, {21, 22, 23, 24, 25, 12, 13, 14, 15, 16, 5, 6, 7, 8, 0, 1, 2}, {22, 23, 24, 25, 26, 13, 14, 15, 16, 17, 5, 6, 7, 8, 9, 0, 1, 2, 3}, {14, 15, 16, 17, 18, 23, 24, 25, 26, 27, 6, 7, 8, 9, 10, 0, 1, 2, 3, 4}, {15, 16, 17, 18, 19, 7, 8, 9, 10, 11, 24, 25, 26, 27, 28, 1, 2, 3, 4}, {8, 9, 10, 11, 16, 17, 18, 19, 20, 2, 3, 4, 25, 26, 27, 28, 29}, {9, 10, 11, 3, 4, 17, 18, 19, 20, 26, 27, 28, 29}, {4, 10, 11, 18, 19, 20, 27, 28, 29}, {11, 19, 20, 28, 29}, {20, 29}, {30, 21, 12}, {30, 31, 21, 22, 12, 13, 5}, {30, 31, 32, 21, 22, 23, 12, 13, 14, 5, 6, 0}, {30, 31, 32, 33, 21, 22, 23, 24, 12, 13, 14, 15, 5, 6, 7, 0, 1}, {30, 31, 32, 33, 34, 21, 22, 23, 24, 25, 12, 13, 14, 15, 16, 5, 6, 7, 8, 0, 1, 2}, {22, 23, 24, 25, 26, 31, 32, 33, 34, 35, 13, 14, 15, 16, 17, 5, 6, 7, 8, 9, 0, 1, 2, 3}, {23, 24, 25, 26, 27, 14, 15, 16, 17, 18, 32, 33, 34, 35, 36, 6, 7, 8, 9, 10, 0, 1, 2, 3, 4}, {15, 16, 17, 18, 19, 24, 25, 26, 27, 28, 7, 8, 9, 10, 11, 33, 34, 35, 36, 37, 1, 2, 3, 4}, {16, 17, 18, 19, 20, 8, 9, 10, 11, 25, 26, 27, 28, 29, 2, 3, 4, 34, 35, 36, 37, 38}, {9, 10, 11, 17, 18, 19, 20, 3, 4, 26, 27, 28, 29, 35, 36, 37, 38}, {10, 11, 4, 18, 19, 20, 27, 28, 29, 36, 37, 38}, {11, 19, 20, 28, 29, 37, 38}, {20, 29, 38}, {39, 30, 21, 12}, {39, 40, 30, 31, 21, 22, 12, 13, 5}, {39, 40, 41, 30, 31, 32, 21, 22, 23, 12, 13, 14, 5, 6}, {39, 40, 41, 42, 30, 31, 32, 33, 21, 22, 23, 24, 12, 13, 14, 15, 5, 6, 7}, {30, 31, 32, 33, 34, 39, 40, 41, 42, 43, 21, 22, 23, 24, 25, 12, 13, 14, 15, 16, 5, 6, 7, 8}, {31, 32, 33, 34, 35, 22, 23, 24, 25, 26, 40, 41, 42, 43, 44, 13, 14, 15, 16, 17, 5, 6, 7, 8, 9}, {23, 24, 25, 26, 27, 32, 33, 34, 35, 36, 14, 15, 16, 17, 18, 41, 42, 43, 44, 45, 6, 7, 8, 9, 10}, {24, 25, 26, 27, 28, 15, 16, 17, 18, 19, 33, 34, 35, 36, 37, 7, 8, 9, 10, 11, 42, 43, 44, 45, 46}, {16, 17, 18, 19, 20, 25, 26, 27, 28, 29, 8, 9, 10, 11, 34, 35, 36, 37, 38, 43, 44, 45, 46, 47}, {17, 18, 19, 20, 9, 10, 11, 26, 27, 28, 29, 35, 36, 37, 38, 44, 45, 46, 47}, {10, 11, 18, 19, 20, 27, 28, 29, 36, 37, 38, 45, 46, 47}, {11, 19, 20, 28, 29, 37, 38, 46, 47}, {20, 29, 38, 47}, {48, 39, 30, 21, 12}, {48, 49, 39, 40, 30, 31, 21, 22, 12, 13}, {48, 49, 50, 39, 40, 41, 30, 31, 32, 21, 22, 23, 12, 13, 14}, {39, 40, 41, 42, 48, 49, 50, 51, 30, 31, 32, 33, 21, 22, 23, 24, 12, 13, 14, 15}, {39, 40, 41, 42, 43, 30, 31, 32, 33, 34, 48, 49, 50, 51, 52, 21, 22, 23, 24, 25, 12, 13, 14, 15, 16}, {31, 32, 33, 34, 35, 40, 41, 42, 43, 44, 22, 23, 24, 25, 26, 49, 50, 51, 52, 53, 13, 14, 15, 16, 17}, {32, 33, 34, 35, 36, 23, 24, 25, 26, 27, 41, 42, 43, 44, 45, 14, 15, 16, 17, 18, 50, 51, 52, 53, 54}, {24, 25, 26, 27, 28, 33, 34, 35, 36, 37, 15, 16, 17, 18, 19, 42, 43, 44, 45, 46, 51, 52, 53, 54, 55}, {25, 26, 27, 28, 29, 16, 17, 18, 19, 20, 34, 35, 36, 37, 38, 43, 44, 45, 46, 47, 52, 53, 54, 55, 56}, {17, 18, 19, 20, 26, 27, 28, 29, 35, 36, 37, 38, 44, 45, 46, 47, 53, 54, 55, 56}, {18, 19, 20, 27, 28, 29, 36, 37, 38, 45, 46, 47, 54, 55, 56}, {19, 20, 28, 29, 37, 38, 46, 47, 55, 56}, {20, 29, 38, 47, 56}, {48, 39, 30, 21}, {57, 48, 49, 39, 40, 30, 31, 21, 22}, {48, 49, 50, 57, 58, 39, 40, 41, 30, 31, 32, 21, 22, 23}, {48, 49, 50, 51, 39, 40, 41, 42, 57, 58, 59, 30, 31, 32, 33, 21, 22, 23, 24}, {39, 40, 41, 42, 43, 48, 49, 50, 51, 52, 30, 31, 32, 33, 34, 57, 58, 59, 60, 21, 22, 23, 24, 25}, {40, 41, 42, 43, 44, 31, 32, 33, 34, 35, 49, 50, 51, 52, 53, 22, 23, 24, 25, 26, 57, 58, 59, 60, 61}, {32, 33, 34, 35, 36, 41, 42, 43, 44, 45, 23, 24, 25, 26, 27, 50, 51, 52, 53, 54, 58, 59, 60, 61, 62}, {33, 34, 35, 36, 37, 24, 25, 26, 27, 28, 42, 43, 44, 45, 46, 51, 52, 53, 54, 55, 59, 60, 61, 62, 63}, {25, 26, 27, 28, 29, 34, 35, 36, 37, 38, 43, 44, 45, 46, 47, 52, 53, 54, 55, 56, 60, 61, 62, 63}, {26, 27, 28, 29, 35, 36, 37, 38, 44, 45, 46, 47, 53, 54, 55, 56, 61, 62, 63}, {27, 28, 29, 36, 37, 38, 45, 46, 47, 54, 55, 56, 62, 63}, {28, 29, 37, 38, 46, 47, 55, 56, 63}, {29, 38, 47, 56}, {48, 39, 30}, {57, 48, 49, 39, 40, 30, 31}, {57, 58, 48, 49, 50, 64, 39, 40, 41, 30, 31, 32}, {48, 49, 50, 51, 57, 58, 59, 39, 40, 41, 42, 64, 65, 30, 31, 32, 33}, {48, 49, 50, 51, 52, 39, 40, 41, 42, 43, 57, 58, 59, 60, 30, 31, 32, 33, 34, 64, 65, 66}, {40, 41, 42, 43, 44, 49, 50, 51, 52, 53, 31, 32, 33, 34, 35, 57, 58, 59, 60, 61, 64, 65, 66, 67}, {41, 42, 43, 44, 45, 32, 33, 34, 35, 36, 50, 51, 52, 53, 54, 58, 59, 60, 61, 62, 64, 65, 66, 67, 68}, {33, 34, 35, 36, 37, 42, 43, 44, 45, 46, 51, 52, 53, 54, 55, 59, 60, 61, 62, 63, 65, 66, 67, 68}, {34, 35, 36, 37, 38, 43, 44, 45, 46, 47, 52, 53, 54, 55, 56, 60, 61, 62, 63, 66, 67, 68}, {35, 36, 37, 38, 44, 45, 46, 47, 53, 54, 55, 56, 61, 62, 63, 67, 68}, {36, 37, 38, 45, 46, 47, 54, 55, 56, 62, 63, 68}, {37, 38, 46, 47, 55, 56, 63}, {38, 47, 56}, {48, 39}, {57, 48, 49, 39, 40}, {57, 58, 64, 48, 49, 50, 39, 40, 41}, {57, 58, 59, 48, 49, 50, 51, 64, 65, 39, 40, 41, 42}, {48, 49, 50, 51, 52, 57, 58, 59, 60, 39, 40, 41, 42, 43, 64, 65, 66}, {49, 50, 51, 52, 53, 40, 41, 42, 43, 44, 57, 58, 59, 60, 61, 64, 65, 66, 67}, {41, 42, 43, 44, 45, 50, 51, 52, 53, 54, 58, 59, 60, 61, 62, 64, 65, 66, 67, 68}, {42, 43, 44, 45, 46, 51, 52, 53, 54, 55, 59, 60, 61, 62, 63, 65, 66, 67, 68}, {43, 44, 45, 46, 47, 52, 53, 54, 55, 56, 60, 61, 62, 63, 66, 67, 68}, {44, 45, 46, 47, 53, 54, 55, 56, 61, 62, 63, 67, 68}, {45, 46, 47, 54, 55, 56, 62, 63, 68}, {46, 47, 55, 56, 63}, {47, 56}, {48}, {57, 48, 49}, {64, 57, 58, 48, 49, 50}, {57, 58, 59, 64, 65, 48, 49, 50, 51}, {57, 58, 59, 60, 48, 49, 50, 51, 52, 64, 65, 66}, {49, 50, 51, 52, 53, 57, 58, 59, 60, 61, 64, 65, 66, 67}, {50, 51, 52, 53, 54, 58, 59, 60, 61, 62, 64, 65, 66, 67, 68}, {51, 52, 53, 54, 55, 59, 60, 61, 62, 63, 65, 66, 67, 68}, {52, 53, 54, 55, 56, 60, 61, 62, 63, 66, 67, 68}, {53, 54, 55, 56, 61, 62, 63, 67, 68}, {54, 55, 56, 62, 63, 68}, {55, 56, 63}, {56}, {}, {57}, {64, 57, 58}, {64, 65, 57, 58, 59}, {57, 58, 59, 60, 64, 65, 66}, {57, 58, 59, 60, 61, 64, 65, 66, 67}, {58, 59, 60, 61, 62, 64, 65, 66, 67, 68}, {59, 60, 61, 62, 63, 65, 66, 67, 68}, {60, 61, 62, 63, 66, 67, 68}, {61, 62, 63, 67, 68}, {62, 63, 68}, {63}, {}, {}, {}, {64}, {64, 65}, {64, 65, 66}, {64, 65, 66, 67}, {64, 65, 66, 67, 68}, {65, 66, 67, 68}, {66, 67, 68}, {67, 68}, {68}, {}, {}};
}