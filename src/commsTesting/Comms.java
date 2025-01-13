package commsTesting;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.*;

public class Comms {

    // TODO: Things to test for.
    // Does Scan Result work? Yes.
    // Did the bunny's world update? Yes.
    // Did the buffer update one term correctly? Yes.
    // Can the bunny send a buffer message to the tower? Yes.
    // Did the buffer update multiple terms correctly? Yes.
    // Does the tower's world update correctly in response to the buffer message? Yes.
    // Does the tower correctly transmit the map to the bunny? Yes.
    // Does the bunny's map correctly update? Yes.
    // Is message conversion working correctly? Yes.
    // TODO: Make the bunny's movement match up with comms (comms will work anyway!! but may not be optimized)
    // TODO: Does this work for multiple robots with a single tower?
    // TODO: Does this work for large maps? no. :(( (not yet!)

    public final int MAP_UPDATE_REQUEST_CODE = 0xFFFF;
    public final int MAP2_UPDATE_REQUEST_CODE = 0xFFFE; // Used for larger maps.
    public final int MAX_MAP_SECTORS_SENT_PER_ROUND = 80;


    RobotController rc;
    int[] myWorld;

    int sectorRows;
    int sectorCols;
    int sectorCount;

    public Comms(RobotController rc) {
        this.rc = rc;

        this.sectorRows = (rc.getMapWidth() + 4) / 5;
        this.sectorCols = (rc.getMapHeight() + 4) / 5;
        this.sectorCount = sectorCols * sectorRows;
        this.myWorld = new int[sectorCount];
    }




    /**
     * Converts tile counts into 2-bit representation:
     * 00: 0-1 tiles
     * 01: 2-4 tiles
     * 10: 5-12 tiles
     * 11: 13+ tiles
     */
    private int convertTileCounts(int tileCounts) {
        if (tileCounts < 2) return 0;
        if (tileCounts < 5) return 1;
        if (tileCounts <= 12) return 2;
        return 3;
    }

    /**
     * Builds the 8-bit representation for a sector given a ScanResult.
     */
    public int encodeSector(ScanResult sr) {
        int sectorValue = 0;

        // NOTE: tile counts are already converted!!!

        // First 2 bits: enemy paint count
        sectorValue |= ((sr.enemyPaintLevel) & 0b11) << 6;

        // Next 2 bits: empty cell count
        sectorValue |= ((sr.emptyPaintLevel) & 0b11) << 4;

        // Next 3 bits: ruin condition (assumed 3 bits)
        sectorValue |= (sr.towerType & 0b111) << 1;

        // Last bit: free

        return sectorValue;
    }


    /**
     * Decodes the sector data and returns an array of values:
     * [enemyPaintCount, emptyCount, ruinCondition, staleBit]
     */
    public int[] decodeSector(int sectorIndex) {
        int sectorValue = myWorld[sectorIndex];

        int staleBit = sectorValue & 0b1;
        int ruinCondition = (sectorValue >> 1) & 0b111;
        int emptyCount = (sectorValue >> 4) & 0b11;
        int enemyPaintCount = (sectorValue >> 6) & 0b11;

        // TODO: Change this to return a ScanResult object instead.
        return new int[]{enemyPaintCount, emptyCount, ruinCondition, staleBit};
    }

    /**
     * Returns the center of a sector given its index.
     */
    public MapLocation getSectorCenter(int sectorIndex) {
        int row = sectorIndex / sectorCols;
        int col = sectorIndex % sectorCols;

        int centerX = col * 5 + 2; // Center of the 5x5 grid
        int centerY = row * 5 + 2;

        return new MapLocation(centerX, centerY);
    }

    /**
     * Returns the sector index for a given MapLocation.
     */
    public int getSectorIndex(MapLocation loc) {
        int col = loc.x / 5;
        int row = loc.y / 5;
        return row * sectorCols + col;
    }


    /**
     * Returns the index of the sector that is fully contained within a vision radius from center.
     * Returns -1 if there is no such sector.
     */
    public int getFullyEnclosedSectorID(MapLocation center) {
        // There is only one sector that could be fully enclosed. It must contain the center.
        int sectorIndex = getSectorIndex(center);

        // If center is within radius squared 4 of the sector center, the sector is fully visible, even if the sector is cutoff!
        if (center.isWithinDistanceSquared(getSectorCenter(sectorIndex), 4)) {
            return sectorIndex;
        }

        return -1;
    }


    /**
     * Scans a sector and returns the scan result containing tower type, enemy paint count, and empty paint count.
     */
    public ScanResult scanSector(int sectorIndex) throws GameActionException {
        int towerType = 0; // Default value
        int enemyPaintCount = 0;
        int emptyPaintCount = 0;

        MapLocation sectorCenter = getSectorCenter(sectorIndex);
        // Determine the bottom-left corner of the sector
        int startX = sectorCenter.x - 2;
        int startY = sectorCenter.y - 2;

        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                int x = startX + dx;
                int y = startY + dy;

                MapLocation scanLoc = new MapLocation(x, y);
                if (!rc.onTheMap(scanLoc) || !rc.canSenseLocation(scanLoc)) {
                    continue; // Skip out-of-bounds locations
                }

                MapInfo mapInfo = rc.senseMapInfo(scanLoc);


                if (towerType == 0) { // only Check for tower if we have not already found one

                    if (mapInfo.hasRuin()) {
                        towerType = 1; //set tower type to one if it has a ruin
                    }
                    if (rc.canSenseRobotAtLocation(scanLoc)) {
                        RobotInfo robot = rc.senseRobotAtLocation(scanLoc);
                        if (Util.isTower(robot.type)) {
                            // Determine tower type based on additional properties
                            towerType = determineTowerType(robot);
                        }
                    }
                }

                if (mapInfo.getPaint() == PaintType.ENEMY_PRIMARY || mapInfo.getPaint() == PaintType.ENEMY_SECONDARY) {
                    enemyPaintCount++;
                } else if (mapInfo.getPaint() == PaintType.EMPTY) {
                    emptyPaintCount++;
                }
            }
        }

        return new ScanResult(towerType,
                convertTileCounts(enemyPaintCount),
                convertTileCounts(emptyPaintCount));
    }


    /**
     * Determines the tower type mapping
     * 0	none
     * 1	ruin
     *
     * (these two cases are handled in the method above)
     *
     * 2	f - paint
     * 3	f - money
     * 4	f - defense
     * 5	e - paint
     * 6	e - money
     * 7	e - defense
     */
    private int determineTowerType(RobotInfo robot) {
        if (robot.getTeam() == rc.getTeam()) {
            switch (robot.type) {
                case LEVEL_ONE_PAINT_TOWER:
                case LEVEL_TWO_PAINT_TOWER:
                case LEVEL_THREE_PAINT_TOWER:
                    return 2;
                case LEVEL_ONE_MONEY_TOWER:
                case LEVEL_TWO_MONEY_TOWER:
                case LEVEL_THREE_MONEY_TOWER:
                    return 3;
                case LEVEL_ONE_DEFENSE_TOWER:
                case LEVEL_TWO_DEFENSE_TOWER:
                case LEVEL_THREE_DEFENSE_TOWER:
                    return 4;
            }
        } else {
            switch (robot.type) {
                case LEVEL_ONE_PAINT_TOWER:
                case LEVEL_TWO_PAINT_TOWER:
                case LEVEL_THREE_PAINT_TOWER:
                    return 5;
                case LEVEL_ONE_MONEY_TOWER:
                case LEVEL_TWO_MONEY_TOWER:
                case LEVEL_THREE_MONEY_TOWER:
                    return 6;
                case LEVEL_ONE_DEFENSE_TOWER:
                case LEVEL_TWO_DEFENSE_TOWER:
                case LEVEL_THREE_DEFENSE_TOWER:
                    return 7;
            }
        }
        return 0; // Default case, should not happen
    }

    public void describeWorld() {
        Util.log("\n -------------------------------- \n");
        Util.log("My World: \n");
        for (int sectorIndex = 0; sectorIndex < sectorCount; sectorIndex++) {
            if (myWorld[sectorIndex] == 0) continue;
            Util.log("Sector Center: " + getSectorCenter(sectorIndex));
            Util.log(Util.getSectorDescription(myWorld[sectorIndex]) + "\n\n");
        }

    }
}










