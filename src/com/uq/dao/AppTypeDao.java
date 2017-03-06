package com.uq.dao;

import java.util.List;

import com.uq.model.AppType;

public interface AppTypeDao extends CommonDao<AppType>{

	public void save(AppType type) ;
	
	public AppType findByName(String typeName);
	
	public AppType findByapptypeid(Integer apptypeid);
	
	public List<AppType> findAllList(String sql);
	
	public Integer getMaxApptypeid(String sourceid);
	
}
