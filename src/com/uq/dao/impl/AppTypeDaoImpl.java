package com.uq.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.AppTypeDao;
import com.uq.model.AppType;


public class AppTypeDaoImpl extends CommonDaoImpl<AppType> implements AppTypeDao {
	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	@Override
	public void save(AppType type) {
		String sql = "insert  into apptype(createtime,updatetime,apptypeid,name,namecolor,sort,iconurl,iconname,remark,status,type,pid,sourceid) values " +
				"(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object params[][] = new Object[1][];//第1维，插入的条数。第2维，每条需要的参数
		 for (int i = 0; i < params.length; i++) {
			 params[i] = new Object[]{
				type.getCreatetime(),
				type.getUpdatetime(),
				type.getApptypeid(),
				type.getName(),
				type.getNamecolor(),
				type.getSort(),
				type.getIconurl(),
				type.getIconname(),
				type.getRemark(),
				type.getStatus(),
				type.getType(),
				type.getPid(),
				type.getSourceid()
			 };
		 }
		 
		 try {
				qr.batch(sql, params);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	@Override
	public AppType findByName(String typeName) {
		String sql = "select * from apptype where name =?";
		return get(sql, typeName);
	}
	@Override
	public List<AppType> findAllList(String sql) {
		return query(sql, null);
	}
	
	@Override
	public AppType findByapptypeid(Integer apptypeid) {
		String sql ="select * from  apptype where apptypeid = ? ";
		return get(sql, apptypeid);
	}
	@Override
	public Integer getMaxApptypeid(String sourceid) {
		String sql = "select max(apptypeid) from apptype where sourceid = ? ";
		long apptypeid = count(sql, sourceid);
		return Integer.valueOf((int)apptypeid); 
	}

}
