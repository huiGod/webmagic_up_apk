package com.uq.util;

public class LanguageUtil {

	//判读是否包含汉字
	public static boolean containsChinese(String s) {
	    if ((s == null) || ("".equals(s.trim())))
	      return false;
	    for (int i = 0; i < s.length(); ++i)
	      if (isChinese(s.charAt(i)))
	        return true;

	    return false;
	  }

	//判断是否包含英文字母
	public static boolean containsEnglish(String s){
		if ((s == null) || ("".equals(s.trim())))
		      return false;
		for (int i = 0; i < s.length(); ++i)
		      if (Character.isLetter(s.charAt(i)))
		        return true;
		return false;
	}
	
	public static boolean isChinese(char a) {
	    int v = a;
	    return ((v >= 19968) && (v <= 171941));
	  }
	
	public static void main(String[] args) {
		System.out.println(containsChinese("sad萨队s"));
	}
}
