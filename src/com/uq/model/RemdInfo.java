package com.uq.model;

import java.util.Date;

import com.uq.base.db.anno.Table;


/**
 * 第三方抓取的合作包记录
 * 
 * @ClassName: RemdInfo
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author aurong
 * @date 2015-6-2 下午03:33:01
 */
@Table(value = "remdinfo")
public class RemdInfo {
	private Integer id;
	private String packagename;
	private String source;
	private Date createtime = new Date();
	private Integer type = 1;

	public RemdInfo() {

	}

	public RemdInfo(String packagename, String source,Integer type) {
		super();
		this.packagename = packagename;
		this.source = source;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
