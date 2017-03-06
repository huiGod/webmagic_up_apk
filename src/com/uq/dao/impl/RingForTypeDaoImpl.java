package com.uq.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.RingForTypeDao;
import com.uq.model.DBParams;
import com.uq.model.RingForType;
import com.uq.model.VirtualORM;



public class RingForTypeDaoImpl extends CommonDaoImpl<RingForType> implements RingForTypeDao{
	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	
	@Override
	public String findCate(Integer ringid, String source) {
		System.out.println("===findcate:"+ringid+" source:"+source);
//		String sql ="select distinct typeid FROM ringfortype cate WHERE cate.ringid= ? AND source = ?;";
		String sql ="select distinct typeid FROM ringfortype cate WHERE cate.ringid= ? ;";
		String returncate ="";
		try {
			List<Object[]> l =qr.query(sql, new ArrayListHandler(), new Object[]{ringid});
			String tmp = "";
			for (int i = 0; i < l.size(); i++) {
				tmp += l.get(i)[0]+",";
			}
			returncate = tmp.substring(0, tmp.length()-1);
			 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returncate;
	}

	@Override
	public boolean isExist(Integer ringid, Integer typeid) {
		String sql ="select count(1) from ringfortype where ringid = ? and typeid = ?";
		long count = count(sql,new Object[]{ringid,typeid});
		if(count >0)return true;
		return false;
	}

	@Override
	public boolean save(RingForType type) {
		boolean flag = true;
		DBParams params = VirtualORM.save(type);
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

}
