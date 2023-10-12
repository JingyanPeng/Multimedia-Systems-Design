import numpy as np
import cv2
from PIL import Image as im
import consts


# two ways
# getTransformation: random choose points
# calculateTranformationMatrixByOpenCV: sift choose points

# choose the points manually
def getTransformation(Blocks):
    height = consts.HEIGHT
    width = consts.WIDTH
    # choose four vertexes; sometimes the vertexes are included in frame n, but not included in frame n -1 => choose inner vertexes
    first = int(width / 3)
    second = int(2 * width / 3)
    third = int(width / 3 + (height - 1) * width)
    four = int(2 * width / 3 + (height - 1) * width)
    transformation_matrix = []
    for i in range(0, 290):
        # first
        # x, y
        x10 = (Blocks[i][first].lt[0] + Blocks[i][first].rb[0]) / 2
        y10 = (Blocks[i][first].lt[1] + Blocks[i][first].rb[1]) / 2
        x11 = x10 + Blocks[i][first].motion_vec[0]
        y11 = y10 + Blocks[i][first].motion_vec[1]
        # second
        x20 = (Blocks[i][second].lt[0] + Blocks[i][second].rb[0]) / 2
        y20 = (Blocks[i][second].lt[1] + Blocks[i][second].rb[1]) / 2
        x21 = x20 + Blocks[i][second].motion_vec[0]
        y21 = y20 + Blocks[i][second].motion_vec[1]
        # third
        x30 = (Blocks[i][third].lt[0] + Blocks[i][third].rb[0]) / 2
        y30 = (Blocks[i][third].lt[1] + Blocks[i][third].rb[1]) / 2
        x31 = x30 + Blocks[i][third].motion_vec[0]
        y31 = y30 + Blocks[i][third].motion_vec[1]
        # four
        x40 = (Blocks[i][four].lt[0] + Blocks[i][four].rb[0]) / 2
        y40 = (Blocks[i][four].lt[1] + Blocks[i][four].rb[1]) / 2
        x41 = x40 + Blocks[i][four].motion_vec[0]
        y41 = y40 + Blocks[i][four].motion_vec[1]

        src = np.float32(
            [[x10, y10], [x20, y20], [x30, y30], [x40, y40]])
        dst = np.float32([[x11, y11], [x21, y21], [x31, y31], [x41, y41]])
        transformation_matrix[i] = WarpPerspectiveMatrix(src, dst)

    return transformation_matrix


def WarpPerspectiveMatrix(src, dst):
    assert src.shape[0] == dst.shape[0] and src.shape[0] >= 4

    nums = src.shape[0]
    A = np.zeros((2 * nums, 8))
    B = np.zeros((2 * nums, 1))
    for i in range(0, nums):
        A_i = src[i, :]
        B_i = dst[i, :]
        A[2 * i, :] = [A_i[0], A_i[1], 1, 0, 0, 0, -A_i[0] * B_i[0], -A_i[1] * B_i[0]]
        B[2 * i] = B_i[0]

        A[2 * i + 1, :] = [0, 0, 0, A_i[0], A_i[1], 1, -A_i[0] * B_i[1], -A_i[1] * B_i[1]]
        B[2 * i + 1] = B_i[1]

    A = np.mat(A)
    warpMatrix = A.I * B

    warpMatrix = np.array(warpMatrix).T[0]
    warpMatrix = np.insert(warpMatrix, warpMatrix.shape[0], values=1.0, axis=0)
    warpMatrix = warpMatrix.reshape((3, 3))
    return warpMatrix


def calculateTransformationMatrixByOpenCV(frames):
    transformation_matrix = []
    for i in range(len(frames) - 1, 0, -1):
        transformation_matrix.append(getTransformationMatrixByOpenCv(frames[i], frames[i - 1]))

    return transformation_matrix


# choose the points by sift function in opencv(input have to be a image)
def getTransformationMatrixByOpenCv(frame1, frame2):
    (kp1, features1) = detectFeatures(frame1)
    (kp2, features2) = detectFeatures(frame2)
    return matchPoints(kp1, kp2, features1, features2)


def detectFeatures(frame):
    img = im.fromarray(frame.astype(np.uint8))
    opencvImage = cv2.cvtColor(np.array(img), cv2.IMREAD_GRAYSCALE)
    # cv2.imshow("image", opencvImage)
    # cv2.waitKey(0)
    descriptor = cv2.xfeatures2d.SIFT_create()
    (kps, features) = descriptor.detectAndCompute(opencvImage, None)
    kps = np.float32([kp.pt for kp in kps])
    return (kps, features)


def matchPoints(kpsA, kpsB, featuresA, featuresB, ratio=0.75, reprojThresh=4.0):
    matcher = cv2.BFMatcher()
    rawMatches = matcher.knnMatch(featuresA, featuresB, 2)
    matches = []
    for m in rawMatches:
        if len(m) == 2 and m[0].distance < m[1].distance * ratio:
            matches.append((m[0].trainIdx, m[0].queryIdx))

    if len(matches) >= 4:
        ptsA = np.float32([kpsA[i] for (_, i) in matches])
        ptsB = np.float32([kpsB[i] for (i, _) in matches])
        (H, status) = cv2.findHomography(ptsA, ptsB, cv2.RANSAC, reprojThresh)
        return H
        #return (matches, H, status)

    return None


# read file into an array of binary formatted strings.
def read_binary(path):
    f = open(path, 'rb')
    binlist = []
    for i in range(consts.HEIGHT * consts.WIDTH * 3):
        bin = f.read(1)
        binlist.append(int.from_bytes(bin, "big", signed=True))
    return binlist


def convertToRgb(path):
    bytes = read_binary(path)
    ind = 0
    matrix = []
    for y in range(0, consts.HEIGHT):
        row = []
        for x in range(0, consts.WIDTH):
            r = bytes[3 * ind]
            g = bytes[3 * ind + 1]
            b = bytes[3 * ind + 2]
            r = r & 0xff
            r = shifting(r, 16)
            g = g & 0xff
            g = shifting(g, 8)
            b = b & 0xff
            pix = ((((0xff000000 | r) & 255) | g) & 255 | b) & 255
            row.append(pix)
            ind = ind + 1
        matrix.append(row)

    array = np.array(matrix)
    return array


def shifting(num, times):
    while times > 0:
        num = (num << 1) & 255
        times = times - 1
    return num


def calculateTransformationVideo(path):
    file = path.split(consts.SEPARATOR)[-1]
    imgs = []
    for index in range(consts.START, consts.FRAMENUM + 1):
        imgs.append(path + consts.SEPARATOR + file + "." + str(index).zfill(3) + ".rgb")

    rgbs = []
    for img in imgs:
        rgb = convertToRgb(img)
        rgbs.append(rgb)

    return calculateTransformationMatrixByOpenCV(rgbs)
