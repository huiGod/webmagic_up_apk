package com.uq.spider.s360;

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
import com.uq.dao.ErrorLogDao;
import com.uq.dao.OtherapksDao;
import com.uq.dao.RemdInfoDao;
import com.uq.dao.impl.ApkdetailDaoImpl;
import com.uq.dao.impl.AppTypeDaoImpl;
import com.uq.dao.impl.CateAppDaoImpl;
import com.uq.dao.impl.ErrorLogDaoImpl;
import com.uq.dao.impl.OtherapksDaoImpl;
import com.uq.dao.impl.RemdInfoDaoImpl;
import com.uq.model.Apkdetail;
import com.uq.model.AppType;
import com.uq.model.CateApp;
import com.uq.model.ErrorLog;
import com.uq.model.Otherapks;
import com.uq.util.CRequest;
import com.uq.util.ConfigUtil;
import com.uq.util.ProUtil;
import com.uq.util.RedisConstant;
import com.uq.util.RedisUtil;
import com.uq.util.SUtil;

public class S360Tool {
	
	public static ApkdetailDao apkdao = new ApkdetailDaoImpl();
	public static OtherapksDao otherapksDao =  new OtherapksDaoImpl();
	public static CateAppDao cateAppDao = new CateAppDaoImpl();
	public static ErrorLogDao logDao = new ErrorLogDaoImpl();
	public static RemdInfoDao remdInfoDao = new RemdInfoDaoImpl();
	public static AppTypeDao typeDao = new AppTypeDaoImpl();
	
	private static Map<String, AppType> typeMap = new HashMap<String, AppType>();//从数据库加载存放360的分类
	private static Map<String, String> thirdMapping = ConfigUtil.getMapping();//从mapping.txt加载其他分类影射
	private static Map<String, AppType> third_typeMap = new HashMap<String, AppType>();//从数据库加载存放360的分类
	
	private static Logger log = LoggerFactory.getLogger(S360Tool.class);
	
	//先初始化应用分类
	static{		
		String sql = "select * from apptype  WHERE sourceid ='360' ";//从列表保存的分类
		List<AppType> list = typeDao.findAllList(sql);
		for (AppType type:list) {
//			System.out.println(type.getName()); //360的大分类下的标签名字会有重复的，所以把大分类和小分类的名字拼起来去比对
			if(type.getPid().equals(1)||type.getPid().equals(2)){//大分类
				System.out.println(type.getName()+"_"+type.getName());
				typeMap.put(type.getName()+"_"+type.getName(), type);
			}else {//小分类
				//获取父分类
				AppType parent = typeDao.findByapptypeid(type.getPid());
				System.out.println(parent.getName()+"_"+type.getName());
				typeMap.put(parent.getName()+"_"+type.getName(), type);
			}
		}
		sql ="select * from apptype  WHERE sourceid ='s360' ";//自己整理的分类
		list = typeDao.findAllList(sql);
		for (AppType type:list) {
//			System.out.println(type.getName());
			third_typeMap.put(type.getName(), type);
		}
		
		for(Map.Entry<String, AppType> entry:typeMap.entrySet()){
			System.out.println("---"+entry.getKey());
		}
	}
	
	/*处理app的具体详情
	 * remdFlag true 要过滤校验合作包(从360采集过来的要过滤)
	 */
	public static void getAppinfo(Page page,boolean remdFlag){
		String url  = page.getUrl().get();
		if(url.contains("mintf/getAppInfoByIds")){
			Json json = page.getJson();		
			System.out.println(json.toString());
			//fastjson解析
			JSONObject tmpObject = JSONObject.parseObject(json.toString());
			String error = tmpObject.getString("errno");
			if("1002".equals(error)){
				return ;//没有返回结果的
			}
//			System.out.println(tmpObject.getJSONArray("data"));
			JSONArray array =tmpObject.getJSONArray("data");
			Object[] objects= array.toArray();
			JSONObject jsonObject= JSONObject.parseObject(objects[0].toString());

			
			String packagename = jsonObject.getString("apkid");
			int versionCode = jsonObject.getIntValue("version_code");
			//System.out.println(packagename+" "+versionCode);

			Jedis jedis = null;
			try{
				jedis = RedisUtil.getJedis();
				if(remdFlag){//合作包
					if(jedis.sismember(RedisConstant.ALL_Remd, packagename.toLowerCase())){
						return;
					}
				}
				
				if(jedis.sismember(RedisConstant.ALL_UN_REMD, packagename.toLowerCase())){//垃圾应用，不抓取
					return;
				}
				
//				if(remdInfoDao.isremdApk(packagename, "360")){//该游戏接入了360的sdk的话，不更新了
//					return ;
//				}
				Apkdetail db_apk = apkdao.findApkByPkg(packagename);
				if(db_apk!=null && "qq".equals(db_apk.getSource())){//数据库原来是从qq抓过来的，也不更新
					return;
				}
				boolean b = (db_apk ==null || db_apk.getVersioncode()< versionCode);
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
								
					apk.setThirdappid(jsonObject.getIntValue("id"));
										
					apk.setAppname(jsonObject.getString("name"));
					apk.setPackagename(jsonObject.getString("apkid"));
					String iconurl = jsonObject.getString("logo_512");
					if(SUtil.isEmpty(iconurl)|| "null".equalsIgnoreCase(iconurl)||iconurl.length()<5){
						iconurl = jsonObject.getString("logo_url_160");
						if(SUtil.isEmpty(iconurl)|| "null".equalsIgnoreCase(iconurl)||iconurl.length()<5){
							iconurl = jsonObject.getString("logo_url");
						}
					}
					
					apk.setSiconurl(iconurl);
					apk.setUpdateversioninfo(jsonObject.getString("update_info"));//更新信息
					apk.setVersioncode(jsonObject.getIntValue("version_code"));
					apk.setMinsdkversion(jsonObject.getIntValue("os_version"));
					
					if( db_apk != null && db_apk.getId()>0){//先把apk给删掉 apkdao.isExist(apk.getPackagename())
						apkdao.deleteAndback(apk.getPackagename());		
					}
					
					apk.setVersionname(jsonObject.getString("version_name"));			
					
					apk.setSapkurl(jsonObject.getString("down_url"));
					
					apk.setAdvertremark(jsonObject.getString("single_word"));//广告语
					apk.setSource("360");
					
					//apk文件md5，签名
					apk.setApkmd5(jsonObject.getString("apk_md5"));
					
					boolean cateflag = page.getRequest().getExtras().containsKey("cate");//从推荐过来的没有设置分类
					CateApp cateApp = new CateApp();
					if(cateflag){//存在分类
						String cate = SUtil.formatObj(page.getRequest().getExtra("cate"));//保存大分类
						AppType appType = typeMap.get(cate);
						if(appType!=null){
							int soft = "soft".equalsIgnoreCase(appType.getType())?1:2; 
							apk.setSoft(soft);
							apk.setCategoryname(appType.getName());
							apk.setCategoryid(typeMap.get(cate).getApptypeid());
						}else {
							String cateids = jsonObject.getString("category_level2_ids");
							cateApp =getcates(cateids, packagename, apk);
						}
					}else {
						String cateids = jsonObject.getString("category_level2_ids");
						cateApp =getcates(cateids, packagename, apk);
					}
					
					apk.setCurrentpage(SUtil.formatStr(page.getRequest().getExtra("page")));
					//官方
					apk.setIsoffical(jsonObject.getIntValue("is_offerwall")==0?1:0);					
					//安全
					apk.setIssafe(jsonObject.getIntValue("is_safe"));//360只有1，都安全？？
					//广告
					int ad = jsonObject.getIntValue("is_ad");
					if(ad == 0){
						apk.setHasadvart(1);//无广告
					}else if(ad ==1 ){
						apk.setHasadvart(0);//有广告
					}else {
						apk.setHasadvart(2);//不确定
					}				
					
					apk.setEm4(jsonObject.getString("is_push_ad"));//推送广告？
					
					apk.setSize(jsonObject.getIntValue("size"));
					apk.setRemark(jsonObject.getString("brief"));
					apk.setDownloadcount(jsonObject.getIntValue("download_times"));
					
					apk.setPermission(jsonObject.getString("uses_permission"));
					apk.setKeywords(jsonObject.getString("baike_name"));//baike_name=大唐双龙传 Android_com.cyou.dtslz.qihu
					apk.setEm3(jsonObject.getString("category_level1_ids"));//category_level2_ids=102233 这个才是大的分类
					String category_level2 = jsonObject.getString("category_level2_ids");
					if(category_level2!=null && category_level2.length()>100){
						category_level2 = category_level2.substring(0, 100);
					}
					apk.setEm4(category_level2);
					apk.setEm2(jsonObject.getString("short_word"));
					boolean listflag = page.getRequest().getExtras().containsKey("islist");
					if(listflag){
						apk.setIslist(1);//表示不是在列表更新的
					}else {
						apk.setIslist(2);//表示不是在列表更新的
					}
					
					try {
						apk.setCreatetime(new Date());
//						apk.setPublishtime(SUtil.formatStrToDate(jsonObject.getString("update_time"), "yyyy-MM-dd HH:mm:ss"));//更新时间
						apk.setPublishtime(jsonObject.getDate("update_time"));
					} catch (Exception e) {
						apk.setCreatetime(new Date());
						apk.setPublishtime(new Date());//更新时间
					}
					
					//开发者
					apk.setPublisherName(json.jsonPath("$.data[0].corp").get());
					apk.setAverageRating(jsonObject.getString("rating"));
					apk.setCatetag(SUtil.formatObj(page.getRequest().getExtra("cate2")));
					apk.setSofttag(jsonObject.getString("list_tag"));
					//软件语言
					/*	1 -- 中文
						2 -- 英文
						3 -- 繁体中文
						9 -- 其他
					*/
					apk.setLanguage(jsonObject.getString("lang"));
					
					//介绍图片
					apk.setImage_h(jsonObject.getString("thrumb_wifi"));//高清的
					apk.setImage_m(jsonObject.getString("thrumb_3g"));
					apk.setImage_l(jsonObject.getString("thrumb_small"));//低
					boolean saveFlag = apkdao.save(apk);
					if(saveFlag){
						//在数据库保存成功的话,才加入redis
						try {
							jedis.sadd(RedisConstant.S360_PKG_ALL, packagename.toLowerCase());
							jedis.hset(RedisConstant.ALL_APPS_PRE+packagename.toLowerCase(), "versioncode", SUtil.formatInt(apk.getVersioncode()));
							if(cateApp!=null && cateApp.getCateid()!=null){
								jedis.sadd(RedisConstant.ALL_Cate+cateApp.getCateid(), packagename.toLowerCase());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}					
					
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("360应用详情错误：", e);
			}finally {
				RedisUtil.returnResource(jedis);
			}
		}
	}
	
	
	//从分类id去获取分类
	public static CateApp getcates(String cateids,String packagename,Apkdetail apk ){
		boolean tt = false;
		CateApp cate = new CateApp();
		if(cateids!=null && !SUtil.isEmpty(cateids)){
			String[] ids = cateids.split(",");			
			for(String id:ids){
				String mapname = thirdMapping.get(id);
				if(mapname!=null){
					//根据名称获取分类					
					AppType appType = third_typeMap.get(mapname);
					if(appType!=null){
						tt = true;
						 cate = new CateApp(appType.getApptypeid(),mapname,packagename,"360");
						int soft = "soft".equalsIgnoreCase(appType.getType())?1:2;
						apk.setSoft(soft);
						apk.setCategoryname(mapname);
						apk.setCategoryid(appType.getApptypeid());
						cateAppDao.save(cate);
					}
					System.out.print(mapname+" ");
					break;
				}
			}
			if(!tt){//不存在分类。记录一下
				logDao.save(new ErrorLog("S360", packagename, cateids, "cate_not_exist", "", ""));
			}
		}
		return cate;
	}
	
	//加入360各分类的链接
	public static void getCatTags(Page page){
		System.out.println(page.getJson());
		String url = page.getUrl().toString();	
		String type = SUtil.formatObj(page.getRequest().getExtra("type"));
		String type_Url ="";
		String fm ="";
		if("game".equalsIgnoreCase(type)){
			type_Url ="http://openbox.mobilem.360.cn/app/list/cid/2/format/";
			fm ="gm004_cid2_tag";
		}else {
			type_Url ="http://openbox.mobilem.360.cn/app/list/cid/1/format/";
			fm ="sf004_cid1_tag";
		}
		
		if(url.contains("app/getCatTags/cid")){
			Json json = page.getJson();
			int size = json.jsonPath("$.data[*].title").all().size();			
			for (int i = 8; i < size; i++) {
				System.out.println(json.jsonPath("$.data["+i+"].title"));
				String title = json.jsonPath("$.data["+i+"].title").get();
//				if(i!=size-1){
//					continue;
//				}
				//比对一下分类名称是否和数据库的一样，不一样会获取不到分类，change by 20150611
				
				//加入全部分类的连接,因为在小分类不一定有这个应用
				String orders[] = new String[]{"weekpure","newest"}; 
				for(String order:orders){
					Request re = new Request();
					// newest 最新排序 weekpure 最热排序
					re.setUrl(type_Url+"webview?tag="+title+"&order="+order+"&tag2="+title+"&needtag=1&os=19&png=1&page=1&fm="+fm+title+"&m=a9194cad661042c6e595c8fa1485532e&m2=4a2bf4b6aacbe5e3b53a094e80724a3d&v=3.2.17&re=1&ch=100130&model=X9077&sn=4.589389937671455&cu=qualcomm+msm+8974+%28flattened+device+tree%29&ppi=1440x2560&startCount=11&snt=-1");
					re.putExtra("type", type);
					page.addTargetRequest(re);
				}
				boolean main_cate_flag = true;
				if(!typeMap.containsKey(title+"_"+title) && !"小编精选".equals(title)){
					//记录一下
					main_cate_flag = false;
					logDao.save(new ErrorLog("cate_error", "", title+"_"+title, "cate_error", null,null));
					//大分类不存在,手工打开
					if("true".equalsIgnoreCase(ProUtil.getString("cate_error_save"))){
						AppType main_type = new AppType();
						main_type.setName(title);
						main_type.setStatus(2);//新增的
						if("game".equalsIgnoreCase(type)){
							main_type.setPid(2);
						}else {
							main_type.setPid(1);
						}
						main_type.setSourceid("360");
						main_type.setType(type);
						saveErrorCate(main_type, "main");
					}
				}
				
				List<String> cate2 = json.jsonPath("$.data["+i+"].title2").all();				
				for (int j = 0; j < cate2.size(); j++) {
					System.out.println(cate2.get(j));
					for(String order:orders){
						Request request = new Request(type_Url+"webview?tag="+title+"&order="+order+"&tag2="+cate2.get(j)+"&needtag=1&os=19&png=1&page=1&fm="+fm+title+"&m=a9194cad661042c6e595c8fa1485532e&m2=4a2bf4b6aacbe5e3b53a094e80724a3d&v=3.2.23&re=1&ch=100130&model=X9077&sn=4.589389937671455&cu=qualcomm+msm+8974+%28flattened+device+tree%29&ppi=1440x2560&startCount=45&snt=-1");
						request.putExtra("type", type);
						page.addTargetRequest(request); 
						System.out.println(request.getUrl());
					}
					if(!typeMap.containsKey(title+"_"+cate2.get(j)) && !"小编精选".equals(title)){
						//记录一下
						logDao.save(new ErrorLog("cate_error", "", title+"_"+cate2.get(j), "cate_error", null,null));
						AppType pType = typeMap.get(title+"_"+title);
						AppType item_type = new AppType();
						item_type.setName(cate2.get(j));
						item_type.setStatus(2);
						item_type.setPid(pType.getApptypeid());
						item_type.setSourceid("360");
						item_type.setType(type);
						item_type.setRemark(title);//暂时保存父分类的名字
						saveErrorCate(item_type, "item");
					}
//					break;					
				}
//				System.out.println(json.jsonPath("$.data["+i+"].title2").all());
//				break;
			}
			
			
		}
		
	}
	
	//保存不存在的分类
	public static void saveErrorCate(AppType type,String catetype){
		System.out.println("------------------------save");
		int apptypeid = typeDao.getMaxApptypeid("360");
		if("main".equals(catetype)){//大分类
			//1、查询目前最大的apptypeid
//			int apptypeid = typeDao.getMaxApptypeid("360");
			type.setApptypeid(apptypeid+1);
			typeDao.save(type);
			//把大类的映射放到typemap
			typeMap.put(type.getName()+"_"+type.getName(), type);
		}else {
			type.setApptypeid(apptypeid+1);
			typeDao.save(type);
			typeMap.put(type.getRemark()+"_"+type.getName(), type);
			
		}
	}
	
	//获取360的列表的apk
	/**
	 * falg true 加入推荐链接
	 */
	public static void getList(Page page,boolean flag){
		String url = page.getUrl().get();
		//区分是应用还是游戏
		String type = SUtil.formatObj(page.getRequest().getExtra("type"));
		if(url.contains("app/list/cid")){
			// is_recommand=1 推广包
			Json json = page.getJson();
			int k = json.jsonPath("$.data[*].apkid").all().size();
			String tag ="";
			String tag2 ="";
			int pagecout =1;
//			System.out.println("条数："+k);
			Jedis jedis = null;
			try {
				jedis = RedisUtil.getJedis();
				if(k > 0){
					String itemUrl = page.getUrl().toString();
					Map<String, String> mapRequest = CRequest.URLRequest(itemUrl.trim());
//					String baseUrl = CRequest.UrlPage(itemUrl);
					 pagecout = SUtil.formatStr(mapRequest.get("page"));
					String tmpurl = itemUrl.replace("page="+pagecout, "page="+(pagecout+1));
//					System.out.println("tag:"+mapRequest.get("tag"));
					tag = mapRequest.get("tag").toString();//大分类
					tag2 = mapRequest.get("tag2").toString();//大分类下的小标签
					if(pagecout<130){//暂时只取前面130页的即可，后面的基本是重复的数据
						Request request = new Request(tmpurl);
						request.putExtra("type", type);
						page.addTargetRequest(request);
					}				
					System.out.println("当前请求页："+pagecout);
//					System.out.println("当前页返回的App："+json.jsonPath("$.data[*].name").all());
					
					//先保存分类标签
					List<String> l = json.jsonPath("$.data[*].apkid").all();
					List<CateApp> cateApps = new ArrayList<CateApp>();
					//先从redis查询是否存在分类，redis不存在的话再去数据库查询
					
					for(String pkg:l){
//						System.out.println(tag+"_"+tag2);
						AppType apptype = typeMap.get(tag+"_"+tag2);
//						System.out.println("======"+apptype.getName());
						if(apptype!=null){
							Integer cateid = apptype.getApptypeid();
							//先从redis判断一下，加快速度
							if(!jedis.sismember(RedisConstant.ALL_Cate+cateid, pkg.toLowerCase()) && !cateAppDao.isExist(pkg, cateid)){
								CateApp cate = new CateApp(cateid,apptype.getName(),pkg,"360");
								cateApps.add(cate);	
							}
						}
																
					}
					if(cateApps!=null && cateApps.size()>0){
						if(cateAppDao.save(cateApps)){
							for(CateApp cate:cateApps){
								jedis.sadd(RedisConstant.ALL_Cate+cate.getCateid(), cate.getPackagename().toLowerCase());
							}
						}
					}
					
				}
				
				for (int i = 0; i < k; i++) {
					String keypre = "$.data["+i+"]";
					String packagename = json.jsonPath(keypre+".apkid").toString();
					String versioncode = json.jsonPath(keypre+".version_code").get();
					if(jedis.sismember(RedisConstant.ALL_Remd, packagename.toLowerCase())){
						continue;
					}
					//360版本信息
					jedis.hset(RedisConstant.S360_PKG+packagename.toLowerCase(),"versioncode", versioncode);
					//列表信息
					jedis.sadd(RedisConstant.S360List_list, packagename.toLowerCase());
					//加入推荐
					if(flag){
						Request recom_request = new Request();
						recom_request.setUrl("http://125.88.193.234/mintf/getRecommandAppsForDetail?png=1&pname="+packagename+"&os=19&m2=4a2bf4b6aacbe5e3b53a094e80724a3d");
						recom_request.putExtra("pkg", packagename);
						page.addTargetRequest(recom_request);
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
	 * 
	 * @Description: 获取推荐应用 
	 * @param @param page
	 * @date 2015-6-22
	 * @author	aurong
	 * @return void
	 */
	public static void getRecommandAppsForDetail(Page page){
		String url = page.getUrl().get();
		if(url.contains("mintf/getRecommandAppsForDetail")){
			Json json = page.getJson();
			//获取连接
			Jedis jedis = null;
			try {
				jedis = RedisUtil.getJedis();
				String pkgname = SUtil.formatObj(page.getRequest().getExtra("pkg"));
				if(pkgname == null ||pkgname == "null"|| SUtil.isEmpty(pkgname)){
					String itemUrl = page.getUrl().toString();
					Map<String, String> mapRequest = CRequest.URLRequest(itemUrl.trim());				
					 pkgname = mapRequest.get("pname");
				}
				if(!jedis.sismember(RedisConstant.ALL_Like+"360", pkgname.toLowerCase()) && !otherapksDao.isExist(pkgname, "360")){
					Otherapks other = new Otherapks(pkgname, json.jsonPath("$.recommand[*].apkid").all().toString(), "360", 1);
					boolean falg = otherapksDao.save(other);
					if(falg){
						jedis.sadd(RedisConstant.ALL_Like+"360", pkgname.toLowerCase());
					}
				}
				
				//加入推荐下载
				int size =  json.jsonPath("$.recommand[*].apkid").all().size();
				
				for (int i = 0; i < size; i++) {
					String packagename = json.jsonPath("$.recommand["+i+"].apkid").get();
					String versioncode = json.jsonPath("$.recommand["+i+"].version_code").get();
					//先把版本信息放进临时的key
					jedis.hset(RedisConstant.S360_PKG+packagename.toLowerCase(),"versioncode", versioncode);					
					//加入推荐
					if(!jedis.sismember(RedisConstant.S360List_remd, packagename.toLowerCase())){
						//列表信息
						jedis.sadd(RedisConstant.S360List_remd, packagename.toLowerCase());
						Request recom_request = new Request();
						recom_request.setUrl("http://125.88.193.234/mintf/getRecommandAppsForDetail?png=1&pname="+packagename+"&os=19&m2=4a2bf4b6aacbe5e3b53a094e80724a3d");
						recom_request.putExtra("pkg", packagename);
						page.addTargetRequest(recom_request);
					}
					
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
				log.error("推荐错误-",e);
			}finally{
				RedisUtil.returnResource(jedis);
			}
		}
	}
}

