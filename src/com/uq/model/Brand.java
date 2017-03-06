package com.uq.model;

import java.util.Date;

import com.uq.base.db.anno.Table;

/**
 * 手机品牌表
 * 
 * @author jinrong
 * 
 */
@Table("brand")
public class Brand {
	private Integer id;
	private String name;
	private String code;
	private String image;
	private Integer status;
	private Integer sortnum =0;
	private Date createtime = new Date();
	private Integer createuserid;
	private Date updatetime = new Date();
	private Integer updateuserid;

	public Brand() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getSortnum() {
		return sortnum;
	}

	public void setSortnum(Integer sortnum) {
		this.sortnum = sortnum;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public Integer getCreateuserid() {
		return createuserid;
	}

	public void setCreateuserid(Integer createuserid) {
		this.createuserid = createuserid;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}

	public Integer getUpdateuserid() {
		return updateuserid;
	}

	public void setUpdateuserid(Integer updateuserid) {
		this.updateuserid = updateuserid;
	}

}
