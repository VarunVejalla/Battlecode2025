from math import sqrt


def get_ordering(center, mapWidth, mapHeight, radius_squared = 20):
    radius = int(sqrt(radius_squared)+0.1) #+0.1 for possible floating point shenanigans
    deltas = []
    for dx in range(-radius, radius+1):
        for dy in range(-radius, radius+1):
            if dx*dx + dy*dy > radius_squared:
                continue
            
            center_x, center_y = center
            
            loc_x = dx+center_x
            loc_y = dy+center_y
            
            if 0 <= loc_x < mapWidth and 0 <= loc_y < mapHeight:
                deltas.append((dx, dy))
    return deltas

def get_indices_within_radius_squared(ordering, radius_squared):
    L = []
    for idx, delta in enumerate(ordering):
        if delta[0]*delta[0] + delta[1]*delta[1] <= radius_squared:
            L.append(idx)
    return L

def isSecondary(delta):
    r2 = delta[0]*delta[0] + delta[1]*delta[1]
    if r2 == 0:
        return True
    elif r2 <= 4:
        return False
    else:
        return True

def conflicts(cx, cy, dx, dy):
    if cx == dx and cy == dy:
        return True
    
    # say we know that a resource pattern center is at (cx, cy)
    existing = {(cx+shiftx, cy+shifty):isSecondary((shiftx, shifty)) for shiftx in range(-2, 3) for shifty in range(-2, 3)}
    goal = {(dx+shiftx, dy+shifty):isSecondary((shiftx, shifty)) for shiftx in range(-2, 3) for shifty in range(-2, 3)}
    
    # if existing and goal conflict, we have a problem
    
    for key, value in existing.items():
        if key in goal and goal[key] != value:
            return True
    return False

def getStringLong(lookup):
    if type(lookup[0]) == int:
        need = [(hex(m).upper()+"L").replace("X", "x") for m in lookup]
        return "{"+",".join(need)+"}"
    else:
        need = [getStringLong(m) for m in lookup_invalid]
        return "{"+",".join(need)+"}"

def getStringInt(lookup):
    if type(lookup[0]) == int:
        need = [(hex(m).upper()).replace("X", "x") for m in lookup]
        return "{"+",".join(need)+"}"
    else:
        need = [getStringInt(m) for m in lookup]
        return "{"+",".join(need)+"}"

canon = get_ordering((20,20), 40,40,20)
# this orders canon in spiral from center based on r^2
indices = [34,25,33,35,43,24,26,42,44,16,32,36,52,15,17,23,27,41,45,51,53,14,18,50,54,8,31,37,60,7,9,22,28,40,46,59,61,6,10,13,19,49,55,58,62,2,30,38,66,1,3,21,29,39,47,65,67,5,11,57,63,0,4,12,20,48,56,64,68]

relevant_indices = indices[:64]

lookup_invalid = []
for dx, dy in canon:
    # suppose this is invalid. what other square get marked invalid?
    lookup = 0
    for index in relevant_indices[::-1]:
        new_dx, new_dy = canon[index]
        if abs(new_dx-dx) <= 2 and abs(new_dy-dy) <= 2:
            lookup = 2*lookup
        else:
            # mark this as 1
            lookup = 2*lookup+1
    lookup_invalid.append(lookup)


lookup_nonoverlap_invalid = []
for dx, dy in canon:
    # suppose this is invalid and you don't want any squares to overlap w/ the other pattern. what other square get marked invalid?
    lookup = 0
    for index in relevant_indices[::-1]:
        new_dx, new_dy = canon[index]
        if abs(new_dx-dx) <= 4 and abs(new_dy-dy) <= 4:
            lookup = 2*lookup
        else:
            # mark this as 1
            lookup = 2*lookup+1
    lookup_nonoverlap_invalid.append(lookup)

# x x x x x
# x x x x x
# x x o x x
# x x x x x
# - - - - -
# x x x x x
# x x o x x
# x x x x x
# x x x x x

lookup_rcoverlap_invalid = []
for dx, dy in canon:
    # suppose this is a resource pattern center and you don't want any squares to overlap w/ the other pattern, unless it works out. what other square get marked invalid?
    lookup = 0
    for index in relevant_indices[::-1]:
        new_dx, new_dy = canon[index]
        abs_dx = abs(new_dx-dx)
        abs_dy = abs(new_dy-dy)
        overlap_x = max(5 - abs(new_dx-dx), 0)
        overlap_y = max(5 - abs(new_dy-dy), 0)

        if abs_dx <= 4 and abs_dy <= 4:
            if (overlap_x == 1 and overlap_y in {1, 2, 5}) or (overlap_y == 1 and overlap_x in {1, 2, 5}):
                lookup = 2*lookup+1 # Valid overlap
            else:
                lookup = 2*lookup # Invalid overlap
        else:
            # mark this as 1
            lookup = 2*lookup+1 # Valid (no overlap)
    lookup_rcoverlap_invalid.append(lookup)



lookup_empty = []
for dx, dy in canon:
    # suppose this is invalid. what other square get marked invalid?
    lookup = 0
    for index in relevant_indices[::-1]:
        new_dx, new_dy = canon[index]
        if abs(new_dx-dx) <= 2 and abs(new_dy-dy) <= 2:
            lookup = 2*lookup+1
        else:
            # mark this as 0
            lookup = 2*lookup
    lookup_empty.append(lookup)



lookup_primary = []
for dx, dy in canon:
    lookup = 0
    
    for (i, index) in enumerate(relevant_indices):
        location = canon[index]
        delta = (location[0]-dx, location[1]-dy)
        if abs(delta[0]) <= 2 and abs(delta[1]) <= 2:
            if not isSecondary(delta):
                # index is fine for a resource center
                lookup += 2**i
        else:
            lookup += 2**i
    lookup_primary.append(lookup)

lookup_secondary = []
for dx, dy in canon:
    lookup = 0
    
    for (i, index) in enumerate(relevant_indices):
        location = canon[index]
        delta = (location[0]-dx, location[1]-dy)
        if abs(delta[0]) <= 2 and abs(delta[1]) <= 2:
            if isSecondary(delta):
                # index is fine for a resource center
                lookup += 2**i
        else:
            lookup += 2**i
    lookup_secondary.append(lookup)        

lookup_confirmed_center = []
for cx, cy in canon:
    lookup = 0
    for (i, index) in enumerate(relevant_indices):
        location = canon[index]
        if not conflicts(cx, cy, location[0], location[1]):
            lookup += 2**i
    lookup_confirmed_center.append(lookup)

lookup_ruin_filling = []

for dx in range(-6, 7):
    for dy in range(-6, 7):
        lookup = []
        for (i, delta) in enumerate(canon):
            if delta[0]*delta[0]+delta[1]*delta[1] > 9:
                continue
            
            if abs(delta[0]-dx) <= 2 and abs(delta[1]-dy) <= 2:
                lookup.append((False, delta[0]*delta[0]+delta[1]*delta[1], i))
            else:
                lookup.append((True, delta[0]*delta[0]+delta[1]*delta[1], i))
        
        lookup.sort()
        lookup = [m[2] for m in lookup]
        lookup_ruin_filling.append(lookup)


def distance_to_segment(x, y, x1, y1, x2, y2):
    # Vector from P1 to the point
    A = (x - x1, y - y1)
    # Vector representing the segment
    B = (x2 - x1, y2 - y1)
    
    # Squared length of the segment
    B_length_squared = B[0]**2 + B[1]**2
    
    # Handle the case when P1 == P2 (zero-length segment)
    if B_length_squared == 0:
        # Distance to the single point P1
        return sqrt((x - x1)**2 + (y - y1)**2)
    
    # Projection scalar (clamp between 0 and 1)
    dot_product = A[0] * B[0] + A[1] * B[1]
    t = max(0, min(1, dot_product / B_length_squared))
    
    # Closest point on the segment
    closest_x = x1 + t * B[0]
    closest_y = y1 + t * B[1]
    
    # Distance from (x, y) to the closest point
    distance = sqrt((x - closest_x)**2 + (y - closest_y)**2)
    
    return distance        

lookup_resource_filling = []

for dx in range(-6, 7):
    for dy in range(-6, 7):
        lookup = []
        for (i, delta) in enumerate(canon):
            if delta[0]*delta[0]+delta[1]*delta[1] > 9:
                continue
            
            distance = distance_to_segment(delta[0], delta[1], 0, 0, dx, dy)
            
            if abs(delta[0]-dx) <= 2 and abs(delta[1]-dy) <= 2:
                lookup.append((False, distance, delta[0]*delta[0]+delta[1]*delta[1], i))
            else:
                lookup.append((True, distance, delta[0]*delta[0]+delta[1]*delta[1], i))

        lookup.sort()
        lookup = [m[3] for m in lookup]
        lookup_resource_filling.append(lookup)


lookup_splasher_offlimits = []
for dx, dy in canon:
    # suppose this is invalid and you don't want any squares to overlap w/ the other pattern. what other square get marked invalid?
    lookup = 0
    for index in relevant_indices[::-1]:
        new_dx, new_dy = canon[index]
        distsquared = (new_dx-dx)*(new_dx-dx) + (new_dy-dy)*(new_dy-dy)
        if distsquared <= 4:
            lookup = 2*lookup
        else:
            # mark this as 1
            lookup = 2*lookup+1
    lookup_splasher_offlimits.append(lookup)

print("Splasher off limits")
print(getStringLong(lookup_splasher_offlimits))

# print("Non-overlapping")
# print(getStringLong(lookup_nonoverlap_invalid))
# print("RC-overlapping")
# print(getStringLong(lookup_rcoverlap_invalid))


# for dx, dy in canon:
#     for square in action radius:
        


# if invalid square at 


# inverse_lookup = {offset:index for index, offset in enumerate(canon)}

# global_lookup = []
# count = 0
# for cx in range(-6, 7):
#     for cy in range(-6, 7):
#         # find all square in center  that are within 
#         indices = []
        
#         tiles = [(dx,dy) for dx in range(-2, 3) for dy in range(-2, 3)]
#         # tiles.sort(key = lambda s: (-cx**2+cy**2, (s[0]+cx)**2 + (s[0]+cy)**2))
        
#         # first prioritize based on distance from center (cx, cy), then on distance from you
        
#         for dx, dy in tiles:
#             shifted_x = cx + dx
#             shifted_y = cy + dy
#             if (shifted_x, shifted_y) in inverse_lookup:
#                 indices.append(inverse_lookup[shifted_x, shifted_y])
#         if len(indices) != 0:
#             count += 1
        
#         global_lookup.append(indices)

# string = str(global_lookup)
# string = string.replace("[", "{").replace("]", "}").replace(" ", "")
# print(string)

# (-6, -6) --> 0
# (-6, -5) --> 1
# ...
# (-6, 6) --> 12
# (-5, -6) --> 13

# (cx+6)*13 + (cy+6)

# cx*13+cy+84


            

# mapWidth = 30
# mapHeight = 35

# lookup = {i:{} for i in range(20)}

# for startingX in range(mapWidth):
#     for startingY in range(mapHeight):
#         ordering = get_ordering((startingX, startingY), mapWidth, mapHeight)
#         for radius_squared in range(20):
#             indices = tuple(get_indices_within_radius_squared(ordering, radius_squared))
#             relevant = lookup[radius_squared]
#             relevant[(startingX, startingY)] = indices

# total_cases = [set() for i in range(20)]

# def get_indices_cracked(radius_squared, current_location, actual_map_width, actual_map_height):
#     if 4 <= current_location[0] <= actual_map_width-5:
#         lookup_x = 4
#     elif current_location[0] <= 3:
#         lookup_x = current_location[0]
#     else:
#         lookup_x = current_location[0]-(actual_map_width-30)
    
#     if 4 <= current_location[1] <= actual_map_height-5:
#         lookup_y = 4
#     elif current_location[1] <= 3:
#         lookup_y = current_location[1]
#     else:
#         lookup_y = current_location[1]-(actual_map_width-35)
    
#     total_cases[radius_squared].add((lookup_x, lookup_y))
    
#     return lookup[radius_squared][(lookup_x, lookup_y)]

# # public static int[] getMapInfoIndicesWithinRadiusSquared(int radius_squared, MapLocation currentLocation) {

# #     }

# mapWidth = 20
# mapHeight = 20

# bro = set()

# for startingX in range(mapWidth):
#     for startingY in range(mapHeight):
#         ordering = get_ordering((startingX, startingY), mapWidth, mapHeight)
#         for radius_squared in range(20):
#             indices = tuple(get_indices_within_radius_squared(ordering, radius_squared))
            
#             l = get_indices_cracked(radius_squared, (startingX, startingY), mapWidth, mapHeight)
            
#             if indices != l:
#                 print("wtf")

# print([len(cases) for cases in total_cases]) # at most 81 cases for any given radius_squared

# for r2 in range(20):
#     lookedAt = set()
#     for sx in range(mapWidth):
#         for sy in range(mapHeight):
#             l = get_indices_cracked(r2, (sx, sy), mapWidth, mapHeight)
#             lookedAt.add(l)
#     print(len(lookedAt)) # these are all <= 81, and indicates that for smaller radiuses, we can look at even fewer cases

# print("------------")

# for r2 in range(20):
#     sx = 10
#     sy = 10
#     l = get_indices_cracked(r2, (sx, sy), mapWidth, mapHeight)
#     print(r2, l)


# # at most 12 comparisons to get result
# # 5 for identifying radius_squared, ceiling of log2(20)
# # 7 for identifying which case within there, ceiling of log2(81)
# # building array afterward, given indices, also takes bytecode
# # maybe this is only worth it for when we are in the interior? i.e. those where we see the full vision radius
# hard to tell what's better, vs scanning again