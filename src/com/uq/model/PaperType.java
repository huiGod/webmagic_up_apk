package com.uq.model;

import java.sql.Date;

import com.uq.base.db.anno.Table;


/**
 * 壁纸分类
 * @author Administrator
 *
 */
@Table(value = "papertype")
public class PaperType {
	private Integer id;
	private Integer thirdid;
	private String name;
	private String logo;
	private Integer count;
	private Integer sort;
	private Integer status;
	private String source;
	private Integer model;
	private Date createtime;
	private Date updatetime;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getThirdid() {
		return thirdid;
	}
	public void setThirdid(Integer thirdid) {
		this.thirdid = thirdid;
	}

	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getSort() {
		return sort;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Integer getModel() {
		return model;
	}
	public void setModel(Integer model) {
		this.model = model;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
