package com.uq.quartz.apk;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.uq.quartz.apk.job.DataUploadJob;
import com.uq.quartz.apk.job.FileDownJob;


/*
 * apk下载和上传数据
 * 
 */

public class QuartzUtil_down {
	public static void main(String[] args) {
		try {  
			SchedulerFactory sFactory=new StdSchedulerFactory();  
	        Scheduler scheduler=sFactory.getScheduler(); 	   

	        
	        //下载文件
	        //每15分钟下载一次 0 */15 * * * ?
	        JobDetail down_job=JobBuilder.newJob(FileDownJob.class).withIdentity("down_job", "job-group1").build();  
	        CronTrigger down_cronTrigger=TriggerBuilder.newTrigger().withIdentity("down_cronTrigger", "trigger-group1").withSchedule(CronScheduleBuilder.cronSchedule(" 0 */10 * * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?   //0 */10 *	         
	        scheduler.scheduleJob(down_job, down_cronTrigger);  
	         
//	        //上传数据，每2分钟一次
	        JobDetail data_job1 =JobBuilder.newJob(DataUploadJob.class).withIdentity("down_job1", "job-group1").build();  
	        CronTrigger down_cronTrigger1 =TriggerBuilder.newTrigger().withIdentity("down_cronTrigger1", "trigger-group1").withSchedule(CronScheduleBuilder.cronSchedule("0 */2 * * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?	      //0 */2 * 
	        scheduler.scheduleJob(data_job1, down_cronTrigger1);
	        
	        scheduler.start();  
	        
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
	
	public static void main1(String[] args) {
		System.out.println("MyEclipse8.5的export-->runnable jar file-->的launch configuration中没有想要选择的类 此时只需要把对应类的main方法在MyEclipse中运行一次就OK了 ");
	}
}
