import math
import numpy as np
import consts
import copy
import Tranformation as trans
import cv2


# def app1(panorama, frames, tagged_blocks, matrices, delta):
#     selected_num = consts.FRAMENUM / 20
#     result = copy.deepcopy(panorama)
#     T = 1
#     for idx in range(frames.shape()[0]):
#         T = np.dot(T, matrices[idx])
#         if idx % selected_num != 0:
#             continue
#         frame = frames[idx]
#         curr_blocks = tagged_blocks[idx]
#         for row in curr_blocks:
#             for block in row:
#                 if block.tag == 'b':
#                     continue
#                 for i in range(block.lt[1], block.rb[1] + 1):
#                     for j in range(block.lt[0], block.rb[0] + 1):
#                         [x, y, _] = np.dot(T, np.array([j, i, 1])) - delta
#                         x = int(x)
#                         y = int(y)
#                         result[y][x] = frame[i][j]
#
#     return result


def app1(pano,frames,maps,n):
    patch = frames[n]
    pathMap = maps[n]
    matrices = trans.calculateTransformationMatrixByOpenCV([pano, patch])
    height, width, _ = pano.shape
    frameh, framew, _ = patch.shape
    T = matrices[0]

    for i in range(frameh):
        for j in range(framew):
            if pathMap[i][j] == True:
                x, y = np.int32(cv2.perspectiveTransform(np.float32([j, i]).reshape(-1, 1, 2), T).flatten())
                x = 0 if x < 0 else (width - 1 if x >= width else x)
                y = 0 if y < 0 else (height - 1 if y >= height else y)
                pano[y][x][:] = patch[i][j][:]