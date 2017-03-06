package com.uq.spider.s360;

import java.util.Date;
import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;

import com.uq.dao.AppTypeDao;
import com.uq.dao.impl.AppTypeDaoImpl;
import com.uq.model.AppType;

/**
 * 抓取准备工作：抓取360手机助手的分类信息
 * 
 * @author aurong
 * 
 */
public class S360cateProcessor implements PageProcessor {

	private int softapptypeid = 101;
	private int gameapptypeid = 301;
	private AppTypeDao typeDao = new AppTypeDaoImpl();
	
	private Site site = Site
			.me()
			.setCharset("UTF-8")
			.setUserAgent("Mozilla/5.0 (Linux; Android 4.4.4; X9077 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36;360appstore")
			.setRetryTimes(3).setSleepTime(1000).setUseGzip(true);

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public void process(Page page) {
		System.out.println(page.getJson());
		Json json = page.getJson();
		if(page.getUrl().toString().contains("app/getCatTags/cid/1")){//应用分类
			int size = json.jsonPath("$.data[*].title").all().size();
			System.out.println("==========="+size);
			System.out.println(json.jsonPath("$.data[8].title"));
			for (int i = 8; i < size; i++) {
				System.out.println(json.jsonPath("$.data["+i+"].title"));
				String title = json.jsonPath("$.data["+i+"].title").get();
				//大的分类
				int typeid = softapptypeid;
				AppType type = new AppType();
				type.setApptypeid(softapptypeid);
				type.setName(title);
				type.setStatus(1);
				type.setType("soft");
				type.setNamecolor(json.jsonPath("$.data["+i+"].char_color").get());
				type.setIconurl(json.jsonPath("$.data["+i+"].logo").get());
				type.setSourceid("360");
				type.setPid(1);
				typeDao.save(type);
				softapptypeid++;
				List<String> cate2 = json.jsonPath("$.data["+i+"].title2").all();
				for (int j = 0; j < cate2.size(); j++) {//保存应用标签
					String title2 = cate2.get(j);
					type.setName(title2);
					type.setPid(typeid);
					type.setIconurl("");
					type.setNamecolor("");
					type.setCreatetime(new Date());
					type.setUpdatetime(new Date());
					type.setApptypeid(softapptypeid);
					typeDao.save(type);
					softapptypeid++;
				}
				System.out.println(json.jsonPath("$.data["+i+"].title2").all());
			}
		}else if(page.getUrl().toString().contains("app/getCatTags/cid/2")){//游戏分类
			int size = json.jsonPath("$.data[*].title").all().size();
			System.out.println("==========="+size);
			System.out.println(json.jsonPath("$.data[8].title"));
			for (int i = 8; i < size; i++) {
				if(i ==(size -1)){//最后一个分类(小编精选)不算分类，过滤掉
					break;
				}
				System.out.println(json.jsonPath("$.data["+i+"].title"));
				String title = json.jsonPath("$.data["+i+"].title").get();
				//大的分类
				int typeid = gameapptypeid;
				AppType type = new AppType();
				type.setApptypeid(gameapptypeid);
				type.setName(title);
				type.setStatus(1);
				type.setType("game");
				type.setNamecolor(json.jsonPath("$.data["+i+"].char_color").get());
				type.setIconurl(json.jsonPath("$.data["+i+"].logo").get());
				type.setSourceid("360");
				type.setPid(2);
				typeDao.save(type);
				gameapptypeid++;
				List<String> cate2 = json.jsonPath("$.data["+i+"].title2").all();
				for (int j = 0; j < cate2.size(); j++) {//保存游戏标签
					String title2 = cate2.get(j);
					System.out.println(title2);
					type.setName(title2);
					type.setPid(typeid);
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
		
	}

	public static void main(String[] args) {
		String softCateUrl ="http://125.88.193.234/app/getCatTags/cid/1?ver_type=1&os=19&png=1&fm=sf004&m=a9194cad661042c6e595c8fa1485532e&m2=4a2bf4b6aacbe5e3b53a094e80724a3d&v=3.2.23&re=1&ch=100130&model=X9077&sn=4.589389937671455&cu=qualcomm+msm+8974+%28flattened+device+tree%29&ppi=1440x2560&startCount=39";
		String gameCateUrl ="http://125.88.193.234/app/getCatTags/cid/2?ver_type=1&os=19&png=1&fm=gm004&m=a9194cad661042c6e595c8fa1485532e&m2=4a2bf4b6aacbe5e3b53a094e80724a3d&v=3.2.23&re=1&ch=100130&model=X9077&sn=4.589389937671455&cu=qualcomm+msm+8974+%28flattened+device+tree%29&ppi=1440x2560&startCount=39";
		Spider.create(new S360cateProcessor())
			.addUrl(softCateUrl,gameCateUrl)
//			.addUrl(gameCateUrl)
			.thread(1)
			.run();
	}
}
