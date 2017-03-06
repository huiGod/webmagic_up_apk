package com.uq.spider.common.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.chinanetcenter.api.domain.HttpClientResult;
import com.chinanetcenter.api.domain.PutPolicy;
import com.chinanetcenter.api.util.Config;
import com.chinanetcenter.api.util.DateUtil;
import com.chinanetcenter.api.util.EncodeUtils;
import com.chinanetcenter.api.wsbox.FileManageCommand;
import com.chinanetcenter.api.wsbox.FileUploadCommand;
import com.uq.dao.ApkdetailDao;
import com.uq.dao.ErrorLogDao;
import com.uq.dao.impl.ApkdetailDaoImpl;
import com.uq.dao.impl.ErrorLogDaoImpl;
import com.uq.model.Apkdetail;
import com.uq.model.ErrorLog;
import com.uq.util.ImageUtil;
import com.uq.util.ProUtil;

/**
 * @ClassName: WcsUploadFile 
 * @Description: 上传文件到网宿云存储 
 * @author aurong
 * @date 2015-5-12 下午07:32:54
 */
public class WcsUploadFile {

	private final static long FileSize = 7*1024*1024L;//大于该大小的用分片上传
	private  static String BucketName_Img =ProUtil.getString("BucketName_Img");
	private  static String BucketName_Apk =ProUtil.getString("BucketName_Apk");
	private  static String BucketName_paper =ProUtil.getString("BucketName_Paper");
	private  static String AK =ProUtil.getString("AK");
	private  static String SK =ProUtil.getString("SK");
	
	
	static{
		String isTest = ProUtil.getString("upload_test");
		if("true".equalsIgnoreCase(isTest)){
		    AK =ProUtil.getString("AK_test");
			SK =ProUtil.getString("SK_test");
			BucketName_Img =ProUtil.getString("BucketName_test");
			BucketName_Apk =ProUtil.getString("BucketName_test");
		}
		System.out.println(isTest);
		Config.init(AK, SK);
	}
	
	public static void main(String[] args) {
		String testname ="max/2015/0729/0dr3p/00a2f75137c0467cb87b1d3faca9c471.jpg";
		String fileName =testname;
		String localFilePath ="Z:/fileupload/"+testname;
	    uploadpaper(fileName,localFilePath);
//		System.out.println(b);
//		String download_uuid ="0358762719054c119b4bad6094513fb5";
//		preUpload(download_uuid);
//		isExist("apk/2015/0612/i7cfm/cdeb2354b5db803e927c0b78f2bb370a/com.zhihu.daily.android.apk");
	}
	
	@Before
	public void init(){
		System.out.println("init");
		Config.init(AK, SK);
	}
	
	@Test
	public void testUpload(){
		String download_uuid ="0548fd02cfe040c0b2751a4058ef34ea";
		preUpload(download_uuid);
	}
	//从数据库查询生成缩略图成功的批次号，统一上传
	public static void preUpload(String download_uuid){
		ApkdetailDao apkdetailDao = new ApkdetailDaoImpl();
		String sql ="select * from app_detail_info where uploadid = '"+download_uuid+"' and status = 7 limit ?,?";
		System.out.println(sql);
		List<Apkdetail> apkList = new ArrayList<Apkdetail>();
		int page =0,pagesize = 1000;
		ErrorLogDao logDao = new ErrorLogDaoImpl();
		do {
			apkList = apkdetailDao.findApkdetails(sql, page, pagesize);
			page++;
			long start = new Date().getTime();
			for (int i = 0; i < apkList.size(); i++) {
				Apkdetail apk = apkList.get(i);
				List<String> uploadList = getFilePath(apk,"");// ConfigUtil.Upload_Path
				long start1 = new Date().getTime();
				boolean b = multiUploadFile(uploadList,ProUtil.getString("Upload_Path")+"/");// 14s
				long end1 = new Date().getTime();
				System.out.println("单个apk所有文件上传耗时："+(end1-start1)/1000+" s");
				if(!b){//文件上传错误
					logDao.save(new ErrorLog("up_file_err", apk.getPackagename(), "", "", null, null));
				}else {//上传成功
					Map<String,String> params = new HashMap<String, String>();
					params.put("packageName", apk.getPackagename());
					params.put("versionCode", apk.getVersioncode().toString());
					try {
//						HttpUtils.get(ProUtil.getString("UpdateFileStatus"), params, 20, "utf-8");
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				/*for (String filekey:uploadList) {
					boolean b = uploadFile(filekey,ConfigUtil.Download_Path+download_uuid+"/"+filekey);
					if(!b){
						System.out.println(filekey);
					}
				}*/
			}
			long end = new Date().getTime();
			System.out.println("总共耗时："+(end-start)/1000+" s");
		} while (apkList.size()>0);
		
		
	}
	
	/**
	 * 
	 * @Description: 上传单个文件 
	 * @param @param fileName
	 * @param @param localFilePath
	 * @param @return
	 * @date 2015-5-19
	 * @author	aurong
	 * @return boolean
	 */
	public static boolean uploadFile(String fileName,String localFilePath){
		String returnBody = "key=$(key)&fname=$(fname)&fsize=$(fsize)&url=$(url)&hash=$(hash)";
		HttpClientResult httpClientResult = null;
		boolean result = false;
		try {
			File file = new File(localFilePath);
			if(localFilePath.endsWith("apk")){
				if(file.length()>= FileSize){
					result = new SliceUploadFile().sliceUpload(BucketName_Apk, fileName, localFilePath);//分片上传
				}else {
					httpClientResult = FileUploadCommand.uploadFileAndReturn(BucketName_Apk, fileName, localFilePath, returnBody);//正常上传
					if(httpClientResult!=null && httpClientResult.getStatus() == 200){
						result = true;
						System.out.println(httpClientResult.getStatus()+"--"+httpClientResult.response);
					}
				}
				
			}else {
				httpClientResult = FileUploadCommand.uploadFileAndReturn(BucketName_Img, fileName, localFilePath, returnBody);
				if(httpClientResult!=null && httpClientResult.getStatus() == 200){
					result = true;
					System.out.println(httpClientResult.getStatus()+"--"+httpClientResult.response);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
		
	}
	
	//上传壁纸
	public static void uploadpaper(String fileName,String localFilePath){
		String returnBody = "key=$(key)&fname=$(fname)&fsize=$(fsize)&url=$(url)&hash=$(hash)";
		HttpClientResult httpClientResult = null;
		httpClientResult = FileUploadCommand.uploadFileAndReturn(BucketName_paper, fileName, localFilePath, returnBody);
		if(httpClientResult!=null && httpClientResult.getStatus() == 200){
			System.out.println(httpClientResult.getStatus()+"--"+httpClientResult.response);
		}
	}
	
	@Test
	public void testex(){
		System.out.println(isExist("image/2015/0519/79s1t/68f5f12c69ab476a84f180354c2df2d1_ds.jpg"));
	}
	/**
	 * 
	 * @Description: 判断文件是否存在wcs 
	 * @param fileKey 文件保存时的key
	 * @date 2015-5-19
	 * @author	aurong
	 * @return boolean
	 */
	public static boolean isExist(String fileKey){
		HttpClientResult httpClientResult = null;
		boolean flag = true;
		try {
			if(fileKey.endsWith("apk")){
				httpClientResult =FileManageCommand.stat(BucketName_Apk, fileKey);
			}else {
				httpClientResult =FileManageCommand.stat(BucketName_Img, fileKey);
			}
			
			//先获取文件的信息，判断是否存在
		System.out.println(httpClientResult.getStatus()+":"+httpClientResult.getResponse());
		if(httpClientResult.getStatus()!= 200){
			flag = false;
		}
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}
	
	//返回apkdetai的图片，apk文件路径
	/**
	 *  pre_path 图片保存的前半部分，数据库保存的是相对路径，为空则返回相对路径
	 */
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
	
	//普通上传
	@Test
	public void testUploafFile(){
		String testname ="com.nitrome.greenninja.apk";
		String fileName ="apk/2015/0613/m7cx6/6a0c0ef33e7b338216fdc1d89b5b9ebf/"+testname;
		String localFilePath ="z:/fileupload/apk/2015/0613/m7cx6/6a0c0ef33e7b338216fdc1d89b5b9ebf/"+testname;
		String returnBody = "key=$(key)&fname=$(fname)&fsize=$(fsize)&url=$(url)&hash=$(hash)";
//		String returnBody = readfile("c:/1.txt");
		HttpClientResult httpClientResult = null;
		if(fileName.endsWith("apk")){
			httpClientResult = FileManageCommand.stat(BucketName_Apk, fileName);
		}else {
			 httpClientResult = FileManageCommand.stat(BucketName_Img, fileName);
		}
		
		System.out.println(httpClientResult.getStatus()+":"+httpClientResult.getResponse());
		System.out.println(httpClientResult.getReturnCodeContent());
		JSONObject jsonObject = JSONObject.parseObject(httpClientResult.getResponse());
		System.out.println(jsonObject.getLongValue("fsize"));
		System.out.println(new File(localFilePath).length());
		 httpClientResult = FileUploadCommand.uploadFileAndReturn(BucketName_Apk, fileName, localFilePath, returnBody);
		System.out.println(httpClientResult.response);
	}
	
	//批量上传
	@Test
	public void testuploadList(){
		List<File> fileList = new ArrayList<File>(); 
		String returnBody = "key=$(key)&fname=$(fname)&fsize=$(fsize)&url=$(url)&hash=$(hash)";
		fileList.add(new File("F:/filedown/0c95d5dbeeff4fccb3653bc8f377f6ad/image/2015/0511/tx93k/895f17ab7ec44547bb6b3527b6da3429.jpg"));
		fileList.add(new File("F:/filedown/0c95d5dbeeff4fccb3653bc8f377f6ad/image/2015/0511/tx93k/b53bdde6c87c43c3826fe5b4ca2a1296.jpg"));
		 StringBuilder scope = new StringBuilder();
	     scope.append(EncodeUtils.urlsafeEncode("weiap:image/2015/0511/tx93k/895f17ab7ec44547bb6b3527b6da3429.jpg:895f17ab7ec44547bb6b3527b6da3429.jpg")).append("#");
	     scope.append(EncodeUtils.urlsafeEncode("weiap:image/2015/0511/tx93k/b53bdde6c87c43c3826fe5b4ca2a1296.jpg:b53bdde6c87c43c3826fe5b4ca2a1296.jpg"));
		PutPolicy putPolicy = new PutPolicy();
		putPolicy.setOverwrite(1);
		 putPolicy.setScope(scope.toString());
//		putPolicy.setReturnBody(returnBody);
		 putPolicy.setDeadline(String.valueOf(DateUtil.nextHours(1, new Date()).getTime()));
		HttpClientResult httpClientResult = FileUploadCommand.multiUpload(fileList, putPolicy);
		System.out.println(httpClientResult.getStatus()+"--"+httpClientResult.getResponse());
		
	}
	//批量上传文件
	public static  boolean multiUploadFile(List<String> fileKeys,String pre_path){
		boolean flag = true;
		try {
			List<File> fileList = new ArrayList<File>(); 
			String returnBody = "key=$(key)&fname=$(fname)&fsize=$(fsize)&url=$(url)&hash=$(hash)";
			StringBuilder scope = new StringBuilder();
			for(String filekey:fileKeys){
				File f = new File(pre_path+filekey);
				if(!f.exists())continue;
				if(!f.getName().endsWith("apk")){
					fileList.add(f);
					scope.append(EncodeUtils.urlsafeEncode(BucketName_Img+":"+filekey+":"+f.getName())).append("#");
				}else if(f.getName().endsWith("apk")){//apk文件大于7M的用分片上传
//					boolean b = new SliceUploadFile().sliceUpload(BucketName_Apk, filekey, f.getAbsolutePath());
					boolean b = uploadFile(filekey, f.getAbsolutePath());
					if(!b){
						flag = false;
					}
				}
				
			}
			scope.substring(0, scope.length()-1);
			PutPolicy putPolicy = new PutPolicy();
			putPolicy.setOverwrite(1);
			putPolicy.setScope(scope.toString());
//			putPolicy.setReturnBody(returnBody);
			putPolicy.setDeadline(String.valueOf(DateUtil.nextHours(1, new Date()).getTime()));
			HttpClientResult httpClientResult = FileUploadCommand.multiUpload(fileList, putPolicy);
			System.out.println(httpClientResult.getStatus()+"--"+httpClientResult.getResponse());
			if(httpClientResult!=null && httpClientResult.getStatus()!= 200){
				flag = false;
			}
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}
	
}
