tocopy = """
        index = Util.getMapInfoIndex(<dx>, <dy>);
        if(index != -1 && nearbyMapInfos[index] != null) {
            switch(nearbyMapInfos[index].getPaint()) {
                case EMPTY:
                    emptyCount++;
                    break;
                <REPLACE>
            }
        }
"""
enemycount = """
                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
                    enemyCount++;
                    break;
"""

output = open("scripts/unrollsplasher.txt", "w+")
emptydist = 4
enemydist = 2
text = ""
for dx in range(-2, 3):
    for dy in range(-2, 3):
        if dx*dx + dy*dy > emptydist:
            continue

        if dx == 0:
            copy = tocopy.replace("<dx>", "loc.x")
        elif dx > 0:
            copy = tocopy.replace("<dx>", f"loc.x + {dx}")
        else:
            copy = tocopy.replace("<dx>", f"loc.x - {abs(dx)}")

        if dy == 0:
            copy = copy.replace("<dy>", "loc.y")
        elif dy > 0:
            copy = copy.replace("<dy>", f"loc.y + {dy}")
        else:
            copy = copy.replace("<dy>", f"loc.y - {abs(dy)}")

        if dx*dx + dy*dy > enemydist:
            copy = copy.replace("<REPLACE>", "")
        else:
            copy = copy.replace("<REPLACE>", enemycount)
        text += copy + "\n"
output.write(text)
