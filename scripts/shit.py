# shit = "34,25,33,35,43,24,26,42,44,16,32,36,52,15,17,23,27,41,45,51,53,14,18,50,54,8,31,37,60,7,9,22,28,40,46,59,61,6,10,13,19,49,55,58,62,2,30,38,66,1,3,21,29,39,47,65,67,5,11,57,63,0,4,12,20,48,56,64,68"
# shit = shit.split(",")
# reverse = [-1 for _ in range(69)]
# for i, s in enumerate(shit):
#     s = int(s)
#     reverse[s] = i
#
# print(",".join([str(i) for i in reverse]))

for i in range(0, 60, 5):
    # print(f"    boolean[][] invalidPotentialLocs{i} = null;")
    print(f"""case {i}:
        invalidPotentialLocs{i} = new boolean[width];
        invalidPotentialLocs{i + 1} = new boolean[width];
        invalidPotentialLocs{i + 2} = new boolean[width];
        invalidPotentialLocs{i + 3} = new boolean[width];
        invalidPotentialLocs{i + 4} = new boolean[width];
        return;""")

    # print(f"            case {i}: return invalidPotentialLocs{i}[y];")
    # print(f"""case {i}:
    #             invalidPotentialLocs{i}[y] = true;
    #             break;""")

