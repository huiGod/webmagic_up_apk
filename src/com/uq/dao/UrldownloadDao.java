package com.uq.dao;

import com.uq.model.Urldownload;


public interface UrldownloadDao extends CommonDao<Urldownload> {
	public void save(Urldownload url);
	public Urldownload getByuuid(String uuid);
	public boolean update(Urldownload url,String[] columns,String whereSql);
	public Object[] getuploadApkInfo(String download_uuid);
}
