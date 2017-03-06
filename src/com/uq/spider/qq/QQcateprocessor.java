package com.uq.spider.qq;

import java.util.Date;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;

import com.uq.dao.AppTypeDao;
import com.uq.dao.impl.AppTypeDaoImpl;
import com.uq.model.AppType;
import com.uq.util.SUtil;

/**
 * 目前只从qq抓取游戏信息
 * @ClassName: QQcateprocessor 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author aurong
 * @date 2015-5-21 下午03:18:43
 */
public class QQcateprocessor implements PageProcessor{

	private AppTypeDao typeDao = new AppTypeDaoImpl();
	
	private Site site = Site.me()
	.setCharset("UTF-8")
	.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0")	
    .setCycleRetryTimes(3).setSleepTime(1000).setUseGzip(true);
	
	private int gameapptypeid = 2001;//自定义的分类
	
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
			System.out.println(mainCateId+" == "+mainCateName);
			int softtag = catetype.equals("soft")?1:2;
			int k = json.jsonPath("$.obj[*].id").all().size();
			AppType type = new AppType();
			int maintypeid = gameapptypeid;
			type.setApptypeid(gameapptypeid);
			type.setName(mainCateName);
			type.setStatus(1);
			type.setSort(mainCateId);//qq自己的id，后期备用
			type.setType(catetype);
			type.setNamecolor("");
			type.setIconurl("");
			type.setSourceid("qq");
			type.setPid(softtag);
			typeDao.save(type);
			gameapptypeid++;
			
			for (int i = 0; i < k; i++) {
				int  itemid = SUtil.formatStr(json.jsonPath("$.obj["+i+"].id"));
				String catename = json.jsonPath("$.obj["+i+"].name").get();
				System.out.print(catename+" ");
				type.setName(catename);
				type.setPid(maintypeid);
				type.setSort(itemid);//qq自己的id，后期备用
				type.setType(catetype);
				type.setIconurl("");
				type.setNamecolor("");
				type.setCreatetime(new Date());
				type.setUpdatetime(new Date());
				type.setApptypeid(gameapptypeid);
				typeDao.save(type);
				gameapptypeid++;
			}
		}
		
	}
	
	public static void main(String[] args) {
		Request soft_re = new Request("http://m5.qq.com/cate/cates.htm?orgame=1");
		soft_re.putExtra("type", "soft");
		
		Request game_re = new Request("http://m5.qq.com/cate/cates.htm?orgame=2");
		game_re.putExtra("type", "game");
		
		Spider.create(new QQcateprocessor())
//		.addUrl("http://m5.qq.com/cate/cates.htm?orgame=1","http://m5.qq.com/cate/cates.htm?orgame=2")//游戏分类
		.addRequest(soft_re,game_re)
		.thread(1)
		.run();
	}
}
