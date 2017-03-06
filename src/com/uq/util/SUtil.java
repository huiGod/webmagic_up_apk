package com.uq.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;



/**
 * 系统公用方法集
 * 
 * @author Administrator
 * 
 */
public class SUtil {
	static Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+"); 
	
	/**
	 * 判断某字符串是否为null，如果长度大于256，则返回256长度的子字符串，反之返回原串
	 * 
	 * @param str
	 * @return
	 */
	public static String checkStr(String str) {
		if (str == null) {
			return "";
		} else if (str.length() > 256) {
			return str.substring(256);
		} else {
			return str;
		}
	}

	/**
	 * 验证是不是Int validate a int string
	 * 
	 * @param str
	 *            the Integer string.
	 * @return true if the str is invalid otherwise false.
	 */
	public static boolean validateInt(String str) {
		if (str == null || str.trim().equals(""))
			return false;

		char c;
		for (int i = 0, l = str.length(); i < l; i++) {
			c = str.charAt(i);
			if (!((c >= '0') && (c <= '9')))
				return false;
		}

		return true;
	}

	/**
	 * 是否为空
	 * 
	 * @param str
	 *            待校验字符串
	 * @return 是否为空
	 */
	public static boolean isEmpty(String str) {
		return org.apache.commons.lang.StringUtils.isEmpty(str);
	}

	/**
	 * 是否非空
	 * 
	 * 任何一个字符串为空,将返回false
	 * 
	 * @param strs
	 *            待校验字符串
	 * @return 是否非空
	 */
	public static boolean isNotBlank(String... strs) {
		if (null == strs) {
			return false;
		}

		for (String str : strs) {
			if (!org.apache.commons.lang.StringUtils.isNotBlank(str)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否是数字
	 * 
	 * @param str
	 *            目标字符串
	 * @return 是否是数字
	 */
	public static boolean isNumeric(String str) {
		return org.apache.commons.lang.StringUtils.isNumeric(str);//只能判断整数

		   /*Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false; 
		   } 
		   return true; */
	}

	/**
	 * 是否为数字 任何一个字符串不是数字，将返回false
	 * 
	 * @param strs
	 *            待校验字符串
	 * @return 是否非空
	 */
	public static boolean isAllNumeric(String... strs) {
		if (null == strs) {
			return false;
		}
		for (String str : strs) {
			if (!org.apache.commons.lang.StringUtils.isNumeric(str)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断一个Interger是否能转为数字,只有当interger为null才不能转换
	 */
	public static boolean isNumeric(Integer str) {
		if (str == null) {
			return false;
		}
		return true;
	}

	public static Integer toInt(Integer str) {
		if (isNumeric(str)) {
			return str;
		}
		return 0;
	}

	public static double toDouble(String str){
		try {
			if (isNumeric(str)) {
				return Double.parseDouble(str);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0.0;
	}
	
	public static int formatStr(Object str){
		if("".equals(str)||str==null){
			return 0;
		}
		if(isNumeric(str.toString())){
			return Integer.valueOf(str.toString());
		}
		return 0;
	}
	//针对豌豆荚返回的json字符为double类型
	public static int formatDoubleStr(Object str){
		if("".equals(str)||str==null){
			return 0;
		}
		System.out.println(str);
		String tmp = str.toString();
		if(isNumeric(str.toString())){
//			System.out.println(tmp.indexOf("."));
//			System.out.println(tmp.substring(0, tmp.indexOf(".")));
			return Integer.valueOf(tmp.substring(0, tmp.indexOf(".")));
		}
		return 0;
	}
	
	/**
	 * 返回yyyy-mm-dd
	 * 
	 * @param aDate
	 * @return
	 */
	public static final String formatdate(Date aDate) {
		SimpleDateFormat df = null;
		String returnValue = "";

		if (aDate != null) {
			df = new SimpleDateFormat("yyyy-MM-dd");
			returnValue = df.format(aDate);
		}

		return (returnValue);
	}

	public static Date formatStrToDate(String strDate,String format){
		try {
			SimpleDateFormat sdf =   new SimpleDateFormat(format);//" yyyy-MM-dd HH:mm:ss " 
			Date date = sdf.parse(strDate);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date();
	}
	/**
	 * Interger 转换为String
	 */
	public static String formatInt(Integer i) {

		if (isNumeric(i))
			return String.valueOf(i);
		return "0";
	}
	
	/**
	 * String 转换为Integer
	 */
	public static int formatStr(String str){
		if("".equals(str)||str==null){
			return 0;
		}
		if(isNumeric(str)){
			return Integer.valueOf(str);
		}
		return 0;
	}
	
	public static String formatObj(Object ss){
		if(ss == null){
			return "";
		}
		return String.valueOf(ss);
	}
	
	/*
	 * 基础数据类型转换为string
	 */
	public static String converString(Object o) {
		try {
			if (o == null || "null".equals(o)) {
				return "";
			}
			return o.toString();
		} catch (Exception e) {
			System.out.println("===========");
			e.printStackTrace();
		}
		return "";
	}
	
	public static String convertSize(Object sumsize){
		String size = converString(sumsize);
		String fsize="";
		if(isAllNumeric(size)){
			Long lsize = Long.valueOf(size);
			int tmp = Math.round(lsize/1024);
			DecimalFormat formater = new DecimalFormat("####.00");
			if(tmp>1024*1024){ // gb
				float f = (float)lsize/(1024*1024*1024);
				fsize = formater.format(f)+"G";				
			}else if(tmp>1024){
				float f = (float)lsize/(1024*1024);
				fsize = formater.format(f)+"MB";
			}else{
				float f = (float)lsize/(1024);
				fsize = formater.format(f)+"KB";
			}
			
		}
		return fsize;
	}
	public static void main(String[] args) {
		System.out.println(isEmpty(null));
		System.out.println(convertSize("2211331243"));
	}
}
