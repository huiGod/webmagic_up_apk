package com.uq.dao;

import java.util.List;

import com.uq.model.CateApp;

public interface CateAppDao extends CommonDao<CateApp>{
	
	public boolean save(CateApp cateapp);
	public boolean save(List<CateApp> list);
	
	public boolean isExist(String pkg,Integer cateid);
	/**
	 * 查找该应用的所有分类
	 * @param pkgname
	 * @param source
	 * @return cateids,用逗号隔开
	 */
	public String findCate(String pkgname,String source);
}
