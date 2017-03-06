package com.uq.quartz.paper.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.spider.common.paper.PaperDownCore;
import com.uq.util.FileDownloadUtil;


/**
 * 壁纸定时下载
 * @author jinrong
 *
 */
public class PaperDownJob implements Job{

	private static boolean flag = true;//保证当期只有一个实例在下载即可
	private Logger log = LoggerFactory.getLogger(PaperDownJob.class);
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("是否可以下载壁纸："+flag);
		if(flag){
			flag = false;
			log.info("下载壁纸："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss")+"=====start====");
			try {
				PaperDownCore.downFile();
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
