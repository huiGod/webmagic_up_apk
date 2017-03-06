package com.uq.spider.upload;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.ApkdetailDao;
import com.uq.dao.AppTypeDao;
import com.uq.dao.CateAppDao;
import com.uq.dao.OtherapksDao;
import com.uq.dao.RemdInfoDao;
import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.ApkdetailDaoImpl;
import com.uq.dao.impl.AppTypeDaoImpl;
import com.uq.dao.impl.CateAppDaoImpl;
import com.uq.dao.impl.OtherapksDaoImpl;
import com.uq.dao.impl.RemdInfoDaoImpl;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.AppType;
import com.uq.model.RemdInfo;
import com.uq.model.Urldownload;
import com.uq.util.FileDownloadUtil;
import com.uq.util.HttpUtils;
import com.uq.util.ImageUtil;
import com.uq.util.LogTest;
import com.uq.util.ProUtil;
import com.uq.util.SUtil;

/**
 * 上传数据
 * @author cp
 *
 */
public class UploadUtil {
	public static final String ThirdCateUrl ="http://apk.shua.cn/thirdcate/add.go";//"http://localhost:8080/i4ser/thirdcate/add.go";//上传第三方分类
	
	public static final String ThirdAppinfoUrl ="http://192.168.1.236:9088/i4ser/appthird/saveAppInfo.go";//上传应用app apk.waip.i4.cn
	private static  Logger log = LoggerFactory.getLogger(UploadUtil.class);
	
	public static void main(String[] args) {
		uploadThirdcate("360");
//		uploadThirdcate("qq");
//		uploadThirdcate("wdj");
//		uploadApp();
//		uploadAppbyDownid("b496adbb14c749908ec1b79b69b715ad");
//		uploadTest("com.tuyoo.tetris");
//		testchouqu();
//		testdate();
//		getUninstallApk();
	}
	/**
	 * 上传第三方分类到服务器
	 * @param source
	 */
	public static void uploadThirdcate(String source){
//		String sql = "select * from apptype where sourceid = ?";
		String sql = "select * from apptype where sourceid = ? and status = 2";
		AppTypeDao appTypeDao = new AppTypeDaoImpl();
		List<AppType> list = appTypeDao.query(sql, source);
		String str = JSONObject.toJSONString(list);
		System.out.println(JSONObject.toJSONString(list));
		try {
			HttpUtils.postOut(ThirdCateUrl, 10, str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * @Description: 根据批次号去抽取status = 7 的数据
	 * @param 
	 * @date 2015-5-16
	 * @author	aurong
	 * @return void
	 */
	public static void uploadAppbyDownid(String download_uuid){
		String sql ="select packagename,appname,versioncode,versionname,size,issafe,isoffical,hasadvart,minsdkversion,apkmd5,filecode,signaturemd5,iconurl,siconurl,apkurl,advertremark," //and packagename ='com.ourpalm.toybattlefield.c360' AND filestatus = 0 and status =7
			+" updateversioninfo,remark,Keywords,remarkimages_h,remarkimages_m,remarkimages_l,image_h,image_m,image_l,downloadcount,soft,source,categoryid,categoryname,language,publishtime,publisherName,permission,uploadid from app_upload app where uploadid =?  and status =7  limit ?,?";
		Integer curpage =0,pagesize = 3000;
		System.out.println(sql);
	    QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	    CateAppDao cateAppDao = new CateAppDaoImpl();
	    ApkdetailDao apkdetailDao = new ApkdetailDaoImpl();
	    UrldownloadDao urldownloadDao = new UrldownloadDaoImpl();
	    OtherapksDao otherapksDao = new OtherapksDaoImpl();
	    List apklist = new ArrayList();
	    boolean b = true;
	    do {
	    	try {
				apklist = qr.query(sql, new Object[]{download_uuid,(curpage++)*pagesize,pagesize}, new MapListHandler());
				System.out.println(apklist.size());
				//一般来说，同一批次号不会有相同的包名
				Map<String,Integer> pkgMap = new HashMap<String, Integer>();
				for (int i = 0; i < apklist.size(); i++) {
					Map<String,Object>  map = (Map)apklist.get(i);
					String pkgname = SUtil.converString(map.get("packagename"));
					System.out.println("======"+pkgname);
					pkgMap.put(pkgname, SUtil.formatStr(map.get("versioncode")));
					String thirdImage ="";
					if(!SUtil.isEmpty(SUtil.converString(map.get("image_h")))){
						thirdImage = SUtil.converString(map.get("image_h"));
						map.remove("image_m");
						map.remove("image_l");
					}else if(!SUtil.isEmpty(SUtil.converString(map.get("image_m")))){
						thirdImage = SUtil.converString(map.get("image_m"));
						map.remove("image_l");
					}else {
						thirdImage = SUtil.converString(map.get("image_l"));
					}
					String[] images_h = FileDownloadUtil.getImage_h(thirdImage);
					map.put("image_h", ImageUtil.makeJson(images_h));
							
//					pkgList.add(pkgname);
					String source = SUtil.converString(map.get("source"));
//					String cateids = jedis.get("s360:"+pkgname.toLowerCase());
					//获取分类
					String cateids = cateAppDao.findCate(pkgname, source);				
					System.out.println("--------"+cateids);
					map.put("cateids", cateids);
					//获取相关推荐的apk
					String otherapks = otherapksDao.findOtherapks(pkgname, source);
					System.out.println("otherapks:"+otherapks);
					if(!SUtil.isEmpty(otherapks)){
						otherapks = otherapks.replaceAll("\\[", "").replaceAll("\\]", "");
					}
					map.put("otherapkid", otherapks);
					//单条更新速度慢
//					apkdetailDao.update("update app_detail_info set filestatus = 1 where packagename = '"+SUtil.converString(map.get("packagename")+"'"));
				}
				//发送json数据
				String str = JSONObject.toJSONString(apklist,SerializerFeature.WriteMapNullValue); //SerializerFeature.WriteMapNullValue
				System.out.println(str);
				if(SUtil.isEmpty(str)||str.length()<10){
					continue;
				}
				System.out.println("ss");
				int retry = 0;
				while(retry<3){
					retry++;
					try {
						String jsonStr = HttpUtils.postOut(ProUtil.getString("ThirdAppinfoUrl"), 1000, str);
						System.out.println("返回结果："+jsonStr);
						b = true;
						JSONObject jsonObject = JSONObject.parseObject(jsonStr);
						if(!jsonObject.getBooleanValue("code")){//更新失败的bao
							JSONArray array = jsonObject.getJSONArray("faillist");
							for (int i = 0; i < array.size(); i++) {
								String pkg = array.get(i).toString();
								System.out.println(pkg);
								try {
									int versioncode = pkgMap.get(pkg.trim());
									apkdetailDao.update("update app_detail_info set status = 9 where packagename = '"+pkg.trim()+"' and versioncode = "+versioncode);
									apkdetailDao.update("update app_upload set status = 9 where packagename = '"+pkg.trim()+"' and versioncode = "+versioncode);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						break;
					} catch (Exception e) {
						e.printStackTrace();
						//上传失败
						b = false;//更新失败标志
						log.error("上传数据错误:"+download_uuid);						
						urldownloadDao.update("update urldownload set datastatus = 0 where uuid ='"+download_uuid+"'");
						log.error("更新数据错误的状态:"+download_uuid);
					}
				}
				System.out.println("break=================");
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} while (apklist.size()>0);
	    
	    if(b){
	    	log.error("上传数据成功:"+download_uuid);
	    	urldownloadDao.update("update urldownload set datastatus = 1 where uuid ='"+download_uuid+"'");
	    }
	    
	}
	
	/**
	 * @Description: 向服务端请求审核不通过的包名，后期直接不抓取
	 * @param 
	 * @date 2015-6-30
	 * @author	aurong
	 * @return void
	 */
	public static void getUninstallApk(){
		String url = ProUtil.getString("GetUninstallApkUrl");
		Map<String,String> params = new HashMap<String, String>();
		params.put("page", "0");
		params.put("pageSize", "100");
		RemdInfoDao remdInfoDao = new RemdInfoDaoImpl();
		try {			
			JSONArray array = null;
			do {
				String jsonStr = HttpUtils.get(url, params, 300, "utf-8");
				array = JSONArray.parseArray(jsonStr);
				List<RemdInfo> l = new ArrayList<RemdInfo>();
				System.out.println(array.size());
				for(int i=0;i<array.size();i++){  
			          String pkgname =array.getString(i);  
			          l.add(new RemdInfo(pkgname, "ser", 2));
			          System.out.println(pkgname);  
			      } 
				remdInfoDao.save(l);
			} while (array!=null && array.size()>0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testchouqu(){
		String sql ="SELECT * FROM urldownload u where u.createtime > (SELECT a.createtime FROM urldownload a WHERE a.uuid='ea83a522369d4cc6ab8eea392029a9f8') and datastatus != 4 ORDER BY u.createtime asc ";
		UrldownloadDao urldownloadDao = new UrldownloadDaoImpl();
		List<Urldownload> l = urldownloadDao.query(sql, null);
		for(Urldownload down:l){
			System.out.println(down.getUuid());
			String uuid = down.getUuid();
			uploadAppbyDownid(uuid);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//测试，把图标转为json的格式
	public static String convertJson(String iconUrl){
		String icons[] = new String[]{"px256","px144","px96","px72","px48"}; 
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < icons.length; i++) {
			map.put(icons[i], iconUrl);
		}
		return JSONObject.toJSONString(map);
	}
	
	/**
	 * @Description: 根据批次号去抽取status = 7 的数据
	 * @param 
	 * @date 2015-5-16
	 * @author	aurong
	 * @return void
	 */
	public static void uploadTest(String packagename){
		String sql ="select packagename,appname,versioncode,versionname,size,issafe,isoffical,hasadvart,minsdkversion,apkmd5,filecode,signaturemd5,iconurl,siconurl,apkurl,advertremark," //and packagename ='com.ourpalm.toybattlefield.c360' AND filestatus = 0 
			+" updateversioninfo,remark,Keywords,remarkimages_h,remarkimages_m,remarkimages_l,image_h,image_m,image_l,downloadcount,soft,source,categoryid,categoryname,language,publishtime,publisherName,permission from app_upload app where  status =9  limit ?,?";
		Integer curpage =0,pagesize = 300;
		System.out.println(sql);
		String countsql ="select count(1)  from app_detail_info app where  status =9 ";
	    QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	    CateAppDao cateAppDao = new CateAppDaoImpl();
	    ApkdetailDao apkdetailDao = new ApkdetailDaoImpl();
	    long count=   apkdetailDao.count(countsql, null);
	    System.out.println(count);
	    int totalpage = (int)(count/pagesize)+1;
	    System.out.println(totalpage);
//	    System.exit(0);
	    UrldownloadDao urldownloadDao = new UrldownloadDaoImpl();
	    List apklist = new ArrayList();
	    boolean b = true;
	    do {
	    	try {
	    		if(curpage>totalpage){
	    			return;
	    		}
	    		//一般来说，同一批次号不会有相同的包名
				Map<String,Integer> pkgMap = new HashMap<String, Integer>();
				apklist = qr.query(sql, new Object[]{(curpage++)*pagesize,pagesize}, new MapListHandler());
				for (int i = 0; i < apklist.size(); i++) {
					Map<String,Object>  map = (Map)apklist.get(i);
					String pkgname = SUtil.converString(map.get("packagename"));
					System.out.println("======"+pkgname);
					pkgMap.put(pkgname, SUtil.formatStr(map.get("versioncode")));
					String thirdImage ="";
					if(!SUtil.isEmpty(SUtil.converString(map.get("image_h")))){
						thirdImage = SUtil.converString(map.get("image_h"));
						map.remove("image_m");
						map.remove("image_l");
					}else if(!SUtil.isEmpty(SUtil.converString(map.get("image_m")))){
						thirdImage = SUtil.converString(map.get("image_m"));
						map.remove("image_l");
					}else {
						thirdImage = SUtil.converString(map.get("image_l"));
					}
					String[] images_h = FileDownloadUtil.getImage_h(thirdImage);
					map.put("image_h", ImageUtil.makeJson(images_h));
							
//					pkgList.add(pkgname);
					String source = SUtil.converString(map.get("source"));
//					String cateids = jedis.get("s360:"+pkgname.toLowerCase());
					String cateids = cateAppDao.findCate(pkgname, source);
					System.out.println("--------"+cateids);
					map.put("cateids", cateids);
					//单条更新速度慢
//					apkdetailDao.update("update app_detail_info set filestatus = 1 where packagename = '"+SUtil.converString(map.get("packagename")+"'"));
				}
				//发送json数据
				String str = JSONObject.toJSONString(apklist,SerializerFeature.WriteMapNullValue); //SerializerFeature.WriteMapNullValue
				System.out.println(str);
				if(SUtil.isEmpty(str)||str.length()<10){
					continue;
				}
				System.out.println("ss");
				int retry = 0;
				while(retry<3){
					retry++;
					try {
						String jsonStr = HttpUtils.postOut(ProUtil.getString("ThirdAppinfoUrl"), 1000, str);
						System.out.println("返回结果："+jsonStr);
						b = true;
						JSONObject jsonObject = JSONObject.parseObject(jsonStr);
						if(!jsonObject.getBooleanValue("code")){//更新失败的bao
							JSONArray array = jsonObject.getJSONArray("faillist");
							for (int i = 0; i < array.size(); i++) {
								String pkg = array.get(i).toString();
								System.out.println(pkg);
								try {
									int versioncode = pkgMap.get(pkg.trim());
									apkdetailDao.update("update app_upload set status = 8 where packagename = '"+pkg.trim()+"' and versioncode = "+versioncode);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						break;
					} catch (Exception e) {
						e.printStackTrace();
						//上传失败
						b = false;//更新失败标志
						
					}
				}
				System.out.println("输出break");
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} while (apklist.size()>0);
	    
	    if(b){
	    	log.error("上传数据成功:"+packagename);
	    }
	    
	}
	
	public static  void testdate(){
		String sql ="  SELECT * FROM app_upload ap  WHERE STATUS = 7 AND ap.uploadid IN( SELECT u.uuid FROM urldownload u WHERE u.createtime > (SELECT a.createtime FROM urldownload a WHERE a.`uuid`='ea83a522369d4cc6ab8eea392029a9f8') AND filestatus = 1)";
		QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
		try {
			List apklist = qr.query(sql, new MapListHandler());
			StringBuffer buf = new StringBuffer();
			for(int i =0;i<apklist.size();i++){
				Map<String,Object>  map = (Map)apklist.get(i);
				String pkgname = SUtil.converString(map.get("packagename"));
				int versioncode = SUtil.formatStr(map.get("versioncode"));
				System.out.println("======"+pkgname+" ==+"+versioncode);
				buf.append(" update app_detail_info_temp set filestatus = 'y' where packagename='"+pkgname+"' and versioncode="+versioncode+";\r\n");
				if(i%500==0){
					LogTest.savelog("c:/uptemp.sql", buf.toString());
					buf.delete(0, buf.length());
				}
			}
			LogTest.savelog("c:/uptemp.sql", buf.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
