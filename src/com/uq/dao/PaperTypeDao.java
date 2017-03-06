package com.uq.dao;

import com.uq.model.PaperType;

public interface PaperTypeDao extends CommonDao<PaperType>{
	public Integer save(PaperType type);
}
