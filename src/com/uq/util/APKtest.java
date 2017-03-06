package com.uq.util;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.uq.base.db.util.DBExecutor;
import com.uq.dao.ApkdetailDao;
import com.uq.dao.impl.ApkdetailDaoImpl;
import com.uq.model.Apkdetail;
import com.uq.model.Apkupload;


public class APKtest {
	static DBExecutor db = new DBExecutor("com.mysql.jdbc.Driver", "jdbc:mysql://rdsi1vj4eblexsnqix0bfpublic.mysql.rds.aliyuncs.com:3306/i4android?useUnicode=true&characterEncoding=utf-8", "i4android", "I4Androidsefs9wefEfwe_efw_ee", "mysql");
	private static QueryRunner qr = new QueryRunner(db.getDataSource());
	
	private static ThreadPoolExecutor rsyncPool;
	private static Logger logger = LoggerFactory.getLogger(APKtest.class);
	
	private static void fileSyncThreadPoolInit() {
		logger.info("开始初始化同步文件需要的线程池...");
		rsyncPool = new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS,new ArrayBlockingQueue(500), new RejectedExecutionHandler() {
					public void rejectedExecution(Runnable r,ThreadPoolExecutor executor) {
						if (!(executor.isShutdown()))
							try {
								executor.getQueue().put(r);
							} catch (InterruptedException e) {
							}
					}
				});
	}
	
	//加载数据库的数据到redis
	//查询正式表app_detail_info 的信息

	public static void findApp_info(){
		String sql ="select packagename,apkurl,signature from app_detail_info  limit ?,?";
//		String sql ="select * from app_detail_info_temp where status = 1 limit ?,?";
		Integer page =0,pagesize = 5000;
		List apkList = new ArrayList();
		Jedis jedis = RedisUtil.getJedis();
		Pipeline p = jedis.pipelined();
		do {
			try {
				apkList = qr.query(sql, new Object[]{page*pagesize,pagesize}, new MapListHandler());
				page++;
				System.out.println("查询第几页："+page);
				for (int i = 0; i < apkList.size(); i++) {
					Map<String, Object> map = (Map)apkList.get(i);
					p.sadd("s360_temp", SUtil.converString(map.get("packagename")));
					p.hset("tepkg:"+map.get("packagename"), "apkurl", SUtil.converString(map.get("apkurl")));
					p.hset("tepkg:"+map.get("packagename"), "signature", SUtil.converString(map.get("signature")));
				}
				p.sync();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} while (apkList.size()>0);
		
		RedisUtil.returnResource(jedis);
	}
	

	public static void findApp_info_his(){
		String sql ="select packagename,apkurl,signature from app_detail_info_history  limit ?,?";
//		String sql ="select * from app_detail_info_temp where status = 1 limit ?,?";
		Integer page =0,pagesize = 5000;
		List apkList = new ArrayList();
		Jedis jedis = RedisUtil.getJedis();
		Pipeline p = jedis.pipelined();
		do {
			try {
				apkList = qr.query(sql, new Object[]{page*pagesize,pagesize}, new MapListHandler());
				page++;
				System.out.println("查询第几页："+page);
				for (int i = 0; i < apkList.size(); i++) {
					Map<String, Object> map = (Map)apkList.get(i);
					p.sadd("s360_temp_his", SUtil.converString(map.get("packagename")));
					p.hset("tepkghis:"+map.get("packagename"), "apkurl", SUtil.converString(map.get("apkurl")));
					p.hset("tepkghis:"+map.get("packagename"), "signature", SUtil.converString(map.get("signature")));
				}
				p.sync();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} while (apkList.size()>0);
		
		RedisUtil.returnResource(jedis);
	}
	
	
	public static void main(String[] args) {
//		findApp_info_his();
//		findApp_info();
//		preUpload("sda");
//		test();
//		test1();
//		test2();
//		test3();
//		test5();
		test6();
//		checkremd("e38e734fbcb24bab938400e072dc5611");
	}
	
	public static boolean checkremd(String download_uuid){
		fileSyncThreadPoolInit();
		String sql ="select * from app_upload a where a.uploadid = '"+download_uuid+"' limit ?,?";
		ApkdetailDao apkDao = new ApkdetailDaoImpl();
		List<Apkdetail> apksList =new ArrayList<Apkdetail>();
		apksList = apkDao.findApkdetails(sql, 0, 1000);
		try {
			for (int i = 0; i < apksList.size(); i++) {
				Apkdetail apk = null;
				Apkupload upload = new Apkupload();
				apk = apksList.get(i);
				System.out.println("检查："+apk.getPackagename()+" "+apk.getAppname());
				String apkurl = "z:/fileupload/"+apk.getApkUrl();
				if(new File(apkurl).exists()){
					Map<String,String> apkinfo =AnalysisApk.unZip(apkurl, "");
					rsyncPool.execute(new AnalysisApk_able(apkurl));
					System.out.println("线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			System.out.println("==关闭==");
			rsyncPool.shutdown();
			 if (!(rsyncPool.isTerminated()));
			 System.out.println("--------------");
			try {
				boolean Flag = true;
				do {
					Flag = !rsyncPool.awaitTermination(50L, TimeUnit.SECONDS);
					logger.error(download_uuid+" 线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
					System.out.println("uploadid:"+download_uuid+"total PoolSze："+rsyncPool.getPoolSize()+",waiting task size："+
							rsyncPool.getQueue().size()+",completed task："+rsyncPool.getCompletedTaskCount());
				} while (Flag);
				long end = new Date().getTime();
				logger.error("========真正完成任务-=");
				return true;
			} catch (InterruptedException e) {
				while (true) {
					logger.error("关闭rsyncPool出错:", e);
				}
			}
		}
	}
	/**
	 * 从数据库查询生成缩略图成功的批次号，统一上传,多线程任务执行完就返回true
	 */
	public static boolean preUpload(String download_uuid){
		
		fileSyncThreadPoolInit();
		long start = new Date().getTime();
		Jedis jedis = null;
		try {
				jedis = RedisUtil.getJedis();
				Set<String> s = jedis.smembers("s360_temp");
				int i =0;
				for(String pkgname : s){
					i++;
					String apkurl = jedis.hget("tepkg:"+pkgname, "apkurl");
//					System.out.println(pkgname+" "+apkurl);
					if(!SUtil.isEmpty(apkurl)){
//						System.out.println(pkgname +"  "+apkurl);
//						rsyncPool.execute(new AnalysisApk_able("/data/fileupload/"+apkurl));
//						System.out.println("==");
						rsyncPool.execute(new AnalysisApk_able("/data/fileupload/"+apkurl));
						System.out.println("线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
								rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
					}
//					if(i>5){break;}
					System.out.println(i);
					
					
//					rsyncPool.execute(new AnalysisApk_able(apkurl));
//					System.out.println("线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
//							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
				}						
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			System.out.println("==关闭==");
			RedisUtil.returnBrokenResource(jedis);
			rsyncPool.shutdown();
			 if (!(rsyncPool.isTerminated()));
			 System.out.println("--------------");
			try {
				boolean Flag = true;
				do {
					Flag = !rsyncPool.awaitTermination(50L, TimeUnit.SECONDS);
					logger.error(download_uuid+" 线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
					System.out.println("uploadid:"+download_uuid+"total PoolSze："+rsyncPool.getPoolSize()+",waiting task size："+
							rsyncPool.getQueue().size()+",completed task："+rsyncPool.getCompletedTaskCount());
				} while (Flag);
				long end = new Date().getTime();
				logger.error("批次："+download_uuid+" 上传总共耗时："+(end-start)/1000+" s");
				logger.error("========真正完成任务-=");
				return true;
			} catch (InterruptedException e) {
				while (true) {
					logger.error("关闭rsyncPool出错:", e);
				}
			}
		}
		
		
	}
	
	@Test
	public static  void test(){
		Jedis jedis = RedisUtil.getJedis();
		Set<String>  set = jedis.keys("new:*");
		for (String key:set) {
			String pkg = key.split(":")[1];
			String sign = jedis.hget(key, "sign");
//			System.out.println(sign);
			String dbsign =jedis.hget("tepkg:"+pkg, "signature");
			if(!dbsign.equals(sign)){
				System.out.println("===="+pkg);
			}
		}
//		System.out.println(jedis.keys("tepkg:*").size());
	}
	
	@Test
	public static void test1(){
		Jedis jedis = RedisUtil.getJedis();
		Set<String>  set = jedis.smembers("all_pkg");
		
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for(String pkgname :set){
			String sign = jedis.hget("new:"+pkgname, "sign");
			System.out.println(sign);
			i++;
			if(!SUtil.isEmpty(sign)){
				sb.append(" update app_detail_info app set signature = '"+sign+"' where app.packagename ='"+pkgname+"';\r\n");
			}
			if(i%1000 == 0){
				LogTest.savelog("c:/sign/sing"+i+".txt", sb.toString());
				sb.delete(0, sb.length());
			}
		}
		LogTest.savelog("c:/sign/sing"+i+".txt", sb.toString());
	}
	
	//测试有那个签名不一样的
	public static void test2(){
		Jedis jedis = RedisUtil.getJedis();
		Set<String>  set = jedis.smembers("all_pkg");
		
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for(String pkgname :set){
			String sign = jedis.hget("new:"+pkgname, "sign");
//			System.out.println(sign);
			String dbsign = jedis.hget("tepkg:"+pkgname, "signature");
			if(SUtil.isEmpty(dbsign)){
				System.out.println("db不存在这包名："+pkgname);
				continue;
			}
			if(!dbsign.equals(sign)){
				System.out.println(pkgname+" dbsign:"+dbsign+" sign:"+sign);
			}
		}
		
	}
	
	
	//测试有那个签名不一样的
	public static void test3(){
		Jedis jedis = RedisUtil.getJedis();
		jedis.sdiffstore("tt", "s360_temp","all_pkg");
		
	}
	
	//测试历史数据有那个签名不一样的
	public static void test5(){
		Jedis jedis = RedisUtil.getJedis();
		Set<String>  set = jedis.smembers("s360_temp_his");
		
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for(String pkgname :set){
			String sign = jedis.hget("new:"+pkgname, "sign");
//			System.out.println(sign);
			String dbsign = jedis.hget("tepkghis:"+pkgname, "signature");
			if(SUtil.isEmpty(dbsign)){
				System.out.println("db不存在这包名："+pkgname);
				continue;
			}
			if(!dbsign.equals(sign)){
				System.out.println(pkgname+" dbsign:"+dbsign+" sign:"+sign);
			}
		}
		
	}
	
	@Test
	public static void test6(){
		Jedis jedis = RedisUtil.getJedis();
		Set<String>  set = jedis.smembers("remdtmp");
		StringBuffer sb = new StringBuffer();
		StringBuffer sb1 = new StringBuffer();
		for(String pkg:set){
			sb.append("update app_upload set status = 10 where packagename ='"+pkg+"';\r\n");
			sb1.append("delete from app_detail_info_temp where packagename='"+pkg+"';\r\n");
		}
		LogTest.savelog("c:/testup.txt", sb.toString());
		LogTest.savelog("c:/testup_aa.txt", sb1.toString());
	}
}
