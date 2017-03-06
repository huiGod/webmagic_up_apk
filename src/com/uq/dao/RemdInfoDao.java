package com.uq.dao;

import java.util.List;
import com.uq.model.RemdInfo;


public interface RemdInfoDao extends CommonDao<RemdInfo> {
	public void save(RemdInfo remd);
	public void save(List<RemdInfo> remdList);
	public boolean isremdApk(String packagename,String source);
}
