import sys

import numpy as np
from rembg import remove
from rembg import new_session
import cv2

import display
import util


def removeSingle(frame):
    session=new_session('u2net_human_seg')
    output = remove(frame,only_mask=True,session=session)
    return output

## true for foreground, false for background
def tagMap(frames):
    maps=[]
    for frame in frames:
        mask=removeSingle(frame)
        tag=(mask>20)
        maps.append(tag)
    return np.array(maps)

def generateBackground(frames,maps):
    output=frames.copy()
    framenum,hei,length=maps.shape
    for p in range(framenum):
        for i in range(hei):
            for j in range(length):
                if maps[p][i][j]:
                    output[p][i][j][:]=0
    return output


def generateForeground(frames,maps):
    output=frames.copy()
    framenum,hei,length=maps.shape
    for p in range(framenum):
        for i in range(hei):
            for j in range(length):
                if not maps[p][i][j]:
                    output[p][i][j][:]=255
    return output