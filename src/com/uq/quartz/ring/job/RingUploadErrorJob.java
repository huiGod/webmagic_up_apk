package com.uq.quartz.ring.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.spider.common.ring.RsyncRingFailCore;


/**
 * 壁纸定时下载
 * @author jinrong
 *
 */
public class RingUploadErrorJob implements Job{

	private static boolean Flag = true;
	private Logger log = LoggerFactory.getLogger(RingUploadErrorJob.class);
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("RingUploadErrorJob 能否继续上传=="+Flag);
		if(Flag){
			Flag = false;	
			String downloadid="";
			//从数据库查询出当前需要上传的批次号
			try {
				RsyncRingFailCore.multiUpload("");				
			} catch (Exception e) {
				e.printStackTrace();
				log.error("RsyncFileQuartz error",e);
			}finally{
				Flag = true;
				log.error("批次号铃声上传完成后设置标志位true=="+downloadid);
			}
		}else {
			System.out.println("===任务正在上传，等待下次！");
		}
	}

}
