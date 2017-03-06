package com.uq.spider.common.ring;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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
import com.uq.dao.RingForTypeDao;
import com.uq.dao.impl.PaperDaoImpl;
import com.uq.dao.impl.RingForTypeDaoImpl;
import com.uq.model.Ring;
import com.uq.model.RingType;
import com.uq.util.FileDownloadUtil;
import com.uq.util.HttpUtils;
import com.uq.util.ProUtil;

/**
 * 上传壁纸图片，完成之后直接上传数据
 * @author jinrong
 *
 */
public class RsyncRingTask implements Callable<String>{ //implements Runnable
	
	private  static String BucketName_Ring =ProUtil.getString("BucketName_Ring");
	private  static String AK =ProUtil.getString("AK");
	private  static String SK =ProUtil.getString("SK");
	private static RingForTypeDao ringForTypeDao = new RingForTypeDaoImpl();
	private static Map<String, RingType> typeMap = new HashMap<String, RingType>();
	
	private static PaperDao paperDao = new PaperDaoImpl();
	
/*	static{
		RingTypeDao tmptypeDao = new RingTypeDaoImpl();
		String sql = "select * from ringtype  WHERE source ='i4' ";//从列表保存的分类
		List<RingType> list = tmptypeDao.query(sql, null);
		for(RingType pt:list){
			typeMap.put(pt.getName(),pt);
		}
	}*/
		
	//批量上传
	private List<Ring> ring_List;
	private List<String> multi_fileKeys;
	private String pre_path;//文件的前半部分路径
	
	public RsyncRingTask(List<Ring> ring_List,List<String> multi_fileKeys,String pre_path){
		this.ring_List = ring_List;
		this.multi_fileKeys = multi_fileKeys;
		this.pre_path = pre_path;
	}
	
	static{
		String isTest = ProUtil.getString("upload_test");
		if("true".equalsIgnoreCase(isTest)){
		    AK =ProUtil.getString("AK_test");
			SK =ProUtil.getString("SK_test");
			BucketName_Ring =ProUtil.getString("BucketName_test");
		}
		System.out.println("是否上传到测试环境:"+isTest);
		Config.init(AK, SK);
	}
	@Override
	public String call() throws Exception {
	
			
			if(multiUploadFile(multi_fileKeys, pre_path)){
				//批量上传数据
				multiUploadData();
			}else{//记录失败的
				
			}
			
//			multiUploadData();
	
		return "suc";
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
				scope.append(EncodeUtils.urlsafeEncode(BucketName_Ring+":"+filekey+":"+f.getName())).append("#");		
				
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
			httpClientResult = FileUploadCommand.uploadFileAndReturn(BucketName_Ring, fileName, localFilePath, returnBody);
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

			httpClientResult =FileManageCommand.stat(BucketName_Ring, fileKey);			
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
	
	
	//批量上传数据
	public void multiUploadData(){
		List l = new ArrayList();
		for(Ring tmpring:ring_List){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", tmpring.getId());
			map.put("title", tmpring.getTitle());
			map.put("tags", tmpring.getTags());
			map.put("url", tmpring.getUploadurl());
			String ext = FileDownloadUtil.getFileSuffix(tmpring.getUploadurl());
			ext = ext.substring(ext.lastIndexOf(".")+1, ext.length());
			map.put("ext", ext);
			map.put("size", tmpring.getSize());
			map.put("md5", tmpring.getMd5());			
			map.put("downcount", tmpring.getDowncount());
			map.put("duration", tmpring.getDuration());
			map.put("updatetime", tmpring.getUpdatetime());
			//查询该壁纸的分类
			String ring_cates = ringForTypeDao.findCate(tmpring.getId(), tmpring.getSource());
			if(ring_cates==null || ring_cates==""){
				ring_cates = "17";
			}
			map.put("cateids", ring_cates);
			l.add(map);
		}
		String str = JSONObject.toJSONString(l,SerializerFeature.WriteMapNullValue);
		System.out.println("上传数据:"+str);
		boolean uploadflg = post(str);
//		boolean uploadflg = true;
		if(!uploadflg){//上传失败
			//更新状态为12,晚点继续上传
			for(Ring ring:ring_List){
				paperDao.update("update ring set status = 12 where id = "+ring.getId());
			} 
		}
	}
	
	public boolean post(String str){
		boolean b = false;
		int retry = 0;
		while(retry<3){
			retry++;
			try {
				System.out.println(ProUtil.getString("ThirdRingUrl"));
				String jsonStr = HttpUtils.postOut(ProUtil.getString("ThirdRingUrl"), 100, str);
				System.out.println("返回结果："+jsonStr);
				b = true;
				JSONObject jsonObject = JSONObject.parseObject(jsonStr);
				System.out.println("code:"+jsonObject.getIntValue("code"));
				
				JSONArray fileerrorArray = jsonObject.getJSONArray("fileerror");
				for (int i = 0; i < fileerrorArray.size(); i++) {
					paperDao.update("update ring set status = 4 where id = "+fileerrorArray.getIntValue(i));
					System.out.print(fileerrorArray.getIntValue(i));
				}
				
				JSONArray errorArray = jsonObject.getJSONArray("error");
				for (int i = 0; i < errorArray.size(); i++) {
					paperDao.update("update ring set status = 9 where id = "+errorArray.getIntValue(i));
					System.out.print(errorArray.getIntValue(i));
				}
				
				JSONArray existArray = jsonObject.getJSONArray("exist");
				for (int i = 0; i < existArray.size(); i++) {
					paperDao.update("update ring set status = 10 where id = "+existArray.getIntValue(i));
					System.out.print(existArray.getIntValue(i));
				}
				
				JSONArray successArray = jsonObject.getJSONArray("success");
				for (int i = 0; i < successArray.size(); i++) {
					paperDao.update("update ring set status = 1 where id = "+successArray.getIntValue(i));
					System.out.print("success:"+successArray.getIntValue(i));
				}
				System.out.println();
				b = true;
				break;
			} catch (Exception e) {
				e.printStackTrace();
				//上传失败
				b = false;//更新失败标志
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}			
		}
		return b;
		
	}

	public static void main(String[] args) {
		String ext = FileDownloadUtil.getFileSuffix("/sda/sd/rr.Mp3");
		ext = ext.substring(ext.lastIndexOf(".")+1, ext.length());
		System.out.println(ext);
	}

}
