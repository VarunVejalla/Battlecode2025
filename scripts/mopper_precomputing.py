shift_dx = [-4,-4,-4,-4,-4,-3,-3,-3,-3,-3,-3,-3,-2,-2,-2,-2,-2,-2,-2,-2,-2,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,4,4,4,4,4]
shift_dy = [-2,-1,0,1,2,-3,-2,-1,0,1,2,3,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-4,-3,-2,-1,0,1,2,3,4,-3,-2,-1,0,1,2,3,-2,-1,0,1,2]
mapping = {}
for i, (x, y) in enumerate(zip(shift_dx, shift_dy)):
    mapping[(x, y)] = i


output = open("scripts/mopper_precomputing.txt", "w+")
output.write("switch(index){\n")
for i, (x, y) in enumerate(zip(shift_dx, shift_dy)):
    output.write(f"\tcase {i}:\n")
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            if dx == 0 and dy == 0:
                continue
            newx, newy = x + dx, y + dy
            if (newx, newy) not in mapping:
                continue
            newidx = mapping[(newx, newy)]
            output.write(f"\t\theuristics[{newidx}] += diff;\n")
    output.write("\t\tbreak;\n")
output.write("}")

output = open("scripts/mopper_precomputing_dist9.txt", "w+")
output.write("switch(index){\n")
for i, (x, y) in enumerate(zip(shift_dx, shift_dy)):
    output.write(f"\tcase {i}:\n")
    for dx in range(-3, 4):
        for dy in range(-3, 4):
            if dx*dx + dy*dy > 9:
                continue
            newx, newy = x + dx, y + dy
            if (newx, newy) not in mapping:
                continue
            newidx = mapping[(newx, newy)]
            output.write(f"\t\theuristics[{newidx}] += diff;\n")
    output.write("\t\tbreak;\n")
output.write("}")
