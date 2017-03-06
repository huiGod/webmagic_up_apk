package com.uq.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ImageUtil {

	//把介绍图片转为map的json格式
	public  static String makeJson(String[] imagePaths){		
		JSONArray jsonArray = new JSONArray();
			int k = 0;
			for (int i = 0; imagePaths!= null && i < imagePaths.length; i++) {
				Map<String, String> map = new HashMap<String, String>();
				String tmp = imagePaths[i];
				if(tmp!=null && !SUtil.isEmpty(tmp)){
					map.put("key", "img"+(k+1));				
					map.put("url", imagePaths[i].replace("\\", "/"));
					map.put("type", "101");
					k++;
				}
				
				jsonArray.add(map);
			}			
//		System.out.println("json:"+jsonArray.toString());
		return jsonArray.toString();
	}
	
	//把介绍图片转为map的json格式
	public  static String makeJson(List<String> imagePaths){		
		JSONArray jsonArray = new JSONArray();
			int k = 0;
			for (int i = 0; imagePaths!= null && i < imagePaths.size(); i++) {
				Map<String, String> map = new HashMap<String, String>();
				String tmp = imagePaths.get(i);
				if(tmp!=null && !SUtil.isEmpty(tmp)){
					map.put("key", "img"+(k+1));				
					map.put("url", imagePaths.get(i).replace("\\", "/"));
					map.put("type", "101");
					k++;
				}
				
				jsonArray.add(map);
			}			
//		System.out.println("json:"+jsonArray.toString());
		return jsonArray.toString();
	}
	
	//解析介绍图片的json字符串
	public static String[] decodeJson(String json){
		String[] images=null;
		if(json!= null && json.length()>5){
			JSONArray jsonArray = JSONArray.parseArray(json);
			Object[] array= jsonArray.toArray();
			images= new String[array.length];
			for(int i=0;i<array.length;i++){
//				JSONObject jsonObject= JSONObject.fromObject(array[i]);
//				System.out.println(array[i]);
				JSONObject jsonObject = JSONObject.parseObject(array[i].toString());
//				System.out.println(jsonObject.get("url"));
				images[i] = jsonObject.get("url").toString();
			}
//			System.out.println(array);
		}
		if(images == null){
			images = new String[0];
		}
		return images;
	} 
	
	//解析图标的json字符串
	public static String[] decodeIconJson(String iconJosn){
		List<String> l = new ArrayList<String>();		
		JSONObject iconObject = JSONObject.parseObject(iconJosn);
		Set<String> set = iconObject.keySet();
		for(String key:set){
//			System.out.println(key);
			l.add(iconObject.getString(key));
		}
		String[] a = l.toArray(new String[0]);
		return a;
		 /*for (Iterator iter = iconObject.keySet(); iter.hasNext();) {		     
			 String key = (String)iter.next();
			 //获取key的value
			  Object value = iconObject.get(key);
			  urlList.add(value.toString());
		}*/
	}
	public static void main(String[] args) {
		Jedis jedis = RedisUtil.getJedis();
		jedis.select(1);
		String json = jedis.hget("pkg:com.bianfeng.market", "remarkimages");
		System.out.println(json);
		decodeJson(json);
	}
}
