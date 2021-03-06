package com.uq.spider.common.apk;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.ApkdetailDao;
import com.uq.dao.ErrorLogDao;
import com.uq.dao.impl.ApkdetailDaoImpl;
import com.uq.dao.impl.ErrorLogDaoImpl;
import com.uq.model.Apkdetail;
import com.uq.spider.common.tool.UploadFileTask;
import com.uq.util.AnalysisApk;
import com.uq.util.ImageUtil;
import com.uq.util.LogTest;
import com.uq.util.ProUtil;
import com.uq.util.SUtil;


public class RsyncErrorfileCore {

	private static ThreadPoolExecutor rsyncPool;
	private static Logger logger = LoggerFactory.getLogger(RsyncErrorfileCore.class);
	public static List<Future> futureList = new ArrayList<Future>();
	
	private static void fileSyncThreadPoolInit() {
		logger.info("开始初始化同步文件需要的线程池...");
		rsyncPool = new ThreadPoolExecutor(12, 12, 0L, TimeUnit.MILLISECONDS,new ArrayBlockingQueue(300), new RejectedExecutionHandler() {
					public void rejectedExecution(Runnable r,ThreadPoolExecutor executor) {
						if (!(executor.isShutdown()))
							try {
								executor.getQueue().put(r);
							} catch (InterruptedException e) {
							}
					}
				});
	}
	
	
	//
	/**
	 * 从数据库查询生成缩略图成功的批次号，统一上传,多线程任务执行完就返回true
	 */
	public static boolean UploadErrorFile(){
		ApkdetailDao apkdetailDao = new ApkdetailDaoImpl();
		String sql ="select app.* from app_upload app,pkginfo p where app.packagename = p.packagename and app.status = 7 and app.versioncode = p.versioncode and p.status = 1 AND DATE_FORMAT(app.createtime,'%Y-%m-%d') >= '2015-07-20' order by app.createtime desc limit ?,?";
		List<Apkdetail> apkList = new ArrayList<Apkdetail>();
		//每次只上传1000条
		int page =0,pagesize = 500;
		ErrorLogDao logDao = new ErrorLogDaoImpl();
		fileSyncThreadPoolInit();
		long start = new Date().getTime();
		try {			
				apkList = apkdetailDao.findApkdetails(sql, page, pagesize);
				page++;				
				for (int i = 0; i < apkList.size(); i++) {
					Apkdetail apk = apkList.get(i);
					List<String> uploadList = getFilePath(apk,"");
//					System.out.println(ProUtil.getString("Upload_Path"));
					String uppath = ProUtil.getString("Upload_Path");
//					rsyncPool.execute(new UploadFileTask(uploadList,uppath, apk,"error"));
					Future<String> f = rsyncPool.submit(new UploadFileTask(uploadList,uppath, apk,"error"));
					futureList.add(f);
					System.out.println("线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
//					break;
				}						
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			System.out.println("==关闭==");
			rsyncPool.shutdown();
			 if (!(rsyncPool.isTerminated()));
			 System.out.println("--------------");
			try {
				boolean Flag = true;
				do {
					Flag = !rsyncPool.awaitTermination(50L, TimeUnit.SECONDS);
					logger.error(" 线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
					System.out.println("total PoolSze："+rsyncPool.getPoolSize()+",waiting task size："+
							rsyncPool.getQueue().size()+",completed task："+rsyncPool.getCompletedTaskCount());
					while(rsyncPool.getPoolSize()<5){//有时上传到最后一个线程会卡主，此时做一下超时控制
						timeoutControl(1800);//线程超时秒数
					}
				} while (Flag);
				long end = new Date().getTime();
				logger.error("批次上传总共耗时："+(end-start)/1000+" s");
				logger.error("========真正完成任务-=");
				return true;
			} catch (InterruptedException e) {
				while (true) {
					logger.error("关闭rsyncPool出错:", e);
				}
			}
		}
		
		
	}
	
	//超时控制
		public static void timeoutControl(long timeout){
			int taskSize = futureList.size();
			while(taskSize > 0)
	        {
	        	System.out.println("-----------------"+taskSize);
	          for (Future<String> future : futureList)
	          {
	            String result = null;
	            
	            try
	            {
	              result = future.get(timeout, TimeUnit.SECONDS);//10分钟
	              System.out.println("result:"+result);
	            } catch (InterruptedException e)
	            {
	              e.printStackTrace();
	            } catch (ExecutionException e)
	            {
	              e.printStackTrace();
	            } catch (TimeoutException e)
	            {
	              // 超时异常需要忽略,因为我们设置了等待时间为0,只要任务没有完成,就会报该异常
	            	future.cancel(true);
	            	System.out.println("超时");
	            	taskSize--;
	            }
	            
	            // 任务已经完成
	            if(result != null)
	            {
	              System.out.println("result=" + result);
	              
	              // 从future列表中删除已经完成的任务
	              futureList.remove(future);  
	              taskSize--;
	              //此处必须break，否则会抛出并发修改异常。（也可以通过将futureList声明为CopyOnWriteArrayList类型解决） 
	              break; // 进行下一次while循环
	            }
	          }
	        }
		}
		
	/**
	 *  pre_path 图片保存的前半部分，数据库保存的是相对路径，为空则返回相对路径
	 */
	public static List<String> getFilePath(Apkdetail apkdetail,String pre_path){
		String iconurljson = apkdetail.getIconurl();
		List<String> fileList =new ArrayList<String>();
		Map<String,String> iconMap = (Map<String,String>)JSONObject.parse(iconurljson);
		for(Map.Entry<String, String> entry:iconMap.entrySet()){
//			System.out.print(entry.getValue()+" ");
			fileList.add(pre_path+entry.getValue());
		}
		String image_h_json = apkdetail.getRemarkimages_h();
		String[] image_hs = ImageUtil.decodeJson(image_h_json);
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
	

	
	
	public static void main(String[] args) {
		UploadErrorFile();
	}
	
	
	//更新签名为空的
	public void updateSingure(){
		String sql =" SELECT app.packagename,app.apkurl FROM app_detail_info app WHERE app.signature IS NULL";
		QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
		try {
			List l = qr.query(sql, new MapListHandler());
			for (int i = 0; i < l.size(); i++) {
				try {
					Map<String, Object> map = (Map)l.get(i);
					String pkgname = SUtil.converString(map.get("packagename"));
					String apkurl ="/data/fileupload/"+SUtil.converString(map.get("apkurl"));
					Map<String, String> rmap = AnalysisApk.unZip(apkurl, "");
					String ss = "update app_detail_info set signature ='"+rmap.get("signature")+"' where packagename ='"+pkgname+"';\r\n";
					LogTest.savelog("/data/test.sql", ss);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
}
