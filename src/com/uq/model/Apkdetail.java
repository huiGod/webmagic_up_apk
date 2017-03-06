package com.uq.model;

import java.util.Date;

import com.uq.base.db.anno.Table;


@Table(value = "app_detail_info")
public class Apkdetail {
	private Integer id;
	// 从其他系统抽取的主键
	private Integer thirdappid;
	// 自己业务系统的主键
	private Integer appid;
	private String packagename;
	private String appname;
	private Integer versioncode;
	private String versionname;
	private long size;
	private Integer issafe;
	private Integer hasadvart;
	private Integer isoffical;
	private Integer minsdkversion;
	private String osversion;
	private String apkmd5;
	private String signaturemd5;
	private String iconurl;// i4服务器保存的路径
	private String siconurl;// 原生的下载路径
	private String apkUrl;
	private String sapkurl;
	private String advertremark;// 广告语
	private String updateversioninfo;// 更新信息
	private String remark;// 简介
	private String keywords;// 关键字
	private String remarkimages_h;// 介绍图片 map
	private String remarkimages_m;
	private String remarkimages_l;
	private String image_h;// 原始下载路径（高像素）
	private String image_m;// 一般像素
	private String image_l;// 低像素
	private String otherapkId;// 相关应用id
	private String otherpkgname;// 相关应用的包名
	private Integer downloadcount;// 下载量
	private Integer currentpage;// 应用在其他应用商店的第几页
	private String source;// 来源id
	private Integer categoryid;// 分类ID
	private String categoryname;// 分类名称
	private String catetag;// 应用分类小标签
	private String softtag;// 应用标签
	private Integer status;// 状态 1 有效，2新增，3上传,4 更新
	private Integer filestatus = 0;
	private String language;// 语言
	private Date createtime;// 创建时间
	private Date publishtime;// 版本更新时间
	private String publisherName;// 程序开发者
	private String averageRating;// 程序评分
	private Integer updatetimes = 0;//更新次数 默认为0
	private String permission;//应用权限
	private String isremd;//是否推广包
	private String uploadid;//上传批次号
	private Integer soft;//是软件还是游戏 1 软件，2游戏
	private Integer islist;//是否在分类列表采集过来的
	private String filecode;//自定义文件完整性校验值
	private String em1;// 备用字段
	private String em2;
	private String em3;
	private String em4;
	private String em5;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getThirdappid() {
		return thirdappid;
	}

	public void setThirdappid(Integer thirdappid) {
		this.thirdappid = thirdappid;
	}

	public Integer getAppid() {
		return appid;
	}

	public void setAppid(Integer appid) {
		this.appid = appid;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public Integer getVersioncode() {
		return versioncode;
	}

	public void setVersioncode(Integer versioncode) {
		this.versioncode = versioncode;
	}

	public String getVersionname() {
		return versionname;
	}

	public void setVersionname(String versionname) {
		this.versionname = versionname;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Integer getIssafe() {
		return issafe;
	}

	public void setIssafe(Integer issafe) {
		this.issafe = issafe;
	}

	public Integer getHasadvart() {
		return hasadvart;
	}

	public void setHasadvart(Integer hasadvart) {
		this.hasadvart = hasadvart;
	}

	public Integer getIsoffical() {
		return isoffical;
	}

	public void setIsoffical(Integer isoffical) {
		this.isoffical = isoffical;
	}

	public Integer getMinsdkversion() {
		return minsdkversion;
	}

	public void setMinsdkversion(Integer minsdkversion) {
		this.minsdkversion = minsdkversion;
	}

	public String getOsversion() {
		return osversion;
	}

	public void setOsversion(String osversion) {
		this.osversion = osversion;
	}

	public String getApkmd5() {
		return apkmd5;
	}

	public void setApkmd5(String apkmd5) {
		this.apkmd5 = apkmd5;
	}

	public String getSignaturemd5() {
		return signaturemd5;
	}

	public void setSignaturemd5(String signaturemd5) {
		this.signaturemd5 = signaturemd5;
	}

	public String getIconurl() {
		return iconurl;
	}

	public void setIconurl(String iconurl) {
		this.iconurl = iconurl;
	}

	public String getSiconurl() {
		return siconurl;
	}

	public void setSiconurl(String siconurl) {
		this.siconurl = siconurl;
	}

	public String getApkUrl() {
		return apkUrl;
	}

	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
	}

	public String getSapkurl() {
		return sapkurl;
	}

	public void setSapkurl(String sapkurl) {
		this.sapkurl = sapkurl;
	}

	public String getAdvertremark() {
		return advertremark;
	}

	public void setAdvertremark(String advertremark) {
		this.advertremark = advertremark;
	}

	public String getUpdateversioninfo() {
		return updateversioninfo;
	}

	public void setUpdateversioninfo(String updateversioninfo) {
		this.updateversioninfo = updateversioninfo;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getRemarkimages_h() {
		return remarkimages_h;
	}

	public void setRemarkimages_h(String remarkimagesH) {
		remarkimages_h = remarkimagesH;
	}

	public String getRemarkimages_m() {
		return remarkimages_m;
	}

	public void setRemarkimages_m(String remarkimagesM) {
		remarkimages_m = remarkimagesM;
	}

	public String getRemarkimages_l() {
		return remarkimages_l;
	}

	public void setRemarkimages_l(String remarkimagesL) {
		remarkimages_l = remarkimagesL;
	}

	public String getImage_h() {
		return image_h;
	}

	public void setImage_h(String imageH) {
		image_h = imageH;
	}

	public String getImage_m() {
		return image_m;
	}

	public void setImage_m(String imageM) {
		image_m = imageM;
	}

	public String getImage_l() {
		return image_l;
	}

	public void setImage_l(String imageL) {
		image_l = imageL;
	}

	public String getOtherapkId() {
		return otherapkId;
	}

	public void setOtherapkId(String otherapkId) {
		this.otherapkId = otherapkId;
	}

	public String getOtherpkgname() {
		return otherpkgname;
	}

	public void setOtherpkgname(String otherpkgname) {
		this.otherpkgname = otherpkgname;
	}

	public Integer getDownloadcount() {
		return downloadcount;
	}

	public void setDownloadcount(Integer downloadcount) {
		this.downloadcount = downloadcount;
	}

	public Integer getCurrentpage() {
		return currentpage;
	}

	public void setCurrentpage(Integer currentpage) {
		this.currentpage = currentpage;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Integer getCategoryid() {
		return categoryid;
	}

	public void setCategoryid(Integer categoryid) {
		this.categoryid = categoryid;
	}

	public String getCategoryname() {
		return categoryname;
	}

	public void setCategoryname(String categoryname) {
		this.categoryname = categoryname;
	}

	public String getCatetag() {
		return catetag;
	}

	public void setCatetag(String catetag) {
		this.catetag = catetag;
	}

	public String getSofttag() {
		return softtag;
	}

	public void setSofttag(String softtag) {
		this.softtag = softtag;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getFilestatus() {
		return filestatus;
	}

	public void setFilestatus(Integer filestatus) {
		this.filestatus = filestatus;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public Date getPublishtime() {
		return publishtime;
	}

	public void setPublishtime(Date publishtime) {
		this.publishtime = publishtime;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	public String getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(String averageRating) {
		this.averageRating = averageRating;
	}

	public String getEm1() {
		return em1;
	}

	public void setEm1(String em1) {
		this.em1 = em1;
	}

	public String getEm2() {
		return em2;
	}

	public void setEm2(String em2) {
		this.em2 = em2;
	}

	public String getEm3() {
		return em3;
	}

	public void setEm3(String em3) {
		this.em3 = em3;
	}

	public String getEm4() {
		return em4;
	}

	public void setEm4(String em4) {
		this.em4 = em4;
	}

	public String getEm5() {
		return em5;
	}

	public void setEm5(String em5) {
		this.em5 = em5;
	}

	
	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public Integer getUpdatetimes() {
		return updatetimes;
	}

	public void setUpdatetimes(Integer updatetimes) {
		this.updatetimes = updatetimes;
	}

	public String getIsremd() {
		return isremd;
	}

	public void setIsremd(String isremd) {
		this.isremd = isremd;
	}

	public String getUploadid() {
		return uploadid;
	}

	public void setUploadid(String uploadid) {
		this.uploadid = uploadid;
	}

	public Integer getSoft() {
		return soft;
	}

	public void setSoft(Integer soft) {
		this.soft = soft;
	}

	public Integer getIslist() {
		return islist;
	}

	public void setIslist(Integer islist) {
		this.islist = islist;
	}

	public String getFilecode() {
		return filecode;
	}

	public void setFilecode(String filecode) {
		this.filecode = filecode;
	}

}
