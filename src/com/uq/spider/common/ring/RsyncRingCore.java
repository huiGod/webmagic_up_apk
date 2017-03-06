package com.uq.spider.common.ring;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.dao.ErrorLogDao;
import com.uq.dao.RingDao;
import com.uq.dao.impl.ErrorLogDaoImpl;
import com.uq.dao.impl.RingDaoImpl;
import com.uq.model.Ring;
import com.uq.util.ProUtil;


public class RsyncRingCore {

	private static ThreadPoolExecutor rsyncPool;
	private static Logger logger = LoggerFactory.getLogger(RsyncRingCore.class);
	public static List<Future> futureList = new ArrayList<Future>();
	private static final String  Ring_SavePath ="audio";
	
	public static void main(String[] args) {
		String uuid ="6be8c598970c4e189253823f52b089e1";
		multiUpload(uuid);
	}
	private static void fileSyncThreadPoolInit() {
		logger.info("开始初始化同步铃声需要的线程池...");
		rsyncPool = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,new ArrayBlockingQueue(300), new RejectedExecutionHandler() {
					public void rejectedExecution(Runnable r,ThreadPoolExecutor executor) {
						if (!(executor.isShutdown()))
							try {
								executor.getQueue().put(r);
							} catch (InterruptedException e) {
							}
					}
				});
	}
	
	//上传铃声
	public static boolean multiUpload(String download_uuid){
		RingDao ringDao = new RingDaoImpl();
		String sql ="select * from ring where uploadid = '"+download_uuid+"' and status = 7 limit ?,?";
//		String sql ="select * from ring where md5='11632edf3cff701a0d6a4a8723fa2294' limit ?,?";
		System.out.println(sql);
		List<Ring> ringList = new ArrayList<Ring>();
		int page =0,pagesize = 2000;
		ErrorLogDao logDao = new ErrorLogDaoImpl();
		fileSyncThreadPoolInit();
		long start = new Date().getTime();
		try {
			String pre_path = ProUtil.getString("Upload_Path");
			do {
				ringList = ringDao.findAllList(sql, page, pagesize);
				page++;	
				List<String> uploadList = new ArrayList<String>();
				List<Ring> list = new ArrayList<Ring>();
//				String uppath = ProUtil.getString("Upload_Path");
				for (int i = 0; i < ringList.size(); i++) {
					Ring ring = ringList.get(i);
					list.add(ring);
					uploadList.add(Ring_SavePath+"/"+ring.getUploadurl());//上传的filekey

					if(i!=0 && i%20 == 0){
						Future<String> f = rsyncPool.submit(new RsyncRingTask(list, uploadList, pre_path));//加入超时控制
						futureList.add(f);
						uploadList = new ArrayList<String>();
						list = new ArrayList<Ring>();
					}
					System.out.println("线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
//					break;
				}
				System.out.println("=========="+list.size());
				if(list.size()>0){
					Future<String> f = rsyncPool.submit(new RsyncRingTask(list, uploadList, pre_path));//加入超时控制
					futureList.add(f);					
				}
				
				
			} while (ringList.size()>0);
			
//			timeoutControl();
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			System.out.println("==关闭==");
			rsyncPool.shutdown();
			 if (!(rsyncPool.isTerminated()));
			 System.out.println("--------------");
			try {
				boolean Flag = true;
				do {
					Flag = !rsyncPool.awaitTermination(50L, TimeUnit.SECONDS);
					logger.error(download_uuid+" 线程池中线程数目："+rsyncPool.getPoolSize()+"，队列中等待执行的任务数目："+
							rsyncPool.getQueue().size()+"，已执行完成的任务数目："+rsyncPool.getCompletedTaskCount());
					System.out.println("uploadid:"+download_uuid+"total PoolSze："+rsyncPool.getPoolSize()+",waiting task size："+
							rsyncPool.getQueue().size()+",completed task："+rsyncPool.getCompletedTaskCount());
					if(rsyncPool.getPoolSize()<=2){
						timeoutControl(900);//线程超时秒数
					}
				} while (Flag);
				long end = new Date().getTime();
				logger.error("批次："+download_uuid+" 上传总共耗时："+(end-start)/1000+" s");
				logger.error("========真正完成任务-=");
				return true;
			} catch (InterruptedException e) {
				while (true) {
					logger.error("关闭rsyncPool出错:", e);
				}
			}
		}
		
		
	}
	
	
	//超时控制
	public static void timeoutControl(long timeout){
		int taskSize = futureList.size();
		while(taskSize > 0)
        {
        	System.out.println("-----------------"+taskSize);
          for (Future<String> future : futureList)
          {
            String result = null;
            
            try
            {
            
              result = future.get(timeout, TimeUnit.SECONDS);//10分钟
              System.out.println("result:"+result);
            } catch (InterruptedException e)
            {
              e.printStackTrace();
            } catch (ExecutionException e)
            {
              e.printStackTrace();
            } catch (TimeoutException e)
            {
              // 超时异常需要忽略,因为我们设置了等待时间为0,只要任务没有完成,就会报该异常
            	future.cancel(true);
            	System.out.println("超时");
            	taskSize--;
            }
            
            // 任务已经完成
            if(result != null)
            {
              System.out.println("result=" + result);
              
              // 从future列表中删除已经完成的任务
              futureList.remove(future);  
              taskSize--;
              //此处必须break，否则会抛出并发修改异常。（也可以通过将futureList声明为CopyOnWriteArrayList类型解决） 
              break; // 进行下一次while循环
            }
          }
        }
	}
}
