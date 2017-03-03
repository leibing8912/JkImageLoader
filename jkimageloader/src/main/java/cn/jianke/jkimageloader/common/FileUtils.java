package cn.jianke.jkimageloader.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import java.io.File;

/**
 * @className: FileUtils
 * @classDescription: file util
 * @author: leibing
 * @createTime: 2017/3/2
 */
public class FileUtils {

    /**
     * get disk cache dir
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // cache path
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * create file dir
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param dir
     * @return
     */
    public static void createFileDir(File dir){
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * get available space
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param path
     * @return
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }
}
