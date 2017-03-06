package com.uq.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.uq.dao.ApkdetailDao;
import com.uq.dao.CateAppDao;
import com.uq.dao.OtherapksDao;
import com.uq.dao.PaperDao;
import com.uq.dao.PaperForTypeDao;
import com.uq.dao.RemdInfoDao;
import com.uq.dao.impl.ApkdetailDaoImpl;
import com.uq.dao.impl.CateAppDaoImpl;
import com.uq.dao.impl.OtherapksDaoImpl;
import com.uq.dao.impl.PaperDaoImpl;
import com.uq.dao.impl.PaperForTypeDaoImpl;
import com.uq.dao.impl.RemdInfoDaoImpl;
import com.uq.model.Apkdetail;
import com.uq.model.CateApp;
import com.uq.model.Otherapks;
import com.uq.model.Paper;
import com.uq.model.PaperForType;
import com.uq.model.RemdInfo;

public class RedisTool {
	
	public static void init(){
//		flushdb();
		addApkinfo();
		addCateinfo();
		addRemdinfo();
		addlikeAppinfo();
	}
	
	public static void initAndClear(){
//		flushdb();
		flushTmp();
		addApkinfo();
		addCateinfo();
		addRemdinfo();
		addlikeAppinfo();
	}
	
	//清空redis
	public static void flushdb(){
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			jedis.flushDB();
			Thread.sleep(1000*30);
		} catch (Exception e) {
		}finally{
			RedisUtil.returnResource(jedis);
		}
	}
	//清空上传采集遗留的信息，不清除原来从数据库加载的东西
	public static void flushTmp(){
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			jedis.del(RedisConstant.S360List_list);//删除抓取360的列表
			jedis.del(RedisConstant.S360List_remd);
			Set<String> keys = jedis.keys(RedisConstant.S360_PKG + "*");
			if(keys !=null && keys.size()>0){
				jedis.del(keys.toArray(new String[] {}));
			}
			jedis.del(RedisConstant.QQlist_list);
			jedis.del(RedisConstant.QQlist_remd);
			
			Set<String> keys1 = jedis.keys(RedisConstant.QQ_PKG + "*");
			if(keys1 !=null && keys1.size()>0){
				jedis.del(keys1.toArray(new String[] {}));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
	}
	
	//加载数据库的apk信息到redis中
	public static void addApkinfo(){
		
		Jedis jedis = null;
		Pipeline p = null;
		
		String sql ="select packagename,versioncode,source from app_detail_info app limit ?,? ";
		try {
			jedis = RedisUtil.getJedis();
			p = jedis.pipelined();
			// 先删除原来存在的
			/*Set<String> keys = jedis.keys(RedisConstant.ALL_APPS_PRE + "*");
			if(keys !=null && keys.size()>0){
				p.del(keys.toArray(new String[] {}));
				p.sync();
			}*/
						
			ApkdetailDao apkDao = new ApkdetailDaoImpl();
			Integer page = 0,pageSize = 5000;
			List<Apkdetail> l = new ArrayList<Apkdetail>();
			
			do {
				l  = apkDao.query(sql, new Object[]{page*pageSize,pageSize});
				page++;
				System.out.println("当前第"+page+"页!");
				for(Apkdetail apk:l){
					if(apk.getPackagename() !=null && !SUtil.isEmpty(apk.getPackagename())){
						try {
							p.hset(RedisConstant.ALL_APPS_PRE+apk.getPackagename().toLowerCase(),"versioncode", SUtil.formatInt(apk.getVersioncode()));
							String key = "";
							if(apk.getSource().equals("360")){
								key = RedisConstant.S360_PKG_ALL;
								p.sadd(key, apk.getPackagename().toLowerCase());//包名设为小写
							}else if(apk.getSource().equals("qq")){
								key = RedisConstant.QQ_PKG_ALL;
								p.sadd(key, apk.getPackagename());//原生包名，不做修改
								p.sadd(RedisConstant.QQ_PKG_ALL_Min, apk.getPackagename().toLowerCase());//包名设为小写
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					
				}
				p.sync();
//				break;
			} while (l.size()>0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
	}
	
	/**
	 * 加载数据库的分类信息到redis的set中
	 * 
	 */
	public static void addCateinfo(){
		Jedis jedis = null;
		Pipeline p = null;
		
		String sql ="select cateid,packagename,source from cate_app  limit ?,? ";
		try {
			jedis = RedisUtil.getJedis();
			p = jedis.pipelined();
			// 先删除原来存在的
			/*Set<String> keys = jedis.keys(RedisConstant.ALL_Cate + "*");
			if(keys !=null && keys.size()>0){
				p.del(keys.toArray(new String[] {}));
				p.sync();
			}*/
						
			CateAppDao cateAppDao = new CateAppDaoImpl();
			Integer page = 0,pageSize = 5000;
			List<CateApp> l = new ArrayList<CateApp>();
			
			do {
				l  = cateAppDao.query(sql, new Object[]{page*pageSize,pageSize});
				page++;
				System.out.println("获取当前分类第"+page+"页!");
				for(CateApp cate:l){
					if(cate.getCateid()!=null){
						p.sadd(RedisConstant.ALL_Cate+cate.getCateid(), cate.getPackagename().toLowerCase());
					}
					if(cate.getSource().equalsIgnoreCase("qq")){
						p.sadd("qqtmp", cate.getPackagename().toLowerCase());
						System.out.println(cate.getPackagename());
					}
					
				}
				p.sync();
//				break;
			} while (l.size()>0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
	}
	
	/**
	 * 
	 * @Description: 加载360的合作包信息和不采集的apk包名
	 * @param @param args
	 * @date 2015-6-19
	 * @author	aurong
	 * @return void
	 */
	public static void addRemdinfo(){
		Jedis jedis = null;
		Pipeline p = null;
		
		String sql ="select * from remdinfo  limit ?,? ";
		try {
			jedis = RedisUtil.getJedis();
			p = jedis.pipelined();
//			jedis.del(RedisConstant.ALL_Remd);
//			jedis.del(RedisConstant.ALL_UN_REMD);
			RemdInfoDao remdInfoDao = new RemdInfoDaoImpl();
			Integer page = 0,pageSize = 5000;
			List<RemdInfo> l = new ArrayList<RemdInfo>();
			
			do {
				l  = remdInfoDao.query(sql, new Object[]{page*pageSize,pageSize});
				page++;
				System.out.println("获取当前合作包第"+page+"页!");
				for(RemdInfo remd:l){
					System.out.println(remd.getPackagename()+" "+remd.getType());
					if(remd.getPackagename()!=null && remd.getType()!=null && remd.getType().equals(1)){
						p.sadd(RedisConstant.ALL_Remd, remd.getPackagename().toLowerCase());
					}else if(remd.getPackagename()!=null) {
						p.sadd(RedisConstant.ALL_UN_REMD, remd.getPackagename().toLowerCase());
					}
					
				}
				p.sync();
//				break;
			} while (l.size()>0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
	}
	
	//加载各应用市场的想关推荐
	public static void addlikeAppinfo(){
		Jedis jedis = null;
		Pipeline p = null;
		
		String sql ="select packagename,source from otherapks  limit ?,? ";
		try {
			jedis = RedisUtil.getJedis();
			p = jedis.pipelined();	
			// 先删除原来存在的
			/*Set<String> keys = jedis.keys(RedisConstant.ALL_Like + "*");
			if(keys !=null && keys.size()>0){
				p.del(keys.toArray(new String[] {}));
				p.sync();
			}*/
			
			OtherapksDao otherapksDao = new OtherapksDaoImpl();
			Integer page = 0,pageSize = 5000;
			List<Otherapks> l = new ArrayList<Otherapks>();
			
			do {
				l  = otherapksDao.query(sql, new Object[]{page*pageSize,pageSize});
				page++;
				System.out.println("获取当前推荐应用第"+page+"页!");
				for(Otherapks otherapk:l){
					if(otherapk.getPackagename()!=null){
						p.sadd(RedisConstant.ALL_Like+otherapk.getSource()	, otherapk.getPackagename().toLowerCase());
					}
					
				}
				p.sync();
//				break;
			} while (l.size()>0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
	}
	
	//加载壁纸资源
	public static void addPaperInfo(){
		Jedis jedis = null;
		Pipeline p = null;
		String sql ="select * from paper limit ?,?";
		try {
			jedis = RedisUtil.getJedis();
			p = jedis.pipelined();
			PaperDao paperDao = new PaperDaoImpl();
			Integer page = 0,pagesize = 5000;
			List<Paper> l = new ArrayList<Paper>();
			do {
				l = paperDao.findAllList(sql, page, pagesize);
				page++;
				for(Paper paper:l){
					if(paper.getSource().equals("wdj")){
						p.sadd(RedisConstant.PAPER_WDJ, SUtil.converString(paper.getThirdid()));
					}
				}
				p.sync();
			} while (l.size()>0);
		} catch (Exception e) {
		}finally{
			RedisUtil.returnResource(jedis);
		}
	}
	
	//加载壁纸分类
	public static void addPaperType(){
		Jedis jedis = null;
		Pipeline p = null;
		String sql ="select * from paperfortype limit ?,?";
		try {
			jedis = RedisUtil.getJedis();
			p = jedis.pipelined();
			PaperForTypeDao paperDao = new PaperForTypeDaoImpl();
			Integer page = 0,pagesize = 5000;
			List<PaperForType> l = new ArrayList<PaperForType>();
			do {
				l = paperDao.findAllList(sql, page, pagesize);
				page++;
				for(PaperForType type:l){
					if(type.getSource().equals("wdj")){
						p.sadd(RedisConstant.PAPER_CATE_WDJ+type.getTypeid(), SUtil.converString(type.getPaperid()));
					}
				}
				p.sync();
			} while (l.size()>0);
		} catch (Exception e) {
		}finally{
			RedisUtil.returnResource(jedis);
		}
	}
	public static void main(String[] args) {
//		addApkinfo();
//		addCateinfo();
//		addRemdinfo();
//		addlikeAppinfo();
//		init();
//		addCateinfo();
//		addRemdinfo();
//		initAndClear();
		addPaperInfo();
		addPaperType();
	}
	
}
