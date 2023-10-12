import numpy as np
import cv2
import consts
import Tranformation as trans
import display


def stitch(back):
    print("begin")
    idx = list(range(0,140,15))+[140, 161, 180, 198, 214, 240, 250, 282, 302, 330, 360, 385, 400, 420, 440, 465,
            476, 490, 500, 510, 540, 560, 580, 602, 620]
    stitchy = cv2.Stitcher.create(cv2.Stitcher_PANORAMA)
    # (status, output) = stitchy.stitch(imgs)
    (status, output) = stitchy.stitch(back[idx])
    print(status)
    output = np.flip(output, axis=-1)
    print("finish")
    cv2.imshow('final result', output)
    cv2.waitKey(0)
    cv2.imwrite(consts.FILENAME + '_panorama.png', output)

def fill(pano,frames,maps,n):
    patch=frames[n]
    pathMap=maps[n]
    matrices=trans.calculateTransformationMatrixByOpenCV([pano,patch])
    height,width, _ =pano.shape
    frameh, framew, _ = patch.shape
    T=matrices[0]

    for i in range(frameh):
        for j in range(framew):
            if pathMap[i][j]:
                continue
            x, y = np.int32(cv2.perspectiveTransform(np.float32([j, i]).reshape(-1, 1, 2), T).flatten())
            x = 0 if x < 0 else (width - 1 if x >= width else x)
            y = 0 if y < 0 else (height - 1 if y >= height else y)
            if np.all(pano[y][x]<4):
                pano[y][x][:] = patch[i][j][:]
