package com.uq.dao.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.base.db.util.ConnectionUtil;
import com.uq.dao.ApkdetailDao;
import com.uq.model.Apkdetail;
import com.uq.model.Apkupload;
import com.uq.model.DBParams;
import com.uq.model.VirtualORM;


public class ApkdetailDaoImpl extends CommonDaoImpl<Apkdetail> implements ApkdetailDao{
	private QueryRunner qr = new QueryRunner(ConnectionUtil.getDataSource());
	private Logger log = LoggerFactory.getLogger(ApkdetailDaoImpl.class);
	
	//保存单个app
	@Override
	public boolean save(Apkdetail apk) {
		
		boolean falg = true;
		//插入详细信息
		String sql = "insert into app_detail_info("
			 +"thirdappid,"
			 +"appid,"
			 +"packagename,"
			 +"appname,"
			 +"versioncode,"
			 +"versionname,"
			 +"size,"
			 +"issafe,"
			 +"isoffical,"
			 +"hasadvart,"
			 +"minsdkversion,"
			 +"osversion,"
			 +"apkmd5,"
			 +"signaturemd5,"
			 +"iconurl,"
			 +"siconurl,"
			 +"apkurl,"
			 +"sapkurl,"
			 +"advertremark,"
			 +"updateversioninfo,"
			 +"remark,"
			 +"Keywords,"
			 +"remarkimages_h,"//23
			 +"remarkimages_m,"
			 +"remarkimages_l,"
			 +"image_h,"
			 +"image_m,"
			 +"image_l,"
			 +"otherapkid,"
			 +"otherpkgname,"
			 +"downloadcount,"
			 +"currentpage,"
			 +"source,"//33
			 +"categoryid,"
			 +"categoryname,"
			 +"catetag,"
			 +"softtag,"
			 +"status,"
			 +"filestatus,"
			 +"language,"
			 +"createtime,"
			 +"publishtime,"
			 +"publishername,"
			 +"averageRating,"
			 +"updatetimes,"
			 +"permission,"
			 +"isremd,"
			 +"soft,"
			 +"islist,"
			 +"uploadid,"
			 +"filecode,"
			 +"em1,"
			 +"em2,"
			 +"em3,"
			 +"em4,"
			 +"em5"
			+ ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
		 Object params[][] = new Object[1][];//第1维，插入的条数。第2维，每条需要的参数
		 for (int i = 0; i < params.length; i++) {
			 params[i] = new Object[] {
					 	apk.getThirdappid(),	
					 	apk.getAppid(),
						apk.getPackagename(),
						apk.getAppname(),
						apk.getVersioncode(),
						apk.getVersionname(),
						apk.getSize(),
						apk.getIssafe(),
						apk.getIsoffical(),
						apk.getHasadvart(),						
						apk.getMinsdkversion(),
						apk.getOsversion(),
						apk.getApkmd5(),
						apk.getSignaturemd5(),
						apk.getIconurl(),
						apk.getSiconurl(),
						apk.getApkUrl(),
						apk.getSapkurl(),
						apk.getAdvertremark(),
						apk.getUpdateversioninfo(),//更新说明
						apk.getRemark(),//详细介绍
						apk.getKeywords(),
						apk.getRemarkimages_h(),//相关图片介绍
						apk.getRemarkimages_m(),
						apk.getRemarkimages_l(),
						apk.getImage_h(),//下载图原路径
						apk.getImage_m(),
						apk.getImage_l(),
						apk.getOtherapkId(),//相关应用
						apk.getOtherpkgname(),
						apk.getDownloadcount(),//下载量
						apk.getCurrentpage(),//所在请求的页码
						apk.getSource(),//抽取来源
						apk.getCategoryid(),
						apk.getCategoryname(),
						apk.getCatetag(),
						apk.getSofttag(),
						apk.getStatus(),
						apk.getFilestatus(),
						apk.getLanguage(),
						apk.getCreatetime(),//生成时间
						apk.getPublishtime(),//更新时间						
						apk.getPublisherName(),
						apk.getAverageRating(),//评分
						apk.getUpdatetimes(),
						apk.getPermission(),
						apk.getIsremd(),
						apk.getSoft(),
						apk.getIslist(),
						apk.getUploadid(),
						apk.getFilecode(),
						apk.getEm1(),
						apk.getEm2(),
						apk.getEm3(),
						apk.getEm4(),
						apk.getEm5()
						};
		}
		try {
			qr.batch(sql, params);
		} catch (SQLException e) {
			falg = false;
			e.printStackTrace();
			log.error("app save error! {}", e);
		}
		return falg;
	}
	
	//批量保存app
	@Override
	public void save(List<Apkdetail> l) {
		
	}

	/**
	 * 
	 * @param packagename
	 * @return true 存在 
	 */	
	public boolean isExist(String packagename){
		String sql ="select count(1) from app_detail_info where packagename = ?";
		long count = count(sql,packagename);
		if(count >0)return true;
		return false;
	}
	
	/**
	 * 
	 * @param packagename
	 * @return true 存在 
	 */	
	public boolean isExist(String packagename,Integer apkid){
		String sql ="select count(1) from app_detail_info where packagename = ? and apkid = ?";
		long count = count(sql,new Object[]{packagename,apkid});
		if(count >0)return true;
		return false;
	}
	
	//直接删除
	public void delete(String packagename){
		String deleteSql = "delete from app_detail_info where packagename =?";
		try {
			qr.update(deleteSql, packagename);//删除
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//删除备份
	@Override
	public void deleteAndback(String packagename) {
		String insertSql = "insert into app_detail_info_history(thirdappid, appid, packagename, appname, versioncode, versionname, size, issafe, isoffical, hasadvart, minsdkversion, osversion, apkmd5, signaturemd5, iconurl, siconurl, apkurl, sapkurl, advertremark, updateversioninfo, remark, Keywords, remarkimages_h, remarkimages_m, remarkimages_l, Image_h, Image_m, Image_l, otherapkId, otherpkgname, downloadcount, currentpage, source, categoryid, categoryname, catetag, softtag, status, filestatus, language, createtime, publishtime, publisherName, averageRating,updatetimes,permission,isremd,soft,islist,uploadid, em1, em2, em3, em4, em5)"+
		" select thirdappid, appid, packagename, appname, versioncode, versionname, size, issafe, isoffical, hasadvart, minsdkversion, osversion, apkmd5, signaturemd5, iconurl, siconurl, apkurl, sapkurl, advertremark, updateversioninfo, remark, Keywords, remarkimages_h, remarkimages_m, remarkimages_l, Image_h, Image_m, Image_l, otherapkId, otherpkgname, downloadcount, currentpage, source, categoryid, categoryname, catetag, softtag, status, filestatus, language, now(), publishtime, publisherName, averageRating,updatetimes,permission,isremd,soft,islist,uploadid, em1, em2, em3, em4, em5 from app_detail_info where packagename =?";
		String deleteSql = "delete from app_detail_info where packagename =?";
		//String apptypeSql = "delete from app_type_info where appid in (select apkid from app_detail_info where packagename =?)";
		try {
			qr.update(insertSql, packagename);				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			qr.update(deleteSql, packagename);//最后删除
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param packagename
	 * @return apkdetail
	 */
	@Override
	public Apkdetail findApkByPkg(String packagename) {
		String sql = "select id,thirdappid,appid,packagename,appname,versioncode,versionname,size, apkmd5,iconurl, siconurl, apkurl, sapkurl,remarkimages_h, remarkimages_m, remarkimages_l, Image_h, Image_m, Image_l, otherapkId, otherpkgname, currentpage, source, categoryid, categoryname, catetag,em4,status from app_detail_info where packagename =?";
		Apkdetail apkdetail = get(sql, packagename);
		return apkdetail;
	}
	
	//根据sql查询
	@Override
	public List findApkdetails(String sql ,int page, int pagesize) {		
		Object[] param1 = new Object[]{page*pagesize,pagesize};	
		return query(sql, param1);
	}

	//更新apk
	@Override
	public boolean update(Apkdetail apk,String[] columns,String whereSql) {
		DBParams params = VirtualORM.update(apk, columns, whereSql);
		System.out.println(params.getSql());
		try {
			qr.update(params.getSql(), params.getParams());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 批量更新apkdetail的filestatus
	 */
	@Override
	public void updateFilestatus(List l,int filestatus) {
		if(l == null)return ;
		String sql = "update app_detail_info set filestatus= ? where packagename= ? ";
		Object params[][] = new Object[l.size()][];// 第1维，插入的条数。第2维，每条需要的参数
		for (int i = 0; i < l.size(); i++) {
			String  pkgname = (String)l.get(i);
			params[i] = new Object[] { 
					filestatus,
					pkgname
			};

		}
		try {
			long start = new Date().getTime();
			qr.batch(sql, params);
			long end = new Date().getTime();
			System.out.println("数据库更新1:" + (end - start) + "ms");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveApkupload(Apkupload apk) {
		DBParams params = VirtualORM.save(apk);
		try {
			qr.update(params.getSql(), params.getParams());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
