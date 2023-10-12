import os
import sys

import numpy as np
import cv2

import consts


def RGB2BGR(frames):
    return np.flip(frames, axis=-1)

def readVideo(path):
    # The video feed is read in as a VideoCapture object
    cap = cv2.VideoCapture(path)
    ret, firstFrame = cap.read()

    consts.HEIGHT,consts.WIDTH,_=firstFrame.shape
    frames=[firstFrame]
    count=0
    while cap.isOpened():
        # ret = a boolean return value from getting the frame, frame = the current frame being projected in the video
        ret, frame = cap.read()
        if not ret:
            break
        frames.append(frame)
        print(ret,count)
        count+=1
    consts.FRAMENUM = len(frames)
    return np.flip(frames, axis=-1) #output rgb

def readImages(path,start,end):
    fileName = path.split(consts.SEPARATOR)[-1]
    images=[]
    for i in range(start-1,end):
        print('read frame'+str(i+1))
        with open(path+consts.SEPARATOR[0]+fileName+"."+str(i+1).zfill(3)+".rgb", "rb") as f:
            frame = f.read()
            frame = [int(x) for x in frame]
            frame = np.array(frame).reshape((consts.HEIGHT, consts.WIDTH, 3)).astype(np.uint8)
            images.append(frame)
    return images

def setConsts(path):
    if os.name == 'nt':
        consts.SEPARATOR = '\\'
    elif os.name == 'posix':
        consts.SEPARATOR = '/'

    if '.' in path:
        fileName = path.split(consts.SEPARATOR)[-1].split('.')[0]
    else:
        fileName = path.split(consts.SEPARATOR)[-1]
        file_para = fileName.split('_')
        consts.WIDTH = int(file_para[1])
        consts.HEIGHT = int(file_para[2])
        consts.FRAMENUM = int(file_para[3])
    consts.FILENAME = fileName

def saveVideo(path,frames, fps=30):
    videoWriter = cv2.VideoWriter(path, cv2.VideoWriter_fourcc('I', '4', '2', '0'),
                                  fps,(frames.shape[2],frames.shape[1]))

    for img in frames:
        videoWriter.write(img)
    videoWriter.release()