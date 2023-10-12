import Tranformation as trans
import numpy as np
import cv2
def app3(back, frames, maps, n):
    patch=back
    for padding_index in [-15, 15]:
        padding = frames[n + padding_index]
        pathMap = maps[n + padding_index]
        matrices=trans.calculateTransformationMatrixByOpenCV([patch,padding])
        height,width, _ =patch.shape
        frameh, framew, _ = padding.shape
        T=matrices[0]
        for i in range(frameh):
            for j in range(framew):
                if pathMap[i][j]:
                    continue
                x, y = np.int32(cv2.perspectiveTransform(np.float32([j, i]).reshape(-1, 1, 2), T).flatten())
                x = 0 if x < 0 else (width - 1 if x >= width else x)
                y = 0 if y < 0 else (height - 1 if y >= height else y)
                if np.all(patch[y][x]<4):
                    patch[y][x][:] = padding[i][j][:]