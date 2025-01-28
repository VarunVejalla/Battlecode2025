output = open("scripts/unrolledFilledInMapInfo.txt", "w+")

text = """
public static byte[] getFilledInMapInfoOrder(int leftOffset, int bottomOffset, int rightOffset, int topOffset){
<REPLACE>
}

"""

arr = []
cases = []
iterator = 0
cases.append("\tswitch(leftOffset){")
for x in range(0, 5):
    cases.append(f"\t\tcase {x}:")
    cases.append("\t\t\tswitch(bottomOffset){")
    for y in range(0, 5):
        order = []
        index = 0
        for dx in range(-4, 5):
            for dy in range(-4, 5):
                if dx*dx + dy*dy > 20:
                    continue
                newx, newy = x + dx, y + dy
                if newx >= 0 and newy >= 0:
                    order.append(index)
                index += 1

        strified = ','.join([str(i) for i in order])
        cases.append("\t\t\t\tcase " + str(y) + " : byte[] arr" + str(iterator) + " = {" + strified + "}; return arr" + str(iterator) + ";")
        iterator += 1

    # cases.append("\t\t\t\tdefault -> throw new RuntimeError('Invalid inputs')")
    cases.append("\t\t\t}")
cases.append("\t\t}\n")


arr = []
cases.append("\tswitch(rightOffset){")
for x in range(0, 5):
    cases.append(f"\t\tcase {x}:")
    cases.append("\t\t\tswitch(bottomOffset){")
    for y in range(0, 5):
        order = []
        index = 0
        for dx in range(-4, 5):
            for dy in range(-4, 5):
                if dx*dx + dy*dy > 20:
                    continue
                newx, newy = -x + dx, y + dy
                if newx <= 0 and newy >= 0:
                    order.append(index)
                index += 1

        strified = ','.join([str(i) for i in order])
        cases.append("\t\t\t\tcase " + str(y) + " : byte[] arr" + str(iterator) + " = {" + strified + "}; return arr" + str(iterator) + ";")
        iterator += 1

    # cases.append("\t\t\t\tdefault -> throw new RuntimeError('Invalid inputs')")
    cases.append("\t\t\t}")
cases.append("\t\t}\n")


arr = []
cases.append("\tswitch(leftOffset){")
for x in range(0, 5):
    cases.append(f"\t\tcase {x}:")
    cases.append("\t\t\tswitch(topOffset){")
    for y in range(0, 5):
        order = []
        index = 0
        for dx in range(-4, 5):
            for dy in range(-4, 5):
                if dx*dx + dy*dy > 20:
                    continue
                newx, newy = x + dx, -y + dy
                if newx >= 0 and newy <= 0:
                    order.append(index)
                index += 1

        strified = ','.join([str(i) for i in order])
        cases.append("\t\t\t\tcase " + str(y) + " : byte[] arr" + str(iterator) + " = {" + strified + "}; return arr" + str(iterator) + ";")
        iterator += 1

    # cases.append("\t\t\t\tdefault -> throw new RuntimeError('Invalid inputs')")
    cases.append("\t\t\t}")
cases.append("\t\t}\n")

arr = []
cases.append("\tswitch(rightOffset){")
for x in range(0, 5):
    cases.append(f"\t\tcase {x}:")
    cases.append("\t\t\tswitch(topOffset){")
    for y in range(0, 5):
        order = []
        index = 0
        count = 0
        for dx in range(-4, 5):
            for dy in range(-4, 5):
                if dx*dx + dy*dy > 20:
                    continue
                newx, newy = -x + dx, -y + dy
                if newx <= 0 and newy <= 0:
                    if x == 3 and y == 3:
                        print(index, dx, dy, newx, newy)
                        count += 1
                    order.append(index)
                index += 1
        print(count)

        strified = ','.join([str(i) for i in order])
        cases.append("\t\t\t\tcase " + str(y) + " : byte[] arr" + str(iterator) + " = {" + strified + "}; return arr" + str(iterator) + ";")
        iterator += 1

    # cases.append("\t\t\t\tdefault -> throw new RuntimeError('Invalid inputs')")
    cases.append("\t\t\t}")
cases.append("\t\t}\n")


text = text.replace("<REPLACE>", "\n".join(cases))
output.write(text)