import os
import sys

import cv2
import jsonpickle
import numpy as np

import consts
import display
import unet
import util

if __name__ == '__main__':
    path = sys.argv[1]
    # consts.START=consts.FRAMENUM-4
    util.setConsts(path)
    frames = util.readVideo(path)
    consts.START = 1
    consts.END = consts.FRAMENUM
    frames = frames[consts.START - 1:consts.END - 1]

    # print('generate output')
    # fore=testml.generateForeground(frames,maps)
    backPath = consts.FILENAME + '_back.json'
    if os.access(backPath, os.F_OK):
        with open(backPath, 'r') as f:
            print('reading back')
            back = jsonpickle.decode(f.read())[300:]
    for i,frame in enumerate(back):
        display.displaySingle(frame,'frame'+str(i))

    # list=[0,20,40,61,83,103,124,140,161,180,198,214,240,250,282,302,330,360,385,400,420,440,465,476,490,500,510,540,560,580,602,620]