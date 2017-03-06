package com.uq.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.RemdInfoDao;
import com.uq.model.DBParams;
import com.uq.model.RemdInfo;
import com.uq.model.VirtualORM;


public class RemdInfoDaoImpl extends CommonDaoImpl<RemdInfo> implements RemdInfoDao{

	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	
	@Override
	public void save(RemdInfo remd) {
		DBParams params = VirtualORM.save(remd);
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查看是否是推广包
	 */
	@Override
	public boolean isremdApk(String packagename, String source) {
		String sql ="select count(1) from remdinfo where packagename = ? and source = ?";
		long count = count(sql,new Object[]{packagename,source});
		if(count >0)return true;
		return false;
	}

	@Override
	public void save(List<RemdInfo> remdList) {
		String sql = "insert into remdinfo(packagename,source,createtime,type) values(?,?,?,?)";
		Object params[][] = new Object[remdList.size()][];//第1维，插入的条数。第2维，每条需要的参数
		 for (int i = 0; i < params.length; i++) {
			 RemdInfo remd = remdList.get(i);
			 params[i] = new Object[] {
					 		remd.getPackagename(),
					 		remd.getSource(),
					 		remd.getCreatetime(),
					 		remd.getType()
						};
		}
		try {
			qr.batch(sql, params);
		} catch (SQLException e) {			
			e.printStackTrace();
		}
	}

}
