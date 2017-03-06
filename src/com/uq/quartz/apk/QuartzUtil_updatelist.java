package com.uq.quartz.apk;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.uq.quartz.apk.job.S360ListJob;
import com.uq.quartz.apk.job.S360RemdJob;
import com.uq.spider.upload.UploadUtil;

//定时器更新列表
public class QuartzUtil_updatelist {
	public static void main(String[] args) {
		try {  
			SchedulerFactory sFactory=new StdSchedulerFactory();  
	        Scheduler scheduler=sFactory.getScheduler(); 	        
	        JobDetail jobDetail=JobBuilder.newJob(S360ListJob.class).withIdentity("myjob_list", "job-group_list").build();  
	        //每4个小时更新一次 0 * */4 * * ?   0 45 0,5,10,17,20 * * ?
	        CronTrigger cronTrigger=TriggerBuilder.newTrigger().withIdentity("cronTrigger_list", "trigger-group_list").withSchedule(CronScheduleBuilder.cronSchedule("0 40 15 * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?	         //0 55 15,22
	        scheduler.scheduleJob(jobDetail, cronTrigger);
	        
	        JobDetail jobDetail_remd=JobBuilder.newJob(S360RemdJob.class).withIdentity("myjob_list_2", "job-group_list").build();  
	        //每4个小时更新一次 0 * */4 * * ?   0 45 0,5,10,17,20 * * ?
	        CronTrigger cronTrigger_remd=TriggerBuilder.newTrigger().withIdentity("cronTrigger_list_2", "trigger-group_list").withSchedule(CronScheduleBuilder.cronSchedule("0 40 15 * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?//0 15 6	         
	        scheduler.scheduleJob(jobDetail_remd, cronTrigger_remd);
	        
 	        
	        scheduler.start();  
	        
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
	
	public static void main1(String[] args) {
		UploadUtil.uploadAppbyDownid("15e9cdaf087547848ae977f5e92f577e");
	}
}

