package com.uq.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import sun.net.ftp.FtpClient;
import sun.util.logging.resources.logging;

public class FTPUploadUtil {

	private static FTPClient ftp = new FTPClient();
	private static Map<String, FTPClient> ftps = new HashMap<String, FTPClient>();
	//分开写不同的日志，以便查找
	private static Logger errorlog = LoggerFactory.getLogger("sycError");
	private static Logger infolog = LoggerFactory.getLogger("sycInfo");
	
	private static String syncAppFileUrl ="src3.ftp.cachecn.net";// ConfigUtils.getString("syncAppFileUrl");
	private static String FTP_USER1 = "1i4_apk";//ConfigUtils.getString("FTP_USER");
	private static String FTP_PWD1 ="1ctZ9UDmkTJFp";// ConfigUtils.getString("FTP_PWD");
	private static String ROOTPATH  ="E:/qqfile/";
	
	public static FTPClient getFTPClient(String ftpNum, int ServerNum){
		if(ftps == null || ftps.get(ftpNum) == null || !ftps.get(ftpNum).isConnected()){
			FTPClient ftp = new  FTPClient();
			try {
				if(ServerNum == 1){
					ftp.connect("192.168.1.249", 21);
//					ftp.connect(syncAppFileUrl, 21);
					if(FTPReply.isPositiveCompletion(ftp.getReplyCode())){
						ftp.login(FTP_USER1, FTP_PWD1);
					}
				}else if(ServerNum == 2){
					ftp.connect("FTPurl_2", 21);
					if(FTPReply.isPositiveCompletion(ftp.getReplyCode())){
						ftp.login("username", "password");
					}
				}else if(ServerNum == 3){
					ftp.connect("FTPurl_3", 21);
					if(FTPReply.isPositiveCompletion(ftp.getReplyCode())){
						ftp.login("username", "password");
					}
				}
				ftps.put(ftpNum, ftp);
			} catch (SocketException e) {
				errorlog.error("FTP连接错误:",e);
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ftps.get(ftpNum);
	}
	
	/**
	 * 上传时新建目录，直接上传时才能找到这个目录去上传
	 */
	public static boolean createDirectory(List<String> originalMuluPath ){
		boolean b = true;
		String ftpNum = Math.random()+""+new Date().getTime();
		FTPClient ftp = getFTPClient(ftpNum, 1);
		for (int i = 0; originalMuluPath!=null && i < originalMuluPath.size(); i++) {
			String tmpPath = originalMuluPath.get(i);
			try {
				if(ftp.cwd(tmpPath) == 550){
					ftp.mkd(tmpPath);
				}
			} catch (IOException e) {
				b = false;
				e.printStackTrace();
			}
		}
		return b;
	}
	
	/**
	 * 上传辅助方法
	 * @return
	 */
	public static boolean upload(List<String> originalUrl,long apksize){
		boolean b = true;
		String ftpNum = Math.random()+""+new Date().getTime();
		FTPClient ftp = getFTPClient(ftpNum, 1);
		long size = 0L;
		for (int i = 0; i < originalUrl.size(); i++) {
			String fileUrl = originalUrl.get(i);
			if(!SUtil.isEmpty(fileUrl)){
				String localPath = ROOTPATH+fileUrl;;
				if(fileUrl.startsWith("apk")){
					size = apksize;
				}
				if(!uploadFile(localPath, size, "/"+fileUrl, ftp)){
					b= false;//有其中一个上传失败，则返回false，不更新应用的状态
				}else{
					System.out.println("成功第"+(i+1)+"个");
				}
			}
				
		}
		shutDown(ftpNum);
		return b;
	}
	/** 
     * 上传文件到FTP服务器，支持断点续传 
     * @param local 本地文件名称，绝对路径 
     * @param remote 远程文件路径，使用/home/directory1/subdirectory/file.ext 按照Linux上的路径指定方式，支持多级目录嵌套，支持递归创建不存在的目录结构 
     * @return 上传结果 
     * @throws IOException 
     */  
	public static boolean uploadFile(String localPath, long bytesize, String url,FTPClient ftp){
		System.out.println("本地文件路径："+localPath);
		System.out.println(url);
		//获取ftp连接
//		FTPClient ftp = getFTPClient("", 1);
		boolean flag = false;
		try {
			File f = new File(localPath);
			String path = url;
			if(f.exists()){//本地是否存在这个文件
				InputStream in = new FileInputStream(f);
				String[] ar = path.split("/");
//				String[] ar = path.split(File.separator);
				String tmpPath = "";
				//对远程ftp目录的处理
				for(int i=0; i<ar.length-1; i++){
					if(ar[i].length() > 0){
						tmpPath+="/"+ar[i];
//						System.out.println("目录："+tmpPath+" "+ftp.cwd(tmpPath));
						if(ftp.cwd(tmpPath) == 550){
							ftp.mkd(tmpPath);
						}
						/*if(ftp.makeDirectory(tmpPath)){
							System.out.println("创建目录成功");
						}else {
							errorlog.error("创建目录失败：{}",ar[i]);
						}*/
					}
				}
				//检查远程是否存在文件
				FTPFile[] files = ftp.listFiles(path);
				if(files.length == 1){//服务器存在这个同名文件，判断是否需要断点续传
					System.out.println("==="+files[0].getName());
					if(files[0].getSize() < f.length()){
						System.out.println(flag=uploadFile(path, f, ftp, files[0].getSize()));
						infolog.info("该文件需要断点续传：",f.getAbsoluteFile());
					}else{
						flag = true;
						System.out.println("已经存在该文件");
					}
				}else{ //不存在的话直接上传即可
					System.out.println(flag=uploadFile(path, f, ftp, 0));
				}
			}else{//本地不存在这个文件的话，说明有可能上传之后被删了，此时去远程服务器检查一下
				FTPFile[] files = ftp.listFiles(path);
				if(path.endsWith(".apk")){
					if(files.length == 1 && files[0].getSize() == bytesize){
						flag = true;
						System.out.println("服务器已存在这文件："+path);
					}else{
						flag = false;
						System.out.println(localPath +" == FileNotFound!");
						errorlog.error("文件不存在：",localPath);
					}
				}else{ //其他图片文件
					if(files.length == 1){
						flag = true;
					}else{
						flag = false;
						System.out.println(localPath +" == FileNotFound!");
						errorlog.error("文件不存在：",localPath);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			errorlog.error("文件上传错误",e);
		}
		return flag;
	}
	
	/** 
     * 上传文件到服务器,新上传和断点续传 
     * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变 
     * @param localFile 本地文件File句柄，绝对路径 
     * @param processStep 需要显示的处理进度步进值 
     * @param ftpClient FTPClient引用 
     * @return 
     * @throws IOException 
     */  
    private static boolean uploadFile(String remoteFile,File localFile,FTPClient ftpClient,long remoteSize) throws IOException{  
        boolean status;  
        //显示进度的上传  
        long step = localFile.length() / 100;  
        long process = 0;  
        long localreadbytes = 0L;  
        ftpClient.enterLocalPassiveMode();  
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);  
        RandomAccessFile raf = new RandomAccessFile(localFile,"r");  
        OutputStream out = ftpClient.appendFileStream(remoteFile);  
        //断点续传  
        if(remoteSize>0){  
            ftpClient.setRestartOffset(remoteSize);  
            process = remoteSize /step;  
            raf.seek(remoteSize);  
            localreadbytes = remoteSize;  
        }  
        byte[] bytes = new byte[1024*10];  
        int c;
        int st = 1;
        while((c = raf.read(bytes))!= -1){
        	st += c;
            out.write(bytes,0,c);  
            localreadbytes+=c;  
            if(localreadbytes / step != process){  
                process = localreadbytes / step;  
                System.out.println("上传进度:" + process);  
                //TODO 汇报上传状态  
            }
            bytes = new byte[1024];
        }  
        out.flush();  
        raf.close();  
        out.close();  
        boolean result =ftpClient.completePendingCommand();  
        FTPFile[] files = ftpClient.listFiles(remoteFile);
		if(files.length == 1 && result){
			if(files[0].getSize() == localFile.length()){
				result = true;
			}else{
				result = false;
			}
		}else{
			result = false;
		}
        return result;  
    }
    
	public static void shutDown(){
		System.out.println("ShutDown FTP Connect!");
		if(ftp != null && ftp.isConnected()){
			try {
				ftp.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				errorlog.error("FTP关闭连接失败:",e);
			} finally{
				ftp = null;
				System.out.println("ShutDown FTP Connect SUCCESS!");
			}
		}
	}
	
	public static void shutDown(String ftpNum){
		System.out.println("ShutDown FTP Connect!");
		if(ftps != null && ftps.get(ftpNum) != null && ftps.get(ftpNum).isConnected()){
			try {
				ftps.get(ftpNum).disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				errorlog.error("FTPS关闭连接失败:",e);
			} finally{
				ftps.remove(ftpNum);
				System.out.println("ShutDown FTP("+ftpNum+") Connect SUCCESS!");
			}
		}
	}
	
	/**
	 * 检查文件是否存在
	 * @param args
	 */
	public static void checkApkFile(String path,FTPClient ftp,String type,long apksize){
		//检查远程是否存在文件
		
		try {
			
			if("apk".equalsIgnoreCase(type)){
				FTPFile[] files = ftp.listFiles(path);
				if(files.length == 1){//服务器存在这个同名文件，判断是否需要断点续传
					System.out.println("存在："+path);
					/*if(files[0].getSize() != apksize){						
						infolog.info("该文件上传错误："+path);
						addRedisSet("error_download",path);
					}*/
				}else{ //不存在的话直接上传即可
					System.out.println("不存在这个文件："+path);
					addRedisSet("no_download",path);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 检查文件是否存在
	 * @param args
	 */
	public static void checkApkFile1(List<Map<String, Long>> maplist,FTPClient ftp,String type){
		//检查远程是否存在文件
		
		try {
			
			if("apk".equalsIgnoreCase(type)){
				for (int i = 0; i < maplist.size(); i++) {
//					Map<String, Long> map = maplist.get(i);
					 for(Map.Entry<String, Long> entry:maplist.get(i).entrySet()){
						String path = entry.getKey();
						 FTPFile[] files = ftp.listFiles(path);
							if(files.length == 1){//服务器存在这个同名文件，判断是否需要断点续传
//								System.out.println(path);
								if(files[0].getSize() != entry.getValue()){						
									infolog.info("该文件上传错误："+path);
									addRedisSet("error_download",path);
								}
							}else{ //不存在的话直接上传即可
								System.out.println("不存在这个文件："+path);
								addRedisSet("redis_no_download",path);
							}
					 }
					 
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
	/*	Jedis jedis = RedisUtil.getPool().getResource();
		Set<String> set  = jedis.smembers("tempdown");
		String ftpNum = Math.random()+""+new Date().getTime();
		FTPClient ftp1 = getFTPClient(ftpNum, 1);
		for(String pkg:set){
			String apkurl = jedis.hget("pkg:"+pkg, "apkurl");
			long size = Long.parseLong(jedis.hget("pkg:"+pkg, "size"));
			checkApkFile(apkurl,ftp1,"apk",size);
		}*/
		
		String ftpNum = Math.random()+""+new Date().getTime();
		FTPClient ftp1 = getFTPClient(ftpNum, 1);
		checkApkFile("app/20141008/com.joelapenna.foursquared_20141008143506_709817.apk",ftp1,"apk",17930260L);
	}
	
	public static void addRedisSet(String key,String member){
//		Jedis jedis = RedisUtil.getJedis();
//		jedis.sadd(key, member);
//		RedisUtil.r
	}
	/*public static void main(String[] args) {
		String localPath = "E:/qqfile/app/20140928/com.ahzs.sy4399_20140928135720_305403.apk";
		long bytesize = new File(localPath).length();
		String url = "/app/20140928/com.ahzs.sy4399_20140928135720_305403.apk";
		String ftpNum = Math.random()+""+new Date().getTime();
		int ftpServer = 1;
		FTPClient ftp1 = getFTPClient(ftpNum, 1);
		uploadFile(localPath, bytesize, url, ftp1);
//		uploadFile(localPath, bytesize, url, ftpNum, ftpServer);
	}*/
}
