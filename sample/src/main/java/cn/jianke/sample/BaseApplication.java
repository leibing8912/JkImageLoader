package cn.jianke.sample;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import cn.jianke.jkimageloader.monitor.LooperLogsDetectByPrinter;

/**
 * @className: BaseApplication
 * @classDescription: base application
 * @author: leibing
 * @createTime: 2017/3/2
 */
public class BaseApplication extends Application{
    // sington
    private static BaseApplication instance;
    // leakcanary ref watcher
    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        // init application instance
        instance = this;
        // monitor ui performance by looper printer log
        LooperLogsDetectByPrinter.start();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
    }

    /**
     * get base application instance
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param
     * @return
     */
    public static BaseApplication getInstance(){
        if (instance == null){
            synchronized (BaseApplication.class){
                if (instance == null)
                    instance = new BaseApplication();
            }
        }
        return instance;
    }

    /**
     * get leakcanary ref watcher
     * @author leibing
     * @createTime 2017/3/3
     * @lastModify 2017/3/3
     * @param
     * @return
     */
    public static RefWatcher getRefWatcher() {
        return instance.refWatcher;
    }
}
