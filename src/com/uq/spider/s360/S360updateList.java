package com.uq.spider.s360;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import com.uq.util.RedisConstant;
import com.uq.util.RedisUtil;
import com.uq.util.SUtil;

/**
 * 第二步，更新抓取的列表
 * ClassName: S360updateList
 * @Description: TODO
 * @author aurong
 * @date 2015-12-1
 */
public class S360updateList implements PageProcessor{

	private Site site = Site
	.me()
	.setCharset("UTF-8")
	.setUserAgent("Mozilla/5.0 (Linux; Android 4.1.1; Nexus S 4G Build/JRO03C) AppleWebKit/537.9 (KHTML, like Gecko) Chrome/23.0.1260.0 Mobile Safari/537.9, 360appstore")
	.setCycleRetryTimes(3).setSleepTime(1000).setUseGzip(true); //.setRetryTimes(3)
	
	private static Logger log = LoggerFactory.getLogger(S360updateList.class);
	
	private boolean remdFlag = true; //默认不抓取360的合作包
	
	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public void process(Page page) {
		String url = page.getUrl().get();
		if(url.contains("mintf/getAppInfoByIds")){
			S360Tool.getAppinfo(page,remdFlag);
		}
	}
	
	//更新360列表的应用
	public static void list_360_update(){
		Spider spider = Spider.create(new S360updateList());
		
		//先抓取列表中的应用
		Jedis jedis = null;
		//Set<String> db360list = jedis.smembers(RedisConstant.S360_PKG_ALL);
		int sum = 0;
		try {
			jedis = RedisUtil.getJedis();
			//更新列表的
			Set<String> s360list = jedis.smembers(RedisConstant.S360List_list);
			for(String pkgname:s360list){	
				if(jedis.sismember(RedisConstant.QQ_PKG_ALL_Min, pkgname.toLowerCase())){//包名从qq抓过来的话，后期还是直接从qq去更新
					continue;
				}
				int db_versioncode = SUtil.formatStr(jedis.hget(RedisConstant.ALL_APPS_PRE+pkgname.toLowerCase(), "versioncode"));//数据库的版本号
				int list_versioncode =  SUtil.formatStr(jedis.hget(RedisConstant.S360_PKG+pkgname.toLowerCase(), "versioncode"));//抓取列表的版本号 版本号为0 表示没有抓取到
				if(list_versioncode>db_versioncode){
					if(db_versioncode>0){
						System.out.println("要更新的："+pkgname+"==new version:"+list_versioncode+" old:"+db_versioncode);
					}else {
						System.out.println("新增的:"+pkgname+"==new version:"+list_versioncode+" old:"+db_versioncode);
					}
					
					String url = "http://125.88.193.234/mintf/getAppInfoByIds?isdes=1&png=1&sort=1&pname="+pkgname+"&market_id=360market&ppi=480_800&pos=0&ad_code=0&fm=cati_cid2_tag%E4%BC%91%E9%97%B2%E7%9B%8A%E6%99%BA_3&m=11df8f077fe25449eb6e9ce5fc375128&m2=f76b61aaef73a318164427e84bb8afb3&v=3.2.16&re=1&ch=100130&os=16&model=GT-N7105&sn=4.589389937671455&cu=smdk4x12&startCount=22&snt=-1";
					Request request= new Request(url);
					request.putExtra("islist", 1);				
					spider.addRequest(request);					
					sum++;
					System.out.println("第几个："+sum);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
		System.out.println("列表要更新的数量："+sum);
		log.error("360自身列表要更新的数量："+sum);
		spider.thread(30).run();
	}
	
	public static void list_qq_update(){
		S360updateList qqtest = new S360updateList();
		qqtest.remdFlag = false;
		Spider spider = Spider.create(qqtest);
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			Set<String> qqlist = jedis.smembers(RedisConstant.QQlist_list);
			int sum = 0;
			for(String pkgname :qqlist){
//				pkgname ="com.zlgame.batianxia.wdj";
				System.out.println(pkgname);
				//过滤掉以前从qq抓取的应用，免得签名不一样(第一次不会从qq找到)
				if(jedis.sismember(RedisConstant.QQ_PKG_ALL_Min, pkgname.toLowerCase())){
					continue;//跳过原来从qq抓取的，后期更新也只能从qq里边更新
				}
//				int db_versioncode = SUtil.formatStr(jedis.hget(RedisConstant.ALL_APPS_PRE+pkgname.toLowerCase(), "versioncode"));//数据库的版本号
//				int list_versioncode =  SUtil.formatStr(jedis.hget(RedisConstant.QQ_PKG+pkgname.toLowerCase(), "versioncode"));//抓取列表的版本号 版本号为0 表示没有抓取到
//				System.out.println(list_versioncode+" "+db_versioncode);
//				if(list_versioncode>db_versioncode){
//					System.out.println(pkgname+"==new version:"+list_versioncode+" old:"+db_versioncode);
					String url = "http://125.88.193.234/mintf/getAppInfoByIds?isdes=1&png=1&sort=1&pname="+pkgname+"&market_id=360market&ppi=480_800&pos=0&ad_code=0&fm=cati_cid2_tag%E4%BC%91%E9%97%B2%E7%9B%8A%E6%99%BA_3&m=11df8f077fe25449eb6e9ce5fc375128&m2=f76b61aaef73a318164427e84bb8afb3&v=3.2.16&re=1&ch=100130&os=16&model=GT-N7105&sn=4.589389937671455&cu=smdk4x12&startCount=22&snt=-1";
					Request request= new Request(url);
					request.putExtra("islist", 1);				
					spider.addRequest(request);
					sum++;
//				}
//				break;
			}
			System.out.println("qq列表在360中要更新的数量："+sum);
			
		} catch (Exception e) {
		}finally{
			RedisUtil.returnResource(jedis);
		}
//		spider.thread(30).run();
	}
	
	//更新360推荐应用的
	public static void remd_360_update(){
		Spider spider = Spider.create(new S360updateList());
		
		//先更新数据库里边的应用
		Jedis jedis = null;
		//		Set<String> db360list = jedis.smembers(RedisConstant.S360_PKG_ALL);
		int sum = 0;
		try {
			jedis = RedisUtil.getJedis();
			//更新列表的
			Set<String> s360_remdlist = jedis.smembers(RedisConstant.S360List_remd);
			for(String pkgname:s360_remdlist){
				if(jedis.sismember(RedisConstant.QQ_PKG_ALL_Min, pkgname.toLowerCase())){
					continue;//跳过原来从qq抓取的，后期更新也只能从qq里边更新
				}				
				int db_versioncode = SUtil.formatStr(jedis.hget(RedisConstant.ALL_APPS_PRE+pkgname.toLowerCase(), "versioncode"));//数据库的版本号
				int list_versioncode =  SUtil.formatStr(jedis.hget(RedisConstant.S360_PKG+pkgname.toLowerCase(), "versioncode"));//抓取列表的版本号 版本号为0 表示没有抓取到
				if(list_versioncode>db_versioncode){
					System.out.println(pkgname+"==new version:"+list_versioncode+" old:"+db_versioncode);
					String url = "http://125.88.193.234/mintf/getAppInfoByIds?isdes=1&png=1&sort=1&pname="+pkgname+"&market_id=360market&ppi=480_800&pos=0&ad_code=0&fm=cati_cid2_tag%E4%BC%91%E9%97%B2%E7%9B%8A%E6%99%BA_3&m=11df8f077fe25449eb6e9ce5fc375128&m2=f76b61aaef73a318164427e84bb8afb3&v=3.2.16&re=1&ch=100130&os=16&model=GT-N7105&sn=4.589389937671455&cu=smdk4x12&startCount=22&snt=-1";
					Request request= new Request(url);
//					request.putExtra("islist", 2);	//不写就是不在列表的			
					spider.addRequest(request);
					sum++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
		System.out.println("列表要更新的数量："+sum);
		spider.thread(30).run();
	}
	
	//在360更新qq推荐应用列表
	public static void remd_qq_update(){
		S360updateList qqtest = new S360updateList();
		qqtest.remdFlag = false;
		Spider spider = Spider.create(qqtest);	
		
		Jedis jedis = null;
		int sum = 0;
		try {
			jedis = RedisUtil.getJedis();
			//更新列表的
			Set<String> qq_remdlist = jedis.smembers(RedisConstant.QQlist_remd);
			for(String pkgname:qq_remdlist){
				if(jedis.sismember(RedisConstant.QQ_PKG_ALL_Min, pkgname.toLowerCase())){
					continue;//跳过原来从qq抓取的，后期更新也只能从qq里边更新
				}
//				int db_versioncode = SUtil.formatStr(jedis.hget(RedisConstant.ALL_APPS_PRE+pkgname.toLowerCase(), "versioncode"));//数据库的版本号
//				int list_versioncode =  SUtil.formatStr(jedis.hget(RedisConstant.QQ_PKG+pkgname.toLowerCase(), "versioncode"));//抓取列表的版本号 版本号为0 表示没有抓取到
//				if(list_versioncode>db_versioncode){
//					System.out.println(pkgname+"==new version:"+list_versioncode+" old:"+db_versioncode);
					String url = "http://125.88.193.234/mintf/getAppInfoByIds?isdes=1&png=1&sort=1&pname="+pkgname+"&market_id=360market&ppi=480_800&pos=0&ad_code=0&fm=cati_cid2_tag%E4%BC%91%E9%97%B2%E7%9B%8A%E6%99%BA_3&m=11df8f077fe25449eb6e9ce5fc375128&m2=f76b61aaef73a318164427e84bb8afb3&v=3.2.16&re=1&ch=100130&os=16&model=GT-N7105&sn=4.589389937671455&cu=smdk4x12&startCount=22&snt=-1";
					Request request= new Request(url);
//					request.putExtra("islist", 2);				
					spider.addRequest(request);
					sum++;
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
		System.out.println("列表要更新的数量："+sum);
		spider.thread(30).run();
	}
	
	//更新数据库360所有的应用
	public static void self_360_update(){
		Spider spider = Spider.create(new S360updateList());
		Jedis jedis = null;
		int sum = 0;
		try {
			jedis = RedisUtil.getJedis();
			Set<String> db360list = jedis.smembers(RedisConstant.S360_PKG_ALL);
			for(String pkgname : db360list){
				if(jedis.sismember(RedisConstant.QQ_PKG_ALL_Min, pkgname.toLowerCase())){//包名从qq抓过来的话，后期还是直接从qq去更新
					continue;
				}
				//合作包和垃圾包，不去更新
				if(jedis.sismember(RedisConstant.ALL_Remd, pkgname.toLowerCase()) || jedis.sismember(RedisConstant.ALL_UN_REMD, pkgname.toLowerCase())){
					continue;
				}
				int db_versioncode = SUtil.formatStr(jedis.hget(RedisConstant.ALL_APPS_PRE+pkgname.toLowerCase(), "versioncode"));//数据库的版本号
				int list_versioncode =  SUtil.formatStr(jedis.hget(RedisConstant.S360_PKG+pkgname.toLowerCase(), "versioncode"));//抓取列表的版本号 版本号为0 表示没有抓取到
				if(list_versioncode>db_versioncode || list_versioncode == 0){
					System.out.println(pkgname+"==new version:"+list_versioncode+" old:"+db_versioncode);
					//360 访问不区分包名的大小写，qq区分
					String url = "http://125.88.193.234/mintf/getAppInfoByIds?isdes=1&png=1&sort=1&pname="+pkgname+"&market_id=360market&ppi=480_800&pos=0&ad_code=0&fm=cati_cid2_tag%E4%BC%91%E9%97%B2%E7%9B%8A%E6%99%BA_3&m=11df8f077fe25449eb6e9ce5fc375128&m2=f76b61aaef73a318164427e84bb8afb3&v=3.2.16&re=1&ch=100130&os=16&model=GT-N7105&sn=4.589389937671455&cu=smdk4x12&startCount=22&snt=-1";
					Request request= new Request(url);
					request.putExtra("islist", 1);				
					spider.addRequest(request);
					sum++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
		log.error("360数据库列表要更新的数量："+sum);
		spider.thread(30).run();		
	}
	
	public static void main(String[] args) {
		list_360_update();
//		self_360_update();
//		list_qq_update();
//		remd_qq_update();
	}
	
}
