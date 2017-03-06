package com.uq.spider.common.tool;

import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import com.chinanetcenter.api.domain.PutPolicy;
import com.chinanetcenter.api.domain.SliceUploadHttpResult;
import com.chinanetcenter.api.exception.HttpClientException;
import com.chinanetcenter.api.sliceUpload.JSONObjectRet;
import com.chinanetcenter.api.sliceUpload.PutExtra;
import com.chinanetcenter.api.util.Config;
import com.chinanetcenter.api.util.DateUtil;
import com.chinanetcenter.api.util.StringUtil;
import com.chinanetcenter.api.util.TokenUtil;
import com.chinanetcenter.api.util.WetagUtil;
import com.chinanetcenter.api.wsbox.SliceUploadResumable;
import com.uq.util.ProUtil;

/**
 * 分片上传
 * @ClassName: SliceUploadFile 
 * @Description: 分片上传，默认以4m为一片
 * @author aurong
 * @date 2015-5-13 下午08:19:22
 */
public class SliceUploadFile {
	
	private boolean flag = true;
	private  static String BucketName_Img =ProUtil.getString("BucketName_Img");
	private  static String BucketName_Apk =ProUtil.getString("BucketName_Apk");
	private  static String AK =ProUtil.getString("AK");
	private  static String SK =ProUtil.getString("SK");
	

	static{
		String isTest = ProUtil.getString("upload_test");
		if("true".equalsIgnoreCase(isTest)){
		    AK =ProUtil.getString("AK_test");
			SK =ProUtil.getString("SK_test");
			BucketName_Img =ProUtil.getString("BucketName_test");
			BucketName_Apk =ProUtil.getString("BucketName_test");
		}
		System.out.println(isTest);
		Config.init(AK, SK);
	}
	
	public static void main(String[] args) {
		SliceUploadFile sliceUploadFile = new SliceUploadFile();
		sliceUploadFile.sliceUpload(BucketName_Apk, "apk/2015/0511/tx93k/com.mojang.minecraftpe_740100501.apk", "F:/filedown/0c95d5dbeeff4fccb3653bc8f377f6ad/apk/2015/0511/tx93k/com.mojang.minecraftpe_740100501.apk");
		System.out.println(sliceUploadFile.isFlag());
	}
	public  boolean sliceUpload(final String bucketName, final String fileKey, final String filePath){
//		boolean flag = true;
		PutPolicy putPolicy = new PutPolicy();
		if (StringUtil.isEmpty(putPolicy.getScope())) {
            putPolicy.setScope(bucketName + ":" + fileKey);
        }
        if (putPolicy.getDeadline() == null) {
            putPolicy.setDeadline(String.valueOf(DateUtil.nextHours(3, new Date()).getTime()));
        }
        putPolicy.setOverwrite(1);//覆盖上传
        // 读取持久化数据，如果不做断点续传，则不需获取
//         PutExtra putExtra = getPutExtra(bucketName, fileKey);
        PutExtra putExtra = setPutExtra(bucketName, fileKey, filePath, putPolicy);
        JSONObjectRet jsonObjectRet = new JSONObjectRet() {
            /**
             * 文件上传成功后会回调此方法
             */
            @Override
            public void onSuccess(JSONObject obj) {
            	System.out.println("==="+obj);
                File fileHash = new File(filePath);
                String eTagHash = WetagUtil.getEtagHash(fileHash.getParent(), fileHash.getName());// 根据文件内容计算hash
                System.out.println("hash:"+eTagHash);
                SliceUploadHttpResult result = new SliceUploadHttpResult(obj);
                if (eTagHash.equals(result.getHash())) {
                    System.out.println("上传成功");
                } else {
                    System.out.println("hash not equal,eTagHash:" + eTagHash + " ,hash:" + result.getHash());
                }
            }

            @Override
            public void onSuccess(byte[] body) {
                System.out.println(new String(body));
            }

            // 文件上传失败回调此方法
            @Override
            public void onFailure(Exception ex) {
                if (ex instanceof HttpClientException) {
                    System.out.println(((HttpClientException) ex).code);
                }
                flag = false;
                System.out.println("上传出错，" + ex.getMessage());
            }

            // 进度条展示，每上传成功一个块回调此方法
            @Override
            public void onProcess(long current, long total) {
                System.out.printf("%s\r", current * 100 / total + " %");
            }

            /**
             * 持久化，断点续传时把进度信息保存，下次再上传时把JSONObject赋值到PutExtra 如果无需断点续传，则此方法放空
             */
            @Override
            public void onPersist(JSONObject obj) {
            /*	String key = DigestUtils.md5Hex(bucketName + ":" + fileKey);
                File configFile = new File(System.getProperty("user.home") + File.separator + bucketName + File.separator + key + "_sliceConfig.properties");

                synchronized (configFile) {
                  FileOutputStream fileOutputStream = null;
                  try {
                    if (!(configFile.getParentFile().exists()))
                      configFile.getParentFile().mkdirs();

                    if (!(configFile.exists()))
                      configFile.createNewFile();

                    fileOutputStream = new FileOutputStream(configFile);
                    fileOutputStream.write(obj.toString().getBytes());
                    fileOutputStream.flush();
                  } catch (Exception e) {
                    e.printStackTrace();
                  } finally {
                    if (fileOutputStream != null)
                      try {
                        fileOutputStream.close();
                      }
                      catch (IOException e)
                      {
                      }
                  }
                }*/
            }
        };
        SliceUploadResumable sliceUploadResumable = new SliceUploadResumable();
        sliceUploadResumable.execUpload(bucketName, fileKey, filePath, putPolicy, putExtra, jsonObjectRet);
        return this.isFlag();
	}
	
    public static PutExtra setPutExtra(String bucketName, String fileKey, String filePath, PutPolicy putPolicy) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("bucketName", bucketName);
        params.put("fileKey", fileKey);
        params.put("filePath", filePath);
        params.put("putPolicy", putPolicy.toString());
        String token = TokenUtil.getUploadToken(putPolicy);
        params.put("token", token);
        PutExtra putExtra = new PutExtra();
        putExtra.params = params;
        return putExtra;
    }

    public PutExtra getPutExtra(String bucketName, String fileName)
    {
      String key = DigestUtils.md5Hex(bucketName + ":" + fileName);
      File configFile = new File(System.getProperty("user.home") + File.separator + bucketName + File.separator + key + "_sliceConfig.properties");

      if (!(configFile.exists()))
        return null;

      int fileLen = (int)configFile.length();
      char[] chars = new char[fileLen];
      try {
        FileReader reader = new FileReader(configFile);
        reader.read(chars);
        String txt = String.valueOf(chars);
        JSONObject obj = new JSONObject(txt);
        PutExtra putExtra = new PutExtra(obj);
        reader.close();
        return putExtra; } catch (Exception e) {
      }
      return null;
    }
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
    
}
