package com.uq.dao;

import com.uq.model.PaperForType;


public interface PaperForTypeDao extends CommonDao<PaperForType>{
	public boolean save(PaperForType pft);
	public boolean isExist(Integer paperid,Integer typeid);
	/**
	 * 查找壁纸的多个分类，以逗号隔开
	 * @param paperid
	 * @param source
	 * @return
	 */
	public String findCate(Integer paperid,String source);
}
