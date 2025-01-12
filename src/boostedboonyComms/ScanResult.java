package boostedboonyComms;

/**
 * Basic datastructure to hold scan results that will be used to populate comms
 */
public class ScanResult {
    public final int towerType;
    public final int enemyPaintCount;
    public final int emptyPaintCount;

    public ScanResult(int towerType, int enemyPaintCount, int emptyPaintCount) {
        this.towerType = towerType;
        this.enemyPaintCount = enemyPaintCount;
        this.emptyPaintCount = emptyPaintCount;
    }

    @Override
    public String toString() {
        return "ScanResult{" +
                "towerType=" + towerType +
                ", enemyPaintCount=" + enemyPaintCount +
                ", emptyPaintCount=" + emptyPaintCount +
                '}';
    }
}