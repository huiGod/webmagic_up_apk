package com.uq.quartz.apk.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.Urldownload;
import com.uq.spider.upload.UploadUtil;
import com.uq.util.FileDownloadUtil;


/**
 * 数据定时上传
 * @ClassName: DataUploadQuartz 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author aurong
 * @date 2015-5-16 下午05:21:52
 */
public class DataUploadJob implements Job{
	private static boolean Flag = true;
	private Logger log = LoggerFactory.getLogger(DataUploadJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		if(Flag){
			Flag = false;
			log.info("上传接口数据："+FileDownloadUtil.getNowTime("yyyy-MM-dd HH:mm:ss"));
			try {
				//上传数据
				UrldownloadDao urldownloadDao = new UrldownloadDaoImpl();
				String sql ="select * from urldownload where status = 3 and datastatus = 0 and type = 1 order by createtime asc limit 0,1 ";
				Urldownload urldownload = urldownloadDao.get(sql,null);				
				if(urldownload!=null){
					log.info("该批次号上传的数据为："+urldownload.getUuid());
					UploadUtil.uploadAppbyDownid(urldownload.getUuid());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				log.error("上传接口数据错误", e);
			}finally{
				Flag = true;
			}
		}
	}

}
