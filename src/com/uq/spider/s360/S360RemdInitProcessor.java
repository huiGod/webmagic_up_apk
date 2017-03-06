package com.uq.spider.s360;

import java.util.Set;

import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import com.uq.util.RedisConstant;
import com.uq.util.RedisUtil;

/**
 * 
 * @ClassName: S360ListInitProcessor 
 * @Description: 抓取360下的分类列表的包名，以及推荐列表
 * @author aurong
 * @date 2015-5-14 上午10:16:40
 */
public class S360RemdInitProcessor implements PageProcessor{
	
	private Site site = Site
			.me()
			.setCharset("UTF-8")
			.setUserAgent("Mozilla/5.0 (Linux; Android 4.1.1; Nexus S 4G Build/JRO03C) AppleWebKit/537.9 (KHTML, like Gecko) Chrome/23.0.1260.0 Mobile Safari/537.9, 360appstore")
			.setCycleRetryTimes(3).setSleepTime(1000).setUseGzip(true); //.setRetryTimes(3)

	@Override
	public Site getSite() {
		return site;
	}
	
	@Override
	public void process(Page page) {
		String url = page.getUrl().get();
		if(url.contains("mintf/getRecommandAppsForDetail")){
			S360Tool.getRecommandAppsForDetail(page);
		}
	}


	public static void remd_job(){
		//从列表加载推荐链接
		Spider spider = Spider.create(new S360RemdInitProcessor());
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			Set<String> listSet = jedis.smembers(RedisConstant.S360List_list);
			for(String pkgname :listSet){
				jedis.sadd(RedisConstant.S360List_remd, pkgname.toLowerCase());
				Request recom_request = new Request();
				recom_request.setUrl("http://125.88.193.234/mintf/getRecommandAppsForDetail?png=1&pname="+pkgname+"&os=19&m2=4a2bf4b6aacbe5e3b53a094e80724a3d");
				recom_request.putExtra("pkg", pkgname);
				spider.addRequest(recom_request);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
		
		spider.thread(30).run();
	}
	

	
	public static void main(String[] args) {
		//从列表加载推荐链接
		Spider spider = Spider.create(new S360RemdInitProcessor());
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			Set<String> listSet = jedis.smembers(RedisConstant.S360List_list);
			for(String pkgname :listSet){
				jedis.sadd(RedisConstant.S360List_remd, pkgname.toLowerCase());
				Request recom_request = new Request();
				recom_request.setUrl("http://125.88.193.234/mintf/getRecommandAppsForDetail?png=1&pname="+pkgname+"&os=19&m2=4a2bf4b6aacbe5e3b53a094e80724a3d");
				recom_request.putExtra("pkg", pkgname);
				spider.addRequest(recom_request);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisUtil.returnResource(jedis);
		}
		
		spider.thread(30).run();
	}
}
