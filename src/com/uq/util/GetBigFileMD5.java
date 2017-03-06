package com.uq.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 计算大文件MD5 
 *  David  2012-10-12
 */
public class GetBigFileMD5 {
   
    static MessageDigest MD5 = null;


    static {
        try {
        MD5 = MessageDigest.getInstance("MD5");//SHA1 MD5
        } catch (NoSuchAlgorithmException ne) {
        ne.printStackTrace();
        }
    }

    /*
     * 对一个文件获取md5值 速度快一点
     */
    
	public static String getmd5(File file) {
		FileInputStream fStream = null;
		try {
			fStream = new FileInputStream(file);
			FileChannel fChannel = fStream.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
			long s = System.currentTimeMillis();
			for (int count = fChannel.read(buffer); count != -1; count = fChannel
					.read(buffer)) {
				buffer.flip();
				MD5.update(buffer);
				if (!buffer.hasRemaining()) {
					buffer.clear();
				}
			}
			s = System.currentTimeMillis() - s;
			System.out.println("耗时1：" + s );
			return new String(Hex.encodeHex(MD5.digest()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {  
		        if( fStream!=null )  
		            fStream.close();  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    } 
		}
		return null;
	}

    /**
     * 对一个文件获取md5值
     * @return md5串
     */
    public static String getMD5(File file) {
        FileInputStream fileInputStream = null;
        try {
        fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            long s = System.currentTimeMillis();
            while ((length = fileInputStream.read(buffer)) != -1) {
            MD5.update(buffer, 0, length);
            }
            s = System.currentTimeMillis() - s;
            System.out.println("耗时2："+s+" ms");
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (FileNotFoundException e) {
        e.printStackTrace();
            return null;
        } catch (IOException e) {
        e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null)
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 求一个字符串的md5值
     * @param target 字符串
     * @return md5 value
     */
    public static String MD5(String target) {
        return DigestUtils.md5Hex(target);
    }


    public static void main(String[] args){
   
    long beginTime =System.currentTimeMillis();
//      File fileZIP = new File("D:/TEST/IMAGE2.zip");
//      String md5=getMD5(fileZIP);
//    	File file = new File("E:/filedowntest/app/20140717/com.linekong.szr.lk_20140717204131_246287.apk");
//    File file = new File("E:/filedowntest/app/20140717/cn.centuryecity.xbhgum_20140717203418_564902.apk");
//    File file = new File("E:/优启/UQweb1/new/gpt_main0_32G.bin");//shoujikong.apk  C:/Users/cp/Downloads/ranshaodeshucai3_101_0811_122_141147.apk
    File file = new File("E:/优启/UQweb1/new/2/gpt_main0_jie.bin"); 
    System.out.println("文件大小："+file.length());
    	//46E8DF11A6078770C85C2C25D6AD1F0D
    	String md5=getMD5(file);
    	System.out.println(md5);
    	System.out.println(MD5("123456"));
      long endTime =System.currentTimeMillis();     
      System.out.println("========="+MD5("chinaszaa"));
     System.out.println("MD5:"+md5+"\n time:"+((endTime-beginTime)/1000)+"s");
     
     
    }
    
    
}