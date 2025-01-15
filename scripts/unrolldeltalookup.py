arr = "-1,-1,0,1,2,3,4,-1,-1,-1,5,6,7,8,9,10,11,-1,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,-1,57,58,59,60,61,62,63,-1,-1,-1,64,65,66,67,68,-1,-1"
arr = arr.split(",")
arr = [int(i) for i in arr]

output = open("scripts/unrolldeltalookup.txt", "w+")
output.write("switch(deltaX){\n")
for dx in range(-4, 5):
    output.write(f"case {dx}:\n")
    output.write("\tswitch(deltaY){\n")
    for dy in range(-4, 5):
        idx = dx*9+dy+40
        if arr[idx] == -1:
            continue
        output.write(f"\tcase {dy}: return {arr[idx]};\n")
    output.write("}\n")
output.write("}\n")
output.close()
