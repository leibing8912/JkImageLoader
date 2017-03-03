package cn.jianke.jkimageloader.monitor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

/**
 * @className: LogMonitor
 * @classDescription: log monitor，used to monitor the ui performance
 * @author: leibing
 * @createTime: 2017/3/1
 */
public class LogMonitor {
    // log tag
    private final static String TAG = "LogMonitor";
    // test the ui performance time interval
    private static final long DETECT_PERFORMANCE_TIME = 1000L;
    // sington
    private static LogMonitor instance;
    // looper thread
    private HandlerThread mLogThread;
    // handler
    private Handler mLogHandler;
    // is monitor
    private boolean isMonitoring = false;
    // log printer runnable
    private static Runnable mLogRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
            for (StackTraceElement s: stackTrace){
                sb.append(s.toString() + "\n");
            }
            Log.e(TAG, sb.toString());
        }
    };

    /**
     * Constructor
     * @author leibing
     * @createTime 2017/3/1
     * @lastModify 2017/3/1
     * @param
     * @return
     */
    private LogMonitor(){
        mLogThread = new HandlerThread("looperLogs");
        mLogThread.start();
        mLogHandler = new Handler(mLogThread.getLooper());
    }

    /**
     * get sington
     * @author leibing
     * @createTime 2017/3/1
     * @lastModify 2017/3/1
     * @param
     * @return
     */
    public static LogMonitor getInstance(){
        if (instance == null){
            synchronized (LogMonitor.class){
                instance = new LogMonitor();
            }
        }
        return instance;
    }

    /**
     * is monitor
     * @author leibing
     * @createTime 2017/3/1
     * @lastModify 2017/3/1
     * @param
     * @return
     */
    public boolean isMonitor(){
        return isMonitoring;
    }

    /**
     * start monitor
     * @author leibing
     * @createTime 2017/3/1
     * @lastModify 2017/3/1
     * @param
     * @return
     */
    public void startMonitor(){
        if (mLogHandler != null && mLogRunnable != null) {
            isMonitoring = true;
            mLogHandler.postDelayed(mLogRunnable, DETECT_PERFORMANCE_TIME);
        }
    }

    /**
     * remove monitor
     * @author leibing
     * @createTime 2017/3/1
     * @lastModify 2017/3/1
     * @param
     * @return
     */
    public void removeMonitor(){
        if (mLogHandler != null && mLogRunnable != null) {
            isMonitoring = false;
            mLogHandler.removeCallbacks(mLogRunnable);
        }
    }
}
