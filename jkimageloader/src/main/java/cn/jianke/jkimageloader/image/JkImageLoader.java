package cn.jianke.jkimageloader.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import cn.jianke.jkimageloader.cache.DiskLruCache;
import cn.jianke.jkimageloader.common.AppUtils;
import cn.jianke.jkimageloader.common.FileUtils;
import cn.jianke.jkimageloader.common.StringUtils;

/**
 * @className: JkImageLoader
 * @classDescription: jian ke image loader
 * @author: leibing
 * @createTime: 2017/3/2
 */
public class JkImageLoader {
    // tag
    private final static String TAG = "JkImageLoader";
    // jk bitmap name
    private final static String JK_BITMAP_NAME = "jk_bitmap";
    // img width tag
    private final static String IMG_WIDTH_TAG = "img_width_tag";
    // img url tag
    private final static String IMG_URL_TAG = "img_url_tag";
    // disk cache value count
    private final static int VALUE_COUNT = 1;
    // msg post result value
    public static final int MESSAGE_POST_RESULT = 1;
    // disk cache index
    private static final int DISK_CACHE_INDEX = 0;
    // cpu count
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    // core pool size
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    // maximum pool size
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    // disk cache size
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    // keep live time
    private static final long KEEP_ALIVE_TIME = 10L;
    // io buffer size
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    // thread factory
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        // a thread safe and subtract operation interface
        private final AtomicInteger sCount = new AtomicInteger(1);
        // create new thread
        public Thread newThread(Runnable r) {
            return new Thread(r, TAG + "#" + sCount.getAndIncrement());
        }
    };
    // thread pool executor
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            sThreadFactory);
    // sington
    private static JkImageLoader instance;
    // memory cache
    private LruCache<String, Bitmap> mMemoryCache;
    // disk cache
    private DiskLruCache mDiskLruCache;
    // application context
    private Context mApplicationContext;
    // a tag for sava img info
    private HashMap<String,String> imgTagMap = new HashMap<>();
    // main handler
    private Handler mainHander = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            JkImageLoaderResult result = (JkImageLoaderResult) msg.obj;
            ImageView imageView = result.imageViewWeakRef.get();
            // if imageview widget ref is null,just return it
            if (imageView == null)
                return;
            // set default drawable as img drawable resource
            if (result.bitmap == null
                    && StringUtils.isEmpty(result.url)){
                // if default drawable weak ref is not null,do this
                if (result.defaultDrawableWeakRef != null
                        && result.defaultDrawableWeakRef.get() != null){
                    imageView.setImageDrawable(result.defaultDrawableWeakRef.get());
                }
            }else {
                HashMap tagMap = (HashMap) imageView.getTag();
                if (tagMap != null
                        && tagMap.get(IMG_URL_TAG) != null
                        && tagMap.get(IMG_WIDTH_TAG) != null) {
                    // get img url
                    String url = String.valueOf(tagMap.get(IMG_URL_TAG));
                    // get img width
                    int width = Integer.parseInt(String.valueOf(tagMap.get(IMG_WIDTH_TAG)));
                    // resolve img misplace
                    if (url.equals(result.url) && result.bitmap != null) {
                        // set img bitmap
                        if (width == 0) {
                            width = result.bitmap.getWidth();
                        }
                        imageView = resetImgWidthHeight(imageView,
                                result.bitmap.getWidth(), result.bitmap.getHeight(), width);
                        imageView.setImageBitmap(result.bitmap);
                    }
                }
            }
        }
    };

    /**
     * Constructor
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param context ref
     * @return
     */
    private JkImageLoader(Context context){
        // init application conext
        mApplicationContext = context.getApplicationContext();
        // init memory cache(one 8 of the max memory)
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
        // init disk cache
        initDiskCache();
    }

    /**
     * init disk cache
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param
     * @return
     */
    private void initDiskCache(){
        try {
            // get disk cache dir
            File diskCacheDir = FileUtils.getDiskCacheDir(mApplicationContext, JK_BITMAP_NAME);
            // get app version
            int appVersion = AppUtils.getAppVersion(mApplicationContext);
            // create disk cache dir
            FileUtils.createFileDir(diskCacheDir);
            // when available space more than disk cache size,invoke
            if (FileUtils.getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, appVersion, VALUE_COUNT,
                        DISK_CACHE_SIZE);
            }
        } catch (Exception e) {
            Log.e(TAG, "init disk cache error#" + e.getCause().getMessage());
        }
    }

    /**
     * get sington
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param context ref
     * @return
     */
    public static JkImageLoader getInstance(Context context){
        if (instance == null){
            synchronized (JkImageLoader.class){
                if (instance == null)
                    instance = new JkImageLoader(context);
            }
        }
        return instance;
    }

    /**
     * start load img
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param url
     * @param imageView
     * @return
     */
    public synchronized void load(final String url, final ImageView imageView){
        // load img by thread pool
        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                loadBitmap(url, imageView,0,null);
            }
        });
    }

    /**
     * start load img with default img
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param url
     * @param imageView
     * @return
     */
    public synchronized void load(final String url, final ImageView imageView,
                                  final Drawable defaultDrawable){
        // load img by thread pool
        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                loadBitmap(url, imageView,0,defaultDrawable);
            }
        });
    }
    
    /**
     * start load img with default img and img width
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param url
     * @param imageView
     * @param width
     * @param defaultDrawable
     * @return
     */
    public synchronized void load(final String url, final ImageView imageView,
                                  final int width, final Drawable defaultDrawable){
        // load img by thread pool
        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                loadBitmap(url, imageView,width,defaultDrawable);
            }
        });
    }

    /**
     * start load img with img width
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param url
     * @param imageView
     * @param width img need width
     * @return
     */
    public synchronized void load(final String url, final ImageView imageView, final int width){
        // load img by thread pool
        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                loadBitmap(url, imageView,width,null);
            }
        });
    }

    /**
     * load bitmap,try to update ui
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param url img url
     * @param imageView
     * @param width img need width
     * @param defaultDrawable default drawable resource
     * @return
     */
    private synchronized void loadBitmap(final String url, final ImageView imageView,
                                         final int width, final Drawable defaultDrawable){
        // if imageview is empty,just return it
        if (imageView == null)
            return;
        // if defaultDrawable is not null,set it as default img
        if (defaultDrawable != null){
            // resolve first enter bugs
            mainHander.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageDrawable(defaultDrawable);
                }
            });
            mainHander.obtainMessage(MESSAGE_POST_RESULT, JkImageLoaderResult.getInstance()
                    .setJkImageLoaderResult(imageView, null, null, defaultDrawable)).sendToTarget();
        }
        // if uri is empty,just return it
        if (StringUtils.isEmpty(url))
            return;
        // sava img tag info
        if (imgTagMap != null){
            imgTagMap.put(IMG_URL_TAG, url);
            imgTagMap.put(IMG_WIDTH_TAG, String.valueOf(width));
            // set a unique tag for imageview
            imageView.setTag(imgTagMap);
        }
        // load bitmap from memory cache
        final SoftReference<Bitmap> memoryBitmapSoftRef
                = new SoftReference<Bitmap>(loadBitmapFromMemCache(url));
        // send msg to main handler update ui from memory cache
        if (memoryBitmapSoftRef != null && memoryBitmapSoftRef.get() != null){
            // resolve first enter bugs
            mainHander.post(new Runnable() {
                @Override
                public void run() {
                    ImageView iv = imageView;
                    if (width != 0) {
                        iv = resetImgWidthHeight(imageView,
                                memoryBitmapSoftRef.get().getWidth(),
                                memoryBitmapSoftRef.get().getHeight(), width);
                    }
                    iv.setImageBitmap(memoryBitmapSoftRef.get());
                }
            });
            mainHander.obtainMessage(MESSAGE_POST_RESULT, JkImageLoaderResult.getInstance()
                    .setJkImageLoaderResult(imageView, url, memoryBitmapSoftRef.get())).sendToTarget();
            Log.e(TAG, "load bitmap by memory cache");
            return;
        }
        // load bitmap from disk cache
        final SoftReference<Bitmap> diskBitmapSoftRef
                = new SoftReference<Bitmap>(loadBitmapFromDiskCache(url));
        // send msg to main handler update ui from disk cache
        if (diskBitmapSoftRef != null && diskBitmapSoftRef.get() != null){
            // resolve first enter bugs
            mainHander.post(new Runnable() {
                @Override
                public void run() {
                    ImageView iv = imageView;
                    if (width != 0) {
                        iv = resetImgWidthHeight(imageView,
                                diskBitmapSoftRef.get().getWidth(),
                                diskBitmapSoftRef.get().getHeight(), width);
                    }
                    iv.setImageBitmap(diskBitmapSoftRef.get());
                }
            });
            mainHander.obtainMessage(MESSAGE_POST_RESULT, JkImageLoaderResult.getInstance()
                    .setJkImageLoaderResult(imageView, url, diskBitmapSoftRef.get())).sendToTarget();
            Log.e(TAG, "load bitmap by disk cache");
            return;
        }
        // load bitmap from http
        final SoftReference<Bitmap> httpBitmapSoftRef
                = new SoftReference<Bitmap>(loadBitmapFromHttp(url));
        // send msg to main handler update ui from http
        if (httpBitmapSoftRef != null && httpBitmapSoftRef.get() != null){
            // resolve first enter bugs
            mainHander.post(new Runnable() {
                @Override
                public void run() {
                    ImageView iv = imageView;
                    if (width != 0) {
                        iv = resetImgWidthHeight(imageView,
                                httpBitmapSoftRef.get().getWidth(),
                                httpBitmapSoftRef.get().getHeight(), width);
                    }
                    iv.setImageBitmap(httpBitmapSoftRef.get());
                }
            });
            mainHander.obtainMessage(MESSAGE_POST_RESULT, JkImageLoaderResult.getInstance()
                    .setJkImageLoaderResult(imageView, url, httpBitmapSoftRef.get())).sendToTarget();
            Log.e(TAG, "load bitmap by http");
            return;
        }
    }
    
    /**
     * load bitmap from memory cache
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param url
     * @return
     */
    private synchronized Bitmap loadBitmapFromMemCache(String url) {
        // not allowed to load memory cache in the main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.e(TAG, "load memory cache bitmap from ui thread, it's not recommended!");
        }
        // get md5 hash key
        final String key = StringUtils.hashKeyFormUrl(url);
        // get bitmap for memory cache
        Bitmap bitmap = getBitmapFromMemCache(key);

        return bitmap;
    }

    /**
     * get bitmap for memory cache
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param key md5 hash key
     * @return
     */
    private synchronized Bitmap getBitmapFromMemCache(String key) {
        if (mMemoryCache == null)
            return null;
        return mMemoryCache.get(key);
    }

    /**
     * add bitmap to memory cache
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param key
     * @param bitmap
     * @return
     */
    private synchronized void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (StringUtils.isNotEmpty(key)
                && bitmap != null
                && mMemoryCache != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * load bitmap from disk cache
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param url
     * @return
     */
    private synchronized Bitmap loadBitmapFromDiskCache(String url){
        // not allowed to load disk cache in the main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.e(TAG, "load disk cache bitmap from ui thread, it's not recommended!");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        // get md5 hash key
        String key = StringUtils.hashKeyFormUrl(url);
        DiskLruCache.Snapshot snapShot = null;
        try {
            snapShot = mDiskLruCache.get(key);
            if (snapShot != null) {
                FileInputStream fileInputStream = (FileInputStream)snapShot.getInputStream(DISK_CACHE_INDEX);
                FileDescriptor fileDescriptor = fileInputStream.getFD();
                bitmap = ImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor,
                        0, 0);
                if (bitmap != null) {
                    // add bitmap to memory cache
                    addBitmapToMemoryCache(key, bitmap);
                }
            }
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "load bitmap from disk cache error#" + e.getCause().getMessage());
        }
        return null;
    }

    /**
     * load bitmap from http
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param
     * @return
     */
    private synchronized Bitmap loadBitmapFromHttp(final String url) {
        // not allowed to visit network in the main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.e(TAG, "visit network from ui thread, it's not recommended!");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        // get md5 hash key
        String key = StringUtils.hashKeyFormUrl(url);
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                if (downloadUrlToStream(url, outputStream)) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                mDiskLruCache.flush();
            }
        }catch (Exception ex){
            Log.e(TAG, "load bitmap from http error#" + ex.getCause());
        }
        return loadBitmapFromDiskCache(url);
    }

    /**
     * down load url to stream
     * @author leibing
     * @createTime 2017/3/2
     * @lastModify 2017/3/2
     * @param urlString
     * @param outputStream
     * @return
     */
    private boolean downloadUrlToStream(String urlString,
                                       OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(),
                    IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            out.close();
            in.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "down load url to stream error#" + e.getCause().getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return false;
    }

    /**
     * reset img width and height
     * @author leibing
     * @createTime 2017/2/28
     * @lastModify 2017/2/28
     * @param imageView
     * @return
     */
    public ImageView resetImgWidthHeight(ImageView imageView, int originWidth, int originHeight,
                                         int targetWidth){
        // get img layout
        ViewGroup.LayoutParams para = imageView.getLayoutParams();
        // target height
        int targetHeight = (originHeight * targetWidth) / originWidth;
        // modify layout width and height
        // modify width
        para.width = targetWidth;
        // modify height
        para.height = targetHeight;
        // reset layout params
        imageView.setLayoutParams(para);

        return imageView;
    }

    /**
     * @className: JkImageLoaderResult
     * @classDescription: jk img loader result
     * @author: leibing
     * @createTime: 2017/3/2
     */
    static class JkImageLoaderResult {
        // sington
        private static JkImageLoaderResult instance;
        // img ui widget
        public WeakReference<ImageView> imageViewWeakRef;
        // default img drawable
        public WeakReference<Drawable> defaultDrawableWeakRef;
        // img url
        public String url;
        // img resource
        public Bitmap bitmap;

        /**
         * Constructor
         * @author leibing
         * @createTime 2017/3/2
         * @lastModify 2017/3/2
         * @return
         */
        private JkImageLoaderResult() {
        }

        /**
         * get sington
         * @author leibing
         * @createTime 2017/3/2
         * @lastModify 2017/3/2
         * @param
         * @return
         */
        public static JkImageLoaderResult getInstance(){
            if (instance == null){
                synchronized (JkImageLoaderResult.class){
                    if (instance == null)
                        instance = new JkImageLoaderResult();
                }
            }
            return instance;
        }

        /**
         * set jk img loader result
         * @author leibing
         * @createTime 2017/3/2
         * @lastModify 2017/3/2
         * @param imageView img ui widget
         * @param url img url
         * @param bitmap img resource
         * @return
         */
        public JkImageLoaderResult setJkImageLoaderResult(ImageView imageView,
                                                          String url, Bitmap bitmap){
            imageViewWeakRef = new WeakReference<ImageView>(imageView);
            this.url = url;
            this.bitmap = bitmap;
            return instance;
        }

        /**
         * set jk img loader result
         * @author leibing
         * @createTime 2017/3/2
         * @lastModify 2017/3/2
         * @param imageView img ui widget
         * @param url img url
         * @param bitmap img resource
         * @param
         * @return
         */
        public JkImageLoaderResult setJkImageLoaderResult(ImageView imageView,
                                                          String url, Bitmap bitmap,
                                                          Drawable drawable){
            imageViewWeakRef = new WeakReference<ImageView>(imageView);
            defaultDrawableWeakRef = new WeakReference<Drawable>(drawable);
            this.url = url;
            this.bitmap = bitmap;
            return instance;
        }
    }
}
