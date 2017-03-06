package com.uq.spider.qq;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.Json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uq.dao.ApkdetailDao;
import com.uq.dao.AppTypeDao;
import com.uq.dao.CateAppDao;
import com.uq.dao.OtherapksDao;
import com.uq.dao.impl.ApkdetailDaoImpl;
import com.uq.dao.impl.AppTypeDaoImpl;
import com.uq.dao.impl.CateAppDaoImpl;
import com.uq.dao.impl.OtherapksDaoImpl;
import com.uq.model.Apkdetail;
import com.uq.model.AppType;
import com.uq.model.CateApp;
import com.uq.model.Otherapks;
import com.uq.util.CRequest;
import com.uq.util.LanguageUtil;
import com.uq.util.RedisConstant;
import com.uq.util.RedisUtil;
import com.uq.util.SUtil;

public class QQTool {
	public static ApkdetailDao apkdao = new ApkdetailDaoImpl();
	public static OtherapksDao otherapksDao =  new OtherapksDaoImpl();
	public static CateAppDao cateAppDao = new CateAppDaoImpl();
	
	private static Map<Integer, AppType> typeMap = new HashMap<Integer, AppType>();//从数据库加载存放qq的分类
	private static Logger log = LoggerFactory.getLogger(QQTool.class);
	private static final int pageSize = 40;
	
	//先初始化应用分类
	static{
		AppTypeDao typeDao = new AppTypeDaoImpl();
		String sql = "select * from apptype  WHERE sourceid ='qq' ";
		List<AppType> list = typeDao.findAllList(sql);
		for (AppType type:list) {
			typeMap.put(type.getSort(), type);//sort保存的是qq自己的分类id
		}

	}
	
	//软件大分类
	public static void getcates(Page page){
		Json json = page.getJson();
		String url = page.getUrl().toString();
		if(url.contains("cate/cates")){
			int size = json.jsonPath("$.obj[*].cateId").all().size();
			System.out.println(size);
			for (int i = 1; i < size; i++) {
//			for (int i = 0; i < size; i++) {
				System.out.println(json.jsonPath("$.obj["+i+"].cateId")+"--"+json.jsonPath("$.obj["+i+"].cateName"));
				String cateid = json.jsonPath("$.obj["+i+"].cateId").get();
				String catename = json.jsonPath("$.obj["+i+"].cateName").get();
				//加入大类的应用列表
				Request request = new Request();
				request.setUrl("http://m5.qq.com/cate/appList.htm?categoryId="+cateid+"&orgame=2&pageSize="+pageSize+"&pageContext=0");//参数说明 pageContext 从第几个开始取，pageSize 取多少个
				request.putExtra("cateid", cateid);//大分类的id
				page.addTargetRequest(request);
				//加入分类的小标签
				Request re = new Request();
				re.setUrl("http://m5.qq.com/cate/tags.htm?cateId="+cateid);
				re.putExtra("cateid", cateid);//大分类的id
				re.putExtra("catename", catename);//大分类的名称
				page.addTargetRequest(re);
//				break;
			}
		}
	}
	//大分类下的小分类
	public static void getcateTags(Page page){
		String url = page.getUrl().get();
		if(url.contains("cate/tags")){
			//大分类的id
			Json json = page.getJson();
			int mainCateId = SUtil.formatStr(page.getRequest().getExtra("cateid").toString());
			String mainCateName = page.getRequest().getExtra("catename").toString();
			System.out.println(mainCateId+" == "+mainCateName);
			int k = json.jsonPath("$.obj[*].id").all().size();
			for (int i = 0; i < k; i++) {
				//小分类的id
				int  itemid = SUtil.formatStr(json.jsonPath("$.obj["+i+"].id"));
				String catename = json.jsonPath("$.obj["+i+"].name").get();
				System.out.println(catename);
				Request itemRequest = new Request();
				itemRequest.setUrl("http://m5.qq.com/cate/tag/appList.htm?pageContext=0&pageSize="+pageSize+"&tagId="+itemid);
				itemRequest.putExtra("cateid", itemid);//小分类的id
				page.addTargetRequest(itemRequest);				
//				break;//所有的小分类
			}
		}
	}
	
	//各大小分类下的应用
	/**
	 * remdflag true 加入推荐链接
	 */
	public static void getappList(Page page,boolean remdflag){
		String url = page.getUrl().get();
		Json json = page.getJson();
		if(url.contains("cate/appList")||url.contains("cate/tag/appList")){
			System.out.println("total count :"+page.getJson().jsonPath("$.count"));
			int pageCount = SUtil.formatStr(page.getJson().jsonPath("$.count").toString());
			Map<String, String> paramMap = CRequest.URLRequest(url);
			//注意，cateid表示是从哪里过来的，大分类的id或者小分类的id
			int cateid = SUtil.formatStr(page.getRequest().getExtra("cateid").toString());
			System.out.println("========"+cateid);
			int currentpage = 0;
			if(pageCount>0){
				int pageContext = SUtil.formatStr(paramMap.get("pageContext"));
				String tmp = "pageContext="+pageContext;
				System.out.println("当前页:"+pageContext/pageSize);
				currentpage = (pageContext/pageSize)+1;
				pageContext = pageContext+pageSize;
				String nextUrl = url.replaceAll(tmp, "pageContext="+pageContext);
				Request itemlist_Request = new Request();
				itemlist_Request.setUrl(nextUrl);
				itemlist_Request.putExtra("cateid", cateid);
				page.addTargetRequest(itemlist_Request);
				
			}			
			
			Jedis jedis = null;
			try {
				//保存分类
				int size = json.jsonPath("$.obj[*].appId").all().size();
				List<CateApp> cateApps = new ArrayList<CateApp>();
				jedis = RedisUtil.getJedis();
				for (int i = 0; i < size; i++) {
					String packagename = json.jsonPath("$.obj["+i+"].pkgName").toString();
					String appname = json.jsonPath("$.obj["+i+"].appName").toString();
					String versioncode = json.jsonPath("$.obj["+i+"].versionCode").get();
					
					jedis.sadd(RedisConstant.QQlist_list, packagename);//qq的包名区分大小写的
					
					jedis.hset(RedisConstant.QQ_PKG+packagename.toLowerCase(), "versioncode", versioncode);
					//加入推荐
					if(remdflag){
						page.addTargetRequest("http://m5.qq.com/app/ulikeapp.htm?apkName="+packagename);
					}
										
					//获取该应用的categoryId
					int cateoryid = SUtil.formatStr(json.jsonPath("$.obj["+i+"].categoryId").get());//该应用的大分类id 151
					System.out.println("----"+cateoryid);
					AppType apptype = typeMap.get(cateid);// 15102
					if(apptype!=null){//一般情况不为空，空的话就是分类变了
						if(cateid == cateoryid){//从大分类列表进来
							Integer mycateid = apptype.getApptypeid();
							if(!jedis.sismember(RedisConstant.ALL_Cate+mycateid, packagename.toLowerCase()) && !cateAppDao.isExist(packagename, mycateid)){
								CateApp cate = new CateApp(mycateid,apptype.getName(),packagename,"qq");
								cateApps.add(cate);	
							}
						}else{//从小分类进来的
							AppType tmp = typeMap.get(cateoryid);//大类 apptypeid = 						
							if(tmp!=null && tmp.getApptypeid().equals(apptype.getPid()) ){//小分类下的是同一大分类才保存
								Integer mycateid = apptype.getApptypeid();
								if(!jedis.sismember(RedisConstant.ALL_Cate+mycateid, packagename.toLowerCase()) && !cateAppDao.isExist(packagename, mycateid)){
									CateApp cate = new CateApp(mycateid,apptype.getName(),packagename,"qq");
									cateApps.add(cate);	
								}
							}else if(tmp!=null) {//只保存大类
								Integer mycateid =tmp.getApptypeid();
								if(!jedis.sismember(RedisConstant.ALL_Cate+mycateid, packagename.toLowerCase()) && !cateAppDao.isExist(packagename, mycateid)){
									CateApp cate = new CateApp(mycateid,tmp.getName(),packagename,"qq");
									cateApps.add(cate);	
								}
								
							}else {
								System.out.println();
								log.error("错误的分类:"+packagename+" ="+cateoryid);
//								LogTest.savelog("c:/catelog_wangl.txt", packagename+" ="+cateoryid+" "+appname+"\r\n");
							}
						}
					}
				}
				
				if(cateApps!=null && cateApps.size()>0){
					boolean b = cateAppDao.save(cateApps);
					if(b){
						for(CateApp c:cateApps){
							jedis.sadd(RedisConstant.ALL_Cate+c.getCateid(), c.getPackagename().toLowerCase());
						}
					}
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				RedisUtil.returnResource(jedis);
			}		
			System.out.println(json.jsonPath("$.obj[*].appName").all());
		}
	}

	
	//相关推荐
	public static void getulikeapp(Page page){
		String url = page.getUrl().get();
		Json json = page.getJson();
		if(url.contains("app/ulikeapp")){
			List<String> unlikeapps = json.jsonPath("$.obj[*].pkgName").all();
			Jedis jedis = null;
			try {
				jedis = RedisUtil.getJedis();
				String pkgname = SUtil.formatObj(page.getRequest().getExtra("pkg"));
				if(pkgname == null ||pkgname == "null"|| SUtil.isEmpty(pkgname)){
					String itemUrl = page.getUrl().toString();
					Map<String, String> mapRequest = CRequest.URLRequest(itemUrl.trim());				
					 pkgname = mapRequest.get("apkName");
				}
				if(!jedis.sismember(RedisConstant.ALL_Like+"qq", pkgname.toLowerCase()) && !otherapksDao.isExist(pkgname, "qq")){
					Otherapks other = new Otherapks(pkgname, json.jsonPath("$.obj[*].pkgName").all().toString(), "qq", 1);
					boolean falg = otherapksDao.save(other);
					if(falg){
						jedis.sadd(RedisConstant.ALL_Like+"qq", pkgname.toLowerCase());
					}
				}
				List<CateApp> cateApps = new ArrayList<CateApp>();
				for (int i = 0; i < unlikeapps.size(); i++) {
//					暂时只采集游戏的应用，过滤掉软件应用
				int categoryId = Integer.valueOf(json.jsonPath("$.obj["+i+"].categoryId").get());
				AppType type = typeMap.get(categoryId);//推荐过来的只能保存大分类了
				if(type !=null && type.getType().equals("game")){
					String pkg = json.jsonPath("$.obj["+i+"].pkgName").get();					
					String versioncode = json.jsonPath("$.obj["+i+"].versionCode").get();
					//先从redis判断一下，加快速度
					int cateid = type.getApptypeid();//自己系统的分类id
					if(!jedis.sismember(RedisConstant.ALL_Cate+cateid, pkg.toLowerCase()) && !cateAppDao.isExist(pkg, cateid)){
						CateApp cate = new CateApp(cateid,type.getName(),pkg,"qq");
						cateApps.add(cate);	
					}
					
					String key = RedisConstant.QQ_PKG+pkg.toLowerCase();	
					jedis.sadd(RedisConstant.QQlist_remd, pkg);//qq的请求包名区分大小写，这里不做修改
					jedis.hset(key, "versioncode", versioncode);
					page.addTargetRequest("http://m5.qq.com/app/ulikeapp.htm?apkName="+pkg);
				}
//				if(!typeMap.containsKey(categoryId))continue;//跳过
					
				}
				//保存好分类
				boolean savefalg = cateAppDao.save(cateApps);
				if(savefalg){
					for(CateApp cate:cateApps){
						jedis.sadd(RedisConstant.ALL_Cate+cate.getCateid(), cate.getPackagename().toLowerCase());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				RedisUtil.returnResource(jedis);
			}
		}
	}
	
	/**
	 * 获取qq应用详情
	 * @param 
	 * @date 2015-6-22
	 * @author	aurong
	 * @return void
	 */
	public static void getappdetail(Page page){
		String url = page.getUrl().get();
		Json json = page.getJson();
		if(url.contains("app/appdetail")){
			//fastjson解析
			JSONObject jsonObject = JSONObject.parseObject(json.toString());
			JSONObject apkjson = jsonObject.getJSONObject("obj");
			String packagename = apkjson.getString("pkgName");
			int versionCode = apkjson.getIntValue("versionCode");
			System.out.println(jsonObject.getJSONObject("obj").getString("appName")+" ver:"+versionCode);
			String publishname = apkjson.getString("authorName");
			if("海沙工作室".equalsIgnoreCase(publishname)){
				return ;
			}
			Jedis jedis = null;
			try {
				jedis = RedisUtil.getJedis();
				if(jedis.sismember(RedisConstant.ALL_UN_REMD, packagename.toLowerCase())){//垃圾应用，不抓取
					return;
				}
				Apkdetail db_apk = apkdao.findApkByPkg(packagename);				
				boolean b = (db_apk ==null || db_apk.getVersioncode()< versionCode || db_apk.getStatus().equals(10));
				if(b){//检查是否存在和是否需要更新	
					Apkdetail apk = new Apkdetail();
					//记录需要更新和新增的apk
					if(db_apk ==null || db_apk.getId()<0){
						apk.setStatus(2);//后期新增
						System.out.println("要新增的："+packagename);
					}else {
						apk.setStatus(4);//后期更新
						apk.setAppid(db_apk.getAppid());//存在的话保存为原来数据库的appid 
						apk.setUpdatetimes(db_apk.getUpdatetimes()+1);
						System.out.println("要更新的："+packagename);
					}
								
					apk.setThirdappid(apkjson.getIntValue("appId"));
										
					apk.setAppname(apkjson.getString("appName"));
					apk.setPackagename(apkjson.getString("pkgName"));
					//腾讯的默认图标是96，直接改为256
					String iconurl = apkjson.getString("iconUrl");
					if(!SUtil.isEmpty(iconurl)){
						int k = iconurl.lastIndexOf("/");
						iconurl = iconurl.substring(0, k+1)+"256";
					}					
					apk.setSiconurl(iconurl);
					apk.setUpdateversioninfo(apkjson.getString("newFeature"));//更新信息
					apk.setVersioncode(apkjson.getIntValue("versionCode"));
					apk.setMinsdkversion(0);//系统最低版本没提供
					
					if( db_apk != null && db_apk.getId()>0){//先把apk给删掉 apkdao.isExist(apk.getPackagename())
						apkdao.deleteAndback(apk.getPackagename());		
					}
					
					apk.setVersionname(apkjson.getString("versionName"));			
					
					apk.setSapkurl(apkjson.getString("apkUrl"));
					
					apk.setAdvertremark(apkjson.getString("editorIntro"));//广告语
					apk.setSource("qq");
					
					//apk文件md5，签名
					apk.setApkmd5(apkjson.getString("apkMd5"));
					//替换为自己的分类
					int qqcateid = apkjson.getInteger("categoryId");
					AppType type = typeMap.get(qqcateid);
					if(type!=null){
						int soft = "soft".equalsIgnoreCase(type.getType())?1:2; 
						apk.setSoft(soft);
						apk.setCategoryname(type.getName());//大分类的名字
						apk.setCatetag(type.getName());
						apk.setCategoryid(type.getApptypeid());
						if(!jedis.sismember(RedisConstant.ALL_Cate+type.getApptypeid(), packagename.toLowerCase()) && cateAppDao.isExist(packagename, type.getApptypeid())){
							CateApp cate = new CateApp(type.getApptypeid(),type.getName(),packagename,"qq");
							if(cateAppDao.save(cate)){
								jedis.sadd(RedisConstant.ALL_Cate+type.getApptypeid(), packagename.toLowerCase());
							}
						}
						
					}					
					
					apk.setCurrentpage(SUtil.formatStr(page.getRequest().getExtra("page")));
					//hasadvart 0 表示无广告 1 表示有
					//isoffical 2 表示  非官方     1 表示官方
					// var tmplFlag=it[i].flag;var isAd = (tmplFlag >>> (0 * 2)) & 3;var isGf = (tmplFlag >>> (1 * 2)) & 3;var isBd = (tmplFlag >>> (2 * 1)) & 3;var isFree  = (tmplFlag >>> (3 * 1)) & 3;
					/*var isAd = (tmplFlag >>> (0 * 2)) & 3; -- 广告 0 没有检测是否有广告模块 1 没有广告 其他 有广告
					var isGf = (tmplFlag >>> (1 * 2)) & 3; --官方 0 不确定 1 官方 其他 非官方
					var isBd = (tmplFlag >>> (2 * 1)) & 3;//病毒（安全） 
					var isFree  = (tmplFlag >>> (3 * 1)) & 3; -- 1 收费 2 免费*/
					int tmpflag = apkjson.getIntValue("flag");
					apk.setIsoffical((tmpflag>>>2&3)== 0?2:((tmpflag>>>2&3)==1)?1:0);//官方
					apk.setHasadvart((tmpflag>>>0&3)== 0?2:((tmpflag>>>0&3)==1)?1:0);//是否有广告 Integer.valueOf(json.jsonPath("$.obj.flag").toString())>>>0&3
//					apk.setIssafe(((tmpflag>>>2)&3)== 0?1:((tmpflag>>>2&3)==1)?1:0);
					
					apk.setIssafe(1);//都安全
					apk.setHasadvart((tmpflag>>>0&3)== 1?1:0);
					apk.setIsoffical((tmpflag>>>2&3)== 1?1:0);
					int kk = (tmpflag >>> (2 * 1)) & 3;
					apk.setEm1(String.valueOf(kk));//安全性
					int em2 = (tmpflag >>> (1 * 2)) & 3;
					apk.setEm2(String.valueOf(em2));//官方
					int em3 = (tmpflag >>> (0 * 2)) & 3;
					apk.setEm3(String.valueOf(em3));
					apk.setSize(apkjson.getLongValue("fileSize"));
					apk.setRemark(apkjson.getString("description"));
					apk.setDownloadcount(apkjson.getIntValue("appDownCount"));
					
					apk.setPermission("");//空
					apk.setKeywords("");
//					apk.setEm2(jsonObject.getString("short_word"));
					int islist = SUtil.formatStr(page.getRequest().getExtra("islist"));
					apk.setIslist(islist);//表示是在列表更新的，后期可能会优先下载这些应用
					try {
						apk.setCreatetime(new Date());
//						apk.setPublishtime(apkjson.getDate("apkPublishTime"));
						apk.setPublishtime(new Date(apkjson.getLongValue("apkPublishTime")*1000));//更新时间
					} catch (Exception e) {
						apk.setCreatetime(new Date());
						apk.setPublishtime(new Date());//更新时间
					}
					
					//开发者
					apk.setPublisherName(apkjson.getString("authorName"));
					apk.setAverageRating(apkjson.getString("averageRating").substring(0, 3));
					//软件语言
					/*	1 -- 中文
						2 -- 英文
						3 -- 繁体中文
						9 -- 其他
					*/
					if(LanguageUtil.containsChinese(apkjson.getString("appName"))){//包含中文
						apk.setLanguage("1");//中文
					}else if(LanguageUtil.containsChinese(apkjson.getString("description"))){
						apk.setLanguage("1");//中文
					}else if(LanguageUtil.containsEnglish(apkjson.getString("appName"))){
						apk.setLanguage("2");
					}else {
						apk.setLanguage("9");
					}
					
					
					//介绍图片
					//高清
					JSONArray images =  apkjson.getJSONArray("snapshotsUrl");
					String image_h ="";
					String image_m ="";
					for (int i = 0; i < images.size(); i++) {
						JSONObject tmpObject = JSONObject.parseObject(images.get(i).toString());						
						if(i == 0){
							image_h = tmpObject.getString("size480Url");
							image_m = tmpObject.getString("size550Url");
						}else {
							image_h = image_h+"|"+tmpObject.getString("size480Url");
							image_m = image_m+"|"+tmpObject.getString("size550Url");
						}
					}
					apk.setImage_h(image_h);
					apk.setImage_m(image_m);
					boolean saveFlag = apkdao.save(apk);
					if(saveFlag){
						//在数据库保存成功的话,才加入redis					
						try {
							jedis.sadd(RedisConstant.QQ_PKG_ALL, apk.getPackagename());//
							jedis.sadd(RedisConstant.QQ_PKG_ALL_Min, apk.getPackagename().toLowerCase());
							jedis.hset(RedisConstant.ALL_APPS_PRE+packagename.toLowerCase(), "versioncode", SUtil.formatInt(apk.getVersioncode()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				RedisUtil.returnResource(jedis);
			}
		}
	}
}
