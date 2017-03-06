package com.uq.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.CateAppDao;
import com.uq.model.CateApp;
import com.uq.model.DBParams;
import com.uq.model.VirtualORM;



public class CateAppDaoImpl extends CommonDaoImpl<CateApp> implements CateAppDao {
	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());

	@Override
	public boolean save(CateApp cateapp) {
		boolean flag = true;
		DBParams params = VirtualORM.save(cateapp);
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

	//批量保存应用分类标签
	@Override
	public boolean save(List<CateApp> list) {		
		boolean flag = true;
		String sql = "insert into cate_app("
			 +"cateid,"
			 +"catetag,"
			 +"packagename,"
			 +"source,"
			 +"createtime,"
			 +"updatetime"
			 + ") values(?,?,?,?,?,?)";
		/*String sql = "insert into cate_app("
			 +"cateid,"
			 +"catetag,"
			 +"pkgname,"
			 +"source,"
			 +"createtime,"
			 +"updatetime"
			 + ") select ?,?,?,?,?,? from dual where not exists (select 'a' from cate_app where cateid = ? and pkgname = ? and source = ? )";*/
		 Object params[][] = new Object[list.size()][];//第1维，插入的条数。第2维，每条需要的参数
		 for (int i = 0; i < list.size(); i++) {
			 CateApp cateApp = list.get(i);
			 params[i] = new Object[] {
					 	cateApp.getCateid(),
					 	cateApp.getCatetag(),
					 	cateApp.getPackagename(),
					 	cateApp.getSource(),
					 	cateApp.getCreatetime(),
					 	cateApp.getUpdatetime()
//					 	cateApp.getCateid(),
//					 	cateApp.getPackagename(),
//					 	cateApp.getSource()
						};
		}
		try {
			qr.batch(sql, params);
		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
		}
		return flag ;
	}

	@Override
	public boolean isExist(String pkg, Integer cateid) {
		String sql ="select count(1) from cate_app where cateid =? and  packagename = ?";
		long count = count(sql,new Object[]{cateid,pkg});
		if(count >0)return true;
		return false;
	}

	@Override
	public String findCate(String pkgname, String source) {
		/*String sql="SELECT GROUP_CONCAT(cateid) AS cateids FROM cate_app cate WHERE cate.pkgname= ? AND source = ?;";
		try {
			List<Object[]> cateids =qr.query(sql, new ArrayListHandler(),new Object[]{pkgname,source});
			return SUtil.converString(cateids.get(0)[0]);
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
		
		String sql ="select distinct  cateid FROM cate_app cate WHERE cate.packagename= ? AND source = ?;";
		String returncate ="";
		try {
			List<Object[]> l =qr.query(sql, new ArrayListHandler(), new Object[]{pkgname,source});
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
