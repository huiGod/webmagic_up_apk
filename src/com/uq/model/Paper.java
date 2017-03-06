package com.uq.model;

import java.util.Date;

import com.uq.base.db.anno.Table;


/*
 * 壁纸
 */
@Table(value = "paper")
public class Paper {
	private Integer id;
	private Integer thirdid;
	private String title;
	private String tags;
	private String thumbnailurl;
	private String downurl;
	private String uploadurl;
	private Integer status;
	private Integer width;
	private Integer height;
	private Long size;
	private Integer downcount;
	private String source;
	private String priovidersource;
	private String md5;
	private Date createtime = new Date();
	private Date updatetime = new Date();
	private String phash;
	private String uploadid;
	private String categoryname;

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

	public String getThumbnailurl() {
		return thumbnailurl;
	}

	public void setThumbnailurl(String thumbnailurl) {
		this.thumbnailurl = thumbnailurl;
	}

	public String getDownurl() {
		return downurl;
	}

	public void setDownurl(String downurl) {
		this.downurl = downurl;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public Integer getDowncount() {
		return downcount;
	}

	public void setDowncount(Integer downcount) {
		this.downcount = downcount;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getPriovidersource() {
		return priovidersource;
	}

	public void setPriovidersource(String priovidersource) {
		this.priovidersource = priovidersource;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getPhash() {
		return phash;
	}

	public void setPhash(String phash) {
		this.phash = phash;
	}

	public String getUploadurl() {
		return uploadurl;
	}

	public void setUploadurl(String uploadurl) {
		this.uploadurl = uploadurl;
	}

	public String getUploadid() {
		return uploadid;
	}

	public void setUploadid(String uploadid) {
		this.uploadid = uploadid;
	}

	public String getCategoryname() {
		return categoryname;
	}

	public void setCategoryname(String categoryname) {
		this.categoryname = categoryname;
	}

}
