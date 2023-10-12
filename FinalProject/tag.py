import math
# import matplotlib.pyplot as plt
# from sklearn.cluster import KMeans
import cv2
import math
import numpy as np
import consts
from collections import deque, defaultdict

# class UnionFind:
#     def __init__(self, m, n):
#         self.m, self.n = m, n
#         self._parents = [i for i in range(m * n)]
#         self._size = [1] * (m * n)

#     def find(self, p):
#         while(p != self._parents[p]):
#             p = self._parents[p]
#         return p

#     def union(self, p, q):
#         root_p, root_q = self.find(p), self.find(q)
#         self._parents[root_p] = root_q
#         self._size[root_q] = self._size[root_q] + self._size[root_p]

#     def connected(self, p, q):
#         return self.find(p)==self.find(q)


# def getIdx(i, j):
#     return i * width + j

vec_len_thrshd = 0.1
theta_thrshd = 2



def set_tag(blocks):

    block_h = int(consts.HEIGHT / 16)  
    block_w = int(consts.WIDTH / 16)
    # print(str(block_h) , str(block_w))
    
    for frame in blocks:
        # uf = UnionFind()
        # use the longest motion vector of four corners as the standard background motion vector  
        block_h = frame.shape[0]   
        block_w = frame.shape[1]
          
        bg_vec_len = 0
        bg_pos = [[0, 0], [0, block_w - 2], [block_h - 2, 0], [block_h - 2, block_w - 2]]
        # print(frame.shape)
        for bg_ij in bg_pos:
            (bg_i, bg_j) = bg_ij
            (bg_vec_dx, bg_vec_dy) = frame[bg_i][bg_j].motion_vec
            bg_vec_len = max(bg_vec_len, math.sqrt(math.pow(bg_vec_dx, 2) + math.pow(bg_vec_dy, 2)))

        # for i in range(consts.HEIGHT):
        #     for j in range(consts.WIDTH):
        for row in frame:
            for block in row:
                (dx, dy) = block.motion_vec
                vec_len = math.sqrt(math.pow(dx, 2) + math.pow(dy, 2))

                # determine backgnd or foreground 
                if vec_len < vec_len_thrshd or bg_vec_len < vec_len_thrshd:
                    block.tag = 'b'
                    continue
                
                theta = math.acos((dx * bg_vec_dx + dy * bg_vec_dy) / (bg_vec_len * vec_len))
                if abs(theta) < theta_thrshd:
                    block.tag = 'b'
                else:
                    block.tag = 'f'


    print_block_tag(blocks)

    for frame in blocks:
        max_f_num, max_fores = get_largest_fore(frame)
        print(max_f_num)
        tag_largest_fore(frame, max_fores)
    
    print_block_tag(blocks)
        

    return blocks


def print_block_tag(blocks):
    for row in blocks[0]:
        # print(block.tag for block in row)
        for block in row:
            print(block.tag, end="")
        print()


# get one largest adjcent foreground blocks, return the number of blocks and positions
def get_largest_fore(frame):
    block_h = frame.shape[0]   
    block_w = frame.shape[1]
    max_f_num = 0
    max_fores = []
    visited = [[False for i in range(block_w)] for i in range(block_h)]
    for idx, block in np.ndenumerate(frame):
            (i, j) = idx
            if visited[i][j] == True or block.tag == 'b':
                continue                
            num, fores = bfs(frame, i, j, visited)
            # print(num, fores)
            if max_f_num < num:
                max_f_num = num
                max_fores = fores
    return max_f_num, max_fores



def bfs(frame, i, j, visited):
    dirs = [[0,1], [0, -1], [1, 0], [-1, 0]]
    block_h = frame.shape[0]   
    block_w = frame.shape[1]
    num = 1
    fores = [[i, j]]
    queue = deque()
    queue.append([i, j])
    visited[i][j] = True
    while len(queue) != 0:
        tmp = queue.pop()
        ci = tmp[0]
        cj = tmp[1]
        for dir in dirs:
            ni = ci + dir[0]
            nj = cj + dir[1]
            if (ni >= 0 and ni < block_h and nj >= 0 and nj < block_w and visited[ni][nj] != True and frame[ni][nj].tag == 'f'):
                queue.append([ni, nj])
                visited[ni][nj] = True
                num += 1
                fores.append([ni, nj])
    return num, fores


# tag all the blocks as background but the blocks in fores
def tag_largest_fore(frame, fores):
    for row in frame:
        for block in row:
            block.tag = 'b'
    for fore in fores:
        (i, j) = fore
        frame[i][j].tag = 'f'

def tag(blocks, sector):
    dAngle = 18
    #line1
    v1 = (consts.WIDTH, 0)
    for frame in blocks:
        hough = defaultdict(dict)
        for row in frame:
            for block in row:
                v2 = block.motion_vec
                angle = math.floor(getAngle(v1, v2) / sector)
                if block.motion_vec not in hough[angle]:
                    hough[angle][block.motion_vec] = 1
                else:
                    hough[angle][block.motion_vec] = 1 + hough[angle][block.motion_vec]
        items = sorted(hough.items(), key=lambda item: sum(item[1].values()), reverse=True)
        most_angle = items[0][0]
        v3 = findAverage(hough[most_angle].items())
        #v3 = findMedian(hough[most_angle].items())
        for r in frame:
            for f in r:
                v4 = f.motion_vec
                angle = getAngle(v3, v4)
                if abs(angle) <= dAngle:
                    f.tag = 'b'
                else:
                    f.tag = 'f'

    #print_block_tag(blocks)
    return blocks
def findAverage(items):
    sum_x = 0.0
    sum_y = 0.0
    times = 0
    for vector in items:
        sum_x = sum_x + vector[0][0] * vector[1]
        sum_y = sum_y + vector[0][1] * vector[1]
        times = times + vector[1]
    average_x = sum_x / times
    average_y = sum_y / times
    return (average_x, average_y)

def findMedian(items):
    angles = dict()
    v1 = (consts.WIDTH, 0)
    total = 0
    vectors = defaultdict(list)
    for vector in items:
        v2 = vector[0]
        total = total + vector[1]
        angle = getAngle(v1, v2)
        if angle in vectors:
            vectors[angle].append(v2)
        else:
            vectors[angle] = list()
            vectors[angle].append(v2)
        if angle not in angles:
            angles[angle] = vector[1]
        else:
            angles[angle] = vector[1] + angles[angle]
    sorted_items = sorted(angles.items(), key=lambda item: item[0], reverse=True)
    median = math.ceil(total/2)
    times = 0
    target_angle = 0.0
    for item in sorted_items:
        times = times + item[1]
        if times >= median:
            target_angle = item[0]
            break

    sum_x = 0.0
    sum_y = 0.0
    times = 0
    for vector in vectors[target_angle]:
        sum_x = sum_x + vector[0]
        sum_y = sum_y + vector[1]
        times = times + 1
    average_x = sum_x / times
    average_y = sum_y / times
    return (average_x, average_y)

def getAngle(v1, v2):
    angle1 = math.atan2(v1[1], v1[0])
    angle1 = (int(angle1 * 180 / math.pi) + 360) % 360
    angle2 = math.atan2(v2[1], v2[0])
    angle2 = (int(angle2 * 180 / math.pi) + 360) % 360
    included_angle = abs(angle1 - angle2)
    return included_angle



