package com.uq.quartz.ring.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.spider.common.ring.RingDownCore;
import com.uq.util.FileDownloadUtil;


/**
 * 壁纸定时下载
 * @author jinrong
 *
 */
public class RingDownJob implements Job{

	private static boolean flag = true;//保证当期只有一个实例在下载即可
	private Logger log = LoggerFactory.getLogger(RingDownJob.class);
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("是否可以下载铃声："+flag);
		if(flag){
			flag = false;
			log.info("下载铃声："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss")+"=====start====");
			try {
				RingDownCore.downFile();
				log.info("下载铃声："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss")+"=====end====");
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				flag = true;
				log.info("设置铃声下载标志:true");
			}
		}
	}

}
