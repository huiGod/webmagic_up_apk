package com.uq.dao;

import com.uq.model.Otherapks;


public interface OtherapksDao extends CommonDao<Otherapks>{
	public boolean save(Otherapks otherapk);
	public boolean isExist(String packagename,String source);
	//查找相关的推荐apk
	public String findOtherapks(String packagename,String source);
}
