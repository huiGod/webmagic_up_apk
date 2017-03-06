package com.uq.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;



public class FileDownloadUtil {

	private static Logger logger = LoggerFactory.getLogger(FileDownloadUtil.class);
	
	public static String[] getImage_h(String image_h){
		if(image_h ==null || SUtil.isEmpty(image_h)){
			return null;
		}
		
		try {
			String ss[] = image_h.split("\\|");
			return ss;
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return null;
	}

	
	/**
	 * 生成包名生成apk文件名
	 */
	public static String getApkFilename(String packagename){		
		String newName = packagename+"_"+getNowTime("yyyyMMddHHmmss") + "_"
				+ getRandomString(6) + ".apk";
		return newName;
	}
	
	/**
	 * 获取文件的后缀名称
	 */
	public static String getFileSuffix(String url){
		String extent ="";
		try {
			if(url.startsWith("http")){
				extent = parseSuffix(url);
			}else {
				System.out.println(url.lastIndexOf("."));
			    extent = url.substring(url.lastIndexOf("."), url.length());
			}
			extent = extent.toLowerCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return extent;
	}
	
	/**
	 * 获取日期时间
	 * @param dateformat
	 * @return
	 */
	public static String getNowTime(String dateformat) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		String hehe = dateFormat.format(now);
		return hehe;
	}
	
	public static String getDateYMD(){
		String dateStr ="";
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MMdd");;
		dateStr = df.format(new Date());	
		return dateStr;
	}
	//随机数
	public static String getRandomString(int length){ 
		String str = "0123456789"; 
		Random random = new Random(); 
		StringBuffer sb = new StringBuffer(); 

		for (int i = 0; i < length; ++i) { 
			int number = random.nextInt(10); 
			sb.append(str.charAt(number)); 
		} 
		return sb.toString(); 
	} 
	//检查目录是否存在，不存在则创建
	public static boolean checkDirExist(String dir){
		boolean flag = false;
		String[] dirAr = StringUtils.removeStart(StringUtils.removeEnd(dir, "/"), "/").split("\\/");;
		int length = 0;
		if(dir.endsWith("\\/")){
			length = dirAr.length;
		}else{
			length = dirAr.length-1;
		}
		File f;
		String tmpDirStr = "";
		for(int i=0; i<length; i++){
			f = new File(tmpDirStr += ("/" + dirAr[i]));
			if(!f.exists() || !f.isDirectory()){
				f.mkdir();
			}
		}
		return flag;
	}
	//删除文件
	public static void deleteFile(List<String> files,String rootPath){
		File file = null;
		for (int i = 0;files!=null && i < files.size(); i++) {
			String filename ="";
			if(rootPath !=null && rootPath!=""){
				filename = rootPath+files.get(i);				
			}else{
				filename = files.get(i);	
			}
			file = new File(filename);
			logger.info(filename);
			file.delete();
		}
	}
	
	public static List<String> saveImagePath(Jedis jedis,String imagepaths,String json){
		if(imagepaths== null){
			return null;
		}
		List<String> l = null;
		try {
			String[] paths = imagepaths.replace("[", "").replace("]", "").split(",");
			l = Arrays.asList(paths);
			String aa[] = ImageUtil.decodeJson(json);
			for (int i = 0; i < l.size(); i++) {
				jedis.hset(RedisConstant.DOWNURL_MAP_TEMP, l.get(i).trim(), aa[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;		
	}
	
	/** 
     * 获取链接的后缀名 
     * @return 
     */  
	final static Pattern pattern = Pattern.compile("\\S*[?]\\S*"); 
    private static String parseSuffix(String url) {  
  
        Matcher matcher = pattern.matcher(url);  
  
        String[] spUrl = url.toString().split("/");  
        int len = spUrl.length;  
        String endUrl = spUrl[len - 1];  
//        System.out.println(matcher.find());
        String suffix ="";
        try {
			if(matcher.find()) {  
			    String[] spEndUrl = endUrl.split("\\?");  
			    suffix = spEndUrl[0].split("\\.")[1];  
			}else {
				String[] tmp = endUrl.split("\\.");
				if(tmp!=null && tmp.length>1)suffix = tmp[1];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(!SUtil.isEmpty(suffix)){
			suffix = "."+suffix;
		}
        return suffix;
    }
	
	public static void main(String[] args) {
		System.out.println(getFileSuffix("http://pp.myapp.com/ma_pic2/0/shot_10945164_18815455_1_1408229170/0"));
		System.out.println(getFileSuffix("f://pp.myapp.com/ma_pic2/0/shot_10945164_18815455_1_1408229170/0.png"));
		System.out.println(getFileSuffix("http://cdnringhlt.shoujiduoduo.com//ringres/user/a48/520/7438520.MP3"));
//		System.out.println(parseSuffix("http://pp.myapp.com/ma_pic2/0/shot_10945164_18815455_1_1408229170/0"));
//		System.out.println(parseSuffix("http://pp.myapp.com/ma_pic2/0/shot_10945164_18815455_1_1408229170/0.jpg"));
//		System.out.println(getDateYMD());
	}
}
