package com.uq.spider.qq;

import java.util.Set;

import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import com.uq.util.RedisConstant;
import com.uq.util.RedisUtil;
import com.uq.util.SUtil;

public class QQAppUpdateProcessor implements PageProcessor{

	private Site site = Site.me()
	.setCharset("UTF-8")
	.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0")	
    .setCycleRetryTimes(3).setSleepTime(1000).setUseGzip(true);
	
	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public void process(Page page) {
		String url = page.getUrl().get();
		if(url.contains("app/appdetai")){
			QQTool.getappdetail(page);
		}
	}

	public static void list_qq_update(){
		Spider spider = Spider.create(new QQAppUpdateProcessor());
		
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			Set<String> qqlist = jedis.smembers(RedisConstant.QQlist_list);
			int sum = 0;
			for(String pkgname :qqlist){
//				pkgname="com.zj.whackmole2";
				//过滤一下是否在360里边已经抓取了
				
				boolean b = jedis.sismember(RedisConstant.ALL_Remd, pkgname);//合作包
				if(!b && jedis.sismember(RedisConstant.S360_PKG_ALL, pkgname.toLowerCase()) ){//不是合作包 而且列表里边，则不采集
					continue;
				}
				
				int db_versioncode = SUtil.formatStr(jedis.hget(RedisConstant.ALL_APPS_PRE+pkgname.toLowerCase(), "versioncode"));//数据库的版本号
				int list_versioncode =  SUtil.formatStr(jedis.hget(RedisConstant.QQ_PKG+pkgname.toLowerCase(), "versioncode"));//抓取列表的版本号 版本号为0 表示没有抓取到
				if(list_versioncode>db_versioncode){
					System.out.println(pkgname+"==new version:"+list_versioncode+" old:"+db_versioncode);
					String url = "http://m5.qq.com/app/appdetail.htm?apkName="+pkgname;
					Request request= new Request(url);
					request.putExtra("islist", 1);				
					spider.addRequest(request);
					sum++;
				}
//				break;
			}
			System.out.println("qq列表要更新的数量："+sum);
			
		} catch (Exception e) {
		}finally{
			RedisUtil.returnResource(jedis);
		}
		spider.thread(30).run();
	}
	
	public static void remd_qq_update(){
		Spider spider = Spider.create(new QQAppUpdateProcessor());
		
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			Set<String> qqlist = jedis.smembers(RedisConstant.QQlist_remd);
			int sum = 0;
			for(String pkgname :qqlist){
				boolean b = jedis.sismember(RedisConstant.ALL_Remd, pkgname);//合作包
				if(!b && jedis.sismember(RedisConstant.S360_PKG_ALL, pkgname.toLowerCase()) ){//不是合作包 而且列表里边，则不采集
					continue;
				}
				if(b){
					String url = "http://m5.qq.com/app/appdetail.htm?apkName="+pkgname;
					Request request= new Request(url);
					request.putExtra("islist", 1);				
					spider.addRequest(request);
				}
				int db_versioncode = SUtil.formatStr(jedis.hget(RedisConstant.ALL_APPS_PRE+pkgname.toLowerCase(), "versioncode"));//数据库的版本号
				int list_versioncode =  SUtil.formatStr(jedis.hget(RedisConstant.QQ_PKG+pkgname.toLowerCase(), "versioncode"));//抓取列表的版本号 版本号为0 表示没有抓取到
				if(list_versioncode>db_versioncode){
					System.out.println(pkgname+"==new version:"+list_versioncode+" old:"+db_versioncode);
					String url = "http://m5.qq.com/app/appdetail.htm?apkName="+pkgname;
					Request request= new Request(url);
					request.putExtra("islist", 2);				
					spider.addRequest(request);
					sum++;
				}
			}
			System.out.println("qq列表在360中要更新的数量："+sum);
			
		} catch (Exception e) {
		}finally{
			RedisUtil.returnResource(jedis);
		}
		spider.thread(30).run();
	}
	
	public static void main(String[] args) {
//		list_qq_update();
		remd_qq_update(); 
	}
}
