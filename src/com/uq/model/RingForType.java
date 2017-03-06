package com.uq.model;

import com.uq.base.db.anno.Table;



@Table(value="ringfortype")
public class RingForType {

	private Integer id;
	private Integer typeid;
	private Integer ringid;
	private Integer thirdid;
	private String source;

	public RingForType(){}
	
	
	public RingForType(Integer typeid, Integer ringid, Integer thirdid,String source) {
		super();
		this.typeid = typeid;
		this.ringid = ringid;
		this.thirdid = thirdid;
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

	public Integer getRingid() {
		return ringid;
	}

	public void setRingid(Integer ringid) {
		this.ringid = ringid;
	}

	public Integer getThirdid() {
		return thirdid;
	}

	public void setThirdid(Integer thirdid) {
		this.thirdid = thirdid;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
