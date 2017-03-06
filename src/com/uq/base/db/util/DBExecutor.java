package com.uq.base.db.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;


public class DBExecutor {

	private DataSource dataSource = null;// ConnectionUtils.getDataSource();
	private QueryRunner queryRunner = null;// new QueryRunner(dataSource);
	private static String database = null;// 数据库类型 mysql ,oracle,sqlserver

	public DBExecutor() {
	}

	/**
	 * @param ds
	 *            数据源
	 * @param database
	 *            数据库类型，mysql,oracle,sqlserver
	 */
	public DBExecutor(DataSource ds, String database) {
		this.dataSource = ds;
		this.queryRunner = new QueryRunner(ds);
		setDatabase(database);
	}

	/**
	 * 自己配置数据源
	 * 
	 * @param driverClassName
	 * @param url
	 * @param username
	 * @param password
	 * @param database
	 */
	public DBExecutor(String driverClassName, String url, String username,
			String password, String database) {
		try {
			DbUtils.loadDriver(driverClassName);
			Properties pros = new Properties();
			pros.setProperty("driverClassName", driverClassName);
			pros.setProperty("url", url);
			pros.setProperty("username", username);
			pros.setProperty("password", password);

			this.dataSource = BasicDataSourceFactory.createDataSource(pros);
			this.queryRunner = new QueryRunner(this.dataSource);
			setDatabase(database);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查某张表的数据量
	 * 
	 * @param model
	 * @return
	 */
	public long count(Class<?> model) {
		return 0;
	}

	/**
	 * 查某张表的数据量
	 * 
	 * @param model
	 * @return
	 */
	public Long count(String sql) {
		Long total = null;
		try {
			Object obj = queryRunner.query(sql, new ScalarHandler<Object>());
			if (obj.getClass() == BigDecimal.class) {
				return ((BigDecimal) obj).longValue();
			} else {
				return (Long) obj;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return total;
	}

	/**
	 * 执行insert,update,delete语句
	 * 
	 * @param sql
	 */
	public void update(String sql) {
		try {
			queryRunner.update(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 得到列对象集合
	 */
	public List<String> columns(String sql) {
		try {
			return queryRunner.query(sql, new ColumnListHandler<String>());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public static String getDatabase() {
		return DBExecutor.database;
	}

	private void setDatabase(String database) {
		DBExecutor.database = database;
	}

}
