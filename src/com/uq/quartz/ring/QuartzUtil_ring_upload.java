package com.uq.quartz.ring;


import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.uq.quartz.ring.job.RingUploadErrorJob;
import com.uq.quartz.ring.job.RingUploadJob;



public class QuartzUtil_ring_upload {
	public static void main(String[] args) {
		try {  
			SchedulerFactory sFactory=new StdSchedulerFactory();  
	        Scheduler scheduler=sFactory.getScheduler(); 	        

	            
	        //上传铃声的正常数据
	        JobDetail down_job1=JobBuilder.newJob(RingUploadJob.class).withIdentity("ring_down_job1", "ring_job-group").build();  
	        CronTrigger down_cronTrigger1=TriggerBuilder.newTrigger().withIdentity("ring_down_cronTrigger1", "ring_trigger-group").withSchedule(CronScheduleBuilder.cronSchedule("0 */3 * * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?	         
	        scheduler.scheduleJob(down_job1, down_cronTrigger1); 
	       
	        JobDetail down_job2=JobBuilder.newJob(RingUploadErrorJob.class).withIdentity("ring_down_job2", "ring_job-group").build();  
	        CronTrigger down_cronTrigger2=TriggerBuilder.newTrigger().withIdentity("ring_down_cronTrigger2", "ring_trigger-group").withSchedule(CronScheduleBuilder.cronSchedule("0 */30 * * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?	         
	        scheduler.scheduleJob(down_job2, down_cronTrigger2); 
	        
	        scheduler.start();  
	        
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
	
	public static void main1(String[] args) {
		
	}
}
