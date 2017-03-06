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

/*
 * 抓取qq列表的推荐包名
 */
public class QQLRemdinitProcessor implements PageProcessor{

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
		/*if(url.contains("cate/cates")){
			QQTool.getcates(page);
		}else if(url.contains("cate/tags")){
			QQTool.getcateTags(page);
		}else if(url.contains("cate/appList")||url.contains("cate/tag/appList")){
			QQTool.getappList(page, false);
		}*/
		if(url.contains("app/ulikeapp")){
			QQTool.getulikeapp(page);
		}
	}

	public static void remd_job(){
		//从列表加载推荐链接
		Spider spider = Spider.create(new QQLRemdinitProcessor());
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			Set<String> listSet = jedis.smembers(RedisConstant.QQlist_list);
			for(String pkgname :listSet){
				jedis.sadd(RedisConstant.QQlist_remd, pkgname);
				Request recom_request = new Request();
				recom_request.setUrl("http://m5.qq.com/app/ulikeapp.htm?apkName="+pkgname);
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
		remd_job();
	}
}
