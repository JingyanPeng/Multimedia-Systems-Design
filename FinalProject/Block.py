class Block:
    def __init__(self, x1, y1, x2, y2, motion_vec=None, tag=None):
        self.lt = (x1, y1)  # x,y coordinate of left top corner
        self.rb = (x2, y2)  # x,y coordinate of right bottom corner
        self.motion_vec = motion_vec
        self.tag = tag  # 'f' for foreground, 'b' for background
