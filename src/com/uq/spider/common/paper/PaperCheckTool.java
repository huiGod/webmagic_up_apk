package com.uq.spider.common.paper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.dao.PaperDao;
import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.PaperDaoImpl;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.Paper;
import com.uq.model.Urldownload;
import com.uq.util.CopyFileUtil;
import com.uq.util.GetBigFileMD5;
import com.uq.util.ImageMagickUtil;
import com.uq.util.ProUtil;


/**
 * 下载壁纸校验
 * @author jinrong
 *
 */
public class PaperCheckTool {

	private static Logger log = LoggerFactory.getLogger(PaperCheckTool.class);
	
	public static void main(String[] args) {
		String download_uuid = "a15ad52fd5ee4141b00035c2783a03ce";
		
//		checkFileDown(download_uuid);
		String UUID = "a15ad52fd5ee4141b00035c2783a03ce";
//		PaperCheckTool.checkFileDown(UUID);//文件下载是否完整
		PaperCheckTool.createThumbnail(UUID);//文件缩略图生成
//		PaperCheckTool.checkFileAfter(UUID);//文件缩略图是否存在
//		PaperCheckTool.copyToupload(UUID);//准备上传
	}
	
	//检查文件是否存在
	public static void checkFileDown(String download_uuid){
		String sql ="select * from paper where status = 3 and uploadid = '"+download_uuid+"' limit ?,?";
		Integer curpage =0,pagesize = 3000;
		PaperDao paperDao = new PaperDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		List<Paper> paperList = new ArrayList<Paper>();
		String pre_path = ProUtil.getString("Download_Path")+download_uuid+"/";
		do {
			paperList = paperDao.findAllList(sql, curpage, pagesize);
//			curpage++;//会改变状态，直接去第一页即可
			for (int i = 0; i < paperList.size(); i++) {
				Paper paper = paperList.get(i);
				String downUrl = pre_path+paper.getUploadurl();
				File f = new File(downUrl);
				if(f.exists()){//存在
					paper.setStatus(5);
					paperDao.update(paper, new String[]{"status"}, " where id = "+paper.getId());
				}else {//下载失败的
					paper.setStatus(0);//下载失败
					paperDao.update(paper, new String[]{"status"}, " where id = "+paper.getId());
				}
			}
		} while (paperList.size()>0);
		urldownloadDao.update(new Urldownload(download_uuid, 2), new String[]{"status"}, " where uuid = '"+download_uuid+"'");
		log.error("壁纸下载后的检查文件是否存在，批次号："+download_uuid);
	}
	
	/**
	 * 生成缩略图
	 */
	public static void createThumbnail(String download_uuid){
		String sql ="select * from paper where status = 5 and uploadid = '"+download_uuid+"' limit ?,?";
		Integer curpage =0,pagesize = 3000;
		PaperDao paperDao = new PaperDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		List<Paper> paperList = new ArrayList<Paper>();
		String pre_path = ProUtil.getString("Download_Path")+download_uuid+"/";
		do {
			paperList = paperDao.findAllList(sql, curpage, pagesize);
//			curpage++;
			for (int i = 0; i < paperList.size(); i++) {
				Paper paper = paperList.get(i);
				try {
//					Paper paper = paperList.get(i);
					String downUrl = pre_path+paper.getUploadurl();
					String max_savepath = pre_path+"max/"+paper.getUploadurl();//大的缩略图保存路径
					String min_savepath = pre_path+"min/"+paper.getUploadurl();//小的缩略图
					File f = new File(downUrl);
					//直接把大图copy到另外的目录
					CopyFileUtil.copyFile(downUrl, max_savepath, true);
					ImageMagickUtil.compressJpg(max_savepath);
					//更新MD5,大小
					List<String> colums = new ArrayList<String>();//需要更新的列
					
					File maxfile = new File(max_savepath);
					String md5 =  GetBigFileMD5.getMD5(maxfile);
					paper.setMd5(md5);
					colums.add("md5");
					paper.setSize(maxfile.length());
					colums.add("size");
					int[] info =ImageMagickUtil.getImageInfo(max_savepath);
					if(info[0]>0 && !paper.getWidth().equals(info[0]) ){
						paper.setWidth(info[0]);
						colums.add("width");
					}
					if(info[1]>0 && !paper.getHeight().equals(info[1]) ){
						paper.setHeight(info[1]);
						colums.add("height");
					}					
					
					//生成小图
					ImageMagickUtil.resizePaper(downUrl, min_savepath, 230);
					paper.setStatus(6);
					colums.add("status");
					String[] update_column = (String[])colums.toArray(new String[0]);
					paperDao.update(paper, update_column, " where id = "+paper.getId());
				} catch (Exception e) {
					paper.setStatus(8);
					paperDao.update(paper, new String[]{"status"}, " where id = "+paper.getId());
				}
			}
		} while (paperList.size()>0);
		
		//修改上传批次号为1 --检查完成
		urldownloadDao.update(new Urldownload(download_uuid, 4), new String[]{"status"}, " where uuid = '"+download_uuid+"'");
		log.error("壁纸生成缩略图完毕--批次号:"+download_uuid);
	}
	
	/**
	 * 检查生成缩略图之后的图片是否存在
	 * @param download_uuid
	 */
	public static void checkFileAfter(String download_uuid){
		String sql ="select * from paper where status = 6 and uploadid = '"+download_uuid+"' limit ?,?";
		Integer curpage =0,pagesize = 3000;
		PaperDao paperDao = new PaperDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		List<Paper> paperList = new ArrayList<Paper>();
		String pre_path = ProUtil.getString("Download_Path")+download_uuid+"/";
		do {
			paperList = paperDao.findAllList(sql, curpage, pagesize);
//			curpage++;
			for (int i = 0; i < paperList.size(); i++) {
				Paper paper = paperList.get(i);
				try {
					String downUrl = pre_path+paper.getUploadurl();
					String max_savepath = pre_path+"max/"+paper.getUploadurl();//大的缩略图保存路径
					String min_savepath = pre_path+"min/"+paper.getUploadurl();//小的缩略图
					List<String> filepath = new ArrayList<String>();
					filepath.add(max_savepath);
					filepath.add(min_savepath);
					boolean b = isExistFile(filepath);
					if(b){
						paper.setStatus(7);						
					}else {
						paper.setStatus(8);//生成缩略图失败
					}
					paperDao.update(paper, new String[]{"status"}, " where id = "+paper.getId());
					
				} catch (Exception e) {
					paper.setStatus(8);
					paperDao.update(paper, new String[]{"status"}, " where id = "+paper.getId());
				}
			}
		} while (paperList.size()>0);
	}
	
	public static boolean isExistFile(List<String> filepath){
		boolean b = true;
		out:
		for(String path:filepath){
			if(!new File(path).exists()){
				b = false;
				System.out.println("文件不存在："+path);
				break out;
			}
		}
		return b;
	}
	
	/**
	 * 复制图片到另外的目录，去准备上传
	 * @param download_uuid
	 */
	public static void copyToupload(String download_uuid){
		String sql ="select * from paper where status = 7 and uploadid = '"+download_uuid+"' limit ?,?";
		Integer curpage =0,pagesize = 3000;
		PaperDao paperDao = new PaperDaoImpl();
		UrldownloadDao urldownloadDao =new UrldownloadDaoImpl();
		List<Paper> paperList = new ArrayList<Paper>();
		String pre_path = ProUtil.getString("Download_Path")+download_uuid+"/";
		String upload_path = ProUtil.getString("Upload_Path");
		do {
			paperList = paperDao.findAllList(sql, curpage, pagesize);
			curpage++;
			for (int i = 0; i < paperList.size(); i++) {
				Paper paper = paperList.get(i);
				try {
					String downUrl = pre_path+paper.getUploadurl();
					String max_savepath = pre_path+"max/"+paper.getUploadurl();//大的缩略图保存路径
					String min_savepath = pre_path+"min/"+paper.getUploadurl();//小的缩略图
					String  destFileName = max_savepath.replace(pre_path, upload_path);
					boolean b =CopyFileUtil.copyFile(max_savepath, destFileName, true);
					if(b){
						new File(max_savepath).delete();//删除源文件
					}
					destFileName = min_savepath.replace(pre_path, upload_path);
					boolean b1 =CopyFileUtil.copyFile(min_savepath, destFileName, true);
					if(b1){
						new File(min_savepath).delete();//删除源文件
					}
					
				} catch (Exception e) {
					paper.setStatus(8);
					paperDao.update(paper, new String[]{"status"}, " where id = "+paper.getId());
				}
			}
		} while (paperList.size()>0);
		log.error("壁纸复制检查完毕，批次号："+download_uuid);
		Urldownload url = urldownloadDao.getByuuid(download_uuid);
		url.setStatus(3);//准备上传
		url.setDownendtime(new Date());
		urldownloadDao.update(url, new String[]{"status","downendtime"}, " where id = "+url.getId() );
	}
}
