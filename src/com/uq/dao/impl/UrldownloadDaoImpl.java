package com.uq.dao.impl;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.UrldownloadDao;
import com.uq.model.DBParams;
import com.uq.model.Urldownload;
import com.uq.model.VirtualORM;


public class UrldownloadDaoImpl extends CommonDaoImpl<Urldownload> implements UrldownloadDao{
	
	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	
	@Override
	public void save(Urldownload url) {
		DBParams params = VirtualORM.save(url);
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Urldownload getByuuid(String uuid) {
		String sql ="select * from urldownload where uuid = ?";
		Urldownload url = get(sql, uuid);
		return url;
	}

	@Override
	public boolean update(Urldownload url,String[] columns,String whereSql) {
		DBParams params = VirtualORM.update(url, columns, whereSql);
		System.out.println(params.getSql());
		try {
			qr.update(params.getSql(), params.getParams());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Object[] getuploadApkInfo(String download_uuid){
		String resultsql ="SELECT COUNT(1) as uploadcount,SUM(app.size) as uploadsize FROM app_upload app WHERE  uploadid = ? AND STATUS = 7";
		Object[] rs = new Object[2];
		try {
			 rs = qr.query(resultsql, download_uuid, new ArrayHandler());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
}
