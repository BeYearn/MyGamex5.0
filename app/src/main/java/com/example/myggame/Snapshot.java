package com.example.myggame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2016/12/14.
 */

public class Snapshot {
  /*  public static Bitmap saveBitmap(View view,  Context context) throws IOException {
        // 得到当前view所在view结构中的根view
        View vv = view.getRootView();
        // 设置属性
        vv.setDrawingCacheEnabled(true);
        // 取得位图
        Bitmap bitmap= vv.getDrawingCache();
        File folder,file = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
        String fileName = simpleDateFormat.format(new Date());
        try {
            folder  = new File( Environment.getExternalStorageDirectory().getCanonicalPath()+""+File.separator + "dcim"+File.separator +"Camera"+File.separator);
            // File folder = new File("/mnt/sdcard/dcim/Camera/");
            if (!folder.exists()) {
                folder.mkdir();
            }
            //Environment.getExternalStorageDirectory() +File.separator + "EMASDK"+File.separator +"ServerUrl"+File.separator;
            file = new File(Environment.getExternalStorageDirectory().getCanonicalPath()+""+File.separator + "dcim"+File.separator +"Camera"+File.separator+ fileName + ".png");

            // File file = new File("/mnt/sdcard/dcim/Camera/" + fileName + ".png");
            //  Toast.makeText(context, "保存图片中", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "保存图片中", Toast.LENGTH_SHORT).show();
        FileOutputStream out;
        if (!file.exists()) {

            try {
                out = new FileOutputStream(file);
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 70, out)) {
                    Toast.makeText(context, "成功存入相册",
                            Toast.LENGTH_SHORT).show();
                    out.flush();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                return  bitmap  ;
            }
        }
        return  bitmap  ;
    }*/
    public static Bitmap getBitmap(String url) {
        URL imageURL = null;
        Bitmap bitmap = null;
         try {
            imageURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) imageURL
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
