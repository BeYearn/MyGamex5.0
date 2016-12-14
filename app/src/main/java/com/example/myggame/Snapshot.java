package com.example.myggame;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/12/14.
 */

public class Snapshot {
    public static void saveBitmap(View view,  Context context) throws IOException {
        // 得到当前view所在view结构中的根view
        View vv = view.getRootView();
        // 设置属性
        vv.setDrawingCacheEnabled(true);
        // 取得位图
        Bitmap bitmap= vv.getDrawingCache();
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
        String fileName=simpleDateFormat.format(new Date());
        File folder = new File("/mnt/sdcard/dcim/Camera/");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File("/mnt/sdcard/dcim/Camera/" + fileName + ".jpg");
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
        }
    }

}
