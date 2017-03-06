package com.uq.dao.impl;


import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.OtherapksDao;
import com.uq.model.DBParams;
import com.uq.model.Otherapks;
import com.uq.model.VirtualORM;


public class OtherapksDaoImpl extends CommonDaoImpl<Otherapks> implements OtherapksDao {
	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	
	@Override
	public boolean save(Otherapks otherapk) {
		DBParams params = VirtualORM.save(otherapk);
		boolean flag = true;
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
		}
		return flag ;
	}

	@Override
	public boolean isExist(String packagename, String source) {
		String sql ="select count(1) from otherapks where packagename = ? and source = ?";
		long count = count(sql,new Object[]{packagename,source});
		if(count >0)return true;
		return false;
	}

	@Override
	public String findOtherapks(String packagename, String source) {
		String sql = "select otherpkgname from otherapks where packagename = ? and source =? limit 0,1 ";
		try {
			String result = qr.query(sql, new ScalarHandler<String>(), new Object[]{packagename,source});
			if(result == null){
				sql ="select otherpkgname from otherapks where packagename = ? limit 0,1";
				result = qr.query(sql, new ScalarHandler<String>(), new Object[]{packagename});
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}



}
