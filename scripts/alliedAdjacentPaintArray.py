file = open("alliedAdjacentPaintArray.txt", "w")

ACTION_RADIUS = 20
OFFSET = 4

file.write("switch (dx) {\n")
for dx in range(-4, 5):
    file.write("case " + str(dx) + ":\n")
    file.write("switch (dy) {\n")
    for dy in range(-4, 5):
        dist = dx*dx + dy*dy
        if dist > ACTION_RADIUS:
            continue
        toadd = []
        for adjx in range(-1, 2):
            for adjy in range(-1, 2):
                if adjx == 0 and adjy == 0:
                    continue
                newdx, newdy = dx + adjx, dy + adjy
                if newdx*newdx + newdy*newdy > ACTION_RADIUS:
                    continue
                toadd.append((newdx, newdy))
        sum = " + ".join([f"isAllied[{x + OFFSET}][{y + OFFSET}]" for x,y in toadd])
        file.write("case " + str(dy) + ": return " + sum + ";\n")
    file.write("case default: return 0;\n")
    file.write("}\n")
file.write("case default: return 0;\n")
file.write("}\n")

"""
    // ~Max like 2.5k bytecode usage.
    public void setAlliedPaintLocations() throws GameActionException {
        alliedPaintLocations = new short[ALLIED_PAINT_LOCATIONS_SIZE*ALLIED_PAINT_LOCATIONS_SIZE];
        MapLocation myLocation = rc.getLocation();
        for(MapInfo info : nearbyMapInfos){
            if(info.getPaint() == PaintType.ALLY_PRIMARY || info.getPaint() == PaintType.ALLY_SECONDARY){
                int dx = info.getMapLocation().x - myLocation.x + ALLIED_PAINT_OFFSET;
                int dy = info.getMapLocation().y - myLocation.y + ALLIED_PAINT_OFFSET;
                alliedPaintLocations[dx*ALLIED_PAINT_LOCATIONS_SIZE+dy] = 1;
            }
        }
    }
"""