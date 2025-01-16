package benchmark;

import battlecode.common.PaintType;



public class AugmentedMapInfo {
    public boolean isPassable;
    public boolean isWall;
    public PaintType paint;
    public PaintType mark;
    public boolean hasRuin;

    public AugmentedMapInfo(boolean isPassable, boolean isWall, PaintType paint, PaintType mark, boolean hasRuin){
        this.isPassable = isPassable;
        this.isWall = isWall;
        this.paint = paint;
        this.mark = mark;
        this.hasRuin = hasRuin;
    }

    public boolean isPassable() {
        return isPassable;
    }
    public boolean isWall() {
        return isWall;
    }
    public boolean hasRuin() {
        return hasRuin;
    }
    public PaintType getPaint() {
        return paint;
    }
    public PaintType getMark() {
        return mark;
    }

    public String toString(){
        return "Location{" +
                (isWall ? ", wall" : "") +
                (hasRuin ? ", with ruin" : "") +
                ", paint=" + paint.toString() +
                ", mark=" + mark.toString() +
                "}";
    }

}