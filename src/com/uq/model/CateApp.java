package com.uq.model;

import java.util.Date;

import com.uq.base.db.anno.Table;



@Table(value = "cate_app")
public class CateApp {
	private Integer id;
	private Integer cateid;
	private String catetag;
	private String packagename;
	private String source;
	private Date createtime = new Date();
	private Date updatetime = new Date();

	public CateApp() {

	}

	public CateApp(Integer cateid, String catetag, String packagename,
			String source) {
		super();
		this.cateid = cateid;
		this.catetag = catetag;
		this.packagename = packagename;
		this.source = source;
	}

	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCateid() {
		return cateid;
	}

	public void setCateid(Integer cateid) {
		this.cateid = cateid;
	}

	public String getCatetag() {
		return catetag;
	}

	public void setCatetag(String catetag) {
		this.catetag = catetag;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
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
