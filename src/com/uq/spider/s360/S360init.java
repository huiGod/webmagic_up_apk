package com.uq.spider.s360;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 第一步，抓取列表中的数据，保存到redis
 * ClassName: S360init
 * @Description: TODO
 * @author aurong
 * @date 2015-12-1
 */
public class S360init implements PageProcessor{

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
		if(url.contains("app/getCatTags/cid")){
			S360Tool.getCatTags(page);
		}else if(url.contains("app/list/cid")){
			S360Tool.getList(page,false);//暂时不请求推荐应用
		}
	}

	/**
	 * 抓取360的各小分类的列表
	 * @Description: TODO
	 * @param    
	 * @return void  
	 * @throws
	 * @author aurong
	 * @date 2015-11-30
	 */
	public static void list_job(){
		String softurl ="http://125.88.193.234/app/getCatTags/cid/1?ver_type=1&os=19&png=1&fm=sf004&m=a9194cad661042c6e595c8fa1485532e&m2=4a2bf4b6aacbe5e3b53a094e80724a3d&v=3.2.23&re=1&ch=100130&model=X9077&sn=4.589389937671455&cu=qualcomm+msm+8974+%28flattened+device+tree%29&ppi=1440x2560&startCount=47";
		
		String gameurl ="http://125.88.193.234/app/getCatTags/cid/2?ver_type=1&os=19&png=1&fm=gm004&m=a9194cad661042c6e595c8fa1485532e&m2=4a2bf4b6aacbe5e3b53a094e80724a3d&v=3.2.23&re=1&ch=100130&model=X9077&sn=4.589389937671455&cu=qualcomm+msm+8974+%28flattened+device+tree%29&ppi=1440x2560&startCount=45";
		Request game_request =new Request(gameurl);
		game_request.putExtra("type", "game");
		
		Request soft_request =new Request(softurl);
		soft_request.putExtra("type", "soft");
		
		Spider.create(new S360init())
		.addRequest(soft_request,game_request)//game_request
		.thread(30)
		.run();
	}
	
	public static void main(String[] args) {
		list_job();
	}
}
