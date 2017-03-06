package com.uq.spider.common.ring;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.dao.RingDao;
import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.RingDaoImpl;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.Ring;
import com.uq.model.Urldownload;
import com.uq.util.CopyFileUtil;
import com.uq.util.GetBigFileMD5;
import com.uq.util.ProUtil;


public class RingCheckTool {
	public static final String RING_SAVEPATH ="audio";
	private static Logger log = LoggerFactory.getLogger(RingCheckTool.class);
	
	//检查文件是否存在,更新大小,md5
	public static void checkFileDown(String download_uuid){
		String sql ="select * from ring where status = 3 and uploadid = '"+download_uuid+"' limit ?,?";
		Integer curpage =0,pagesize = 3000;
		RingDao ringDao = new RingDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		List<Ring> ringList = new ArrayList<Ring>();
		String pre_path = ProUtil.getString("Download_Path")+download_uuid+"/"+RING_SAVEPATH+"/";
		do {
			ringList = ringDao.findAllList(sql, curpage, pagesize);
//			curpage++;//会改变状态，直接去第一页即可
			for (int i = 0; i < ringList.size(); i++) {
				Ring ring = ringList.get(i);
				List<String> colums = new ArrayList<String>();//需要更新的列
				String downUrl = pre_path+ring.getUploadurl();
				File f = new File(downUrl);
				if(f.exists()){//存在
					String md5 =  GetBigFileMD5.getMD5(f);
					ring.setMd5(md5);
					colums.add("md5");
					ring.setSize(f.length());
					colums.add("size");
					//检查一下是否有重复的MD5，判断是同一文件
					boolean b = ringDao.findBymd5(md5);
					if(b){//已经存在这个md5了
						ring.setStatus(11);
					}else {
						ring.setStatus(6);
					}
					
					colums.add("status");
				}else {//下载失败的
					ring.setStatus(0);//下载失败
					colums.add("status");
				}
				String[] update_column = (String[])colums.toArray(new String[0]);
				ringDao.update(ring, update_column, " where id = "+ring.getId());
			}
		} while (ringList.size()>0);
		urldownloadDao.update(new Urldownload(download_uuid, 2), new String[]{"status"}, " where uuid = '"+download_uuid+"'");
		log.error("铃声下载后的检查文件是否存在，批次号："+download_uuid);
	}
	
	/**
	 * 复制铃声到另外的目录，去准备上传
	 * @param download_uuid
	 */
	public static void copyToupload(String download_uuid){
		String sql ="select * from ring where status = 6 and uploadid = '"+download_uuid+"' limit ?,?";
		Integer curpage =0,pagesize = 3000;
		RingDao ringDao = new RingDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		List<Ring> ringList = new ArrayList<Ring>();
		String pre_path = ProUtil.getString("Download_Path")+download_uuid+"/"+RING_SAVEPATH+"/";
		String upload_path = ProUtil.getString("Upload_Path")+RING_SAVEPATH+"/";
		do {
			ringList = ringDao.findAllList(sql, curpage, pagesize);
			curpage++;
			for(int i = 0; i < ringList.size(); i++) {				
				Ring ring = ringList.get(i);
				try {					
					String sourceUrl = pre_path+ring.getUploadurl();
					String  destFileName = upload_path+ring.getUploadurl();
					boolean b =CopyFileUtil.copyFile(sourceUrl, destFileName, true);
					if(b){
						new File(sourceUrl).delete();
					}
				} catch (Exception e) {
					ring.setStatus(8);
					ringDao.update(ring, new String[]{"status"}, " where id = "+ring.getId());
				}
			}
		} while (ringList.size()>0);
		urldownloadDao.update(new Urldownload(download_uuid, 2), new String[]{"status"}, " where uuid = '"+download_uuid+"'");
		log.error("铃声下载复制到上传目录，批次号："+download_uuid);
	}
	
	//检查复制到上传目录的文件是否存在
	public static void checkDownAfter(String download_uuid){
		String sql ="select * from ring where status = 6 and uploadid = '"+download_uuid+"' limit ?,?";
		Integer curpage =0,pagesize = 3000;
		RingDao ringDao = new RingDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		List<Ring> ringList = new ArrayList<Ring>();
		String upload_path = ProUtil.getString("Upload_Path")+RING_SAVEPATH+"/";
		do {
			ringList = ringDao.findAllList(sql, curpage, pagesize);
//			curpage++;//会改变状态，直接去第一页即可
			for(int i = 0; i < ringList.size(); i++) {				
				Ring ring = ringList.get(i);				
				try {					
					String sourceUrl = upload_path+ring.getUploadurl();
					File f = new File(sourceUrl);
					if(f.exists()){
						ring.setStatus(7);						
					}else {
						ring.setStatus(8);
					}					
					ringDao.update(ring, new String[]{"status"}, " where id = "+ring.getId());
				} catch (Exception e) {
					ring.setStatus(8);
					ringDao.update(ring, new String[]{"status"}, " where id = "+ring.getId());
				}
			}
		} while (ringList.size()>0);
		Urldownload urldownload =new Urldownload(download_uuid, 3);
		urldownload.setDownendtime(new Date());
		urldownloadDao.update(urldownload, new String[]{"status","downendtime"}, " where uuid = '"+download_uuid+"'");
		log.error("检查铃声复制后的文件是否存在，批次号："+download_uuid);
	}
	
	public static void main(String[] args) {
		String UUID ="0ed74b6756fc422ead31d70fea32cc67";
		RingCheckTool.checkFileDown(UUID);
		RingCheckTool.copyToupload(UUID);
		RingCheckTool.checkDownAfter(UUID);
	}
}
