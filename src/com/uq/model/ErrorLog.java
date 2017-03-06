package com.uq.model;

import java.util.Date;

import com.uq.base.db.anno.Table;


@Table(value="errorlog")
public class ErrorLog {
	private Integer id;
	private String errorkey;
	private String pkgname;
	private String value;
	private String errortype;
	private Date createtime = new Date();
	private Date updatetime = new Date();
	private String em1;
	private String em2;

	public ErrorLog() {
	}

	public ErrorLog(String errorkey, String pkgname, String value,
			String errortype, String em1, String em2) {
		super();
		this.errorkey = errorkey;
		this.pkgname = pkgname;
		this.value = value;
		this.errortype = errortype;
		this.em1 = em1;
		this.em2 = em2;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getErrorkey() {
		return errorkey;
	}

	public void setErrorkey(String errorkey) {
		this.errorkey = errorkey;
	}

	public String getPkgname() {
		return pkgname;
	}

	public void setPkgname(String pkgname) {
		this.pkgname = pkgname;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getErrortype() {
		return errortype;
	}

	public void setErrortype(String errortype) {
		this.errortype = errortype;
	}

	public String getEm1() {
		return em1;
	}

	public void setEm1(String em1) {
		this.em1 = em1;
	}

	public String getEm2() {
		return em2;
	}

	public void setEm2(String em2) {
		this.em2 = em2;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}

}
