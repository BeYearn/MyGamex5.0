package com.emagroup.sdk;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yby on 2016/10/12.
 */
public class MyImageLoad {

    private static MyImageLoad mInstance;
    private String uri;
    private View mView;
    private byte[] picByte;

    //private MyImageLoad(){}

    /*public static MyImageLoad getInstance() {
        if (mInstance == null) {
            mInstance = new MyImageLoad();
        }
        return mInstance;
    }*/

    public void setPicture(String uri,View view){  //支持button 和 imageView
        this.uri = uri;
        this.mView = view;
        new Thread(runnable).start();
    }

    @SuppressLint("HandlerLeak")
    Handler handle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (picByte != null) {
                    Bitmap bitmapRaw = BitmapFactory.decodeByteArray(picByte, 0, picByte.length);

                    Drawable drawable = reSizeBitmap(bitmapRaw,84,84);

                    if(mView instanceof ImageView ){
                        ((ImageView)mView).setImageDrawable(drawable);
                    }else if(mView instanceof Button){
                        ((Button) mView).setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);
                    }

                }
            }
        }
    };

    /**
     * 缩放图片尺寸
     * @param bitmap  原图
     * @param w       修改后的宽
     * @param h       修改后的高
     */
    private Drawable reSizeBitmap(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);

        return new BitmapDrawable(resizedBitmap);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(uri);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() == 200) {
                    InputStream fis =  conn.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024];
                    int length = -1;
                    while ((length = fis.read(bytes)) != -1) {
                        bos.write(bytes, 0, length);
                    }
                    picByte = bos.toByteArray();
                    bos.close();
                    fis.close();

                    Message message = new Message();
                    message.what = 1;
                    handle.sendMessage(message);
                }


            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

}
