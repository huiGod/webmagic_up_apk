package com.uq.dao.impl;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.ErrorLogDao;
import com.uq.model.DBParams;
import com.uq.model.ErrorLog;
import com.uq.model.VirtualORM;



public class ErrorLogDaoImpl extends CommonDaoImpl<ErrorLog> implements ErrorLogDao{

	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	
	@Override
	public boolean save(ErrorLog log) {
		boolean flag = true;
		DBParams params = VirtualORM.save(log);
		System.out.println(params.getSql());
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

}
