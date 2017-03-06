package com.uq.util;

import java.util.UUID;

/**
 * 生成唯一的随机数
 * @author cp
 *
 */
public class UuidUtil {
	/**
	 * 生成32位的uuid
	 */
	public static String getUUID(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}
	

}
