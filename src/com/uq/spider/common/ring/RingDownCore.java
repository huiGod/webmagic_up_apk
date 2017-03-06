package com.uq.spider.common.ring;

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

import com.uq.dao.RingDao;
import com.uq.dao.UrldownloadDao;
import com.uq.dao.impl.RingDaoImpl;
import com.uq.dao.impl.UrldownloadDaoImpl;
import com.uq.model.Ring;
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
public class RingDownCore {

	public static ThreadPoolExecutor rsyncPool;
	private static Logger logger = LoggerFactory.getLogger(RingDownCore.class);
	public static final String RING_SAVEPATH ="audio";

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
		String sql ="select * from ring where status = 2  order by id limit ?,? ";
		RingDao ringDao = new RingDaoImpl();
		List<Ring> l = new ArrayList<Ring>();
		Integer curpage = 0, pagesize =1500;
		UrldownloadDao urlDao = new UrldownloadDaoImpl();
		String UUID = UuidUtil.getUUID();// 批次号
		try {
			fileSyncThreadPoolInit();
			l = ringDao.findAllList(sql, curpage, pagesize);
			System.out.println("下载条数:"+l.size());
			String rootPath = ProUtil.getString("Download_Path");//下载保存的目录
			String file_date = FileDownloadUtil.getDateYMD();// 2015/0510
			String randomNum = GenerateRandomUtils.getCharAndNumr(5);// 生成5位的随机数			
			for (int i = 0; i < l.size(); i++) {
				try {
					Ring ring = l.get(i);
					String down_ringUrl =ring.getDownurl();
					Map<String, String> downMap = new HashMap<String, String>();
					if (!SUtil.isEmpty(down_ringUrl)) {
						String tmp_ring =  file_date + "/" + randomNum + "/" + UuidUtil.getUUID()+(SUtil.isEmpty(FileDownloadUtil.getFileSuffix(down_ringUrl))?".mp3":FileDownloadUtil.getFileSuffix(down_ringUrl));
						System.out.println(ring.getId()+":"+down_ringUrl);
						downMap.put(down_ringUrl, UUID + "/"+RING_SAVEPATH+"/" + tmp_ring);
						ring.setUploadurl(tmp_ring);
					}
					ring.setUploadid(UUID);
					ring.setStatus(3);//已加入下载
					ringDao.update(ring, new String[]{"uploadurl","status","uploadid"}, " where id = "+ring.getId());
					rsyncPool.execute(new DownloadTask(rootPath, downMap));
					System.out.println("壁纸下载线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 插入当前下载批次信息
			if(l!=null && l.size()>0){
				urlDao.save(new Urldownload(UUID, 0,0,3,l.size()));//3表示铃声类型
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
					RingCheckTool.checkFileDown(UUID);
					RingCheckTool.copyToupload(UUID);
					RingCheckTool.checkDownAfter(UUID);
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
