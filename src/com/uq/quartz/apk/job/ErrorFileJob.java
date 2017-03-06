package com.uq.quartz.apk.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.spider.common.apk.RsyncErrorfileCore;
import com.uq.util.FileDownloadUtil;


/**
 * 重新上传失败的文件
 * @author aurong
 * @date 2015-5-14 下午02:30:03
 */
public class ErrorFileJob implements Job{
	private Logger log = LoggerFactory.getLogger(ErrorFileJob.class);
	public  static boolean RunFlag = true; //是否正在运行
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("定时上传pkginfo的错误文件："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss"));
		if(RunFlag){
			try {
				RunFlag = false;
				RsyncErrorfileCore.UploadErrorFile();
				log.info("定时上传pkginfo的错误文件成功,时间："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss"));
			} catch (Exception e) {
				
			}finally{
				RunFlag = false;
			}
		}else {
			log.info("正在定时上传pkginfo的错误文件成功,请等待下次上传!");
		}	

			
	}

}
