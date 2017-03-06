package com.uq.spider.common.apk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.dao.ApkdetailDao;
import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.ApkdetailDaoImpl;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.Apkdetail;
import com.uq.model.Apkupload;
import com.uq.model.Urldownload;
import com.uq.spider.common.tool.DownloadTask;
import com.uq.util.FileDownloadUtil;
import com.uq.util.GenerateRandomUtils;
import com.uq.util.ImageUtil;
import com.uq.util.ProUtil;
import com.uq.util.SUtil;
import com.uq.util.UuidUtil;


/*
 * 文件下载
 */
public class FileDownCore {
	public static ThreadPoolExecutor rsyncPool;
	private static Logger logger = LoggerFactory.getLogger(FileDownCore.class);

	private static void fileSyncThreadPoolInit() {
		logger.info("开始初始化下载文件需要的线程池...");
		rsyncPool = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue(300), new RejectedExecutionHandler() {
					public void rejectedExecution(Runnable r,
							ThreadPoolExecutor executor) {
						if (!(executor.isShutdown()))
							try {
								executor.getQueue().put(r);
							} catch (InterruptedException e) {
								logger.error("fileSyncThreadPoolInit 放入任务异常");
							}
					}
				});
	}

	/*
	 * 下载文件
	 */
	public static void downfile() {
//		String sql = "select * from app_detail_info app where (status =2 or status =4) AND source ='360' ORDER BY status desc, soft ASC,islist ASC,currentpage asc  limit ?,? ";
		String sql = "SELECT * FROM app_detail_info app WHERE (STATUS =2 OR STATUS =4)  ORDER BY source ASC,STATUS DESC, soft ASC,islist ASC  limit ?,? ";//com.zj.whackmole2
		List<Apkdetail> apksList = new ArrayList<Apkdetail>();
		Integer curpage = 0, pagesize = 300;//300
		ApkdetailDao apkdao = new ApkdetailDaoImpl();
		UrldownloadDao urlDao = new UrldownloadDaoImpl();
		String UUID = UuidUtil.getUUID();// 批次号
		String file_date = FileDownloadUtil.getDateYMD();// 2015/0510
		fileSyncThreadPoolInit();
		Integer sort = 0;//优先上传标志
		Integer downcount = 0;
		try {
			apksList = apkdao.findApkdetails(sql, curpage, pagesize);
			downcount = apksList.size();
			if(apksList!=null && apksList.size()== 0 ){
				logger.error(UUID+"批次号下载条数:" +apksList.size());
				return;
			}
			logger.error(UUID+"批次号下载条数:" +apksList.size());
			String randomNum = GenerateRandomUtils.getCharAndNumr(5);// 生成5位的随机数

			String rootPath = ProUtil.getString("Download_Path");
			for (int i = 0; i < apksList.size(); i++) {
				Apkdetail apkdetail =null;
				try {
					 apkdetail = apksList.get(i);
					String pkg = apkdetail.getPackagename();// 包名
					System.out.println("======" + pkg);
					if(apkdetail.getStatus().equals(4)){
						sort = 1;//优先更新的
					}
					Map<String, String> downMap = new HashMap<String, String>();
					if (i % 300 == 0) {// 每个随机数保存500个文件
						randomNum = GenerateRandomUtils.getCharAndNumr(5);// 生成5位的随机数
					}
					// 下载图标
					String iconurl = apkdetail.getSiconurl();
					if (!SUtil.isEmpty(iconurl)) {
						String tmp_icon = "icon/" + file_date + "/" + randomNum + "/" + UuidUtil.getUUID()+ (SUtil.isEmpty(FileDownloadUtil.getFileSuffix(iconurl))?".png":FileDownloadUtil.getFileSuffix(iconurl));
						downMap.put(iconurl, UUID + "/" + tmp_icon);
						apkdetail.setIconurl(tmp_icon);
					}
					// 下载介绍图片
					String image_down = "";
					String type = "";
					if (!SUtil.isEmpty(apkdetail.getImage_h())) {
						image_down = apkdetail.getImage_h();
						type = "remarkimages_h";
					} else if (!SUtil.isEmpty(apkdetail.getImage_m())) {
						image_down = apkdetail.getImage_m();
						type = "remarkimages_m";
					} else {
						image_down = apkdetail.getImage_l();
						type = "remarkimages_l";
					}
					String[] images = FileDownloadUtil.getImage_h(image_down);
					List<String> imagejson = new ArrayList<String>();
					//介绍图片可能有同样的图片，必须先过滤掉
					System.out.println("======"+images);
					
					if (images != null && images.length > 0) {
						images = array_unique(images);
						for (String image_h : images) {
							String path = "image/" + file_date + "/" + randomNum + "/" + UuidUtil.getUUID()+ (SUtil.isEmpty(FileDownloadUtil.getFileSuffix(image_h))?".jpg":FileDownloadUtil.getFileSuffix(image_h));
							downMap.put(image_h, UUID + "/" + path);
							imagejson.add(path);

						}

					}
					String jsonString = ImageUtil.makeJson(imagejson);
					if (type == "remarkimages_h") {
						apkdetail.setRemarkimages_h(jsonString);
					} else if (type == "remarkimages_m") {
						apkdetail.setRemarkimages_m(jsonString);
					} else if (type == "remarkimages_l") {
						apkdetail.setRemarkimages_l(jsonString);
					}

					// 下载apk
					String apkurl = apkdetail.getSapkurl();
					if (!SUtil.isEmpty(apkurl)) {
						String tmp_apk = "apk/" + file_date + "/" + randomNum + "/"+ apkdetail.getApkmd5() + "/" + pkg + ".apk";
						downMap.put(apkurl, UUID + "/" + tmp_apk);
						apkdetail.setApkUrl(tmp_apk);
					}
					apkdetail.setUploadid(UUID);// 记录一下批次号
					apkdetail.setStatus(3);// 表示已加入下载
					// 更新apk的icon,image,apk下载路径
					apkdao.update(apkdetail, new String[] { "iconurl", "apkurl", type,"status", "uploadid" }, "where id = " + apkdetail.getId());
					//把文件也插入到要上传数据的app_upload表中，防止抓取数据时把这些数据删除或者覆盖掉
					Apkupload apkupload = new Apkupload();
					BeanUtils.copyProperties(apkupload, apkdetail);//复制属性
					apkdao.saveApkupload(apkupload);
					System.out.println(downMap);
					rsyncPool.execute(new DownloadTask(rootPath, downMap));
					System.out.println("线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("加入下载任务时错误：pkg",apkdetail.getPackagename(),e);
				}
				
			}

			// 插入当前下载批次信息
			if(apksList.size()>0)urlDao.save(new Urldownload(UUID, 0,sort,1,downcount));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			rsyncPool.shutdown();
			 if (!(rsyncPool.isTerminated()));
			 System.out.println("--------------");
			try {
				boolean Flag = true;
				do {
					Flag = !rsyncPool.awaitTermination(30L, TimeUnit.SECONDS);
					System.out.println("线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());

				} while (Flag);
				System.out.println("========完成下载任务-=");
				logger.error("====批次号下载完毕！==="+UUID);
				if(apksList.size()>0){
					urlDao.update("update urldownload set status = 4,downendtime = now() where uuid ='"+UUID+"'");
					FileCheckTool.checkFileDown(UUID);
					//更新检查完成
					urlDao.update("update urldownload set status = 1,downendtime = now() where uuid ='"+UUID+"'");
					//生成各种图片的缩略图，读取apk的签名，成功的话更新apk status为6
					FileCheckTool.checkfile(UUID);
					//检查生成缩略图之后的文件是否存在,存在状态 修改为7
					FileCheckTool.checkDownAfter(UUID);
					//copy成功的文件到其他目录，准备上传
					FileCheckTool.copyToupload(UUID);
//					抽取数据,status 为7
				}
				
			} catch (InterruptedException e) {
				while (true) {
					logger.error("关闭rsyncPool出错:", e);
				}
			}
		}
	}
	
    public static String[] array_unique(String[] a) {
        // array_unique
        List<String> list = new LinkedList<String>();
        for(int i = 0; i < a.length; i++) {
            if(!list.contains(a[i])) {
                list.add(a[i]);
            }
        }
        return (String[])list.toArray(new String[list.size()]);
    }
    
    @Test
	public static void downfileTest() {
		String sql = "select * from app_detail_info app where uploadid='244affd1617a412eb89a64dc898b64cb' AND source ='360' ORDER BY soft ASC,islist ASC,currentpage asc  limit ?,? ";
		List<Apkdetail> apksList = new ArrayList<Apkdetail>();
		Integer curpage = 0, pagesize = 300;
		ApkdetailDao apkdao = new ApkdetailDaoImpl();
		UrldownloadDao urlDao = new UrldownloadDaoImpl();
		String UUID = UuidUtil.getUUID();// 批次号
		String file_date = FileDownloadUtil.getDateYMD();// 2015/0510
		fileSyncThreadPoolInit();
		try {
			apksList = apkdao.findApkdetails(sql, curpage, pagesize);
			logger.error(UUID+"批次号下载条数:" +apksList.size());
			String randomNum = GenerateRandomUtils.getCharAndNumr(5);// 生成5位的随机数

			String rootPath = ProUtil.getString("Download_Path");
			for (int i = 0; i < apksList.size(); i++) {
				Apkdetail apkdetail =null;
				try {
					 apkdetail = apksList.get(i);
					String pkg = apkdetail.getPackagename();// 包名
					System.out.println("======" + pkg);
					Map<String, String> downMap = new HashMap<String, String>();
					if (i % 300 == 0) {// 每个随机数保存500个文件
						randomNum = GenerateRandomUtils.getCharAndNumr(5);// 生成5位的随机数
					}
					// 下载图标
					String iconurl = apkdetail.getSiconurl();
					if (!SUtil.isEmpty(iconurl)) {
						String tmp_icon = "icon/" + file_date + "/" + randomNum + "/" + UuidUtil.getUUID()+ FileDownloadUtil.getFileSuffix(iconurl);
						downMap.put(iconurl, UUID + "/" + tmp_icon);
						apkdetail.setIconurl(tmp_icon);
					}
					// 下载介绍图片
					String image_down = "";
					String type = "";
					if (!SUtil.isEmpty(apkdetail.getImage_h())) {
						image_down = apkdetail.getImage_h();
						type = "remarkimages_h";
					} else if (!SUtil.isEmpty(apkdetail.getImage_m())) {
						image_down = apkdetail.getImage_m();
						type = "remarkimages_m";
					} else {
						image_down = apkdetail.getImage_l();
						type = "remarkimages_l";
					}
					String[] images = FileDownloadUtil.getImage_h(image_down);
					List<String> imagejson = new ArrayList<String>();
					//介绍图片可能有同样的图片，必须先过滤掉
					System.out.println("======"+images);
					
					if (images != null && images.length > 0) {
						images = array_unique(images);
						for (String image_h : images) {
							String path = "image/" + file_date + "/" + randomNum + "/" + UuidUtil.getUUID()+ FileDownloadUtil.getFileSuffix(image_h);
							downMap.put(image_h, UUID + "/" + path);
							imagejson.add(path);

						}

					}
					String jsonString = ImageUtil.makeJson(imagejson);
					if (type == "remarkimages_h") {
						apkdetail.setRemarkimages_h(jsonString);
					} else if (type == "remarkimages_m") {
						apkdetail.setRemarkimages_m(jsonString);
					} else if (type == "remarkimages_l") {
						apkdetail.setRemarkimages_l(jsonString);
					}

					// 下载apk
					String apkurl = apkdetail.getSapkurl();
					if (!SUtil.isEmpty(apkurl)) {
						String tmp_apk = "apk/" + file_date + "/" + randomNum + "/"+ apkdetail.getApkmd5() + "/" + pkg + ".apk";
						downMap.put(apkurl, UUID + "/" + tmp_apk);
						apkdetail.setApkUrl(tmp_apk);
					}
					apkdetail.setUploadid(UUID);// 记录一下批次号
					apkdetail.setStatus(3);// 表示已加入下载
					// 更新apk的icon,image,apk下载路径
//					apkdao.update(apkdetail, new String[] { "iconurl", "apkurl", type,"status", "uploadid" }, "where id = " + apkdetail.getId());
					System.out.println(downMap);
					rsyncPool.execute(new DownloadTask(rootPath, downMap));
					System.out.println("线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("加入下载任务时错误：pkg",apkdetail.getPackagename(),e);
				}
				
			}

			// 插入当前下载批次信息
			urlDao.save(new Urldownload(UUID, 0));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			rsyncPool.shutdown();
			 if (!(rsyncPool.isTerminated()));
			 System.out.println("--------------");
			try {
				boolean Flag = true;
				do {
					Flag = !rsyncPool.awaitTermination(30L, TimeUnit.SECONDS);
					System.out.println("线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());

				} while (Flag);
				System.out.println("========完成下载任务-=");
				logger.error("====批次号下载完毕！==="+UUID);
				urlDao.update("update urldownload set status = 4,updatetime = now() where uuid ='"+UUID+"'");
				FileCheckTool.checkFileDown(UUID);
				//更新检查完成
				urlDao.update("update urldownload set status = 1,updatetime = now() where uuid ='"+UUID+"'");
//				//生成各种图片的缩略图，读取apk的签名，成功的话更新apk status为6
				FileCheckTool.checkfile(UUID);
//				//检查生成缩略图之后的文件是否存在,存在状态 修改为7
				FileCheckTool.checkDownAfter(UUID);
//				//copy成功的文件到其他目录，准备上传
				FileCheckTool.copyToupload(UUID);
//				抽取数据,status 为7
			} catch (InterruptedException e) {
				while (true) {
					logger.error("关闭rsyncPool出错:", e);
				}
			}
		}
	}
	
	public static void main(String[] args) {
//		downfileTest();
//		downfile();
		test1();
	}
	
	@Test
	public static  void test1(){
//		String UUID ="e38e734fbcb24bab938400e072dc5611";
		//从配置文件读取批次号
		String UUID = readFile("/data/uuid.txt");
		if(SUtil.isEmpty(UUID)){
			return ;
		}
		FileCheckTool.checkFileDown(UUID);
		//更新检查完成
//		urlDao.update("update urldownload set status = 1,updatetime = now() where uuid ='"+UUID+"'");
//		//生成各种图片的缩略图，读取apk的签名，成功的话更新apk status为6
		FileCheckTool.checkfile(UUID);
//		//检查生成缩略图之后的文件是否存在,存在状态 修改为7
		FileCheckTool.checkDownAfter(UUID);
		//copy成功的文件到其他目录，准备上传
		FileCheckTool.copyToupload(UUID);
	}
	public static String readFile(String filepath){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));
			String uuid = reader.readLine();
			return uuid;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
