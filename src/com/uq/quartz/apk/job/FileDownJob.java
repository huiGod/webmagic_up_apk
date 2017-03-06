package com.uq.quartz.apk.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.spider.common.apk.FileDownCore;
import com.uq.util.FileDownloadUtil;

/**
 * 文件下载
 * @ClassName: FileDownJob 
 * @Description: 跑完更新列表的job之后才开始下载文件 
 * @author aurong
 * @date 2015-5-15 下午03:17:05
 */
public class FileDownJob implements Job{

	private static boolean flag = true;//保证当期只有一个实例在下载即可
	private Logger log = LoggerFactory.getLogger(FileDownJob.class);
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//必须等更新列表完成之后才开始下载
		System.out.println("FileDownJob==========");
//		if(S360ListJob.RunFlag){//正在跑任务，不下载
//			log.info("正在更新列表，稍后下载.....");
//			return ;
//		}
		log.info("是否可以下载："+flag);
		if(flag){//一直可以下载
			flag = false;
			log.info("下载图片和apk文件："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss")+"=====start====");
			try {
//				FileDownProcessor.quartJob();//下载文件
				FileDownCore.downfile();
//				Thread.sleep(30*1000);
//				System.out.println("睡眠30s");
				log.info("下载图片和apk文件："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss")+"=====end====");
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				flag = true;
				log.info("设置下载标志:true");
			}
		}
		
	}

}
