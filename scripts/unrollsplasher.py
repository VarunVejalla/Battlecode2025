# tocopy = """
#         index = Util.getMapInfoIndex(<dx>, <dy>);
#         if(index != -1 && nearbyMapInfos[index] != null) {
#             switch(nearbyMapInfos[index].getPaint()) {
#                 case EMPTY:
#                     emptyCount++;
#                     break;
#                 <REPLACE>
#             }
#         }
# """
# enemycount = """
#                 case ENEMY_PRIMARY:
#                 case ENEMY_SECONDARY:
#                     enemyCount++;
#                     break;
# """
#
# output = open("scripts/unrollsplasher.txt", "w+")
# emptydist = 4
# enemydist = 2
# text = ""
# for dx in range(-2, 3):
#     for dy in range(-2, 3):
#         if dx*dx + dy*dy > emptydist:
#             continue
#
#         if dx == 0:
#             copy = tocopy.replace("<dx>", "loc.x")
#         elif dx > 0:
#             copy = tocopy.replace("<dx>", f"loc.x + {dx}")
#         else:
#             copy = tocopy.replace("<dx>", f"loc.x - {abs(dx)}")
#
#         if dy == 0:
#             copy = copy.replace("<dy>", "loc.y")
#         elif dy > 0:
#             copy = copy.replace("<dy>", f"loc.y + {dy}")
#         else:
#             copy = copy.replace("<dy>", f"loc.y - {abs(dy)}")
#
#         if dx*dx + dy*dy > enemydist:
#             copy = copy.replace("<REPLACE>", "")
#         else:
#             copy = copy.replace("<REPLACE>", enemycount)
#         text += copy + "\n"
# output.write(text)


tocopy = """
        index = Util.getMapInfoIndex(<dx>, <dy>);
        if(nearbyMapInfos[index] != null) {
            switch(nearbyMapInfos[index].getPaint()) {
                <REPLACE>           }
        }
"""

emptyblock = """case EMPTY:
<REPLACE>
                    break;
"""


enemyblock = """                case ENEMY_PRIMARY:
                case ENEMY_SECONDARY:
<REPLACE>
                    break;
"""

# Indexing:
"""
x x 12 x x
x 9 10 11 x
4 5 6 7 8
x 1 2 3 x
x x 0 x x
"""


output = open("scripts/unrollsplasher.txt", "w+")
emptydist = 4
enemydist = 2
text = ""

def getidx(x, y, is_enemy):
    arr = [
        [-1, -1, 0, -1, -1],
        [-1, 1, 2, 3, -1],
        [4, 5, 6, 7, 8],
        [-1, 9, 10, 11, -1],
        [-1, -1, 12, -1, -1]
    ]
    idx = arr[y+2][x+2]
    assert(idx != -1)
    if is_enemy:
        idx += 13
    return idx

for x in range(-4, 5):
    for y in range(-4, 5):
        empties = []
        enemies = []
        for dx in range(-2, 3):
            for dy in range(-2, 3):
                newx, newy = x+dx, y+dy
                if newx*newx + newy*newy > 4:
                    continue
                if dx*dx + dy*dy <= emptydist:
                    empties.append((newx, newy))
                if dx*dx + dy*dy <= enemydist:
                    enemies.append((newx, newy))
        if len(empties) == 0 and len(enemies) == 0:
            continue

        empty, enemy = "", ""
        if len(empties) > 0:
            empty = emptyblock.replace("<REPLACE>", "\n".join([f"                counts_arr[{getidx(nx, ny, False)}]++;" for nx, ny in empties]))
        if len(enemies) > 0:
            enemy = enemyblock.replace("<REPLACE>", "\n".join([f"                counts_arr[{getidx(nx, ny, True)}]++;" for nx, ny in enemies]))
        copy = tocopy.replace("<dx>", str(x)).replace("<dy>", str(y)).replace("<REPLACE>", empty + enemy)
        text += copy + "\n"
output.write(text)
