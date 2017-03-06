package com.uq.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

public class CheckFileCode {
	public static String getFileCheckCode(File file){
		String rtCode = "";
		if(file != null && file.exists()){
			FileInputStream is = null;
			int readSize = 1024*8;
			byte[] bytes = new byte[readSize];
			byte[] tmpBytes = {0, 0, 0, 0, 0, 0, 0, 0};
			int s=0;
			try{
				is = new FileInputStream(file);
		        while(is.read(bytes, 0, readSize)!= -1){
		        	s++;
		        	for(int i=0; i<readSize/64; i++){
		            	for(int j=0; j<8; j++){
		            		tmpBytes[j] = (byte) (bytes[i*64+j*8] ^ tmpBytes[j]);
		            	}
		            }
		            bytes = new byte[readSize];
		        }
		        rtCode = BASE64.decodeToHex(BASE64.encode(tmpBytes));
			}catch(Exception e){
				e.printStackTrace();
			} finally{
				if(is != null){
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						is = null;
					}
				}
			}
		}
		return rtCode;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		String pre = "C:\\Users\\lxl\\Desktop\\首页推荐前5 APP\\";
//		String[] names = {"58同城_5.8.0.ipa", "爱奇艺视频播放器_5.8.1.ipa", "全民奇迹.ipa", "手机百度_6.0.6.ipa", "笑傲江湖_1.0.5.ipa", "s1416490152523_210434.ipa"};
//		String[] codes = {"D28B2E74559B66F9", "B846BF257CC61F1A", "C422F076B4057373", "60F3E9CB26D2512A", "6488A7E896BCD0AE", "595819EA67321834"};
//		for(int i=0; i<names.length; i++){
//			System.out.println("------------------------------");
//			File f = new File(pre+names[i]);
//			Date d = new Date();
//			String code = getFileCheckCode(f);
//			System.out.println(code + "  ==  " + codes[i] + "  ==  " + code.equals(codes[i]));
//			System.out.println(f.length() + "  ==  " + (new Date().getTime() - d.getTime()));
//		}
		long start = new Date().getTime();
		String a = "F:/fileupload/com.supercell.boombeach.qihoo_20050.apk";
		String code = getFileCheckCode(new File(a));
		System.out.println(code);
		long end = new Date().getTime();
		System.out.println(end-start);

	}

}
