package com.uq.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.PaperDao;
import com.uq.model.DBParams;
import com.uq.model.Paper;
import com.uq.model.VirtualORM;



public class PaperDaoImpl extends CommonDaoImpl<Paper> implements PaperDao{

	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	private Connection conn = ConnectionUtil.getConn();
	
	//多线程会有问题
/*	@Override
	public Integer save(Paper paper) {
		Integer id = null;
		DBParams params = VirtualORM.save(paper);
		try {
			qr.update(params.getSql(), params.getParams());
			BigInteger tmpid = (BigInteger) qr.query("SELECT LAST_INSERT_ID()", new ScalarHandler(1)); 
			id = tmpid.intValue();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}*/

	
	@Override
	public Integer save(Paper paper) {
		Integer id = null;
		DBParams params = VirtualORM.save(paper);
		 // 用这种方法能得到插入自增的id 很好用    
        //long autoIncKeyFromApi = -1;   
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

	
	/**
	 * 查找原来从某个应用商店抓的图片是否存在
	 */
	@Override
	public boolean isExist(Integer thirdid, String source) {
		String sql ="select count(1) from paper where thirdid = ? and source = ?";
		long count = count(sql,new Object[]{thirdid,source});
		if(count >0)return true;
		return false;
	}

}
