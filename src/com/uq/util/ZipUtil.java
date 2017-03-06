package com.uq.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;


/**
 * 
 * @ClassName: ZipUtil 
 * @Description: 以zipfile读取apk失败时，用这工具类来读取
 * @author aurong
 * @date 2015-5-26 上午10:22:58
 */
public class ZipUtil {
	private static final Namespace NS = Namespace.getNamespace("http://schemas.android.com/apk/res/android");
	
	public static void main(String[] args) {
//		String zippath="z:/fileupload/apk/2015/0523/jr113/a2e0a71f38c6f2cf76eb6894faa6187d/com.hp.moonrise.apk";
		String zippath = "f:/test/apk/4f4c5f6eb32848892be826891d4205d2.apk";
		Map<String, String> map = getinfoForApk(zippath);
		for(Map.Entry<String, String> entry:map.entrySet()){
			System.out.println(entry.getKey()+"=="+entry.getValue());
		}
	}
	
	public static Map<String, String> getinfoForApk(String apkurl){
		//先把解压apk解压到当前目录
		File apkFile = new File(apkurl);
		Map<String, String> resultMap = new HashMap<String, String>();
		try {
			unzipFileIntoDirectory(apkFile);
			Map<String, String> pathMap = getFilepath(apkurl);
			String xmlPath = pathMap.get("xmlPath");
			String rsaPath = pathMap.get("rsaPath");			
			resultMap = readXml(xmlPath);
			resultMap.put("signature", readSign(rsaPath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultMap;
	}
	
	//解压apk
	public static void unzipFileIntoDirectory(File archive) throws Exception {

        final int BUFFER_SIZE = 1024;

        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(archive);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        File destFile;
        String fileSuffix = FileDownloadUtil.getFileSuffix(archive.getAbsolutePath());
        File destinationDir = new File(archive.getParentFile().getAbsolutePath()+File.separator+archive.getName().replace(fileSuffix, ""));//解压的目录
        FileDownloadUtil.checkDirExist(destinationDir.getAbsolutePath());
        System.out.println(destinationDir.getPath());
        while((entry = zis.getNextEntry()) != null) {               

            destFile = combineFileNames(destinationDir, entry.getName());

            if (entry.isDirectory()) {
                destFile.mkdirs();
                continue;
            } else {
                int count;
                byte data[] = new byte[BUFFER_SIZE];

                destFile.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream(destFile);
                dest = new BufferedOutputStream(fos, BUFFER_SIZE);
                while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                    dest.write(data, 0, count);
                }

                dest.flush();
                dest.close();
                fos.close();
            }
        }
        zis.close();
        fis.close();                        
    }
	//获取apk的xml配置和签名路径
	public static Map getFilepath(String apkurl){
		//解压在当前目录，以包名命名
		String pkgfile = apkurl.replace(FileDownloadUtil.getFileSuffix(apkurl), "");
		System.out.println(pkgfile);
		File f = new File(pkgfile);
		Map<String, String> map = new HashMap<String, String>();
		File[] files = f.listFiles();
		for(File tmpFile:files ){
			if(tmpFile.getName().equalsIgnoreCase("AndroidManifest.xml")){
				System.out.println(tmpFile.getAbsolutePath());
				map.put("xmlPath", tmpFile.getAbsolutePath());
			}else if(tmpFile.getName().equalsIgnoreCase("META-INF")){
				if(tmpFile.isDirectory()){
					File[] meta = tmpFile.listFiles();
					for(File m:meta){
						if("CERT.RSA".equals(m.getName())||m.getName().matches("[\\w]+?.RSA")||m.getName().matches("[\\w]+?.DSA")||m.getName().toLowerCase().endsWith("rsa")||m.getName().toLowerCase().endsWith("dsa")){
							System.out.println(m.getAbsolutePath());
							map.put("rsaPath", m.getAbsolutePath());
						}
					
					}
				}
			}
		}
		return map;
	}
	public static File combineFileNames(File destinationDir,String name){
		String path = destinationDir.getAbsolutePath()+File.separator+name;
		return new File(path);
	}
	
	public static  Map readXml(String xmlPath){
		Map<String, String> map = new HashMap<String, String>();
		try {			
			InputStream inputStream = new FileInputStream(xmlPath);
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
				System.out.println("==pkg=="+root.getAttributeValue("package", NS));
				map.put("packagename", root.getAttributeValue("package",NS)==null?root.getAttributeValue("package"):root.getAttributeValue("package",NS));
				Element elemUseSdk = root.getChild("uses-sdk");//子节点-->uses-sdk
				Element e1 = root.getChild("application");
				List<Element> list = e1.getChildren();
				remdOut:
				for(Element e2:list){
					System.out.println(e2.getName());
					if("meta-data".equalsIgnoreCase(e2.getName())){
						System.out.println("属性名:"+e2.getAttributeValue("name"));
						List<Attribute> l2 = e2.getAttributes();
						for(Attribute e3:l2){
							if("name".equalsIgnoreCase(e3.getName()) && "QHOPENSDK_APPID".equalsIgnoreCase(e3.getValue())){
								map.put("isremd", "y");//是360的合作包
								break remdOut;
							}
						}
					}
				}
				System.out.println("---"+elemUseSdk.getAttributeValue("minSdkVersion"));
				map.put("minsdkversion", elemUseSdk.getAttributeValue("minSdkVersion", NS)==null?elemUseSdk.getAttributeValue("minSdkVersion"):elemUseSdk.getAttributeValue("minSdkVersion", NS));
				
				//权限
				List listPermission = root.getChildren("uses-permission");//子节点是个集合
				StringBuffer buf = new StringBuffer();
				for(Object object : listPermission){
					String permission = ((Element)object).getAttributeValue("name", NS);
					String lable = ((Element)object).getAttributeValue("description",NS);
//				System.out.println("label:"+lable);
					buf.append(permission).append(",");
//				System.out.println("权限："+permission);							
				}
				System.out.println(buf.length());
				if(buf.length()>1){
					map.put("permission", buf.substring(0,buf.length()-1));
				}else {
					map.put("permission", "");
				}
				return map;
			}catch (Exception e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static String readSign(String rsaPath){
		String sign = "";
		try {
			InputStream in = new FileInputStream(rsaPath);
			X509Certificate publicKey = AnalysisApk.readSignatureBlock(in);
			System.out.println(AnalysisApk.getMessageDigest(publicKey.getEncoded(),"sha"));
			sign = AnalysisApk.getMessageDigest(publicKey.getEncoded(),"sha");
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sign;
	}
}
