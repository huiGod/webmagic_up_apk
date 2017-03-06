package com.uq.dao;

import com.uq.model.Pkginfo;

public interface PkginfoDao extends CommonDao<Pkginfo>{
	public void save(Pkginfo pkg);
}
