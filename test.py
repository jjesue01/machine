import rawpy
import imageio

try:
    path = 'image/hdr/hdr3.arw'
    with rawpy.imread(path) as raw:
        rgb = raw.postprocess()
    imageio.imsave('image/hdr/hdr3.jpg', rgb);
except Exception as e:
    raise IOException("Failed reading image: %s"%e)