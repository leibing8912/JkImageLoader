package cn.jianke.jkimageloader.common;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * @className: BitmapUtils
 * @classDescription: bitmap util
 * @author: leibing
 * @createTime: 2017/3/3
 */
public class BitmapUtils {

    /**
     * drawable convert to bitmap
     * @author leibing
     * @createTime 2017/3/3
     * @lastModify 2017/3/3
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitamp(Drawable drawable) {
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
    }
}
