import cv2 as cv
import numpy as np
import math
import sys
from Block import Block
import consts


# frame: RGB -> YUV Y value only
def getyuv_y(frame):
    red = frame[:,:,0]
    green = frame[:,:,1]
    blue = frame[:,:,2]
    return 0.299*red + 0.587*green + 0.114*blue

# MAD = SAD/(w*h)
## w -> width ; h -> height
def mad(block1, block2, w, h):
    sum = 0
    diff = [[0 for xw in range(w)] for xh in range(h)]
    for i in range(h):
        for j in range(w):
            diff[i][j] = abs(int(block1[i][j])-int(block2[i][j]))
            sum += diff[i][j]
    return sum/(w*h)

# Divide each image frame into blocks & Compute Motion Vectors based on pervious frame
## n*n marcoblock
## k search parameter
def get_blocks(frame1, frame2, n, k):
    # frame1 -> previous
    yuv_y1 = getyuv_y(frame1)
    # frame2 -> target
    yuv_y2 = getyuv_y(frame2)
    height, width = yuv_y2.shape
    hei = math.ceil(height/n*1.0)
    wid = math.ceil(width/n*1.0)

    #blocks for a target frame (frame2)
    blocks = []
    #vectors = []
    for i in range(hei):
        for j in range(wid):
            print('calculating block: '+str((i,j)))
            x1 = j*n
            y1 = i*n
            x2 = (j+1)*n-1 if width>(j+1)*n else width-1
            y2 = (i+1)*n-1 if height>(i+1)*n else height-1
            b = Block(x1, y1, x2, y2)

            # Compute b's motion vector from the previous frame (frame1, search parameter: k)
            block2 = yuv_y2[y1:y2+1, x1:x2+1] # block[h][w]
            block1 = yuv_y1[y1:y2+1, x1:x2+1]
            if np.array_equal(block1, block2): # equal
                b.motion_vec = (0, 0)
            else:
                ## search area in frame1 :(lt(a1, b1), rb(a2, b2))
                a1 = x1-k if x1-k>0 else 0
                b1 = y1-k if y1-k>0 else 0
                a2 = x2+k if width>x2+k else width-1
                b2 = y2+k if height>y2+k else height-1
                min = 1000
                best_px1 = 0
                best_py1 = 0
                for py1 in range(b1, b2+1):
                    py2 = py1+(y2-y1)
                    if b2 < py2:
                        break
                    for px1 in range(a1, a2+1):
                        px2 = px1+(x2-x1)
                        if a2 < px2:
                            break
                        block1 = yuv_y1[py1:py2+1, px1:px2+1]
                        current_mad = mad(block1, block2, x2-x1+1, y2-y1+1)
                        if current_mad < min:
                            min = current_mad
                            best_px1 = px1
                            best_py1 = py1
                b.motion_vec = (x1-best_px1, y1-best_py1)
                # print(b.motion_vec)
            blocks.append(b)
            #vectors.append(b.motion_vec)

    #vectors = np.array(vectors).reshape(hei, wid, 2)
    blocks = np.array(blocks).reshape(hei, wid)
    # print(blocks.shape)
    return blocks

#open rgb file (with its filename & frame_count)
def open_rgb(path, filename, count):
    f = open(path+consts.SEPARATOR[0]+filename+"."+str(count).zfill(3)+".rgb", "rb")
    # print("video_rgb/"+filename+"/"+filename+"."+count_str+".rgb")
    frame = f.read()
    f.close()
    frame = [int(x) for x in frame]
    frame = np.array(frame).reshape((consts.HEIGHT, consts.WIDTH, 3)).astype(np.uint8)
    # bgr = frame[..., ::-1]
    # cv.imshow("Frame", bgr)
    # cv.waitKey(1000)  
    return frame

#exist indicates if an intermediary file is provided
def motion_vectors(path, start=consts.START, end=consts.FRAMENUM, exist=False):
    fileName = path.split(consts.SEPARATOR)[-1]
    allblocks = []
    frames = []
    frames.append(open_rgb(path, fileName, 1))
    for count in range(start, end):
        print('frame: '+str(count))
        frame1 = open_rgb(path, fileName, count)
        frame2 = open_rgb(path, fileName, count+1)
        frames.append(frame2)
        if not exist:
            frameblocks = get_blocks(frame1, frame2, 16, consts.K) #frame1, frame2, n, k
            allblocks.append(frameblocks)
    frames = np.array(frames)
    allblocks = np.array(allblocks)
    # print(frames.shape) # int[framecount][width][height][3]
    # print(allblocks.shape) # Block[framecount][wid][hei]
    return allblocks, frames





# fileName = sys.argv[1]  # inputvideo_width_height_framecount
# print(motion_vectors(fileName))





