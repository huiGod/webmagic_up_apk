package com.uq.dao;

import java.util.List;

import com.uq.model.Apkdetail;
import com.uq.model.Apkupload;


public interface ApkdetailDao extends CommonDao<Apkdetail> {

	/**
	 * 保存
	 * @param apk
	 */
	public boolean save(Apkdetail apk);
	public void save(List<Apkdetail> l);
	
	public void saveApkupload(Apkupload apk);
	//是否存在
	public boolean isExist(String packagename);
	public boolean isExist(String packagename,Integer apkid);
	//根据包名去查找apk
	public Apkdetail findApkByPkg(String packagename);
	
	//直接删除
	public void delete(String packagename);
	//备份并删除
	public void deleteAndback(String packagename);
	
	public List findApkdetails(String sql ,int page, int pagesize);
	
	public boolean update(Apkdetail apk,String[] columns,String whereSql);
	
	//批量更新filestatus、
	public void updateFilestatus(List list,int filestatus);
}
