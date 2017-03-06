package com.uq.quartz.apk;


import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.uq.quartz.apk.job.ErrorFileJob;
import com.uq.quartz.apk.job.RsyncFileQuartz;

/**
 * 上传文件
 */
public class QuartzUtil_uploadfile {
	public static void main(String[] args) {
		try {  
			SchedulerFactory sFactory=new StdSchedulerFactory();  
	        Scheduler scheduler=sFactory.getScheduler(); 	        
        
//	        //上传错误文件
	        JobDetail data_job2 =JobBuilder.newJob(ErrorFileJob.class).withIdentity("down_job2", "job-group").build();  
	        CronTrigger down_cronTrigger2 =TriggerBuilder.newTrigger().withIdentity("down_cronTrigger2", "trigger-group").startNow().withSchedule(CronScheduleBuilder.cronSchedule("0 */50 * * * ?")).build(); //0 10 0/2 * * ?	         
	        scheduler.scheduleJob(data_job2, down_cronTrigger2);
//	        
	        JobDetail data_job3 =JobBuilder.newJob(RsyncFileQuartz.class).withIdentity("down_job3", "job-group").build();  
	        CronTrigger down_cronTrigger3 =TriggerBuilder.newTrigger().withIdentity("down_cronTrigger3", "trigger-group").startNow().withSchedule(CronScheduleBuilder.cronSchedule("0 */2 * * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?	         
	        scheduler.scheduleJob(data_job3, down_cronTrigger3);
	        	        
	        scheduler.start();  
	        
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
	
	public static void main1(String[] args) {
		System.out.println("--");
	}
}
