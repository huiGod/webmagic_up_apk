package com.uq.quartz.ring;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.uq.quartz.ring.job.RingDownJob;



public class QuartzUtil_ring_down {
	public static void main(String[] args) {
		try {  
			SchedulerFactory sFactory=new StdSchedulerFactory();  
	        Scheduler scheduler=sFactory.getScheduler(); 	        

	        
	        //下载文件
	        //每15分钟下载一次 0 */15 * * * ?
	        JobDetail down_job=JobBuilder.newJob(RingDownJob.class).withIdentity("ring_down_job", "ring_job-group").build();  
	        CronTrigger down_cronTrigger=TriggerBuilder.newTrigger().withIdentity("ring_down_cronTrigger", "ring_trigger-group").withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * * * ?")).build(); //0 0 23 * * ?  //0/5 * * * * ?	         
	        scheduler.scheduleJob(down_job, down_cronTrigger);  
       	        
	        scheduler.start();  
	        
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
	
	public static void main1(String[] args) {
		System.out.println();
	}
}
