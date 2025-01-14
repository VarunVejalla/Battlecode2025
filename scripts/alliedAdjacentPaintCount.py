file = open("alliedAdjacentPaintCount.txt", "w")

ACTION_RADIUS = 9
VISION_RADIUS = 20
OFFSET = 4
LENGTH = 9

file.write("switch (dx) {\n")
for dx in range(-3, 4):
    file.write("case " + str(dx) + ":\n")
    file.write("switch (dy) {\n")
    for dy in range(-3, 4):
        dist = dx*dx + dy*dy
        if dist > ACTION_RADIUS:
            continue
        toadd = []
        for adjx in range(-1, 2):
            for adjy in range(-1, 2):
                if adjx == 0 and adjy == 0:
                    continue
                newdx, newdy = dx + adjx, dy + adjy
                if newdx*newdx + newdy*newdy > VISION_RADIUS:
                    continue
                toadd.append((newdx, newdy))
        sum = " + ".join([f"isAllied[{(x + OFFSET)*LENGTH+(y + OFFSET)}]" for x,y in toadd])
        file.write("case " + str(dy) + ": return " + sum + ";\n")
    file.write("default: return 0;\n")
    file.write("}\n")
file.write("default: return 0;\n")
file.write("}\n")
