package com.uq.util;

import java.io.IOException;
import java.util.Properties;

public class ProUtil {
private static Properties p = new Properties();
	
	static{
		try {
			p.load(RedisConfig.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			System.out.println("读取资源文件出错！");
			e.printStackTrace();
		}
	}


	public static String getString(String key) {
		return p.getProperty(key).trim();
	}
	
	public static int getInt(String key){
		return SUtil.formatStr(p.getProperty(key));
	}
}
