package com.uq.spider.qq;


import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/*
 * 抓取qq列表的包名
 */
public class QQListinitProcessor implements PageProcessor{

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
		if(url.contains("cate/cates")){
			QQTool.getcates(page);
		}else if(url.contains("cate/tags")){
			QQTool.getcateTags(page);
		}else if(url.contains("cate/appList")||url.contains("cate/tag/appList")){
			QQTool.getappList(page, false);
		}
	}

	public static void list_job(){
		Spider.create(new QQListinitProcessor())
		.addUrl("http://m5.qq.com/cate/cates.htm?orgame=2")//游戏分类
		.thread(30)
		.run();
	}
	
	public static void main(String[] args) {
		Spider.create(new QQListinitProcessor())
		.addUrl("http://m5.qq.com/cate/cates.htm?orgame=2")//软件分类
//		.addUrl("http://m5.qq.com/cate/tag/appList.htm?pageContext=400&pageSize=40&tagId=14701")
//		.addUrl("http://m5.qq.com/app/appdetail.htm?apkName=com.pocketmu.MoGoo.qjsj")
		.thread(30)
		.run();
	}
}
