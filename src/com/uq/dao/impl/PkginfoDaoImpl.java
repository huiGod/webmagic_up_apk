package com.uq.dao.impl;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.PkginfoDao;
import com.uq.model.DBParams;
import com.uq.model.Pkginfo;
import com.uq.model.VirtualORM;


public class PkginfoDaoImpl extends CommonDaoImpl<Pkginfo> implements PkginfoDao{

	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	
	@Override
	public void save(Pkginfo pkg) {
		DBParams params = VirtualORM.save(pkg);
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
