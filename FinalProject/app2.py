import numpy as np
import copy
import Tranformation as trans
import cv2

# def app2_using_blocks(panorama, frames, tagged_blocks, matrices, delta):
#     result = []
#     T = 1
#     #for every frame
#     for idx in range(len(frames)):
#         pano = copy.deepcopy(panorama)
#         T = np.dot(T, matrices[idx])
#         frame = frames[idx]
#         curr_blocks = tagged_blocks[idx]
#         foreground_top, foreground_bottom, foreground_left, foreground_right = 1000000000, 0, 1000000000, 0
#         for row in curr_blocks:
#             for block in row:
#                 if block.tag == 'b':
#                     continue
#                 #for every foreground block
#                 for i in range(block.lt[1], block.rb[1] + 1):
#                     for j in range(block.lt[0], block.rb[0] + 1):
#                         [x, y, _] = np.dot(T, np.array([j, i, 1])) - delta
#                         x = int(x)
#                         y = int(y)
#                         pano[y][x] = frame[i][j]
#                         if x < foreground_left:
#                             foreground_left = x
#                         if x > foreground_right:
#                             foreground_right = x
#                         if y < foreground_top:
#                             foreground_top = y
#                         if y > foreground_bottom:
#                             foreground_bottom = y
#         frame_left = int((foreground_left + foreground_right - consts.WIDTH) / 2)
#         frame_top = int((foreground_top + foreground_bottom - consts.HEIGHT) / 2)
#         frame_new = pano[frame_top : frame_top + consts.HEIGHT][frame_left : frame_left + consts.WIDTH]
#         result.append(frame_new)                
#     return result


def app2(panorama, frames, maps):
    filepath = 'test2_app2'
    for idx in range(len(frames) -15, len(frames)):
        pano = copy.deepcopy(panorama)
        patch = frames[idx]
        patchMap = maps[idx]
        matrix = trans.calculateTransformationMatrixByOpenCV([pano, patch])
        height, width, _ = pano.shape
        frameh, framew, _ = patch.shape
        T = matrix[0]
        foreground_top, foreground_bottom, foreground_left, foreground_right = 1000000000, 0, 1000000000, 0

        for i in range(frameh):
            for j in range(framew):
                if patchMap[i][j] == True:
                    x, y = np.int32(cv2.perspectiveTransform(np.float32([j, i]).reshape(-1, 1, 2), T).flatten())
                    x = 0 if x < 0 else (width - 1 if x >= width else x)
                    y = 0 if y < 0 else (height - 1 if y >= height else y)
                    pano[y][x][:] = patch[i][j][:]
                    if x < foreground_left:
                        foreground_left = x
                    if x > foreground_right:
                        foreground_right = x
                    if y < foreground_top:
                        foreground_top = y
                    if y > foreground_bottom:
                        foreground_bottom = y
        # cv2.imshow("image", pano)
        # cv2.waitKey(0)
        if foreground_right - foreground_left > framew:
            frame_left = 0
        else :
            frame_left = int((foreground_left + foreground_right - framew) / 2)
            frame_left = 0 if frame_left < 0 else (width - framew - 1 if frame_left >= width - framew else frame_left)
        if foreground_bottom - foreground_top > frameh:
            frame_top = 0
        else:
            frame_top = int((foreground_top + foreground_bottom - frameh) / 2)
            frame_top = 0 if frame_top < 0 else (height - frameh - 1 if frame_top >= height - frameh else frame_top)
        frame_new = pano[frame_top : (frame_top + frameh), frame_left : (frame_left + framew), :]
        cv2.imwrite(filepath + '/' + str(idx) + '.png', np.flip(frame_new, axis=-1))
