package moneymoneymoney;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Comms {

    public final int MAP_UPDATE_REQUEST_CODE = 0xFFFF;
    public final int MAP2_UPDATE_REQUEST_CODE = 0xFFFE; // Used for larger maps.
    public final int MAX_MAP_SECTORS_SENT_PER_ROUND = 80;
    public final int NULL_MESSAGE = 0x0FF0;

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
    public int convertTileCounts(int tileCounts) {
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

        // Last bit: experienced bit. Given a scan result, assuming scan complete.
        sectorValue |= 0b1;

        return sectorValue;
    }


    /**
     * Decodes the sector data and returns an array of values:
     * [enemyPaintCount, emptyCount, ruinCondition, staleBit]
     */
    public ScanResult decodeSector(int encodedSector) {

        int ruinCondition = (encodedSector >> 1) & 0b111;
        int emptyCount = (encodedSector >> 4) & 0b11;
        int enemyPaintCount = (encodedSector >> 6) & 0b11;

        // Put in a fake sector and a fake round number.
        return new ScanResult(-1, ruinCondition, enemyPaintCount, emptyCount, -1);
    }

    /**
     * Returns the center of a sector given its index.
     */
    public MapLocation getSectorCenter(int sectorIndex) {
        int row = sectorIndex / sectorRows;
        int col = sectorIndex % sectorRows;

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
        return row * sectorRows + col;
    }

    /**
     * Returns an array containing the index of the sector that contains the given MapLocation
     * as well as the indices of its neighboring sectors.
     */
    public int[] getSectorAndNeighbors(MapLocation loc, int sectorsAway) {
        int col = loc.x / 5;
        int row = loc.y / 5;

        // Precompute bounds to avoid repeated checks
        int minRow = Math.max(0, row - sectorsAway);
        int maxRow = Math.min(sectorCols - 1, row + sectorsAway);
        int minCol = Math.max(0, col - sectorsAway);
        int maxCol = Math.min(sectorRows - 1, col + sectorsAway);

        // Validate bounds
        if (maxRow < minRow || maxCol < minCol) {
            throw new IllegalStateException("Invalid sector bounds");
        }

        // Calculate the number of valid neighbors
        int neighborCount = (maxRow - minRow + 1) * (maxCol - minCol + 1);

        int[] neighbors = new int[neighborCount];
        int index = 0;

        // Iterate only over valid rows and columns
        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                neighbors[index++] = r * sectorRows + c;
            }
        }

        return neighbors;
    }



    /**
     * Returns the index of the sector that is fully contained within a vision radius from center.
     * Returns -1 if there is no such sector.
     */
    public int getFullyEnclosedSectorID(MapLocation center) {
        // There is only one sector that could be fully enclosed. It must contain the center.
        int sectorIndex = getSectorIndex(center);
        assert sectorIndex < sectorCount;
        // If center is within radius squared 4 of the sector center, the sector is fully visible, even if the sector is cutoff!
        if (center.isWithinDistanceSquared(getSectorCenter(sectorIndex), 4)) {
            return sectorIndex;
        }

        return -1;
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
    public int determineTowerType(RobotInfo robot) {
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
            Util.log(Util.getSectorDescription(myWorld[sectorIndex]));
        }
    }
}
