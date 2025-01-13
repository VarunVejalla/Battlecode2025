package boostedboony;

import battlecode.common.UnitType;

enum PatternType {
    RESOURCE,
    PAINT_TOWER,
    MONEY_TOWER,
    DEFENSE_TOWER;

    public boolean isTower() {
        return this != RESOURCE;
    }

    public UnitType getUnitType() {
        if (this == PAINT_TOWER) {
            return UnitType.LEVEL_ONE_PAINT_TOWER;
        } else if (this == MONEY_TOWER) {
            return UnitType.LEVEL_ONE_MONEY_TOWER;
        } else if (this == DEFENSE_TOWER) {
            return UnitType.LEVEL_ONE_DEFENSE_TOWER;
        } else {
            return null;
        }
    }
}