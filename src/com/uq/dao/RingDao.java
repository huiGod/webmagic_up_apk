package com.uq.dao;

import com.uq.model.Ring;


public interface RingDao extends CommonDao<Ring>{
	public Integer save(Ring ring);
	public boolean findBymd5(String md5);
	public boolean isExist(int thirdid, String source);
}
