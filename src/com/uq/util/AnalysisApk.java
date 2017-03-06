package com.uq.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;

import sun.security.pkcs.PKCS7;
import android.content.res.AXmlResourceParser;
import android.util.TypedValue;



/**
 * 分析APK文件，取得APK文件中的 包名、版本号及图标
 */
public class AnalysisApk {
	
	private static final Namespace NS = Namespace.getNamespace("http://schemas.android.com/apk/res/android");
	
	/**
	 * 解压 zip 文件(apk可以当成一个zip文件)，注意不能解压 rar 文件哦，只能解压 zip 文件 解压 rar 文件 会出现
	 * java.io.IOException: Negative seek offset 异常 create date:2009- 6- 9
	 * author:Administrator
	 * 
	 * @param apkUrl
	 *            zip 文件，注意要是正宗的 zip 文件哦，不能是把 rar 的直接改为 zip 这样会出现
	 *            java.io.IOException: Negative seek offset 异常
	 * @param logoUrl
	 *            图标生成的地址
	 * @return msg[4] [0]:版本名称,[1]:版本号,[2]:包名,[3]:最低android系统要求,[4]:apk签名信息(SHA) 
	 * @throws IOException
	 */
	public static Map unZip(String apkUrl, String logoUrl) {
		
//		String[] st = new String[5];
		Map<String, String> map = new HashMap<String, String>();
		byte b[] = new byte[1024];
		int length;
		
		try {
			ZipFile zipFile = null;
			zipFile = new ZipFile(new File(apkUrl));			
			Enumeration<?> enumeration = zipFile.entries();
			ZipEntry zipEntry = null;
			while (enumeration.hasMoreElements()) {
				zipEntry = (ZipEntry) enumeration.nextElement();
//				System.out.println(zipEntry.getName());
				if (zipEntry.isDirectory()) {

				} else {
					if ("AndroidManifest.xml".equals(zipEntry.getName())) {
						InputStream inputStream = null;
						inputStream = zipFile.getInputStream(zipEntry);
						AXMLPrinter xmlPrinter = new AXMLPrinter();
						xmlPrinter.startPrinf(inputStream);
						inputStream = new ByteArrayInputStream(xmlPrinter.getBuf().toString().getBytes("UTF-8"));
						SAXBuilder builder = new SAXBuilder();
						Document document = null;
						try{
							document = builder.build(inputStream);
							Element root = document.getRootElement();//根节点-->manifest
							map.put("versionname", root.getAttributeValue("versionName",NS)==null?root.getAttributeValue("versionName"):root.getAttributeValue("versionName",NS));
							map.put("versioncode", root.getAttributeValue("versionCode",NS)==null?root.getAttributeValue("versionCode"):root.getAttributeValue("versionCode",NS));
//							System.out.println("==pkg=="+root.getAttributeValue("package", NS));
							map.put("packagename", root.getAttributeValue("package",NS)==null?root.getAttributeValue("package"):root.getAttributeValue("package",NS));
							Element elemUseSdk = root.getChild("uses-sdk");//子节点-->uses-sdk
//							System.out.println("---"+elemUseSdk.getAttributeValue("minSdkVersion"));
							Element e1 = root.getChild("application");
//							e1.getChild("meta-data");
//							e1.getChildren("");
							List<Element> list = e1.getChildren("meta-data");
							remdOut:
							for(Element e2:list){
								if("meta-data".equalsIgnoreCase(e2.getName())){
									List<Attribute> l2 = e2.getAttributes();
									for(Attribute e3:l2){
										System.out.println("-=-=-=-="+e3.getName());
										System.out.println("====-"+e3.getValue());
										if("name".equalsIgnoreCase(e3.getName()) && "QHOPENSDK_APPID".equalsIgnoreCase(e3.getValue())){
//											System.out.println("===进入了");
											map.put("isremd", "y");//是360的合作包
											break remdOut;
										}																		
									}
								}
							}
							//豌豆荚的推广包
//							wdjOut:
							List<Element> activitylist = e1.getChildren("activity");
							for(Element ac:activitylist){
								System.out.println(ac.getName());
								if("activity".equalsIgnoreCase(ac.getName())){
									List<Attribute> lac = ac.getAttributes();
									for(Attribute e3:lac){
//										System.out.println("-=-=-=-="+e3.getName());
//										System.out.println("====-"+e3.getValue());
										if("name".equalsIgnoreCase(e3.getName()) && "com.wandoujia.oakenshield.activity.OakenshieldActivity".equalsIgnoreCase(e3.getValue())){
											map.put("isremd", "y");//是豌豆荚的合作包
//											break wdjOut;
										}
									}
								}
							}
							
							map.put("minsdkversion", elemUseSdk.getAttributeValue("minSdkVersion", NS)==null?elemUseSdk.getAttributeValue("minSdkVersion"):elemUseSdk.getAttributeValue("minSdkVersion", NS));
							//权限
							List listPermission = root.getChildren("uses-permission");//子节点是个集合
							StringBuffer buf = new StringBuffer();
							for(Object object : listPermission){
								String permission = ((Element)object).getAttributeValue("name", NS);
								String lable = ((Element)object).getAttributeValue("description",NS);
//								System.out.println("label:"+lable);
								buf.append(permission).append(",");
//								System.out.println("权限："+permission);							
							}
							System.out.println(buf.length());
							if(buf.length()>1){
								map.put("permission", buf.substring(0,buf.length()-1));
							}else {
								map.put("permission", "");
							}
							
						}catch (Exception e) {
							e.printStackTrace();
							try {
								AXmlResourceParser parser = new AXmlResourceParser();
								parser.open(zipFile.getInputStream(zipEntry));
								while (true) {
									int type = parser.next();
									if (type == XmlPullParser.END_DOCUMENT) {
										break;
									}
									switch (type) {
									case XmlPullParser.START_TAG: {
										for (int i = 0; i != parser.getAttributeCount(); ++i) {
											System.out.println("属性名字："+parser.getAttributeName(i));
											System.out.println(getAttributeValue(parser, i));
											if ("versionName".equals(parser.getAttributeName(i))) {
												map.put("versionname", getAttributeValue(parser, i));
											} else if ("versionCode".equals(parser.getAttributeName(i))) {
												map.put("versioncode", getAttributeValue(parser, i));
											} else if ("package".equals(parser.getAttributeName(i))) {
												map.put("packagename", getAttributeValue(parser, i));
											}else if("minSdkVersion".equalsIgnoreCase(parser.getAttributeName(i))){
												map.put("minsdkversion", getAttributeValue(parser, i));
											}
										}
									}
									}
								}
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						
						
						/*try {
							AXmlResourceParser parser = new AXmlResourceParser();
							parser.open(zipFile.getInputStream(zipEntry));
							while (true) {
								int type = parser.next();
								if (type == XmlPullParser.END_DOCUMENT) {
									break;
								}
								switch (type) {
								case XmlPullParser.START_TAG: {
									for (int i = 0; i != parser.getAttributeCount(); ++i) {
										System.out.println("属性名字："+parser.getAttributeName(i));
										System.out.println(getAttributeValue(parser, i));
										if ("versionName".equals(parser.getAttributeName(i))) {
											map.put("versionname", getAttributeValue(parser, i));
										} else if ("versionCode".equals(parser.getAttributeName(i))) {
											map.put("versioncode", getAttributeValue(parser, i));
										} else if ("package".equals(parser.getAttributeName(i))) {
											map.put("packagename", getAttributeValue(parser, i));
										}else if("minSdkVersion".equalsIgnoreCase(parser.getAttributeName(i))){
											map.put("minsdkversion", getAttributeValue(parser, i));
										}
									}
								}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}*/
					}
					//获取图标
					if (logoUrl !=null && !"".equals(logoUrl) && "res/drawable-hdpi/icon.png".equals(zipEntry.getName())) {
						OutputStream out = null ;
						InputStream inputStream =null;
						try {
							 out = new FileOutputStream(logoUrl);
							 inputStream= zipFile.getInputStream(zipEntry);
							while ((length = inputStream.read(b)) > 0)
								out.write(b, 0, length);
						} catch (IOException e) {
							System.out.println("解析文件图标出错");
						}finally{
							if(out !=null){
								try {
									out.close();
								} catch (Exception e) {
								}
							}
							if(inputStream !=null){
								try {
									inputStream.close();
								} catch (Exception e) {
								}
							}
						}
						
					}
					
					//获取apk签名信息
//					System.out.println("===="+zipEntry.getName());
					if(zipEntry.getName().startsWith("META-INF")){
						if("META-INF/CERT.RSA".equals(zipEntry.getName())||zipEntry.getName().matches("META-INF/[\\w]+?.RSA")||zipEntry.getName().matches("META-INF/[\\w]+?.DSA")||zipEntry.getName().toLowerCase().endsWith("rsa")||zipEntry.getName().toLowerCase().endsWith("dsa")){
							try {
								X509Certificate publicKey = readSignatureBlock(zipFile.getInputStream(zipEntry));
//								st[4] = getMessageDigest(publicKey.getEncoded(),"sha");
								map.put("signature", getMessageDigest(publicKey.getEncoded(),"SHA1"));//sha
							} catch (GeneralSecurityException e) {
								e.printStackTrace();
							}
							
						}
					}					
				}
			}
			zipFile.close();//关闭资源
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("读取zip文件失败");
			map = ZipUtil.getinfoForApk(apkUrl);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}

	@Test
	public void testsingn(){
		try {
			String path = "F:/test/t/META-INF/YOPAN.RSA";
			InputStream in = new FileInputStream(path);
			X509Certificate publicKey = readSignatureBlock(in);
//			st[4] = getMessageDigest(publicKey.getEncoded(),"sha");
			System.out.println(getMessageDigest(publicKey.getEncoded(),"sha1"));
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static X509Certificate readSignatureBlock(InputStream in)
			throws IOException, GeneralSecurityException {
		PKCS7 pkcs7 = new PKCS7(in);
		X509Certificate[] cers = pkcs7.getCertificates();
		return cers[0];
	}
	
	public static final String getMessageDigest(byte[] paramArrayOfByte,String algorithm) {
		char[] arrayOfChar1 = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102 };
		try {
			MessageDigest localMessageDigest = MessageDigest.getInstance(algorithm);
			localMessageDigest.update(paramArrayOfByte);
			byte[] arrayOfByte = localMessageDigest.digest();
			int i = arrayOfByte.length;
			char[] arrayOfChar2 = new char[i * 2];
			int j = 0;
			int k = 0;
			while (true) {
				if (j >= i)
					return new String(arrayOfChar2);
				int m = arrayOfByte[j];
				int n = k + 1;
				arrayOfChar2[k] = arrayOfChar1[(0xF & m >>> 4)];
				k = n + 1;
				arrayOfChar2[n] = arrayOfChar1[(m & 0xF)];
				j++;
			}
		} catch (Exception localException) {
		}
		return null;
	}
	
	private static String getAttributeValue(AXmlResourceParser parser, int index) {
		int type = parser.getAttributeValueType(index);
		int data = parser.getAttributeValueData(index);
		if (type == TypedValue.TYPE_STRING) {
			return parser.getAttributeValue(index);
		}
		if (type == TypedValue.TYPE_ATTRIBUTE) {
			return String.format("?%s%08X", getPackage(data), data);
		}
		if (type == TypedValue.TYPE_REFERENCE) {
			return String.format("@%s%08X", getPackage(data), data);
		}
		if (type == TypedValue.TYPE_FLOAT) {
			return String.valueOf(Float.intBitsToFloat(data));
		}
		if (type == TypedValue.TYPE_INT_HEX) {
			return String.format("0x%08X", data);
		}
		if (type == TypedValue.TYPE_INT_BOOLEAN) {
			return data != 0 ? "true" : "false";
		}
		if (type == TypedValue.TYPE_DIMENSION) {
			return Float.toString(complexToFloat(data)) + DIMENSION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
		}
		if (type == TypedValue.TYPE_FRACTION) {
			return Float.toString(complexToFloat(data)) + FRACTION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
		}
		if (type >= TypedValue.TYPE_FIRST_COLOR_INT && type <= TypedValue.TYPE_LAST_COLOR_INT) {
			return String.format("#%08X", data);
		}
		if (type >= TypedValue.TYPE_FIRST_INT && type <= TypedValue.TYPE_LAST_INT) {
			return String.valueOf(data);
		}
		return String.format("<0x%X, type 0x%02X>", data, type);
	}

	private static String getPackage(int id) {
		if (id >>> 24 == 1) {
			return "android:";
		}
		return "";
	}

	// ///////////////////////////////// ILLEGAL STUFF, DONT LOOK :)
	public static float complexToFloat(int complex) {
		return (float) (complex & 0xFFFFFF00) * RADIX_MULTS[(complex >> 4) & 3];
	}

	private static final float RADIX_MULTS[] = { 0.00390625F, 3.051758E-005F, 1.192093E-007F, 4.656613E-010F };
	private static final String DIMENSION_UNITS[] = { "px", "dip", "sp", "pt", "in", "mm", "", "" };
	private static final String FRACTION_UNITS[] = { "%", "%p", "", "", "", "", "", "" };
	
	
	public static void main(String[] args) {
		String apkUrl ="f:/test/apk/com.argthdk.suansuan.apk";//微信 360版本
//		String apkUrl ="f:/test/weixin610android540.apk";
//		String apkUrl ="f:/test/com.tencent.token_44.apk"; // qq安全中心
//		apkUrl ="f:/koufei.apk"; // 微信 百度移动应用
//		apkUrl ="f:/test/apk/f252baee8d6b1328018368498f300d00.apk";
//		apkUrl ="f:/test/apk/com.zj.whackmole2_11.apk";//qq 英雄最传奇
//		apkUrl ="f:/test/apk/去哪儿旅行_com.Qunar.apk";
//		apkUrl ="z:/fileupload/apk/2015/0628/6m3rv/7AF80D309E13CEDD09C2AAC6A588B88C/com.ninefang.xjtx.wdj.apk";//com.Qunar.apk
		Map<String, String> map = AnalysisApk.unZip(apkUrl, "");
//		String md5 = GetBigFileMD5.getMD5(new File(apkUrl));
//		System.out.println(md5);
		for (Map.Entry<String, String> entry:map.entrySet()) {
			System.out.println(entry.getKey()+"----"+entry.getValue());
		}
		
//		Map<String, String> map1 = ZipUtil.getinfoForApk(apkUrl);
//		for (Map.Entry<String, String> entry:map1.entrySet()) {
//			System.out.println(entry.getKey()+"----"+entry.getValue());
//		}
	}
	
	
}
