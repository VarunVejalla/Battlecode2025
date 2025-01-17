RESOURCE_PATTERN = 28873275
PAINT_TOWER_PATTERN = 18157905
MONEY_TOWER_PATTERN = 15583086
DEFENSE_TOWER_PATTERN = 4685252

patterns = [RESOURCE_PATTERN, PAINT_TOWER_PATTERN, MONEY_TOWER_PATTERN, DEFENSE_TOWER_PATTERN]

for pattern in patterns:
    binary = bin(pattern)[2:]
    binary = binary.zfill(25)
    for i in range(5):
        print(binary[i*5:i*5+5])
    print()

