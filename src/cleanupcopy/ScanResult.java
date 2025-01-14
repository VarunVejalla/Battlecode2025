package cleanupcopy;

/**
 * Basic datastructure to hold scan results that will be used to populate comms
 */
public class ScanResult {
    public final int towerType;
    public final int enemyPaintLevel;
    public final int emptyPaintLevel;

    public ScanResult(int towerType, int enemyPaintLevel, int emptyPaintLevel) {
        this.towerType = towerType;
        this.enemyPaintLevel = enemyPaintLevel;
        this.emptyPaintLevel = emptyPaintLevel;
    }

    @Override
    public String toString() {
        return "ScanResult{" +
                "towerType=" + towerType +
                ", enemyPaintLevel=" + enemyPaintLevel +
                ", emptyPaintLevel=" + emptyPaintLevel +
                '}';
    }
}