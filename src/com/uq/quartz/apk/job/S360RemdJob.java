package com.uq.quartz.apk.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.spider.s360.S360RemdInitProcessor;
import com.uq.spider.s360.S360init;
import com.uq.spider.s360.S360updateList;
import com.uq.spider.upload.UploadUtil;
import com.uq.util.FileDownloadUtil;
import com.uq.util.RedisTool;


/**
 * 一天只更新一次
 * @ClassName: S360ListJob 
 * @Description: 每天凌晨定时执行更新360列表中的应用,
 * @author aurong
 * @date 2015-5-14 下午02:30:03
 */
public class S360RemdJob implements Job{
	private Logger log = LoggerFactory.getLogger(S360RemdJob.class);
	public  static boolean RunFlag = false; //是否正在运行
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("定时执行-- 推荐 --360的软件和游戏列表的应用任务,时间："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss"));
		try {
			RunFlag = true;
			//先初始化好redis数据
			//先同步一下审核不通过的应用
			UploadUtil.getUninstallApk();
			RedisTool.init();
			S360init.list_job();//抓取360的列表
			
			S360RemdInitProcessor.remd_job(); 
			S360updateList.remd_360_update();//更新推荐
			
			
		} catch (Exception e) {
			
		}finally{
			RunFlag = false;
		}

		log.info("执行完毕-- 推荐 --360的软件和游戏列表的应用任务成功,时间："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss"));	
	}

}
