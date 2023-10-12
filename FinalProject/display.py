import time

import cv2
from matplotlib import pyplot as plt
import numpy as np


def display(frame):
    plt.imshow(frame)
    plt.show()


def _displayTag(frame, blocks):
    image = frame.copy()
    for row in blocks:
        for block in row:
            if block.tag == 'f':
                for i in range(block.lt[1], block.rb[1] + 1):
                    for j in range(block.lt[0], block.rb[0] + 1):
                        image[i][j][:]=0
    displaySingle(image)

def _displayMultiTag(frames, blocks):
    images = frames.copy()
    for p,image in enumerate(images):
        page=blocks[p]
        for row in page:
            for block in row:
                if block.tag == 'f':
                    for i in range(block.lt[1], block.rb[1] + 1):
                        for j in range(block.lt[0], block.rb[0] + 1):
                            image[i][j][:]=0
    displayMulti(images)

def _displayMultiTagInPixel(frames, tags):
    for p,frame in enumerate(frames):
        print('frame'+str(p))
        for i,row in enumerate(frame):
            for j in range(len(row)):
                if tags[p][i][j]:
                    frames[p][i][j][:]=0
        displaySingle(frames[p])
    # displayMulti(frames)


def displaySingle(frame,name='image'):
    showing = np.flip(frame, axis=-1)
    cv2.imshow(name,showing)
    # cv2.setWindowProperty('image', cv2.WND_PROP_TOPMOST, 1)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

def displayMulti(frames, frameRate=15):
    for frame in frames:
        showing = np.flip(frame, axis=-1)
        cv2.imshow('video', showing)
        cv2.setWindowProperty('video', cv2.WND_PROP_TOPMOST, 1)
        cv2.waitKey(int(1000/frameRate))
        # time.sleep(1/frameRate)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
