package com.uq.model;

import java.util.Date;

import com.uq.base.db.anno.Table;

@Table(value="ring")
public class Ring {
	private Integer id;
	private Integer thirdid;
	private String title;
	private String tags;
	private String author;
	private Integer duration;// 播放秒数
	private String downurl;
	private String uploadurl;
	private long size;
	private String md5;
	private String source;
	private String uploadid;
	private Integer status;
	private Integer downcount;
	private String categoryname;
	private Date createtime = new Date();
	private Date updatetime = new Date();

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getDownurl() {
		return downurl;
	}

	public void setDownurl(String downurl) {
		this.downurl = downurl;
	}

	public String getUploadurl() {
		return uploadurl;
	}

	public void setUploadurl(String uploadurl) {
		this.uploadurl = uploadurl;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getUploadid() {
		return uploadid;
	}

	public void setUploadid(String uploadid) {
		this.uploadid = uploadid;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getDowncount() {
		return downcount;
	}

	public void setDowncount(Integer downcount) {
		this.downcount = downcount;
	}

	public String getCategoryname() {
		return categoryname;
	}

	public void setCategoryname(String categoryname) {
		this.categoryname = categoryname;
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

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

}
