package com.uq.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.PaperForTypeDao;
import com.uq.model.DBParams;
import com.uq.model.PaperForType;
import com.uq.model.VirtualORM;


public class PaperForTypeDaoImpl extends CommonDaoImpl<PaperForType> implements PaperForTypeDao{

	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	
	@Override
	public boolean save(PaperForType pft) {
		boolean flag = true;
		DBParams params = VirtualORM.save(pft);
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	public boolean isExist(Integer paperid, Integer typeid) {
		String sql ="select count(1) from paperfortype where paperid = ? and typeid = ?";
		long count = count(sql,new Object[]{paperid,typeid});
		if(count >0)return true;
		return false;
	}

	@Override
	public String findCate(Integer paperid, String source) {
		String sql ="select distinct typeid FROM paperfortype cate WHERE cate.paperid= ? AND source = ?;";
		String returncate ="";
		try {
			List<Object[]> l =qr.query(sql, new ArrayListHandler(), new Object[]{paperid,source});
			String tmp = "";
			for (int i = 0; i < l.size(); i++) {
				tmp += l.get(i)[0]+",";
			}
			returncate = tmp.substring(0, tmp.length()-1);
			 
		} catch (Exception e) {
			System.out.println("======dfsd");
			e.printStackTrace();
		}
		return returncate;
	}

}
