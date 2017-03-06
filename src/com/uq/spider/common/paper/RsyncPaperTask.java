package com.uq.spider.common.paper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import redis.clients.jedis.Jedis;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chinanetcenter.api.domain.HttpClientResult;
import com.chinanetcenter.api.domain.PutPolicy;
import com.chinanetcenter.api.util.Config;
import com.chinanetcenter.api.util.DateUtil;
import com.chinanetcenter.api.util.EncodeUtils;
import com.chinanetcenter.api.wsbox.FileManageCommand;
import com.chinanetcenter.api.wsbox.FileUploadCommand;
import com.uq.dao.PaperDao;
import com.uq.dao.PaperForTypeDao;
import com.uq.dao.PaperTypeDao;
import com.uq.dao.impl.PaperDaoImpl;
import com.uq.dao.impl.PaperForTypeDaoImpl;
import com.uq.dao.impl.PaperTypeDaoImpl;
import com.uq.model.Paper;
import com.uq.model.PaperType;
import com.uq.util.HttpUtils;
import com.uq.util.ProUtil;
import com.uq.util.RedisUtil;

/**
 * 上传壁纸图片，完成之后直接上传数据
 * @author jinrong
 *
 */
public class RsyncPaperTask implements Callable<String>{ //implements Runnable
	
	private  static String BucketName_Paper =ProUtil.getString("BucketName_Paper");
	private  static String AK =ProUtil.getString("AK");
	private  static String SK =ProUtil.getString("SK");
	private static PaperForTypeDao paperForTypeDao = new PaperForTypeDaoImpl();
	private static Map<String, PaperType> typeMap = new HashMap<String, PaperType>();
	
	private static PaperDao paperDao = new PaperDaoImpl();
	
	static{
		PaperTypeDao tmptypeDao = new PaperTypeDaoImpl();
		String sql = "select * from papertype  WHERE source ='wdj' ";//从列表保存的分类
		List<PaperType> list = tmptypeDao.query(sql, null);
		for(PaperType pt:list){
			typeMap.put(pt.getName(),pt);
		}
	}
	
	//单个壁纸上传
	private Paper paper;
	private List<String> fileKeys;
	private String pre_path;//文件的前半部分路径
	private String paper_cates;
	
	//批量上传
	private List<Paper> papers_List;
	private List<String> multi_fileKeys;
	private boolean isMultiUpload = false;//是否批量上传
	
	public RsyncPaperTask(List<String> fileKeys,String pre_path,Paper paper,String paper_cates){
		this.fileKeys = fileKeys;
		this.paper = paper;
		this.pre_path = pre_path;
		this.paper_cates = paper_cates;
	}
	
	public RsyncPaperTask(List<Paper> papers_List,List<String> multi_fileKeys,String pre_path,boolean isMultiUpload){
		this.papers_List = papers_List;
		this.multi_fileKeys = multi_fileKeys;
		this.isMultiUpload = isMultiUpload;
		this.pre_path = pre_path;
	}
	
	static{
		String isTest = ProUtil.getString("upload_test");
		if("true".equalsIgnoreCase(isTest)){
		    AK =ProUtil.getString("AK_test");
			SK =ProUtil.getString("SK_test");
			BucketName_Paper =ProUtil.getString("BucketName_test");
		}
		System.out.println("是否上传到测试环境:"+isTest);
		Config.init(AK, SK);
	}
	@Override
	public String call() throws Exception {
		if(isMultiUpload){//批量上传
			List<String> max_fileKeys = new ArrayList<String>();
			List<String> min_fileKeys = new ArrayList<String>();
			for(String tmpfile:multi_fileKeys){
				max_fileKeys.add("max/"+tmpfile);
				min_fileKeys.add("min/"+tmpfile);
			}
			if(multiUploadFile(max_fileKeys, pre_path) && multiUploadFile(min_fileKeys, pre_path) ){
				//批量上传数据
//				multiUploadData();
			}else{//记录失败的
				logredis();
			}
			
//			multiUploadData();
		}else {//单个壁纸文件上传
			
		}
		return "";
	}
	
/*	@Override
	public void run() {
		if(isMultiUpload){//批量上传
			List<String> max_fileKeys = new ArrayList<String>();
			List<String> min_fileKeys = new ArrayList<String>();
			for(String tmpfile:multi_fileKeys){
				max_fileKeys.add("max/"+tmpfile);
				min_fileKeys.add("min/"+tmpfile);
			}
			if(multiUploadFile(max_fileKeys, pre_path) && multiUploadFile(min_fileKeys, pre_path) ){
				//批量上传数据
//				multiUploadData();
			}else{//记录失败的
				logredis();
			}
			
//			multiUploadData();
		}else {//单个壁纸文件上传
			
		}

	}*/
	
	public void logredis(){
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			for(Paper tmpPaper:papers_List){
				jedis.sadd("uploadpaperid", tmpPaper.getId().toString());
			}
		} catch (Exception e) {
			
		}finally{
			RedisUtil.returnResource(jedis);
		}
		
	}

	//批量上传文件
	public static  boolean multiUploadFile(List<String> fileKeys,String pre_path){
		boolean flag = true;
		try {
			List<File> fileList = new ArrayList<File>(); 
			String returnBody = "key=$(key)&fname=$(fname)&fsize=$(fsize)&url=$(url)&hash=$(hash)";
			StringBuilder scope = new StringBuilder();
			for(String filekey:fileKeys){
				File f = new File(pre_path+filekey);
				if(!f.exists()){
					System.out.println(f.getAbsolutePath());
					continue;//不存在，不要上传
				}
				fileList.add(f);
				scope.append(EncodeUtils.urlsafeEncode(BucketName_Paper+":"+filekey+":"+f.getName())).append("#");		
				
			}
			scope.substring(0, scope.length()-1);
			PutPolicy putPolicy = new PutPolicy();
			putPolicy.setOverwrite(1);
			putPolicy.setScope(scope.toString());
//			putPolicy.setReturnBody(returnBody);
			putPolicy.setDeadline(String.valueOf(DateUtil.nextHours(1, new Date()).getTime()));
			HttpClientResult httpClientResult = FileUploadCommand.multiUpload(fileList, putPolicy);
			System.out.println(httpClientResult.getStatus()+"--"+httpClientResult.getResponse());
			if(httpClientResult!=null && httpClientResult.getStatus()== 200){
				try {
					JSONObject jsonObject =JSONObject.parseObject(httpClientResult.response);
					int failNum  = JSONObject.parseObject(jsonObject.getString("brief")).getIntValue("failNum");
					if(failNum>=1){
						flag = false;
						System.out.println("失败个数："+failNum);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
		
				
			}else {
				flag = false;
			}
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 
	 * @Description: 上传单个文件 
	 * @param @param fileName
	 * @param @param localFilePath
	 * @param @return
	 * @date 2015-5-19
	 * @author	aurong
	 * @return boolean
	 */
	public static boolean uploadFile(String fileName,String localFilePath){
		String returnBody = "key=$(key)&fname=$(fname)&fsize=$(fsize)&url=$(url)&hash=$(hash)";
		HttpClientResult httpClientResult = null;
		boolean result = false;
		try {
			File file = new File(localFilePath);
			if(!file.exists()) return true;
			httpClientResult = FileUploadCommand.uploadFileAndReturn(BucketName_Paper, fileName, localFilePath, returnBody);
			if(httpClientResult!=null && httpClientResult.getStatus() == 200){
				result = true;
				System.out.println(httpClientResult.getStatus()+"--"+httpClientResult.response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
		
	}
	
	/**
	 * 
	 * @Description: 判断文件是否存在wcs 
	 * @param fileKey 文件保存时的key
	 * @date 2015-5-19
	 * @author	aurong
	 * @return boolean
	 */
	public static boolean isExist(String fileKey){
		HttpClientResult httpClientResult = null;
		boolean flag = true;
		try {

			httpClientResult =FileManageCommand.stat(BucketName_Paper, fileKey);			
			//先获取文件的信息，判断是否存在
		System.out.println(httpClientResult.getStatus()+":"+httpClientResult.getResponse());
		if(httpClientResult.getStatus()!= 200){
			flag = false;
		}
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}
	
	//上传数据
	public void uploadData(){
		Map map = new HashMap<String, Object>();
		map.put("id", paper.getId());
		map.put("title", paper.getTitle());
		map.put("tags", paper.getTags());
		map.put("url", paper.getUploadurl());
		map.put("md5", paper.getMd5());
		map.put("size", paper.getSize());
		map.put("width", paper.getWidth());
		map.put("height", paper.getHeight());
		//查询出壁纸的分类
		map.put("cateids", paper_cates);
		System.out.println(paper.getId()+" "+paper_cates);
		String str = JSONObject.toJSONString(map,SerializerFeature.WriteMapNullValue);
		post(str);
		System.out.println(str);
	}
	
	//批量上传数据
	public void multiUploadData(){
		List l = new ArrayList();
		for(Paper tmpPaper:papers_List){
			Map map = new HashMap<String, Object>();
			map.put("id", tmpPaper.getId());
			map.put("title", tmpPaper.getTitle());
			map.put("tags", tmpPaper.getTags());
			map.put("url", tmpPaper.getUploadurl());
			map.put("md5", tmpPaper.getMd5());
			map.put("size", tmpPaper.getSize());
			map.put("width", tmpPaper.getWidth());
			map.put("height", tmpPaper.getHeight());
			
			//查询该壁纸的分类
			String paper_cates = paperForTypeDao.findCate(tmpPaper.getId(), tmpPaper.getSource());
			if(paper_cates == ""){
				//通过paper的categoryname去查找
				PaperType pType = typeMap.get(tmpPaper.getCategoryname());
				if(pType==null){
					pType = typeMap.get("其他");//默认为其他
				}
				paper_cates = pType.getId().toString();
			}
			map.put("cateids", paper_cates);
			l.add(map);
		}
		String str = JSONObject.toJSONString(l,SerializerFeature.WriteMapNullValue);
		System.out.println("上传数据:"+str);
		post(str);
		
	}
	
	public void post(String str){
		boolean b = false;
		int retry = 0;
		while(retry<3){
			retry++;
			try {
				String jsonStr = HttpUtils.postOut(ProUtil.getString("ThirdPaperUrl"), 100, str);
				System.out.println("返回结果："+jsonStr);
				b = true;
				JSONObject jsonObject = JSONObject.parseObject(jsonStr);
				System.out.println("code:"+jsonObject.getIntValue("code"));
				
				JSONArray errorArray = jsonObject.getJSONArray("error");
				for (int i = 0; i < errorArray.size(); i++) {
					paperDao.update("update paper set status = 9 where id = "+errorArray.getIntValue(i));
					System.out.print(errorArray.getIntValue(i));
				}
				
				JSONArray existArray = jsonObject.getJSONArray("exist");
				for (int i = 0; i < existArray.size(); i++) {
					paperDao.update("update paper set status = 10 where id = "+existArray.getIntValue(i));
					System.out.print(existArray.getIntValue(i));
				}
				
				JSONArray successArray = jsonObject.getJSONArray("success");
				for (int i = 0; i < successArray.size(); i++) {
					paperDao.update("update paper set status = 1 where id = "+successArray.getIntValue(i));
					System.out.print("success:"+successArray.getIntValue(i));
				}
				System.out.println();
				break;
			} catch (Exception e) {
				e.printStackTrace();
				//上传失败
				b = false;//更新失败标志
				
			}
		}
	}


}
