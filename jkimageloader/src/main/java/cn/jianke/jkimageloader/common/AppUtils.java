package cn.jianke.jkimageloader.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * @className: AppUtils
 * @classDescription: app util
 * @author: leibing
 * @createTime: 2017/3/2
 */
public class AppUtils {

    /**
     * get app version
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param context
     * @return
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
