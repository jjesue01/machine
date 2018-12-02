import rawpy
import rawpy.enhance
import imageio
import os
import sys
import scipy.misc
from scipy import ndimage

def conv(src_file, dest_file):
    try:
        with rawpy.imread(src_file) as raw:
            buffer = raw.postprocess(use_auto_wb=True, no_auto_bright=False, auto_bright_thr=0.04, demosaic_algorithm=rawpy.DemosaicAlgorithm.LINEAR)
    except Exception as e:
        print("Failed reading image: %s"%e)

    if buffer is not None:
        # buffer = ndimage.median_filter(buffer, 3)
        try:
            scipy.misc.toimage(buffer).save(dest_file)
            print('Saved image: ' + dest_file)
        except Exception as e:
            print("Failed writing image: %s"%e)


def main():
    source_path = sys.argv[1]
    if os.path.isdir(source_path):
        dest_path = source_path + '_png'

        if not os.path.exists(dest_path):
            os.makedirs(dest_path)
        
        images = os.listdir(source_path)
        num_images = len(images)

        for index in range(num_images):
            src_file = source_path + '\\' + images[index]
            dest_file = dest_path + '\\' + images[index][:-4] + '.png'
            conv(src_file, dest_file)
    
    else:
        dest_file = source_path[:-4] + '.png'
        conv(source_path, dest_file)

if __name__ == '__main__':
    main()