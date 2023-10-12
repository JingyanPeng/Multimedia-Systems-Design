from typing import List

from Block import Block
import consts
import numpy as np
import math
import cv2


def synthesis(frames: List[List[List[List[int]]]], blocks: List[List[List[Block]]], matrixList):
    (lty, ltx), (rby, rbx) = (0, 0), (consts.HEIGHT, consts.WIDTH)
    corners = [
        np.array([0, 0, 1]),
        np.array([consts.WIDTH, 0, 1]),
        np.array([consts.WIDTH, consts.HEIGHT, 1]),
        np.array([0, consts.HEIGHT, 1]),
    ]
    T = 1
    # calculate final border
    for idx in range(len(matrixList)):
        T = np.dot(T, matrixList[idx])
        for corner in corners:
            [x, y, _] = np.dot(T, corner)
            lty = math.floor(min(lty, y))
            ltx = math.floor(min(ltx, x))
            rby = math.ceil(max(rby, y))
            rbx = math.ceil(max(rbx, x))

    width = rbx - ltx
    height = rby - lty
    pano = [[np.array([0,0,0]) for _ in range(width)] for _ in range(height)]
    bitmap = [[False for _ in range(width)] for _ in range(height)]
    delta = np.array([ltx, lty, 0]) - np.array([0, 0, 0])

    T = 1
    for idx in range(len(frames)):
        T = np.dot(T, matrixList[idx])
        frame = frames[idx]
        currBlocks = blocks[idx]

        for row in currBlocks:
            for block in row:
                if block.tag == 'f':
                    continue
                for i in range(block.lt[1], block.rb[1] + 1):
                    for j in range(block.lt[0], block.rb[0] + 1):
                        [x, y, _] = np.dot(T, np.array([j, i, 1])) - delta
                        x = int(x)
                        y = int(y)
                        if 0<=y<height and 0<=x<width and not bitmap[y][x]:
                            pano[y][x] = frame[i][j]
                            bitmap[y][x] = True


    return pano,delta

def synthesis1(frames, maps, matrixList):
    frames=frames[1:]
    [lty, ltx, rby, rbx]= np.float32([0, 0, consts.HEIGHT-1, consts.WIDTH-1])
    corners = [[0, 0], [0, consts.HEIGHT - 1], [consts.WIDTH - 1, consts.HEIGHT - 1], [consts.WIDTH - 1, 0]]
    T = 1
    # calculate final border
    for idx in range(len(matrixList)):
        T = np.dot(T, matrixList[idx])
        pts = np.float32(corners).reshape(-1, 1, 2)
        dst = cv2.perspectiveTransform(pts, T)
        lty=min(min(dst[...,1][0]),lty)
        ltx=min(min(dst[...,0][0]),ltx)
        rby=max(min(dst[...,1][0]),rby)
        rbx=max(min(dst[...,0][0]),rbx)


    width = np.int32(rbx - ltx)
    height = np.int32(rby - lty)
    pano = np.zeros([height,width,3],dtype=np.uint8)
    bitmap = np.zeros([height,width],dtype=bool)
    delta = np.array([ltx, lty])

    T = 1
    for idx in range(len(frames)):
        T = np.dot(T, matrixList[idx])
        frame = frames[idx]
        frameh,framew,_=frame.shape

        for i in range(frameh):
            for j in range(framew):
                if maps[idx][i][j]:
                    continue
                x,y=np.int32(cv2.perspectiveTransform(np.float32([j,i]).reshape(-1, 1, 2), T).flatten()-delta)
                x = 0 if x<0 else (width-1 if x>=width else x)
                y = 0 if y<0 else (height-1 if y>=height else y)
                if not bitmap[y][x]:
                    pano[y][x] = frame[i][j]
                    bitmap[y][x] = True

    return pano,delta
