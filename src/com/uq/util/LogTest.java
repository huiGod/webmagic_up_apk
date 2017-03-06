package com.uq.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LogTest {

	public static void savelog(String path,String content){
		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(new File(path), true));
			buf.write(content);
			buf.flush();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void writeFile(String filePathAndName, String fileContent) {
		  try {
		   File f = new File(filePathAndName);
		   if (!f.exists()) {
		    f.createNewFile();
		   }
		   OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f,true),"UTF-8");
		   BufferedWriter writer=new BufferedWriter(write);  
		   //PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePathAndName)));
		   //PrintWriter writer = new PrintWriter(new FileWriter(filePathAndName));
		   writer.write(fileContent);
		   writer.close();
		  } catch (Exception e) {
		   System.out.println("写文件内容操作出错");
		   e.printStackTrace();
		  }
		} 
}
