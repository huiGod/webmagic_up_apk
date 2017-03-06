package com.uq.spider.common.paper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.dao.PaperDao;
import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.PaperDaoImpl;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.Paper;
import com.uq.model.Urldownload;
import com.uq.spider.common.tool.DownloadTask;
import com.uq.util.FileDownloadUtil;
import com.uq.util.GenerateRandomUtils;
import com.uq.util.ProUtil;
import com.uq.util.SUtil;
import com.uq.util.UuidUtil;


/**
 * 壁纸下载
 * @author jinrong
 *
 */
public class PaperDownCore {

	public static ThreadPoolExecutor rsyncPool;
	private static Logger logger = LoggerFactory.getLogger(PaperDownCore.class);
	public static final String Ring_SavaPath = "audio";

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
	
	/**
	 * 下载铃声
	 */
	public static void downFile(){
		String sql ="select * from paper where status = 2 order by id limit ?,? ";
		PaperDao paperDao = new PaperDaoImpl();
		List<Paper> l = new ArrayList<Paper>();
		Integer curpage = 0, pagesize =1500;
		UrldownloadDao urlDao = new UrldownloadDaoImpl();
		String UUID = UuidUtil.getUUID();// 批次号
		try {
			fileSyncThreadPoolInit();
			l = paperDao.findAllList(sql, curpage, pagesize);
			System.out.println("下载条数:"+l.size());
			String rootPath = ProUtil.getString("Download_Path");//下载保存的目录
			String file_date = FileDownloadUtil.getDateYMD();// 2015/0510
			String randomNum = GenerateRandomUtils.getCharAndNumr(5);// 生成5位的随机数			
			for (int i = 0; i < l.size(); i++) {
				try {
					Paper paper = l.get(i);
					String down_imageUrl =paper.getDownurl();
					Map<String, String> downMap = new HashMap<String, String>();
					if (!SUtil.isEmpty(down_imageUrl)) {
						String tmp_icon =  file_date + "/" + randomNum + "/" + UuidUtil.getUUID()+".jpg";
						System.out.println(paper.getId()+":"+down_imageUrl);
						downMap.put(down_imageUrl, UUID + "/" + tmp_icon);
						paper.setUploadurl(tmp_icon);
					}
					paper.setUploadid(UUID);
					paper.setStatus(3);//已加入下载
					paperDao.update(paper, new String[]{"uploadurl","status","uploadid"}, " where id = "+paper.getId());
					rsyncPool.execute(new DownloadTask(rootPath, downMap));
					System.out.println("壁纸下载线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 插入当前下载批次信息
			if(l!=null && l.size()>0){
				urlDao.save(new Urldownload(UUID, 0,0,2,l.size()));//壁纸批次
			}
			System.out.println("下载批次号："+UUID);
			
		} catch (Exception e) {
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
				//检查文件
				if(l.size()>0){
					PaperCheckTool.checkFileDown(UUID);//文件下载是否完整
					PaperCheckTool.createThumbnail(UUID);//文件缩略图生成
					PaperCheckTool.checkFileAfter(UUID);//文件缩略图是否存在
					PaperCheckTool.copyToupload(UUID);//准备上传
				}
				
				
			} catch (InterruptedException e) {
				while (true) {
					logger.error("关闭rsyncPool出错:", e);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		downFile();
	}
}
