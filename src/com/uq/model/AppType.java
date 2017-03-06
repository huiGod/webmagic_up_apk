package com.uq.model;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class AppType {
	@JSONField(format="yyyy-MM-dd HH:mm:ss")
	private Date createtime = new Date();
	@JSONField(format="yyyy-MM-dd HH:mm:ss")
	private Date updatetime = new Date();
	private Integer apptypeid;
	private String name;
	private String namecolor;
	private Integer sort;
	private String iconurl;
	private String iconname;
	private String remark;
	private Integer status;
	private String type;
	private Integer pid;//分类id 1 应用 2 游戏 
	private String sourceid;

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

	public Integer getApptypeid() {
		return apptypeid;
	}

	public void setApptypeid(Integer apptypeid) {
		this.apptypeid = apptypeid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamecolor() {
		return namecolor;
	}

	public void setNamecolor(String namecolor) {
		this.namecolor = namecolor;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getIconurl() {
		return iconurl;
	}

	
	public String getIconname() {
		return iconname;
	}

	public void setIconname(String iconname) {
		this.iconname = iconname;
	}

	public void setIconurl(String iconurl) {
		this.iconurl = iconurl;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getSourceid() {
		return sourceid;
	}

	public void setSourceid(String sourceid) {
		this.sourceid = sourceid;
	}

	@Override
	public String toString() {
		return "AppType [apptypeid=" + apptypeid + ", name=" + name + ", pid="
				+ pid + ", sourceid=" + sourceid + ", status=" + status
				+ ", type=" + type + "]";
	}

	

}
