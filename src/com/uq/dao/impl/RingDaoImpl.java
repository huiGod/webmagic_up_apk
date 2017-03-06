package com.uq.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.RingDao;
import com.uq.model.DBParams;
import com.uq.model.Ring;
import com.uq.model.VirtualORM;


public class RingDaoImpl extends CommonDaoImpl<Ring> implements RingDao{
	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	private Connection conn = ConnectionUtil.getConn();
	
	@Override
	public boolean findBymd5(String md5) {
		String sql ="select count(1) from ring where md5 = ?";
		long count = count(sql,md5);
		if(count >0)return true;
		return false;
	}

	@Override
	public Integer save(Ring ring) {
		Integer id = null;
		DBParams params = VirtualORM.save(ring);
		   // 用这种方法能得到插入自增的id 很好用    
        //        long autoIncKeyFromApi = -1;   
		int autoIncKeyFromApi = -1;
		try {
			PreparedStatement pstmt = conn.prepareStatement(params.getSql(),PreparedStatement.RETURN_GENERATED_KEYS);    
			qr.fillStatement(pstmt, params.getParams());			   
			pstmt.executeUpdate();     
			ResultSet rs = pstmt.getGeneratedKeys();    
			if (rs.next()) {    
			  autoIncKeyFromApi = rs.getInt(1);    
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}    
            
        return autoIncKeyFromApi;
	}

	@Override
	public boolean isExist(int thirdid, String source) {
		String sql ="select count(1) from ring where thirdid = ? and source = ?";
		long count = count(sql,new Object[]{thirdid,source});
		if(count >0)return true;
		return false;
	}

}
