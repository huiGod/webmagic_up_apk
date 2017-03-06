package com.uq.dao.impl;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.UrldownloadinfoDao;
import com.uq.model.DBParams;
import com.uq.model.Urldownloadinfo;
import com.uq.model.VirtualORM;


public class UrldownloadinfoDaoImpl extends CommonDaoImpl<Urldownloadinfo> implements UrldownloadinfoDao {
	
	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());

	@Override
	public void save(Urldownloadinfo info) {
		DBParams params = VirtualORM.save(info);
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
