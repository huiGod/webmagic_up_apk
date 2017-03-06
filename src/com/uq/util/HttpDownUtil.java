package com.uq.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

//下载文件
public class HttpDownUtil {
	
	public static boolean downFile(String url,String savePath){
			HttpClient httpClient = new DefaultHttpClient();  
			boolean b = false;
	        HttpGet httpGet = new HttpGet(url);  
	        try {
				HttpResponse httpResponse = httpClient.execute(httpGet);  
  
				StatusLine statusLine = httpResponse.getStatusLine();  
				if (statusLine.getStatusCode() == 200) {  
					FileDownloadUtil.checkDirExist(savePath);//检查目录是否存在,不存在会创建	
				    File xml = new File(savePath);  
				    FileOutputStream outputStream = new FileOutputStream(xml);  
				      
				    InputStream inputStream = httpResponse.getEntity().getContent();  
				      
				    byte buff[] = new byte[4096];  
				    int counts = 0;  
				    while ((counts = inputStream.read(buff)) != -1) {   
				        outputStream.write(buff, 0, counts);  
				          
				    }  
				    outputStream.flush();  
				    outputStream.close(); 
				    b = true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  
	  
	        httpClient.getConnectionManager().shutdown();  
	        System.out.println("success: "+savePath);
	        return b;
	}
}
