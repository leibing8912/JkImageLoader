package cn.jianke.sample.module;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;
import java.util.ArrayList;
import cn.jianke.sample.R;

/**
 * @className: MainActivity
 * @classDescription: main page
 * @author: leibing
 * @createTime: 2017/3/2
 */
public class MainActivity extends AppCompatActivity {
    // tag
    private final static String TAG = "MainActivity";
    // listview
    private ListView imgLv;
    // img list
    private ArrayList<ImgModel> mData;
    // img adapter
    private ImgAdapter mAdapter;
    // screen width
    private int screenWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // get screen width
        WindowManager wm = this.getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
        // findView
        imgLv = (ListView) findViewById(R.id.lv_img);
        // init img list
        mData = new ArrayList<>();
        // add data to img list
        addData();
        // set adapter
        mAdapter = new ImgAdapter(this, mData, screenWidth);
        imgLv.setAdapter(mAdapter);
    }

    /**
     * add data to img list
     * @author leibing
     * @createTime 2017/3/3
     * @lastModify 2017/3/3
     * @param
     * @return
     */
    private void addData() {
        for (int i=0;i<10000;i++) {
            ImgModel model = new ImgModel();
            if (i%5 == 0) {
                model.imgUrl = "https://timgsa.baidu.com/timg?" +
                        "image&quality=80&size=b9999_10000&sec=1489107250" +
                        "&di=359fb15bb0fa39ec79d4d40f31c7c0df&imgtype=jpg" +
                        "&er=1&src=http%3A%2F%2Fpic21.nipic.com%2F20120524%2F1747338_185448014000_2.jpg";
            }else if (i%5 == 1){
                model.imgUrl = "https://timgsa.baidu.com/timg?" +
                        "image&quality=80&size=b9999_10000&sec=1488512464305&di=23f96e9ed1a987db2a795d25525632fc" +
                        "&imgtype=0&src=http%3A%2F%2Fwww.lzbs.com.cn%2Fimages%2F2008-12%2F22%2FMona210C.JPG";
            }else if (i%5 == 2){
                model.imgUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488512464304" +
                        "&di=e92143b1919d4da24b50fe3428c3b113&imgtype=0" +
                        "&src=http%3A%2F%2Fwww.weixin234.com%2Fuploadfile%2Fa%2F201611%2F7f01671f40ab4d4.jpg";
            }else if (i%5 == 3){
                model.imgUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488512464300" +
                        "&di=4bebe16b85b6b0f6b667f12e51fe44c9&imgtype=0" +
                        "&src=http%3A%2F%2Fhimg2.huanqiu.com%2Fattachment2010%2F2016%2F0824%2F20160824070457414.jpg";
            }else {
                model.imgUrl = "https://timgsa.baidu.com/timg?" +
                        "image&quality=80&size=b9999_10000&sec=1488512464305&di=f2fbb422135be6acf5a22f1818e9bf26" +
                        "&imgtype=0&src=http%3A%2F%2Fimg3.selfimg.com.cn%2FgalleryNoWatermark%2F2016%2F04%2F28%2F1461836013_aYqusS.jpg";
            }
            model.imgNum = i;
            mData.add(model);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        Log.e(TAG, "onTrimMemory level = " + level);
    }

    @Override
    public void onLowMemory() {
        Log.e(TAG, "onLowMemory");
    }
}
