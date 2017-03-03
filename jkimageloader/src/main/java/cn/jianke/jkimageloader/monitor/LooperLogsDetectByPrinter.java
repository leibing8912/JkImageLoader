package cn.jianke.jkimageloader.monitor;

import android.os.Looper;
import android.util.Printer;

/**
 * @className: LooperLogsDetectByPrinter
 * @classDescription: monitor ui performance by looper printer log
 * @author: leibing
 * @createTime: 2017/3/1
 */
public class LooperLogsDetectByPrinter {
    // before looper dispatch msg
    private final static String START = ">>>>> Dispatching to";
    // after looper finished msg
    private final static String END = "<<<<< Finished to";

    /**
     * start monitor ui thread
     * @author leibing
     * @createTime 2017/3/1
     * @lastModify 2017/3/1
     * @param
     * @return
     */
    public static void start(){
        Looper.getMainLooper().setMessageLogging(new Printer() {
            @Override
            public void println(String s) {
                if (s.startsWith(START)){
                    LogMonitor.getInstance().startMonitor();
                }
                if (s.startsWith(END)){
                    LogMonitor.getInstance().removeMonitor();
                }
            }
        });
    }
}
