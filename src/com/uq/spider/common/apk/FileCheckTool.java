package com.uq.spider.common.apk;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.uq.dao.ApkdetailDao;
import com.uq.dao.ErrorLogDao;
import com.uq.dao.RemdInfoDao;
import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.ApkdetailDaoImpl;
import com.uq.dao.impl.ErrorLogDaoImpl;
import com.uq.dao.impl.RemdInfoDaoImpl;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.Apkdetail;
import com.uq.model.Apkupload;
import com.uq.model.ErrorLog;
import com.uq.model.RemdInfo;
import com.uq.model.Urldownload;
import com.uq.util.AnalysisApk;
import com.uq.util.CheckFileCode;
import com.uq.util.ConfigUtil;
import com.uq.util.CopyFileUtil;
import com.uq.util.GetBigFileMD5;
import com.uq.util.ImageMagickUtil;
import com.uq.util.ImageUtil;
import com.uq.util.ProUtil;
import com.uq.util.SUtil;

/**
 * 下载文件处理
 * @author cp
 *
 */
public class FileCheckTool {
	private static Logger log = LoggerFactory.getLogger(FileCheckTool.class);
	public static void main(String[] args) {
		String download_uuid ="5270c9714b6c4a2586e2b8871b909167";
//		checkFileDown(download_uuid);
		checkfile(download_uuid);
		
	}
	
	/**
	 * 生成各种图片的缩略图，读取apk的签名，成功的话更新apk status为6
	 * @param download_uuid 下载批次号
	 * @change jinrong 2015-06-10 修改为从中间表查询
	 */
	public static void checkfile(String download_uuid){
//		String sql ="select * from app_detail_info app where  uploadid = '"+download_uuid.trim()+"' and status = 5 limit ?,? "; //and packagename='com.pocketdigi.webmaster' 
//		String countSql ="select count(1) from app_detail_info app where  uploadid = '"+download_uuid.trim()+"' and status = 5";
		String sql ="select * from app_upload app where  uploadid = '"+download_uuid.trim()+"' and status = 5 limit ?,? "; //and packagename='com.pocketdigi.webmaster' 
		String countSql ="select count(1) from app_upload app where  uploadid = '"+download_uuid.trim()+"' and status = 5";
		Integer curpage =0,pagesize = 500;
		ApkdetailDao apkDao = new ApkdetailDaoImpl();
		long pageCount = apkDao.count(countSql, null);
		System.out.println("总条数:"+pageCount);
		long totalPage = pageCount/pagesize+1;
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		RemdInfoDao remdInfoDao = new RemdInfoDaoImpl();
		List<Apkdetail> apksList =new ArrayList<Apkdetail>();
		int doCount = 0;
		do {
			System.out.println("查询页数："+curpage);
			if(doCount>totalPage){//防止下面执行时出异常，不更新status，造成死循环
				return;
			}
			doCount++;
			apksList = apkDao.findApkdetails(sql, curpage, pagesize);
//			curpage++;//状态会变，一直取第0页即可
			System.out.println(apksList.size()+" "+curpage);
			List<String> colums = new ArrayList<String>();//需要更新的列
			String prePath = ProUtil.getString("Download_Path")+download_uuid+"/";
			for (int i = 0; i < apksList.size(); i++) {
				Apkdetail apk = null;
				Apkupload upload = new Apkupload();
				try {
					apk = apksList.get(i);
					System.out.println("检查："+apk.getPackagename()+" "+apk.getAppname());
					//先读取apk,顺便判断一下是否是别人的合作包
					//读取apk
					String apkurl = prePath+apk.getApkUrl();
					Map<String,String> apkinfo =AnalysisApk.unZip(apkurl, "");
					if(apk.getSoft().equals(2)){
						String isremd = apkinfo.get("isremd");
						if(!SUtil.isEmpty(isremd) && "y".equalsIgnoreCase(isremd)){
							//不上传别人的推广包
							apk.setStatus(10);//更新status = 5 文件全部下载完毕
							apk.setIsremd("1");
							upload.setStatus(10);
							upload.setIsremd("1");
							upload.setId(apk.getId());
							apkDao.update(apk, new String[]{"status","isremd"}, "where id = "+apk.getId());
							apkDao.update(upload, new String[]{"status","isremd"}, "where id = "+upload.getId());
							remdInfoDao.save(new RemdInfo(apk.getPackagename(), apk.getSource(),1));
							continue;
						}
					}
					
					List<String> file_path = new ArrayList<String>();
					String iconUrl = prePath+apk.getIconurl();
					file_path.add(iconUrl);
					String image_h = "";
					String type ="";
					if(!SUtil.isEmpty(apk.getRemarkimages_h())){
						image_h = apk.getRemarkimages_h();
						type = ConfigUtil.Image_h;
					}else if(!SUtil.isEmpty(apk.getRemarkimages_m())){
						image_h = apk.getRemarkimages_m();
						type = ConfigUtil.Image_m;
					}else if(!SUtil.isEmpty(apk.getRemarkimages_l())){
						image_h = apk.getRemarkimages_l();
						type = ConfigUtil.Image_l;
					}
					String[] tmp =null;
					List image_file_path = new ArrayList();
					if(image_h !=null && !SUtil.isEmpty(image_h)){
						tmp = ImageUtil.decodeJson(image_h);
						for(String tt:tmp){
							file_path.add(prePath+tt);
							image_file_path.add(prePath+tt);
						}
//					file_path.addAll(Arrays.asList(tmp));
					}
//				String[] images ="";
					file_path.add(prePath+apk.getApkUrl());
					//先检查下载的文件是否存在,只要有一个不存在，则返回false
					boolean b = isExistFile(file_path);
					if(!b){
						//文件不存在的跳过
						apk.setStatus(0);
						apkDao.update(apk, new String[]{"status"}, " where id = "+apk.getId());
						upload.setStatus(0);
						upload.setId(apk.getId());
						apkDao.update(upload, new String[]{"status"}, " where id = "+upload.getId());
						continue;
					}
					
					//生成图标各种分辨率
					Map<String, String>  iconMap = ImageMagickUtil.resizeAllPng(iconUrl, apk.getIconurl());
					System.out.println(JSONObject.toJSON(iconMap).toString());
					apk.setIconurl(JSONObject.toJSON(iconMap).toString());
					colums.add("iconurl");
					//生成介绍图片高中低三种分辨率(分开360和qq)
					if("360".equalsIgnoreCase(apk.getSource())){
						Map<String, String> imageMap = ImageMagickUtil.resizeAllJpg(image_file_path, tmp, type);
						if(imageMap!= null && imageMap.size()>0){
							String image_h_jsonStr = imageMap.get("image_h");
							String image_m_jsonStr = imageMap.get("image_m");
							String image_l_jsonStr = imageMap.get("image_l");
							if(image_h_jsonStr!=null && image_h_jsonStr.length()>5){
								apk.setRemarkimages_h(image_h_jsonStr);
								colums.add("remarkimages_h");
							}
							if(image_m_jsonStr!=null && image_m_jsonStr.length()>5){
								apk.setRemarkimages_m(image_m_jsonStr);
								colums.add("remarkimages_m");
							}
							if(image_l_jsonStr!= null && image_l_jsonStr.length()>5){
								apk.setRemarkimages_l(image_l_jsonStr);
								colums.add("remarkimages_l");
							}
						}
					}else{//适用于qq等，此处生成各种分辨率是根据原始图片的像素去决定
						Map<String, String> imageMap = ImageMagickUtil.resizeAllJpgForQQ(image_file_path, tmp);//会生成高中低的分辨率，某个分辨率没有的话，返回[]
						if(imageMap!=null){
							String image_h_jsonStr = imageMap.get("image_h");
							String image_m_jsonStr = imageMap.get("image_m");
							String image_l_jsonStr = imageMap.get("image_l");
							if(image_h_jsonStr!=null ){
								apk.setRemarkimages_h(image_h_jsonStr);
								colums.add("remarkimages_h");
							}
							if(image_m_jsonStr!=null ){
								apk.setRemarkimages_m(image_m_jsonStr);
								colums.add("remarkimages_m");
							}
							if(image_l_jsonStr!= null ){
								apk.setRemarkimages_l(image_l_jsonStr);
								colums.add("remarkimages_l");
							}
						}
					}
					
					
					//获取签名
					String sign = (String)apkinfo.get("signature");
					if(sign!=null && !SUtil.isEmpty(sign)){
						apk.setSignaturemd5(sign);
						colums.add("signaturemd5");
					}
					//比较抓取的包名和读取的包名是否一样
					if(!SUtil.isEmpty(apkinfo.get("packagename")) && !apk.getPackagename().equals(apkinfo.get("packagename"))){
						apk.setPackagename(apkinfo.get("packagename"));
						colums.add("packagename");
					}
					//qq抓取的没有权限和minsdkversion
					if(SUtil.isEmpty(apk.getPermission())){
						apk.setPermission(apkinfo.get("permission"));
						colums.add("permission");
						apk.setMinsdkversion(SUtil.formatStr(apkinfo.get("minsdkversion")));
						colums.add("minsdkversion");
					}
					if(SUtil.isEmpty(apk.getVersionname())){
						apk.setVersionname(apkinfo.get("versionname"));
						colums.add("versionname");
					}
					
					//计算自定义的filecode
					String filecode = CheckFileCode.getFileCheckCode(new File(apkurl));
					apk.setFilecode(filecode);
					colums.add("filecode");
					apk.setStatus(6);//文件缩略图生成成功
					colums.add("status");
					String[] update_column = (String[])colums.toArray(new String[0]);
					apkDao.update(apk, update_column, "where id = "+apk.getId());
					//赋值给upload
					BeanUtils.copyProperties(upload, apk);
					apkDao.update(upload, update_column, "where id = "+upload.getId());
				} catch (Exception e) {
					if(apk!=null){
						apk.setStatus(8);
						apkDao.update(apk, new String[]{"status"}, "where id = "+apk.getId());
						upload.setStatus(8);
						apkDao.update(upload, new String[]{"status"}, "where id = "+apk.getId());
					}									
					e.printStackTrace();
					log.error("检查文件错误："+apk.getAppname(),e);
				}
			}
		} while (apksList.size()>0);
		
		//修改上传批次号为1 --检查完成
		urldownloadDao.update(new Urldownload(download_uuid, 4), new String[]{"status"}, " where uuid = '"+download_uuid+"'");
		log.error("生成缩略图完毕--批次号:"+download_uuid);
	}
	
	public static boolean isExistFile(List<String> filepath){
		boolean b = true;
		out:
		for(String path:filepath){
			if(!new File(path).exists()){
				b = false;
				System.out.println("文件不存在："+path);
				break out;
			}
		}
		return b;
	}
	
	/**
	 * 只适应于刚下载后的检查
	 * 注意检查图片文件是否成功下载，apk是否完整
	 * @param download_uuid
	 */
	public static void checkFileDown(String download_uuid){
//		String sql ="select * from app_detail_info app where  uploadid = '"+download_uuid.trim()+"'  AND status = 3  limit ?,? ";
		String sql ="select * from app_upload app where  uploadid = '"+download_uuid.trim()+"'  AND status = 3  limit ?,? ";
		Integer curpage =0,pagesize = 1000;
		ApkdetailDao apkDao = new ApkdetailDaoImpl();
		ErrorLogDao errorLogDao = new ErrorLogDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		List<Apkdetail> apksList =new ArrayList<Apkdetail>();
		String pre_path = ProUtil.getString("Download_Path")+download_uuid+"/";
		do {
			apksList = apkDao.findApkdetails(sql, curpage, pagesize);
			curpage++;			
			System.out.println(apksList.size());
			for (int i = 0; i < apksList.size(); i++) {
				try {
					List<String> colums = new ArrayList<String>();//需要更新的列
					Apkdetail apk = apksList.get(i);							
					List<String> file_path = new ArrayList<String>();
					file_path.add(pre_path+apk.getIconurl());//图标
					String image_h = "";
					String type ="";
					if(!SUtil.isEmpty(apk.getRemarkimages_h())){
						image_h = apk.getRemarkimages_h();
						type = ConfigUtil.Image_h;
					}else if(!SUtil.isEmpty(apk.getRemarkimages_m())){
						image_h = apk.getRemarkimages_m();
						type = ConfigUtil.Image_m;
					}else if(!SUtil.isEmpty(apk.getRemarkimages_l())){
						image_h = apk.getRemarkimages_l();
						type = ConfigUtil.Image_l;
					}
					String[] tmp =null;
					if(image_h !=null && !SUtil.isEmpty(image_h)){
						tmp = ImageUtil.decodeJson(image_h);
						for(String tt:tmp){
							file_path.add(pre_path+tt);
						}
					}
					file_path.add(pre_path+apk.getApkUrl());
					//只要有一个文件不存在就返回false
					boolean b = isExistFile(file_path);					
					
					if(b){
						//再次检查apk的MD5，大小
						File file = new File(pre_path+apk.getApkUrl());
						String md5 =  GetBigFileMD5.getMD5(file);
						if(apk.getApkmd5()!=null && apk.getApkmd5().equalsIgnoreCase(md5)){//md5对的上再去比对文件大小
							if(apk.getSize()!= file.length()){
								apk.setSize(file.length());
								apk.setStatus(5);
								apkDao.update(apk, new String[]{"status","size"}, "where id = "+apk.getId());
								colums.add("status");
								colums.add("size");
							}else {
								apk.setStatus(5);//更新status = 5 文件全部下载完毕
								apkDao.update(apk, new String[]{"status"}, "where id = "+apk.getId());
								colums.add("status");
							}
						}else {//MD5不相等，记录log
							System.out.println(apk.getPackagename()+" md5:"+md5+"---"+apk.getApkmd5());
							apk.setStatus(11);//更新status = 2 重新下载
							apkDao.update(apk, new String[]{"status"}, "where id = "+apk.getId());
							colums.add("status");
							errorLogDao.save(new ErrorLog(download_uuid, apk.getPackagename(), "md5不匹配", "down_before", "", ""));
						}
						
					}else {//不成功的话。。。
						System.out.println("文件下载不成功：" +apk.getAppname()+"--"+apk.getPackagename());
						apk.setStatus(0);//更新status = 0 文件下载失败
						apkDao.update(apk, new String[]{"status"}, "where id = "+apk.getId());
						colums.add("status");
					}
					//更新apkupload的字段
					Apkupload upload = new Apkupload();
					BeanUtils.copyProperties(upload, apk);
					String[] update_column = (String[])colums.toArray(new String[0]);
					apkDao.update(upload, update_column, " where id = "+upload.getId());
				} catch (Exception e) {
					log.error("刚下载后的检查错误信息：", e);
					e.printStackTrace();
				}
			}
		} while (apksList.size()>0);
		urldownloadDao.update(new Urldownload(download_uuid, 2), new String[]{"status"}, " where uuid = '"+download_uuid+"'");
		log.error("apk刚下载后的检查完毕，批次号："+download_uuid);
		
	}
	
	/**
	 * 检查360下载的游戏包是否是合作 包
	 */
	public static boolean isremdApk(String apkurl){
		File f = new File(apkurl);
		if(f.exists()){
			Map<String, String> map = AnalysisApk.unZip(apkurl, "");
			String isremd = map.get("isremd");
			if(!SUtil.isEmpty(isremd) && "y".equalsIgnoreCase(isremd)){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 检查生成缩略图之后的文件是否存在
	 * @param download_uuid
	 */
	public static void checkDownAfter(String download_uuid){
//		String sql ="select * from app_detail_info app where  uploadid = '"+download_uuid.trim()+"' and status = 6 limit ?,? ";
		String sql ="select * from app_upload app where  uploadid = '"+download_uuid.trim()+"' and status = 6 limit ?,? ";
		Integer curpage =0,pagesize = 1000;
		ApkdetailDao apkDao = new ApkdetailDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		ErrorLogDao errorLogDao = new ErrorLogDaoImpl();
		List<Apkdetail> apksList =new ArrayList<Apkdetail>();
		do {
			apksList= apkDao.findApkdetails(sql, curpage, pagesize);
			String prePath = ProUtil.getString("Download_Path")+download_uuid+"/";
			curpage++;
			for (int i = 0; i < apksList.size(); i++) {
				Apkdetail apkdetail = apksList.get(i);
				Apkupload upload = new Apkupload();
				System.out.println(apkdetail.getIconurl());
				List<String> fileList = getFilePath(apkdetail,prePath);
				boolean b = isExistFile(fileList);
				if(b){//都存在
					apkdetail.setStatus(7);	
					upload.setStatus(7);
				}else {
					apkdetail.setStatus(8);//生成缩略图失败
					upload.setStatus(8);
					errorLogDao.save(new ErrorLog(download_uuid, apkdetail.getPackagename(), "生成缩略图失败", "down_before", "", ""));
				}
				upload.setId(apkdetail.getId());
				apkDao.update(apkdetail, new String[]{"status"}, " where id = "+apkdetail.getId());
				apkDao.update(upload, new String[]{"status"}, " where id = "+upload.getId());
			}
			
		} while (apksList.size()>0);
		log.error("检查生成缩略图之后的文件是否存在，批次号："+download_uuid);
		
		Urldownload url = urldownloadDao.getByuuid(download_uuid);
		url.setStatus(2);//检查完成更新状态为2
		urldownloadDao.update(url, new String[]{"status"}, " where id = "+url.getId() );
	}
	
	/**
	 * 复制图片到另外的目录，去准备上传
	 * @param download_uuid
	 */
	public static void copyToupload(String download_uuid){
//		String sql ="select * from app_detail_info app where  uploadid = '"+download_uuid.trim()+"' and status = 7 limit ?,? ";
		String sql ="select * from app_upload app where  uploadid = '"+download_uuid.trim()+"' and status = 7 limit ?,? ";
		Integer curpage =0,pagesize = 1000;
		ApkdetailDao apkDao = new ApkdetailDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		List<Apkdetail> apksList =new ArrayList<Apkdetail>();
		do {
			apksList= apkDao.findApkdetails(sql, curpage, pagesize);
			curpage++;
			String download_path = ProUtil.getString("Download_Path")+download_uuid+"/";
			String upload_path = ProUtil.getString("Upload_Path");
			for (int i = 0; i < apksList.size(); i++) {
				Apkdetail apkdetail = apksList.get(i);
				System.out.println(apkdetail.getIconurl());
				List<String> fileList = getFilePath(apkdetail,download_path);
				for(String filePath:fileList){
					System.out.println(filePath);
					String  destFileName = filePath.replace(download_path, upload_path);
					boolean b =CopyFileUtil.copyFile(filePath, destFileName, true);
					if(b){
						new File(filePath).delete();//删除源文件
					}
				}
			}
			
		} while (apksList.size()>0);
		log.error("复制检查完毕，批次号："+download_uuid);
		Urldownload url = urldownloadDao.getByuuid(download_uuid);
		url.setStatus(3);//准备上传
		url.setDownendtime(new Date());		
		Object[] rs = urldownloadDao.getuploadApkInfo(download_uuid);
		if(rs!=null){
			url.setUploadcount(SUtil.formatStr(rs[0]));
			url.setUploadsize(SUtil.convertSize(rs[1]));
		}
		urldownloadDao.update(url, new String[]{"status","downendtime","uploadcount","uploadsize"}, " where id = "+url.getId() );
	}
	
	@Test
	public void testcopyToupload(){
		copyToupload("948a741f9214431ea7aeaf3a80b304e9");
		
	}
	
	@Test
	public void test(){
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		Object[] rs = urldownloadDao.getuploadApkInfo("ec9b2099ebcf4592ae3abfda994d4baf");
		System.out.println(rs[0]+"=="+rs[1]);
	}
	//返回apkdetai的图片，apk文件路径
	public static List<String> getFilePath(Apkdetail apkdetail,String pre_path){
		String iconurljson = apkdetail.getIconurl();
//		String pre_path = ConfigUtil.Download_Path+apkdetail.getEm5()+"/";
		List<String> fileList =new ArrayList<String>();
		Map<String,String> iconMap = (Map<String,String>)JSONObject.parse(iconurljson);
		for(Map.Entry<String, String> entry:iconMap.entrySet()){
			System.out.print(entry.getValue()+" ");
			fileList.add(pre_path+entry.getValue());
		}
		String image_h_json = apkdetail.getRemarkimages_h();
		String[] image_hs = ImageUtil.decodeJson(image_h_json);
		System.out.println("image_hs:"+image_hs);
		for (int i =0;image_hs !=null && i < image_hs.length; i++) {
			fileList.add(pre_path+image_hs[i]);
		}
		
		String image_m_json = apkdetail.getRemarkimages_m();
		String[] image_ms = ImageUtil.decodeJson(image_m_json);
		for(String tmp:image_ms){
			fileList.add(pre_path+tmp);
		}
		String image_l_json = apkdetail.getRemarkimages_l();
		String[] image_ls = ImageUtil.decodeJson(image_l_json);
		for(String tmp:image_ls){
			fileList.add(pre_path+tmp);
		}
		fileList.add(pre_path+apkdetail.getApkUrl());
		return fileList; 
	}
	
	@Test
	public void testcheckDownAfter(){
		String download_uuid ="0c95d5dbeeff4fccb3653bc8f377f6ad";
		checkDownAfter(download_uuid);
		
	}
	
	
}
