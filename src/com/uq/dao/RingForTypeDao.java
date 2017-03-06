package com.uq.dao;

import com.uq.model.RingForType;

public interface RingForTypeDao extends CommonDao<RingForType>{
	public String findCate(Integer ringid,String source);

	public boolean isExist(Integer ringid, Integer typeid);

	public boolean save(RingForType type);
}
