package com.uq.util;

import java.io.File;

import com.chinanetcenter.api.domain.HttpClientResult;
import com.chinanetcenter.api.util.Config;
import com.chinanetcenter.api.wsbox.FileManageCommand;
import com.chinanetcenter.api.wsbox.FileUploadCommand;


/**
 * 网宿工具辅助类
 * @author jinrong
 *
 */
public class WcsUtil {

	private  static String AK =ProUtil.getString("AK");
	private  static String SK =ProUtil.getString("SK");
	
	static{
		String isTest = ProUtil.getString("upload_test");
		if("true".equalsIgnoreCase(isTest)){
		    AK =ProUtil.getString("AK_test");
			SK =ProUtil.getString("SK_test");
		}
		System.out.println(isTest);
		Config.init(AK, SK);
	}
	
	public static void main(String[] args) {
		
		deleteFile("audio/2015/0806/xhxg3/132f9763e7044711a1a4e75a5c8c76dc.mp3","uq-ring");
	}
	
	
	public static boolean deleteFile(String fileKey,String bucketName){
		if(SUtil.isEmpty(fileKey)){
			return false;
		}
		HttpClientResult httpClientResult = null;
		boolean flag = true;
		try {
			httpClientResult =FileManageCommand.delete(bucketName, fileKey);			
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
	
	/**
	 * 上传单个文件
	 * @return
	 */
	public static boolean uploadSigleFile(String fileKey,String localFilePath, String bucketName){
		String returnBody = "key=$(key)&fname=$(fname)&fsize=$(fsize)&url=$(url)&hash=$(hash)";
		HttpClientResult httpClientResult = null;
		boolean result = false;
		try {
			File file = new File(localFilePath);
			if(!file.exists()) return true;
			httpClientResult = FileUploadCommand.uploadFileAndReturn(bucketName, fileKey, localFilePath, returnBody);
				
			if(httpClientResult!=null && httpClientResult.getStatus() == 200){
					result = true;
					System.out.println(httpClientResult.getStatus()+"--"+httpClientResult.response);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
