package com.uq.quartz.apk.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.spider.qq.QQAppUpdateProcessor;
import com.uq.spider.qq.QQLRemdinitProcessor;
import com.uq.spider.qq.QQListinitProcessor;
import com.uq.spider.s360.S360init;
import com.uq.spider.s360.S360updateList;
import com.uq.spider.upload.UploadUtil;
import com.uq.util.FileDownloadUtil;
import com.uq.util.RedisTool;



/**
 * 一天只更新2次
 * @ClassName: S360ListJob 
 * @Description: 每天凌晨定时执行更新360列表中的应用,
 * @author aurong
 * @date 2015-5-14 下午02:30:03
 */
public class S360ListJob implements Job{
	private Logger log = LoggerFactory.getLogger(S360ListJob.class);
	public  static boolean RunFlag = false; //是否正在运行
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("定时执行更新360的软件和游戏列表的应用任务,时间："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss"));
		try {
			RunFlag = true;	

			//先同步一下审核不通过的应用
			UploadUtil.getUninstallApk();
			//先初始化好redis数据
			RedisTool.initAndClear();
			S360init.list_job();//抓取36的列表
			S360updateList.list_360_update();//更新列表
			S360updateList.self_360_update();//更新数据库中360的apk
			
//			S360LRemdInitProcessor.remd_job();
//			S360updateTest.remd_360_update();//更新推荐
			
			//qq
			QQListinitProcessor.list_job();//抓取qq列表的包名
			S360updateList.list_qq_update();//把包名到360里面去抓，只抓360不存在的包名，免得包名冲突
			QQAppUpdateProcessor.list_qq_update();//从qq的应用商店抓
			QQLRemdinitProcessor.remd_job();//qq的推荐应用
			S360updateList.remd_qq_update();
			QQAppUpdateProcessor.remd_qq_update();
//			S360InitProcessor.quartzJob();//从360的列表去更新
//			S360RemdProcessor.quartzJob();//从推荐相关应用更新
			
		} catch (Exception e) {
			
		}finally{
			RunFlag = false;
		}

		log.info("执行更新360的软件和游戏列表的应用任务成功,时间："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss"));	
	}

}
