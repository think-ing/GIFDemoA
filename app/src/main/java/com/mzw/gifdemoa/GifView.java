package com.mzw.gifdemoa;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by admin on 2017/4/30.
 */
public class GifView extends View {


    private static final String TAG = GifView.class.getSimpleName();
    private Movie mMovie;
    //private Bitmap mBmp;
    private long mPlayMovieTime;
    private String download_addr;
    private Context context;
    private float mScale = 1;
    private int w = 0, h = 0,screenW = 0,screenH = 0;

    private String cacheImageUrl = "";
    ProgressBar gif_view_loading;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mScale = 1;
                    float tt = 1;
                    if(screenW > 0 && screenH > 0){
                        if(mMovie.width() > 0 && mMovie.height() > 0){
                            w = mMovie.width();
                            h = mMovie.height();
                            int i = 0;
                            while (true){
                                if(w > screenW && h <= screenH){
                                    Log.i("---mzw---", "=-=-=-1=-=-=-=");
                                    tt = (float)w / (float)screenW;
                                    mScale = (float)screenW / (float)w;
                                }else if(h > screenH && w <= screenW){
                                    Log.i("---mzw---", "=-=-=-2=-=-=-=");
                                    tt = (float)h / (float)screenH;
                                    mScale = (float)screenH / (float)h;
                                }else if(w < screenW && h <= screenH){
                                    Log.i("---mzw---", "=-=-=-3=-=-=-=");
                                    tt = (float)w / (float)screenW;
                                    mScale = (float)screenW / (float)w;
                                }else if(h < screenH && w <= screenW){
                                    Log.i("---mzw---", "=-=-=-4=-=-=-=");
                                    tt = (float)screenH / (float)h;
                                    mScale = (float)h / (float)screenH;
                                }
                                Log.i("---mzw---", w + " , " + h + " , " + tt + " , " + mScale);
                                w = (int) (w / tt);
                                h = (int) (h / tt);
                                if((w == screenW && h <= screenH) || (w <= screenW && h == screenH)){
                                    Log.i("---mzw---", "=-=-=-5=-=-=-=");
                                    Log.i("---mzw---", w + " , " + h + " , " + mScale);
                                    setLayoutParams(new RelativeLayout.LayoutParams(w,h));
                                    break;
                                }
                                Log.i("---mzw---", "=-=-=-6=-=-=-=");
                                if(i >= 10){
                                    break;
                                }
                                i++;
                            }
                        }
                        Log.i("---mzw---", "=-=-=-7=-=-=-=");
                        if(gif_view_loading != null){
                            gif_view_loading.setVisibility(View.GONE);
                        }
                    }
                    Log.i("---mzw---", "=-=-=-8=-=-=-=");
                    invalidate();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void setScreenW(int screenW) {
        this.screenW = screenW;
    }

    public void setScreenH(int screenH) {
        this.screenH = screenH;
    }

    public void setDownload_addr(String _download_addr,ProgressBar mProgressBar) {

        this.gif_view_loading = mProgressBar;
        if(gif_view_loading != null){
            gif_view_loading.setVisibility(View.VISIBLE);
        }

        if(!TextUtils.isEmpty(_download_addr)){
            this.download_addr = _download_addr;
            Log.i("---mzw---","GifView.setDownload_addr 图片地址 : " + download_addr);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(download_addr.startsWith("http")){
                        Log.i("---mzw---","GifView.setDownload_addr 图片地址为网络图片");
                        String imgName = download_addr.substring(download_addr.lastIndexOf("/"), download_addr.length());
                        String cacheFile = getPortraitUrlPath("/cache");
                        if(!TextUtils.isEmpty(cacheFile)){
                            File dirFile = new File(cacheFile);
                            if(!dirFile.exists()){
                                Log.i("---mzw---", "GifView.setDownload_addr : 文件夹不存在-->创建");
                                dirFile.mkdirs();
                            }
                            if(!TextUtils.isEmpty(imgName) && imgName.toLowerCase().endsWith("gif")){
                                cacheImageUrl = cacheFile + imgName;
                            }else{
                                cacheImageUrl = cacheFile + "/qwe.gif";
                            }

                            Log.i("---mzw---","GifView.setDownload_addr 生成缓存地址 ： " + cacheImageUrl);
                            File f = new File(cacheImageUrl);

                            if(f.length() <= 0){
                                Log.i("---mzw---", "GifView.setDownload_addr : 文件小于0，为错误文件-->删除文件");
                                f.delete();
                            }
                            if(!f.exists()){
                                Log.i("---mzw---", "GifView.setDownload_addr : 文件未缓存-->请求网络");
                                httpTest();
                            }else{
                                Log.i("---mzw---", "GifView.setDownload_addr : 文件已缓存-->请求本地");
                                download_addr = cacheImageUrl;
                                sdTest();
                            }
                        }else{
                            httpTestNoSD();
                        }
                    }else{
                        Log.i("---mzw---","GifView.setDownload_addr 图片地址为本地图片");
                        sdTest();
                    }
                    if(mMovie != null){
                        mHandler.sendEmptyMessage(0);
                    }
                }
            }).start();
        }
    }

    public GifView(Context context) {
        super(context);
        this.context = context;
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    private void readGifFormNative() {//资源图片
//        InputStream in;
//        in = context.getResources().openRawResource(R.raw.gif_pic);
//        mMovie = Movie.decodeStream(in);
//    }

//    private void readGifFormNet() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                readGifFormNet();//播放网络的gif格式图片
//                mHandler.sendEmptyMessage(0);
//            }
//        }).start();
//    }
    private void httpTestNoSD() {
        try {
            Log.i("---mzw---", "GifView.httpTestNoSD : 请求网络 -- 没有sd卡");
            URL url = new URL(download_addr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // connection.setRequestMethod("GET");
            int size = connection.getContentLength();
            Log.e(TAG, "size = " + size);
            // 输入流
            InputStream in = connection.getInputStream();

            byte[] array = streamToBytes(in);
            mMovie = Movie.decodeByteArray(array, 0, array.length);
            in.close();
        } catch (IOException e) {
            Log.i("---mzw---", "Exception=-=-=- 1 -=-=-=" + e.getMessage());
            e.printStackTrace();
        }
    }
    private void httpTest() {
        try {
            Log.i("---mzw---", "GifView.httpTest : 请求网络");
            URL url = new URL(download_addr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // connection.setRequestMethod("GET");
            int size = connection.getContentLength();
            Log.e(TAG, "size = " + size);
            // 输入流
            InputStream in = connection.getInputStream();

            if(!TextUtils.isEmpty(cacheImageUrl)){//缓存
                Log.i("---mzw---", "GifView.httpTest : 本地缓存");
                // 1K的数据缓冲
                byte[] bs = new byte[1024];
                // 读取到的数据长度
                int len;
                // 输出的文件流
                OutputStream os = new FileOutputStream(cacheImageUrl);
                // 开始读取
                while ((len = in.read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
                // 完毕，关闭所有链接
                os.close();
            }
            in.close();

            //网络请求  不直接显示图片， 得到网络图片后 缓存到本地  之后  在从本地缓存中得到图片显示
            download_addr = cacheImageUrl;
            sdTest();


        } catch (IOException e) {
            Log.i("---mzw---", "Exception=-=-=- 2 -=-=-=" + e.getMessage());
            e.printStackTrace();
        }
    }
    private void sdTest() {
        Log.i("---mzw---", "GifView.sdTest : 请求本地"+download_addr);
        try {
            FileInputStream fis = new FileInputStream(download_addr);
            byte[] array = streamToBytes(fis);
            Log.i("---mzw---", "array.length： "+array.length);
            mMovie = Movie.decodeByteArray(array, 0, array.length);
            fis.close();
        } catch (Exception e) {
            Log.i("---mzw---", "Exception=-=-=- 3 -=-=-=" + e.getMessage());
            e.printStackTrace();
        }
    }


    private static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            Log.i("---mzw---", "Exception=-=-=- 4 -=-=-=" + e.getMessage());
            e.printStackTrace();
        }
        return os.toByteArray();
    }

    @SuppressLint({"NewApi", "WrongConstant"})
    @Override
    protected void onDraw(Canvas canvas) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE, p);

        if (mMovie != null) {
            long now = android.os.SystemClock.uptimeMillis();
            if (mPlayMovieTime == 0) {   // first time
                mPlayMovieTime = now;
            }
            int dur = mMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }
            int relTime = (int) ((now - mPlayMovieTime) % dur);
            mMovie.setTime(relTime);
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.scale(mScale, mScale);//设置比例

            mMovie.draw(canvas, 0 , 0);//居中显示
//            mMovie.draw(canvas, (getWidth() - mMovie.width()) / 2, (getHeight() - mMovie.height()) / 2);//居中显示
            invalidate();
        }
    }


    //------------------------------------------------缓存------------------------------------------
    public static String getPortraitUrlPath(String str){
        String sdcard = "";
        if (SDisExists()) {
            sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
            return sdcard + "/test" + str;
        }else{
            return sdcard;
        }
    }
    /**
     * 判断是否有sd卡
     */
    public static boolean SDisExists() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
}