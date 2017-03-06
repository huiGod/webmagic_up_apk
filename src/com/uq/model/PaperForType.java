package com.uq.model;

import com.uq.base.db.anno.Table;



@Table(value="paperfortype")
public class PaperForType {
	private Integer id;
	private Integer typeid;
	private Integer paperid;
	private String source;
	
	public PaperForType(){};
	
	public PaperForType(Integer typeid, Integer paperid,String source) {
		super();
		this.typeid = typeid;
		this.paperid = paperid;
		this.source = source;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getTypeid() {
		return typeid;
	}
	public void setTypeid(Integer typeid) {
		this.typeid = typeid;
	}
	public Integer getPaperid() {
		return paperid;
	}
	public void setPaperid(Integer paperid) {
		this.paperid = paperid;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	
}
