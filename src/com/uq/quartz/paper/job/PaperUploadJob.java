package com.uq.quartz.paper.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.Urldownload;
import com.uq.spider.common.apk.DownloadUtil;
import com.uq.spider.common.paper.RsyncPaperCore;
import com.uq.util.FileDownloadUtil;


/**
 * 壁纸定时下载
 * @author jinrong
 *
 */
public class PaperUploadJob implements Job{

	private static boolean Flag = true;
	private Logger log = LoggerFactory.getLogger(PaperUploadJob.class);
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("PaperUploadJob 能否继续上传=="+Flag);
		if(Flag){
			Flag = false;	
			String downloadid="";
			//从数据库查询出当前需要上传的批次号
			try {

				Urldownload urldownload = new Urldownload();
				 downloadid = DownloadUtil.getPaperDownloadid(2);//壁纸
				log.info("PaperUploadJob 上传壁纸,批次号："+downloadid+" "+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss")+"=====start====");
				if(downloadid!=null && downloadid.length()>5){
					urldownload.setUuid(downloadid);
					RsyncPaperCore.multiUpload(downloadid);
					urldownload.setFilestatus(1);
					urldownload.setUploadendtime(new Date());//上传完成时间
					UrldownloadDao urldownloadDao = new UrldownloadDaoImpl();
					urldownloadDao.update(urldownload, new String[]{"filestatus","uploadendtime"}, " where uuid = '"+urldownload.getUuid()+"'");
					log.info("PaperUploadJob 上传壁纸完成，批次号："+downloadid+" "+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss")+"=====end====");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				log.error("RsyncFileQuartz error",e);
			}finally{
				Flag = true;
				log.error("批次号完成后设置标志位true=="+downloadid);
			}
		}else {
			System.out.println("===任务正在上传，等待下次！");
		}
	}

}
