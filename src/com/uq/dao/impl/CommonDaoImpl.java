package com.uq.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;


import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.CommonDao;
import com.uq.model.DBParams;
import com.uq.model.VirtualORM;
import com.uq.util.ReflectUtils;


public class CommonDaoImpl<T> implements CommonDao<T>{

	// 在构造函数中反射出泛型类对象  
    private Class<T> tClass;  
    QueryRunner qRunner = new QueryRunner(ConnectionUtil.getDataSource());
  
    @SuppressWarnings("unchecked")  
    public CommonDaoImpl() {  
//        tClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//    	System.out.println("=========");
    	tClass = ReflectUtils.getClassGenricType(getClass());
    	ConnectionUtil.getDataSource();
    	qRunner = new QueryRunner(ConnectionUtil.getDataSource());
    }  
    
    public boolean saveEntity(T tclass){
    	DBParams params = VirtualORM.save(tclass);
		System.out.println(params.getSql());
		try {
			qRunner.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    /** 
     * 插入对象 
     *  
     * @param sql 
     * @param params 
     */  
    public int add(String sql, Object... params) {  
        try {  
            int inserts = qRunner.update(sql, params);  
            return inserts;  
        } catch (SQLException e) {  
            e.printStackTrace();  
            return -1;  
        } 
    }  
  
    /** 
     * 查找多个对象 
     *  
     * @param sql 
     * @param params 
     * @return 
     */  
    @SuppressWarnings( { "unchecked", "deprecation" })  
    public List<T> query(String sql, Object... params) {  
        List<T> beans = null;   
        try {     
            beans = (List<T>) qRunner.query(sql, params, new BeanListHandler(tClass));
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
        return beans;  
    }  
  
    /** 
     * 查找对象 
     *  
     * @param sql 
     * @param params 
     * @return 
     */  
    @SuppressWarnings( { "unchecked", "deprecation" })  
    public T get(String sql, Object... params) {  
        T obj = null;  
        try {  
            List<T> litT = (List<T>) qRunner.query( sql, params, new BeanListHandler(tClass));
  
            if (litT != null && litT.size() > 0)  
                obj = litT.get(0);  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }   
        return obj;  
    }  
  
    /** 
     * 执行更新的sql语句,插入,修改,删除 
     *  
     * @param sql 
     * @return 
     */  
    public boolean update(String sql) {  
        boolean flag = false;  
        try {  
            int i = qRunner.update(sql);  
            if (i > 0) {  
                flag = true;  
            }  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
        return flag;  
    }

    public boolean update(T bean,String[] columns,String whereSql){
    	boolean flag = false;
    	DBParams params = VirtualORM.update(bean, columns, whereSql);
		System.out.println(params.getSql());
		try {
			int i = qRunner.update(params.getSql(), params.getParams());
			if (i > 0) {  
                flag = true;  
            }  
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;
    }
    
    public boolean update(T bean,boolean NullFlag,String whereSql){
    	boolean flag = false;
    	DBParams params = VirtualORM.update(bean, NullFlag, whereSql);
    	System.out.println(params.getSql());
		try {
			int i = qRunner.update(params.getSql(), params.getParams());
			if (i > 0) {  
                flag = true;  
            }  
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;
    }
    
	@Override	
	public long count(String sql, Object... params) {  
        Number num = 0;  
        try { 
              
            if (params == null) {  
                num = (Number) qRunner.query(sql, new ResultSetHandler<Integer>(){

					@Override
					public Integer handle(ResultSet rs) throws SQLException {
						rs.next();
						return rs.getInt(1);
					}});  
            } else {  
                num = (Number) qRunner.query(sql, new ScalarHandler(), params);  
            }  
        } catch (SQLException e) {  
            e.printStackTrace();   
        }  
        return (num != null) ? num.longValue() : -1;  
    }

	@Override
	public List<T> findAllList(String sql, Integer page, Integer pagesize) {		
		return query(sql, new Object[]{page*pagesize,pagesize});
	}

/*	@Override
	public int getPrikeyID(String seqname) {
		Number num = 0;
		String sql = "select seq(?)";
		try {
			num = (Number) qRunner.query(sql, new ScalarHandler(), seqname);
		} catch (SQLException e) {
			e.printStackTrace();
		}  
		return (num != null) ? num.intValue() : -1;
	}  */
}
