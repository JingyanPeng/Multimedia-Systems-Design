import os
import sys

import cv2
import jsonpickle
import numpy as np

import consts
import display
import synthesis3
import unet
import util
import app1
import app3
import app2

if __name__ == '__main__':
    path = sys.argv[1]
    # consts.START=consts.FRAMENUM-4
    util.setConsts(path)
    frames = util.readVideo(path)
    consts.START = 1
    consts.END = consts.FRAMENUM
    frames = frames[consts.START - 1:consts.END - 1]



    print('calculate tag')
    interPath = consts.FILENAME + '.json'
    if os.access(interPath, os.F_OK):
        with open(interPath, 'r') as f:
            print('reading json')
            maps = jsonpickle.decode(f.read())[consts.START - 1:consts.END]
    else:
        maps = unet.tagMap(frames)
        s = jsonpickle.encode(maps)
        with open(interPath, 'w') as f:
            f.write(s)

    # print('generate output')
    # fore=unet.generateForeground(frames,maps)
    backPath = consts.FILENAME + '_back.json'
    if os.access(backPath, os.F_OK):
        with open(backPath, 'r') as f:
            print('reading back')
            back = jsonpickle.decode(f.read())
    else:
        back=unet.generateBackground(frames,maps)
        s = jsonpickle.encode(back)
        with open(backPath, 'w') as f:
            f.write(s)

    # synthesis3.stitch(back)


    # print('saving video')
    # util.saveVideo(consts.FILENAME+'_f.avi',fore)
    # util.saveVideo(consts.FILENAME + '_b.avi', back)

    # frames: the original input
    # maps: true for foreground, false for background
    # pano=np.flip(cv2.imread('test2_panorama.png'),axis=-1)
    # synthesis3.fill(pano, frames, maps, 570)
    # synthesis3.fill(pano, frames, maps,539)
    #display.displaySingle(pano)
    # cv2.imwrite('test2_panotama_full.png', np.flip(pano,axis=-1))

    pano = np.flip(cv2.imread('/Users/jsq/Documents/USC-Study/CSCI576/PanoramaGen/test2_panotama_full.png'), axis=-1)
    # for i in []:
    #     app1.app1(pano, frames, maps, i)
    # display.displaySingle(pano)
    # filePath = '/Users/jsq/Documents/USC-Study/CSCI576/PanoramaGen/test2_app3'
    # for i in range(40, 100):
    #     app3.app3(back[i], frames, maps, i)
    #     cv2.imwrite(filePath + '/' + str(i) + '.png', np.flip(back[i], axis=-1))
    app2.app2(pano, frames, maps)



