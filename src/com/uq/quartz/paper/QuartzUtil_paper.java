package com.uq.quartz.paper;


import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.uq.quartz.paper.job.PaperUploadJob;




public class QuartzUtil_paper {
	public static void main(String[] args) {
		try {  
			SchedulerFactory sFactory=new StdSchedulerFactory();  
	        Scheduler scheduler=sFactory.getScheduler(); 	        

	        
	        //下载文件
	        //每15分钟下载一次 0 */15 * * * ?
//	        JobDetail down_job=JobBuilder.newJob(PaperDownJob.class).withIdentity("paper_down_job", "paper_job-group").build();  
//	        CronTrigger down_cronTrigger=TriggerBuilder.newTrigger().withIdentity("paper_down_cronTrigger", "paper_trigger-group").withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?	         
//	        scheduler.scheduleJob(down_job, down_cronTrigger);  
        
	        //上传数据
	        JobDetail down_job1=JobBuilder.newJob(PaperUploadJob.class).withIdentity("paper_down_job1", "paper_job-group").build();  
	        CronTrigger down_cronTrigger1=TriggerBuilder.newTrigger().withIdentity("paper_down_cronTrigger1", "paper_trigger-group").withSchedule(CronScheduleBuilder.cronSchedule("0 */3 * * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?	         
	        scheduler.scheduleJob(down_job1, down_cronTrigger1); 
	        
	        scheduler.start();  
	        
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
	
}
