package com.uq.spider.qq;

import java.util.Date;

import com.uq.dao.AppTypeDao;
import com.uq.dao.impl.AppTypeDaoImpl;
import com.uq.model.AppType;
import com.uq.util.SUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;

/**
 * 检查一下qq的分类是不是变了
 * ClassName: QQCateTest
 * @Description: TODO
 * @author aurong
 * @date 2015-12-1
 */
public class QQCateTest implements PageProcessor{

	private AppTypeDao typeDao = new AppTypeDaoImpl();
	
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
		System.out.println(page.getJson());
		Json json = page.getJson();
		String url = page.getUrl().toString();
		String catetype = page.getRequest().getExtra("type").toString();
		if(url.contains("cate/cates")){
			int size = json.jsonPath("$.obj[*].cateId").all().size();
			System.out.println(size);
			for (int i = 0; i < size; i++) {
				System.out.println(json.jsonPath("$.obj["+i+"].cateId")+"--"+json.jsonPath("$.obj["+i+"].cateName"));
				String cateid = json.jsonPath("$.obj["+i+"].cateId").get();
				String catename = json.jsonPath("$.obj["+i+"].cateName").get();
				//加入分类的小标签
				Request re = new Request();
				re.setUrl("http://m5.qq.com/cate/tags.htm?cateId="+cateid);
				re.putExtra("cateid", cateid);
				re.putExtra("catename", catename);
				re.putExtra("type", catetype);
				page.addTargetRequest(re);
				
			}
		}else if(url.contains("cate/tags")){
			int mainCateId = SUtil.formatStr(page.getRequest().getExtra("cateid").toString());
			String mainCateName = page.getRequest().getExtra("catename").toString();
			int k = json.jsonPath("$.obj[*].id").all().size();
			String sql = "select count(1) from apptype where sourceid = 'qq' and sort = ? ";		
			int t = (int)typeDao.count(sql, mainCateId);
			System.out.println("-----------t:"+t);
			if(t != 1){
				System.out.println("不存在" + mainCateName);
			}
			for (int i = 0; i < k; i++) {
				int  itemid = SUtil.formatStr(json.jsonPath("$.obj["+i+"].id"));
				String catename = json.jsonPath("$.obj["+i+"].name").get();
				int t1 = (int)typeDao.count(sql, itemid);
				System.out.println("-----------t1:"+t1);
				if(t1 != 1){
					System.out.println("不存在"+catename);
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		Request soft_re = new Request("http://m5.qq.com/cate/cates.htm?orgame=1");
		soft_re.putExtra("type", "soft");
		
		Request game_re = new Request("http://m5.qq.com/cate/cates.htm?orgame=2");
		game_re.putExtra("type", "game");
		
		Spider.create(new QQCateTest())
//		.addUrl("http://m5.qq.com/cate/cates.htm?orgame=1","http://m5.qq.com/cate/cates.htm?orgame=2")//游戏分类
		.addRequest(soft_re,game_re)
		.thread(1)
		.run();
	}
}
