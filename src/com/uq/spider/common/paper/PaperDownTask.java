package com.uq.spider.common.paper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.spider.common.apk.FileDownCore;
import com.uq.util.FileDownloadUtil;


/**
 * @ClassName: PaperDownTask 
 * @Description: webmagic的下载不稳定，自己用httpclient下载
 * @author aurong
 * @date 2015-7-22 上午11:19:30
 */
public class PaperDownTask implements Runnable{

	private Map<String, String> downUrl = new HashMap<String, String>();//存放下载url和下载路径
	public String rootPath ="F://filedown/";
	private static Logger log = LoggerFactory.getLogger(PaperDownTask.class);
	
	public PaperDownTask(){}
	public PaperDownTask(String rootPath,Map<String, String> map){
		this.rootPath = rootPath;
		this.downUrl = map;
	}
	
	@Override
	public void run() {
		try {
			for(Map.Entry<String, String> entry:downUrl.entrySet()){			
				if(FileDownCore.rsyncPool.getPoolSize()<2){
					System.out.println("下载的路径："+entry.getKey());
				}
				down(entry.getKey(), rootPath+entry.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("线程run异常"+e);
		}
	}
	
	public void down(String url,String savePath){
		// 生成一个httpclient对象
		int retry = 0;
		while(retry<5){//重试次数
			retry++;
			try {
				CloseableHttpClient httpclient = HttpClients.createDefault();
//				httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,2000);//连接时间				
				HttpGet httpget =new HttpGet(url);
				RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();//设置请求和传输超时时间
				httpget.setConfig(requestConfig);
				HttpResponse response = httpclient.execute(httpget);
				int statusCode = response.getStatusLine().getStatusCode();
				System.out.println("返回码："+statusCode);
				if(response.getStatusLine().getStatusCode() == 200){
					String length = response.getFirstHeader("Content-Length").getValue();//文件大小
					System.out.println(length);
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					File file =new File(savePath);
					try{
						FileDownloadUtil.checkDirExist(savePath);//检查目录是否存在,不存在会创建		
					    FileOutputStream fout =new FileOutputStream(file);
					    int l = -1;
					    byte[] tmp =new byte[1024*5];
					    while((l = in.read(tmp)) != -1) {
					        fout.write(tmp,0, l);
					    }
					    fout.flush();
					    fout.close();
					    if(String.valueOf(file.length()).equals(length)){
					    	break;
					    }else {
							System.out.println("文件下载错误--："+file.getAbsolutePath()+"==="+url);
						}
					    System.out.println(file.length());
					}catch (Exception e) {
						log.error("第"+retry+"次文件文件下载错误:"+url,e);
					}finally{
					    // 关闭低层流。
					    in.close();
					}
				}
				httpclient.close();
			} catch (Exception e) {
				log.error("DownloadTask error:",e);
			}
		}

	}
	
	public static void main(String[] args) {
		new PaperDownTask().down("http://dd.myapp.com/16891/7AF80D309E13CEDD09C2AAC6A588B88C_tt.apk?fsname=com.ninefang.xjtx.wdj_1.2.0_120.apk&asr=8eff", "f:/test/123s.apk");
	}
}
