package cn.jianke.sample.module;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import cn.jianke.jkimageloader.image.JkImageLoader;
import cn.jianke.sample.BaseApplication;
import cn.jianke.sample.R;

/**
 * @className: ImgAdapter
 * @classDescription: img adapter
 * @author: leibing
 * @createTime: 2017/3/2
 */
public class ImgAdapter extends BaseAdapter{
    // layout
    private LayoutInflater mLayoutInflater;
    // data
    private ArrayList<ImgModel> mData;
    // screen width
    private int screenWidth = 0;

    /**
     * Constructor
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param context
     * @param mData
     * @return
     */
    public ImgAdapter(Context context, ArrayList<ImgModel> mData, int screenWidth){
        mLayoutInflater = LayoutInflater.from(context);
        this.mData = mData;
        this.screenWidth = screenWidth;
    }

    /**
     * set data
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param mData
     * @return
     */
    public void setData(ArrayList<ImgModel> mData){
        this.mData = mData;
        ImgAdapter.this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData != null?mData.size():0;
    }

    @Override
    public Object getItem(int i) {
        return mData != null?mData.get(i):null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null){
            view = mLayoutInflater.inflate(R.layout.item_img, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        if (mData != null
                && mData.size() != 0
                && i<mData.size())
            holder.updateUI(mData.get(i), screenWidth);

        return view;
    }

    /**
     * @className: ViewHolder
     * @classDescription: view holder
     * @author: leibing
     * @createTime: 2017/3/2
     */
    static class ViewHolder{
        // img widget
        private ImageView itemIv;
        // img num
        private TextView itemNumTv;

        /**
         * Constructor
         * @author leibing
         * @createTime 2017/3/2
         * @lastModify 2017/3/2
         * @param view
         * @return
         */
        public ViewHolder(View view){
            itemIv = (ImageView) view.findViewById(R.id.iv_item);
            itemNumTv = (TextView) view.findViewById(R.id.tv_num);
        }

        /**
         * update ui
         * @author leibing
         * @createTime 2017/3/2
         * @lastModify 2017/3/2
         * @param model
         * @param screenWidth
         * @return
         */
        public void updateUI(ImgModel model, int screenWidth){
            JkImageLoader.getInstance(BaseApplication.getInstance()).load(model.imgUrl,
                    itemIv, screenWidth, BaseApplication.getInstance().getResources()
                            .getDrawable(R.drawable.ic_launcher));
            itemNumTv.setText(String.valueOf(model.imgNum));
        }
    }
}
