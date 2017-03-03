package cn.jianke.jkimageloader.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.FileDescriptor;
import java.lang.ref.SoftReference;

/**
 * @className: ImageResizer
 * @classDescription: img compress
 * @author: leibing
 * @createTime: 2017/3/2
 */
public class ImageResizer {
	// tag
	private static final String TAG = "ImageResizer";

	/**
	 * get bitmap from filedescriptor
	 * @author leibing
	 * @createTime 2017/3/2
	 * @lastModify 2017/3/2
	 * @param fd
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) {
		// bitmap soft ref
		SoftReference<Bitmap> bitmapSoftRef;
		// first decode with injustdecodebounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			// calculate insamplesize
			options.inSampleSize = calculateInSampleSize(options, reqWidth,
					reqHeight);
			// decode bitmap with insamplesize set
			options.inJustDecodeBounds = false;
			// bitmap soft ref
			bitmapSoftRef
					= new SoftReference<Bitmap>(BitmapFactory.decodeFileDescriptor(fd, null, options));
			if (bitmapSoftRef != null && bitmapSoftRef.get() != null)
				return bitmapSoftRef.get();
		}catch (OutOfMemoryError ex){
			// add sample size
			options.inSampleSize = options.inSampleSize * 4;
			options.inJustDecodeBounds = false;
			// bitmap soft ref
			bitmapSoftRef
					= new SoftReference<Bitmap>(BitmapFactory.decodeFileDescriptor(fd, null, options));
			if (bitmapSoftRef != null && bitmapSoftRef.get() != null)
				return bitmapSoftRef.get();
		}
		return null;
	}

	/**
	 * calculateInSampleSize
	 * @author leibing
	 * @createTime 2017/3/2
	 * @lastModify 2017/3/2
	 * @param options
	 * @param reqHeight
	 * @param reqWidth
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
									 int reqWidth, int reqHeight) {
		if (reqWidth == 0 || reqHeight == 0) {
			return 1;
		}
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight
					&& (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
}
