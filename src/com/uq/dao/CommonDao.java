package com.uq.dao;

import java.util.List;

public interface CommonDao<T> {

	 boolean saveEntity(T tclass);
	
	/**
	 * 插入对象
	 * 
	 * @param sql
	 * @param params
	 */
	int add(String sql, Object... params);

	/**
	 * 查找多个对象
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	List<T> query(String sql, Object... params);

	/**
	 * 查找多个对象
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	List<T> findAllList(String sql, Integer page,Integer pagesize);
	
	/**
	 * 查找对象
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	T get(String sql, Object... params);

	/**
	 * 执行更新的sql语句,插入,修改,删除
	 * 
	 * @param sql
	 * @return
	 */
	boolean update(String sql);
	
	public boolean update(T bean,String[] columns,String whereSql);
	
	public boolean update(T bean,boolean NullFlag,String whereSql);
	
	public long count(String sql ,Object... params);
	

}
