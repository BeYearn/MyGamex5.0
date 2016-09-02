package com.emagroup.sdk;

import java.io.File;
import java.io.RandomAccessFile;

import android.os.Environment;

public class LOGToSdcardHelper {

	private static final String SDCARD_LOGFILE_PATH = "EMASDK/emalog.txt";
	
	private static boolean mFlag;//标记是需要写入sdcard
	
	public static void setWriteFlag(boolean flag){
		mFlag = flag;
	}
	
	public static void writeLog(String logInfo){
		if(mFlag){
			write(logInfo + "\n");
		}
	}
	
	private static void write(String logInfo){
		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				File sDir = Environment.getExternalStorageDirectory();
				File file = new File(sDir + File.separator + SDCARD_LOGFILE_PATH);
				if(!file.getParentFile().exists()){
					File paretnFile = file.getParentFile();
					paretnFile.mkdirs();
				}
				
				RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
				randomFile.seek(randomFile.length());
				randomFile.writeBytes(logInfo);
				randomFile.close();
				
			}else{
				System.out.println("没有sdcard");
			}
		} catch (Exception e) {
			mFlag = false;//如果报错了，就不再继续写入了，浪费资源
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除文件
	 */
	public static void cleanFile(){
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File sdCardDir = Environment.getExternalStorageDirectory();
				File targetFile = new File(sdCardDir + File.separator + SDCARD_LOGFILE_PATH);
				if(targetFile.exists())
				{
					targetFile.delete();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
