package com.emagroup.packtool;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

public class CommonTool {

	public CommonTool() {
	}

	/*
	 * 如果在控制台输入“abc”按回车，控制台会输出5个整形数值，最后两个是13，10
	 * 13和10正好是\n\r的ascii码值。那么对reader2在进行优化，当键盘输入over 的时候，终止这个阻塞式方法，让其不再等待键盘的录入。
	 */
	public static String valueInput() {
		int ch;
		StringBuilder sb = new StringBuilder();
		InputStream in = System.in;
		String dir = "";
		try {
			while ((ch = in.read()) != -1) {
				if (ch == '\n') {
					continue;
				}
				if (ch == '\r') {
					dir = sb.toString();
					sb.delete(0, sb.length());// 一行读取结束后将sb清空，这里面运用的是删除元素的方法
					break;
				} else {
					sb.append((char) ch);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dir;
	}

	/**
	 * 把document对象写入新的文件
	 * 
	 * @param document
	 * @throws Exception
	 */
	public static void writer(Document document, String filename) throws Exception {
		// 排版缩进的格式
		OutputFormat format = OutputFormat.createPrettyPrint();
		// 设置编码
		format.setEncoding("UTF-8");
		// 创建XMLWriter对象,指定了写出文件及编码格式
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(new File(filename)), "UTF-8"),
				format);
		// 写入
		writer.write(document);
		// 立即写入
		writer.flush();
		// 关闭操作
		writer.close();
	}

	/**
	 * 实时打印dos返回信息
	 * @param process
	 */
	public static void outputDosMes(Process process) {
		// 记录dos命令的返回信息
		StringBuffer resStr = new StringBuffer();
		// 获取返回信息的流
		InputStream in = process.getInputStream();
		Reader reader;
		try {
			reader = new InputStreamReader(in, "GBK");// 因为控制台用的是gbk输出的，而代码是utf_8的
			BufferedReader bReader = new BufferedReader(reader);
			for (String res = ""; (res = bReader.readLine()) != null;) {
				resStr.append(res + "\n");
				System.out.println(res);
			}
			System.out.println("=========done========");
			bReader.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void Log(String s){
		System.out.println("###"+s+"###");
	}
}
