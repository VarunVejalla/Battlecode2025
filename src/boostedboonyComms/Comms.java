package boostedboonyComms;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.UnitType;

public class Comms {

    RobotController rc;
    Robot robot;
    Constants constants;
    int[][] myWorld;

    int sectorRows;
    int sectorCols;
    int sectorCount;

    public Comms(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;

        sectorRows = (rc.getMapWidth() + 4) / 5;
        sectorCols = (rc.getMapHeight() + 4) / 5;
        sectorCount = sectorCols * sectorRows;
        myWorld = new int[sectorRows][sectorCols];

    }

    // Format packet / message

    // Format sector
    private int convertTileCounts(int tileCounts) {
        if(tileCounts < 2) {
            return 0;
        } else if (tileCounts < 5) {
            return 1;
        } else if (tileCounts < 12) {
            return 2;
        }
        return 3;
    }
    public void buildSector(int sectorIndex, int enemyPaintCount, int emptyCount, int ruinCondition, boolean staleBit ) {
        int sectorValue = convertTileCounts(enemyPaintCount) << 2 + convertTileCounts(emptyCount);
        sectorValue = sectorValue << 3 + ruinCondition;
        sectorValue = sectorValue << 1 + (staleBit ? 1 : 0);
        myWorld[sectorIndex] = sectorValue;
    }

    public MapLocation getSectorCenter(int sectorIndex) {
        return new MapLocation(myWorld[sectorIndex], myWorld[sectorIndex + 1]);
    }

}