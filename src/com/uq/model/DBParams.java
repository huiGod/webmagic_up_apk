package com.uq.model;

public class DBParams {
	/**
	 * 该类用于封装VirtualORM的方法的返回参数。
	 * 
	 * @author lw
	 */

	private String sql = "";
	private Object[] params;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}
}
