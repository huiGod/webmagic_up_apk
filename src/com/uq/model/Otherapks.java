package com.uq.model;

import java.util.Date;

import com.uq.base.db.anno.Table;

@Table(value="otherapks")
public class Otherapks {
	
	private String packagename;
	private String otherpkgname;
	private String source;
	private Integer status;
	private Date createtime = new Date();
	private Date updatetime = new Date();

	public Otherapks(){};
	public Otherapks(String packagename, String otherpkgname, String source,
			Integer status) {
		super();
		this.packagename = packagename;
		this.otherpkgname = otherpkgname;
		this.source = source;
		this.status = status;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	public String getOtherpkgname() {
		return otherpkgname;
	}

	public void setOtherpkgname(String otherpkgname) {
		this.otherpkgname = otherpkgname;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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
