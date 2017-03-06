package com.uq.model;

import java.util.Date;

import com.uq.base.db.anno.Table;

/**
 * 记录上传文件状态失败的apk
 * 
 * @ClassName: Pkginfo
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author aurong
 * @date 2015-6-11 上午10:00:14
 */
@Table(value="pkginfo")
public class Pkginfo {
	private Integer id;
	private String packagename;
	private Integer versioncode;
	private Date createtime = new Date();
	private Integer status = 1;//上传失败 3 上传成功
	private Integer type;

	public Pkginfo() {
	}

	public Pkginfo(String packagename, Integer versioncode,Integer type) {
		super();
		this.packagename = packagename;
		this.versioncode = versioncode;
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

	public Integer getVersioncode() {
		return versioncode;
	}

	public void setVersioncode(Integer versioncode) {
		this.versioncode = versioncode;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
