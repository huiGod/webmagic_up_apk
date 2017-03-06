package com.uq.spider.common.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.alibaba.fastjson.JSONObject;
import com.chinanetcenter.api.domain.HttpClientResult;
import com.chinanetcenter.api.domain.PutPolicy;
import com.chinanetcenter.api.util.Config;
import com.chinanetcenter.api.util.DateUtil;
import com.chinanetcenter.api.util.EncodeUtils;
import com.chinanetcenter.api.wsbox.FileManageCommand;
import com.chinanetcenter.api.wsbox.FileUploadCommand;
import com.uq.dao.PkginfoDao;
import com.uq.dao.impl.PkginfoDaoImpl;
import com.uq.model.Apkdetail;
import com.uq.model.Pkginfo;
import com.uq.util.HttpUtils;
import com.uq.util.ProUtil;

/**
 * 多线程上传文件
 * @ClassName: UploadFileTask 
 * @author aurong
 * @date 2015-5-20 下午01:56:28
 * change 2015-07-30 加入线程执行超时时间的控制
 */
public class UploadFileTask implements Callable<String>{//implements Runnable

	private final static long FileSize = 5*1024*1024L;//大于该大小的用分片上传
	private  static String BucketName_Img =ProUtil.getString("BucketName_Img");
	private  static String BucketName_Apk =ProUtil.getString("BucketName_Apk");
	private  static String AK =ProUtil.getString("AK");
	private  static String SK =ProUtil.getString("SK");
	
	private static PkginfoDao pkginfoDao = new PkginfoDaoImpl();
	
//	private final static String BucketName_Img =ProUtil.getString("BucketName_test");
//	private final static String BucketName_Apk =ProUtil.getString("BucketName_test");
//	private final static String AK =ProUtil.getString("AK_test");
//	private final static String SK =ProUtil.getString("SK_test");
	
	
	
	private List<String> fileKeys;
	private String pre_path;//文件的前半部分路径
	private Apkdetail apk;
	private String type;//要同步的类型 new 第一次上传,error 上传错误的
//	private String packagename;
	
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
	
	public UploadFileTask(List<String> fileKeys,String pre_path,Apkdetail apk,String type){
		this.fileKeys = fileKeys;
		this.pre_path = pre_path;
		this.apk = apk;
		this.type = type;
	}

	
/*	@Override
	public void run() {	
		if("new".equalsIgnoreCase(this.type)){
			firstUpload();
		}else if("error".equalsIgnoreCase(this.type)){
			errorUpload();
		}
	
	}*/
	
	@Override
	public String call() throws Exception {
		if("new".equalsIgnoreCase(this.type)){
			firstUpload();
		}else if("error".equalsIgnoreCase(this.type)){
			errorUpload();
		}
		return "suc";
	}
	
	//同步第一次先上传的文件
	public void firstUpload(){
		if(apk!=null){
			System.out.println("正在上传: "+apk.getAppname()+" 这个应用!");
		}		
		boolean b = multiUploadFile(fileKeys,pre_path);
		if(b){
			Map<String,String> params = new HashMap<String, String>();
			params.put("packageName", apk.getPackagename());
			params.put("versionCode", apk.getVersioncode().toString());
			//修改为连接不上时重试5次
			int retry = 0;
			boolean flag = false;
			while(retry<5){
				retry++;
				try {
					HttpUtils.get(ProUtil.getString("UpdateFileStatus"), params, 30, "utf-8");
					flag = true;
					break;//成功请求跳出
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
			
			//试了5次还错误的插入pkginfo，status= 1,type = 2
			 if(!flag){
				 pkginfoDao.save(new Pkginfo(apk.getPackagename(), apk.getVersioncode(),2));//上报信息失败
			 }
		}else {//上传文件失败的,status= 1,type = 1
			 pkginfoDao.save(new Pkginfo(apk.getPackagename(), apk.getVersioncode(),1));
		}
	}

	//重新上传 --上传失败的
	public void errorUpload(){
		if(apk!=null){
			System.out.println("正在上传: "+apk.getAppname()+" 这个应用!");
		}		
		boolean b = multiUploadFile(fileKeys,pre_path);
		if(b){
			Map<String,String> params = new HashMap<String, String>();
			params.put("packageName", apk.getPackagename());
			params.put("versionCode", apk.getVersioncode().toString());
			//修改为连接不上时重试5次
			int retry = 0;
			boolean flag = false;
			while(retry<5){
				retry++;
				try {
					HttpUtils.get(ProUtil.getString("UpdateFileStatus"), params, 30, "utf-8");
					flag = true;
					break;//成功请求跳出
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
			
			//上传成功的，更新状态为3
			 if(flag){				 
				 pkginfoDao.update("update pkginfo set status = 3 where packagename ='"+apk.getPackagename()+"' and versioncode = "+apk.getVersioncode());
			 }
		}
	}
	
	//批量上传文件
	public static  boolean multiUploadFile(List<String> fileKeys,String pre_path){
		boolean flag = true;
		boolean apkflag = true;
		try {
			List<File> fileList = new ArrayList<File>(); 
			String returnBody = "key=$(key)&fname=$(fname)&fsize=$(fsize)&url=$(url)&hash=$(hash)";
			StringBuilder scope = new StringBuilder();
			for(String filekey:fileKeys){
				File f = new File(pre_path+filekey);
				if(!f.exists())continue;//不存在，不要上传
				if(!f.getName().toLowerCase().endsWith("apk")){
					fileList.add(f);
					scope.append(EncodeUtils.urlsafeEncode(BucketName_Img+":"+filekey+":"+f.getName())).append("#");
				}else if(f.getName().toLowerCase().endsWith("apk")){//apk文件大于7M的用分片上传
					int retry = 0;
					out:
					while(retry<3){
						retry++;
						apkflag = uploadFile(filekey, f.getAbsolutePath());
						if(apkflag){
							break out;
						}
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
			if(httpClientResult!=null && httpClientResult.getStatus()== 200){
				try {
					JSONObject jsonObject =JSONObject.parseObject(httpClientResult.response);
					int failNum  = JSONObject.parseObject(jsonObject.getString("brief")).getIntValue("failNum");
					if(failNum>=1){
						flag = false;
						System.out.println("失败个数："+failNum);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
		
				
			}else {
				flag = false;
			}
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		return flag&&apkflag;
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
			if(!file.exists()) return true;
			if(localFilePath.toLowerCase().endsWith("apk")){
				//apk文件的话，先判断一下是否存在
				if(isExist(fileName))return true;//已经存在，返回true
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
			if(fileKey.toLowerCase().endsWith("apk")){
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
	
	public static void main(String[] args) {
//		uploadFile("icon/2015/0519/h9l11/f56031af88af48b49727bebd77cf0006.png","F:/filedown/a99b638132844aaf80a5f35036ae9158/icon/2015/0519/h9l11/f56031af88af48b49727bebd77cf0006.png");
		isExist("icon/2015/0519/h9l11/f56031af88af48b49727bebd77cf0006_sa.png");
	}


	
}
