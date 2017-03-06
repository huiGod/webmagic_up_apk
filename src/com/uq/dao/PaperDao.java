package com.uq.dao;

import com.uq.model.Paper;


public interface PaperDao extends CommonDao<Paper>{
	public Integer save(Paper paper);
	//根据id判断是否已经存在这个图片了
	public boolean isExist(Integer thirdid,String source);
	
}
