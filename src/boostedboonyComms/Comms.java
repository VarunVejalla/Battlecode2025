package boostedboonyComms;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Comms {

    RobotController rc;
    int[] myWorld;

    int sectorRows;
    int sectorCols;
    int sectorCount;

    public Comms(RobotController rc) {
        this.rc = rc;

        sectorRows = (rc.getMapWidth() + 4) / 5;
        sectorCols = (rc.getMapHeight() + 4) / 5;
        sectorCount = sectorCols * sectorRows;
        myWorld = new int[sectorCount];
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
     * Builds the 8-bit representation for a sector and stores it in `myWorld`.
     */
    public void buildSector(int sectorIndex, int enemyPaintCount, int emptyCount, int ruinCondition, boolean staleBit) {
        int sectorValue = 0;

        // First 2 bits: enemy paint count
        sectorValue |= (convertTileCounts(enemyPaintCount) & 0b11) << 6;

        // Next 2 bits: empty cell count
        sectorValue |= (convertTileCounts(emptyCount) & 0b11) << 4;

        // Next 3 bits: ruin condition (assumed 3 bits)
        sectorValue |= (ruinCondition & 0b111) << 1;

        // Last bit: stale flag
        sectorValue |= (staleBit ? 1 : 0);

        myWorld[sectorIndex] = sectorValue;
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

        return new int[] {enemyPaintCount, emptyCount, ruinCondition, staleBit};
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
}
