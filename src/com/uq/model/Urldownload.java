package com.uq.model;

import java.util.Date;
import com.uq.base.db.anno.Table;


@Table(value = "urldownload")
public class Urldownload {
	private Integer id;
	private String uuid;
	private Integer status;
	private Integer datastatus =0;
	private Integer filestatus =0;
	private Integer sort = 0;//上传顺序，值越大越优先上传
	private Integer type = 1;//1 apk上传，2 壁纸上传，3 铃声上传
	private Integer downcount = 0;//批次下载条数
	private Integer uploadcount = 0;
	private String uploadsize ;
	private Date createtime = new Date();//下载时间
	private Date downendtime ;
	private Date uploadstarttime;
	private Date uploadendtime;
	
	public Urldownload() {
	}

	public Urldownload(String uuid, Integer status) {
		super();
		this.uuid = uuid;
		this.status = status;
	}

	public Urldownload(String uuid,Integer status,Integer sort,Integer downcount) {
		super();
		this.uuid = uuid;
		this.downcount = downcount;
		this.status = status;
		this.sort = sort;
	}
	
	public Urldownload(String uuid, Integer status,Integer sort,Integer type,Integer downcount) {
		super();
		this.uuid = uuid;
		this.status = status;
		this.sort = sort;
		this.type = type;
		this.downcount = downcount;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getDatastatus() {
		return datastatus;
	}

	public void setDatastatus(Integer datastatus) {
		this.datastatus = datastatus;
	}

	public Integer getFilestatus() {
		return filestatus;
	}

	public void setFilestatus(Integer filestatus) {
		this.filestatus = filestatus;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public Date getDownendtime() {
		return downendtime;
	}

	public void setDownendtime(Date downendtime) {
		this.downendtime = downendtime;
	}

	public Date getUploadstarttime() {
		return uploadstarttime;
	}

	public void setUploadstarttime(Date uploadstarttime) {
		this.uploadstarttime = uploadstarttime;
	}

	public Date getUploadendtime() {
		return uploadendtime;
	}

	public void setUploadendtime(Date uploadendtime) {
		this.uploadendtime = uploadendtime;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getDowncount() {
		return downcount;
	}

	public void setDowncount(Integer downcount) {
		this.downcount = downcount;
	}

	public Integer getUploadcount() {
		return uploadcount;
	}

	public void setUploadcount(Integer uploadcount) {
		this.uploadcount = uploadcount;
	}

	public String getUploadsize() {
		return uploadsize;
	}

	public void setUploadsize(String uploadsize) {
		this.uploadsize = uploadsize;
	}

}
