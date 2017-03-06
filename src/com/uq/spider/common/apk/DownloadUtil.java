package com.uq.spider.common.apk;

import java.util.Date;

import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.Urldownload;


public class DownloadUtil {
 
	/**
	 * 获取上传apk的批次号
	 * @return
	 */
	
	public synchronized static String getdownloadid(){
		 UrldownloadDao urldownloadDao = new UrldownloadDaoImpl();
		String sql ="select * from urldownload where status = 3 and filestatus = 0 and type =1 and datastatus = 1 order by sort DESC, createtime asc limit 0,1 ";
		Urldownload urldownload = urldownloadDao.get(sql,null);
		if(urldownload!=null){
			urldownload.setFilestatus(2);
			urldownload.setUploadstarttime(new Date());//上传开始时间
			urldownloadDao.update(urldownload, new String[]{"filestatus","uploadstarttime"}, " where uuid = '"+urldownload.getUuid()+"'");
			return urldownload.getUuid();		
			
		}
		return "";
	}
	
	/**
	 * 获取上传壁纸的批次号
	 * type 2 壁纸，3 铃声
	 */
	public synchronized static String getPaperDownloadid(int type){
		UrldownloadDao urldownloadDao = new UrldownloadDaoImpl();
		String sql ="SELECT * FROM urldownload WHERE STATUS = 3 AND TYPE = ? AND filestatus = 0 ORDER BY createtime asc limit 0,1 ";
		Urldownload urldownload = urldownloadDao.get(sql, type);
		if(urldownload!=null){
			urldownload.setFilestatus(2);//上传中
			urldownload.setUploadstarttime(new Date());//上传开始时间
			urldownloadDao.update(urldownload, new String[]{"filestatus","uploadstarttime"}, " where uuid = '"+urldownload.getUuid()+"'");
			return urldownload.getUuid();	
		}
		return "";
	}
}
