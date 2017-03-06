package com.uq.util;

public class RedisConstant {

	//redis app保存前缀
	public static final  String  ALL_APPS_PRE ="pkg:";
	//redis 分类
	public static final String ALL_Cate ="cate:";
	//360合作包
	public static final String ALL_Remd ="remd";
	public static final String ALL_UN_REMD ="uninstall";//垃圾应用，永不采集
	
	public static final String ALL_Like ="like:";
	
	//抓取360列表的apk包名
	public static final String S360List_list = "S360:list";
	//360相关列表的包名
	public static final String S360List_remd = "S360:remd";
	
	public static final String QQlist_list = "qq:list";
	public static final String QQlist_remd = "qq:remd";
	//qq 的临时app信息
	public static final String QQ_PKG = "qqpkg:";
	public static final String S360_PKG = "360pkg:";
	
	//数据的
	public static final String S360_PKG_ALL = "db360list";//存放的包名全部为小写
	public static final String QQ_PKG_ALL = "dbqqlist"; //存放的包名区分带小写
	public static final String QQ_PKG_ALL_Min = "dbqqlistmin"; //存放的包名区分带小写
	
	//自己的业务主键
	public static final String ALL_PK_APKID="pk_apkid";
	
	//保存每种类型的文件随机数，限制每个文件夹值保存5000个，以便以后好查找文件
	public static final String SAVE_FILE_COUNT = "save_file_count";
	
	//豌豆荚保存的壁纸ids
	public static final String PAPER_WDJ = "paper:wdjlist";
	public static final String PAPER_CATE_WDJ = "paper:wdjcate:";
	
	//360保存的铃声
	public static final String Ring_360 ="ring:360list";
	public static final String Ring_CATE_360 = "ring:360cate:";
	
	//爱思铃声
	public static final String Ring_i4 = "ring:i4list";
	public static final String Ring_CATE_i4 = "ring:i4cate:";
	
	//铃声多多
	public static final String Ring_duoduo = "ring:ddlist";
	public static final String Ring_CATE_duoduo = "ring:ddcate:";
	
	//酷狗铃声
	public static final String Ring_kugo = "ring:kglist";
	public static final String Ring_CATE_kugo = "ring:kgcate:";
	
	//下载文件的路径
	public static final String DOWNURL_MAP ="download_url:";
	//临时下载文件的路径
	public static final String DOWNURL_MAP_TEMP ="download_url_temp";
	
	//需要更新的app -- list
	public static final String UPDATE_APP_LIST = "update_app_list";
	//新增的app -- list
	public static final String NEW_APP_LIST = "new_app_list";
	//新增应用中 已经下载的记录
	public static final String NEW_APP_DOWNED_LIST = "new_app_down_list";
	//
	public static final String RANDOM_COUNT ="random_count";
	
	
	
	//====================后期检查
	public static final String REPEAT_APPID ="repeat_appid";
	
}
