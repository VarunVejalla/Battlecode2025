package thebiggestcommie;

/**
 * Basic datastructure to hold scan results that will be used to populate comms
 */
public class ScanResult {
    public final int sectorID;
    public final int towerType;
    public final int enemyPaintLevel;
    public final int emptyPaintLevel;
    public final int roundNum;

    public ScanResult(int sectorID, int towerType, int enemyPaintLevel, int emptyPaintLevel, int roundNum) {
        this.sectorID = sectorID;
        this.towerType = towerType;
        this.enemyPaintLevel = enemyPaintLevel;
        this.emptyPaintLevel = emptyPaintLevel;
        this.roundNum = roundNum;
    }

    @Override
    public String toString() {
        return "ScanResult for Sector "+ sectorID +" {" +
                "towerType=" + towerType +
                ", enemyPaintLevel=" + enemyPaintLevel +
                ", emptyPaintLevel=" + emptyPaintLevel +
                ", roundNum=" + roundNum +
                '}';
    }
}