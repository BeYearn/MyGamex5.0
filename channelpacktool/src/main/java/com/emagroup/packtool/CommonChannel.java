package com.emagroup.packtool;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class CommonChannel {

	public void fixed(String channelStr, String gameApkName,String configPath) {
		
		String[] attrChannel = channelStr.split(",");
		//String configurationPath="D:\\packages\\cydzz\\configuration";
		String configurationPath=configPath;
		//String outputChannelPath="D:\\packages\\cydzz";
		String outputChannelPath=configurationPath.replace("\\configuration", "");
		
		// 创建saxReader对象
		SAXReader reader = new SAXReader();

		
		for(int i=0;i<attrChannel.length;i++){
			String fileName = outputChannelPath+"\\"+attrChannel[i].trim()+"\\"+gameApkName+"\\AndroidManifest.xml";
			// 配置的清单文件路径 "C:\\Users\\Administrator\\Desktop\\config\\yybconfig.xml";
			String maniConfigPath = configurationPath+"\\config"+"\\"+attrChannel[i].trim()+"Config.xml";
			
			
			
			// 读取两个manifest文件 转换成Document对象
			Document document = null;
			try {
				document = reader.read(new File(fileName));
			} catch (DocumentException e) {
				e.printStackTrace();
				System.out.print("please checkout the file path");
			}
			Document configDoc = null;
			try {
				configDoc = reader.read(new File(maniConfigPath));
			} catch (DocumentException e) {
				e.printStackTrace();
				System.out.print("please checkout the file path");
			}
			
			// 获取根节点元素对象
			Element rootNode = document.getRootElement();
			Element configRootNode = configDoc.getRootElement();
			
			// 得到配置文件的包名 渠道号
			Attribute configPackageAttr = configRootNode.attribute("package");
			String packageName = configPackageAttr.getValue();
			Attribute configChannelidAttr = configRootNode.attribute("channelid");
			String channelId = configChannelidAttr.getValue();
			//设置包名
			Attribute packageAttr = rootNode.attribute("package");
			packageAttr.setValue(packageName);
			//设置渠道号
			Node  applicaNode = document.selectSingleNode("manifest/application");
			List<Node> appidNodeList = applicaNode.selectNodes("meta-data[@android:name='EMA_CHANNEL_ID']");
			Node appidNode=appidNodeList.get(0);
			Attribute appidAtr = ((Element)appidNode).attribute(1);  // 疑问 为何用："android:value" 获取不到（null），只能用1（第二个）来获取到
			appidAtr.setValue(channelId);
			//修改环境
			//修改tag等
			
			// 修改个推两个权限的名字
			List<Attribute> attrListP = document.selectNodes("manifest/permission/@android:name");
			Iterator<Attribute> i1 = attrListP.iterator();
			while (i1.hasNext()) {
				Attribute attribute = i1.next();
				if (attribute.getValue().startsWith("getui.permission.GetuiService")) {
					attribute.setValue("getui.permission.GetuiService." + packageName);
				}
			}

			List<Attribute> attrListUp = document.selectNodes("manifest/uses-permission/@android:name");
			Iterator<Attribute> i2 = attrListUp.iterator();
			while (i2.hasNext()) {
				Attribute attribute = i2.next();
				if (attribute.getValue().startsWith("getui.permission.GetuiService")) {
					attribute.setValue("getui.permission.GetuiService." + packageName);
				}
			}
			List<Attribute> attrListProvi = document.selectNodes("manifest/application/provider/@android:authorities");
			Iterator<Attribute> i3 = attrListProvi.iterator();
			while (i3.hasNext()) {
				Attribute attribute = i3.next();
				if (attribute.getValue().startsWith("downloads")) {
					attribute.setValue("downloads." + packageName);
				}
			}
			
			// 合并
			Element mainNode = (Element) document.selectSingleNode("manifest");
			Element insertNode = (Element) configDoc.selectSingleNode("yyb");

			Element insertNodeAuth = (Element) insertNode.selectSingleNode("authority");
			mainNode.appendContent(insertNodeAuth);

			Element mainNodeAppliacton = (Element) mainNode.selectSingleNode("application");
			Element insertNodeComponent = (Element) insertNode.selectSingleNode("component");
			mainNodeAppliacton.appendContent(insertNodeComponent);

			// 写入
			try {
				CommonTool.writer(document, fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("***==================AndroidManifest-"+attrChannel[i]+" done==================***");
		}

	}

}
