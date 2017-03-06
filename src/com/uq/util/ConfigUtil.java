package com.uq.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class ConfigUtil {
	
	//下载文件的目录
//	public static final String Download_Path ="/data/filedown/";
	//上传文件的目录
//	public static final String Upload_Path ="/data/fileupload/";
	//存放未上传文件fqf的目录
	public static final String Upload_Fqf_Path ="F:/fileupload/fqfupload/";
	//备份上传文件fqf的目录
	public static final String Back_Fqf_Path ="F:/fileupload/fqfback/";
	//flashfxp的目录
	public static final String Flashfxp_Path ="D:/tools/FlashFXP/";
	
	public static final String Image_h = "remarkimages_h";
	public static final String Image_m = "remarkimages_m";
	public static final String Image_l = "remarkimages_l";
	
	//redis保存的key
	public static final String APK_MAP_INFO = "pkg:";
	//wcs云存储
//	public  static final String AK ="1057f27271aa52b72fc0ff4f507fe63345c114b9";
//	public static final String SK ="57e9d2342ac9ff57d570bbb1eab729cc75712afa";
//	public static final String BucketName ="weiap";//weiap
	//读取mapping.txt
	public static void main(String[] args) {
		InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream("mapping.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			String tmp ="";
			while((tmp =reader.readLine())!=null){				
				String ss[] = tmp.split("=");
				System.out.println(ss[0]+"--"+ss[1]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		.getResourceAsStream("dbconfig.properties")
	}
	
	/**
	 * 
	 * @Description: 适用于抓取数据的分类比对
	 * @param @return
	 * @date 2015-6-12
	 * @author	aurong
	 * @return Map<String,String>
	 */
	public static Map<String, String> getMapping(){
		Map<String, String> mapping = new HashMap<String, String>();
 		InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream("mapping.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			String tmp ="";
			while((tmp =reader.readLine())!=null){				
				String ss[] = tmp.split("=");
				System.out.println(ss[0]+"--"+ss[1]);
				mapping.put(ss[0], ss[1].split("-")[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mapping;
	}
	
	//适用于保存分类进数据库
	public static Map<String, String> getCateMapping(){
		Map<String, String> mapping = new HashMap<String, String>();
 		InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream("mapping.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			String tmp ="";
			while((tmp =reader.readLine())!=null){				
				String ss[] = tmp.split("=");
				mapping.put(ss[0], ss[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mapping;
	}
}
