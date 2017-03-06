package com.uq.dao;

import com.uq.model.ErrorLog;


public interface ErrorLogDao extends CommonDao<ErrorLog>{
	public boolean save(ErrorLog log);
}
