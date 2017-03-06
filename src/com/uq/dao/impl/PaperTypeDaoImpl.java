package com.uq.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.PaperTypeDao;
import com.uq.model.DBParams;
import com.uq.model.PaperType;
import com.uq.model.VirtualORM;


public class PaperTypeDaoImpl extends CommonDaoImpl<PaperType> implements PaperTypeDao{
	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	private Connection conn = ConnectionUtil.getConn();
	
	@Override
	public Integer save(PaperType type) {
		DBParams params = VirtualORM.save(type);
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

}
