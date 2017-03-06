package com.uq.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteFileUtil {

	private static String planDir="";
	
	private static String writeTaskPlan(String path, String taskName, long size, int count){
		System.out.println(taskName);
		BufferedWriter out = null;
		boolean rname = false;
		String url = planDir+taskName;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String hehe = dateFormat.format(new Date());
		String newfilename = "c_"+hehe+"_"+size+"_"+count+".fqf";
		String newurl = planDir+newfilename;
		String fp = path.substring(0, path.lastIndexOf("/"));
		try{
			File file = new File(url);
			if(!file.exists()){
				file.createNewFile();
			}
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
			StringBuffer s = new StringBuffer();
			s.append(1);
			s.append((char)1);
			s.append(4);
			s.append((char)1);
			s.append((char)2);
			s.append("-");
			s.append((char)1);
			s.append((char)3);
			s.append("34567890kuaiwang_3");
			s.append((char)1);
			s.append("X:\\cracked\\resources\\soft\\"+fp.replace("/", "\\"));
			s.append((char)1);
			s.append("/d.app3.i4.cn/soft/"+fp);
			s.append((char)1);
			s.append(0);
			s.append((char)1);
			s.append(0);
			s.append((char)1);
			s.append((char)1);
			s.append(0);
            out.write(s.toString()+"\n");
            rname = true;
		} catch(Exception e){
			e.printStackTrace();
			rname = false;
			newfilename = taskName;
		} finally{
			try {     
                if(out != null){  
                    out.close();     
                }  
            } catch (IOException e) {     
            }
            if(rname){
            	try{
            		File file = new File(url);
                	file.renameTo(new File(newurl));
            	}catch(Exception e){
            		newfilename = taskName;
            	}
            }
		}
		return newfilename;
	}
}
