package com.uq.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

public class ImageMagickUtil {

	public static String imageMagickPath = "D:/Program Files/ImageMagick-6.9.1-Q16";
	public static String OS = System.getProperty("os.name");

	@Test
	public void testresizeAllPng() {
		String srcPath = "E:/data/filedown/icon/2015/0526/omm8i/788d4626f09e4c0f93d40dcefc235e70.png";
		String jsonPath = "icon/2015/0526/omm8i/788d4626f09e4c0f93d40dcefc235e70.png";
		resizeAllPng(srcPath, jsonPath);
	}

	// 生成五种不同的分辨率
	public static Map resizeAllPng(String srcPath, String jsonPath) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(srcPath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int imageWidth = image.getWidth();// 原始图标的宽高
		int imageHeight = image.getHeight();

		int[] destWidth = { 256, 144, 96, 72, 48 };// 分辨率宽高一样

		String prepath = jsonPath.substring(0, jsonPath.lastIndexOf("/") + 1);
		System.out.println(prepath);

		File dir = new File(srcPath).getParentFile();
		String mulu = dir.getAbsolutePath();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		Map<String, String> imageMap = new HashMap<String, String>();
		for (int i = 0; i < destWidth.length; i++) {
			int tmpWidth = destWidth[i];
			if (imageWidth < tmpWidth || imageHeight < tmpWidth) {
				continue;
			}
			if (imageWidth == tmpWidth || imageHeight == tmpWidth) {
				imageMap.put("px" + tmpWidth, jsonPath);// 原图本身为该分辨率的不需生成压缩了
				continue;
			}
			String UUID = UuidUtil.getUUID();
			IMOperation op = new IMOperation();
			op.addImage(srcPath);
			op.resize(tmpWidth, tmpWidth);

			System.out.println(dir.getAbsolutePath());
			String newPath = mulu + "/" + UUID + ".png";
			System.out.println(newPath);
			while (true) {
				if (new File(newPath).exists()) {// 说明有重复的
					UUID = UuidUtil.getUUID();
					newPath = mulu + "/" + UUID + ".png";
				} else {
					break;
				}
			}
			System.out.println("====");
			op.addImage(newPath);
			ConvertCmd convert = new ConvertCmd();
			// linux下不要设置此值，不然会报错
			if (OS.toLowerCase().startsWith("win")) {
				convert.setSearchPath(imageMagickPath);
			}
			try {
				convert.run(op);
				if (new File(newPath).exists()) {
					System.out.println("success:" + newPath);
					compressPng(newPath);
					imageMap.put("px" + tmpWidth, prepath + UUID + ".png");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("=======" + srcPath);

			}

		}
		if(imageMap.size()==0){
			//低于最低标准了，直接设为最低的
			imageMap.put("px48", jsonPath);
			System.out.println("=====");
			}
		System.out.println(JSONObject.toJSONString(imageMap));
		return imageMap;
	}

	// 缩小png,生成单个的png
	public static boolean resizepng(int width, int height, String srcPath,
			String newPath) {
		BufferedImage image = null;
		boolean flag = false;
		try {
			image = ImageIO.read(new File(srcPath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int imageWidth = image.getWidth();// 原始图标的宽高
		int imageHeight = image.getHeight();
		System.out.println(srcPath + " " + imageWidth + "_" + imageHeight);
		if (imageWidth < width || imageHeight < height) {// 原图尺寸小于给定的值图片不允许放大
			return flag;
		}
		IMOperation op = new IMOperation();
		op.addImage(srcPath);
		op.resize(width, height);
		op.addImage(newPath);
		File dir = new File(newPath).getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		ConvertCmd convert = new ConvertCmd();
		// linux下不要设置此值，不然会报错
		if (OS.toLowerCase().startsWith("win")) {
			convert.setSearchPath(imageMagickPath);
		}
		try {
			convert.run(op);
			if (new File(newPath).exists()) {
				System.out.println("success:" + newPath);
				compressPng(newPath);
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("=======" + srcPath);

		}
		return flag;
	}

	// 压缩png
	public static void compressPng(String srcpath) {
		Process p;
		// String
		// cmd="pngquant --force --verbose --ext _aa.png --ordered --speed=1 --quality=80-90  "+srcpath;
		// String cmd
		// ="pngquant --force --verbose -o "+outpath+" --ordered --speed=1  "+srcpath;
		// //2.3版本有bug，不能覆盖原文件
		// System.out.println(System.getProperty("user.dir"));
		String cmd = "pngquant --force --ext .png --ordered --speed=1  "
				+ srcpath;
		;// 改为用2.1版本
		System.out.println(cmd);
		// System.setProperty("user.dir", "C:/test1/360/pngquant");
		try {
			// System.out.println(new File(srcpath).length());
			// 执行命令
//			 System.setProperty("user.dir", "D:/Program Files/ImageMagick-6.9.1-Q16/pngquant/pngquant.exe");
			p = Runtime.getRuntime().exec(cmd);
			System.out.println("===压缩png");
			// p.wait(500);
			// System.out.println(p.waitFor());
			// System.out.println(new File(srcpath).length());
			p.waitFor();
			// System.out.println(new File(srcpath).length());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 生成高中低的分辨率
	 * 适用于360
	 * @return
	 */
	public static Map<String, String> resizeAllJpg(List<String> imagefiles,
			String[] imagesjson, String type) {
		List<String> image_h = new ArrayList<String>();
		List<String> image_m = new ArrayList<String>();
		List<String> image_l = new ArrayList<String>();
		Map<String, String> jsonMap = new HashMap<String, String>();
		for (int i = 0; i < imagefiles.size(); i++) {

			String srcPath = imagefiles.get(i);
			String jsonStr = imagesjson[i];

			// 把png的格式转为jpg的
			String tmp = pngToJpg(srcPath, jsonStr);
			System.out.println("tmp==" + tmp);
			if (tmp != null && tmp != "") {// 说明转换成功
				System.out.println(srcPath);
				srcPath = new File(srcPath).getParentFile().getAbsolutePath()
						+ "/"
						+ tmp.substring(tmp.lastIndexOf("/") + 1, tmp.length());
				jsonStr = tmp;
			}
			System.out.println(srcPath);
			if (ConfigUtil.Image_h.equals(type)) {// 原图是高分辨率的
				image_h.add(jsonStr);
				compressJpg(srcPath);// 先压缩一下原图
				// 生成缩略图，并且是已经经过压缩的
				String tmp_image_m = resizeJpg(srcPath, 320, 533, jsonStr);// 普通分辨的图片json路径
				image_m.add(tmp_image_m);
				String tmp_image_l = resizeJpg(srcPath, 240, 400, jsonStr);
				image_l.add(tmp_image_l);
			} else if (ConfigUtil.Image_m.equals(type)) {
				image_m.add(jsonStr);
				compressJpg(srcPath);// 先压缩一下原图
				String tmp_image_l = resizeJpg(srcPath, 240, 400, jsonStr);
				image_l.add(tmp_image_l);
			} else if (ConfigUtil.Image_l.equals(type)) {
				image_l.add(jsonStr);
				compressJpg(srcPath);// 先压缩一下原图
			}
			jsonMap.put("image_h", ImageUtil.makeJson(image_h));
			jsonMap.put("image_m", ImageUtil.makeJson(image_m));
			jsonMap.put("image_l", ImageUtil.makeJson(image_l));
			System.out.println(ImageUtil.makeJson(image_h));
			System.out.println(ImageUtil.makeJson(image_m));
			System.out.println(ImageUtil.makeJson(image_l));

		}

		return jsonMap;
	}

	public static String resizeJpg(String srcPath, int width, int height,
			String jsonStr) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(srcPath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int imageWidth = image.getWidth();// 原始图标的宽高
		int imageHeight = image.getHeight();
		IMOperation op = new IMOperation();
		op.addImage(srcPath);

		String UUID = UuidUtil.getUUID();
		String prepath = jsonStr.substring(0, jsonStr.lastIndexOf("/") + 1);
		String mulu = new File(srcPath).getParentFile().getAbsolutePath();
		String newPath = mulu + "/" + UUID + ".jpg";
		String jsonTmp = prepath + UUID + ".jpg";
		while (true) {
			if (new File(newPath).exists()) {// 说明有重复的
				UUID = UuidUtil.getUUID();
				newPath = mulu + "/" + UUID + ".jpg";
				jsonTmp = prepath + UUID + ".jpg";
			} else {
				break;
			}
		}
		System.out.println(imageWidth + "---" + imageHeight);
		if (imageWidth < imageHeight) {// 不是宽屏的
			op.resize(width, height);
		} else {
			op.resize(height, width);
		}
		// op.addImage(newPath);
		File dir = new File(srcPath).getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		op.addImage(newPath);
		ConvertCmd convert = new ConvertCmd();
		// linux下不要设置此值，不然会报错
		if(OS.toLowerCase().startsWith("win")){
			convert.setSearchPath(imageMagickPath);
		}
		try {
			convert.run(op);
			if (new File(newPath).exists()) {
				System.out.println("success:" + newPath);
				return jsonTmp;
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return "";
	}

	// png 转换为jpg

	public static String pngToJpg(String srcPath, String imagejson) {
		if (!srcPath.endsWith(".png")) {
			return "";
		}
		System.out.println("srcpath:" + srcPath + " imagejson:" + imagejson);
		File f = new File(srcPath);
		String name = f.getName();// 文件名
		String pre_mulu = f.getParentFile().getAbsolutePath();
		System.out.println("pre_mulu:" + pre_mulu);
		String UUID = UuidUtil.getUUID();
		String newPath = pre_mulu + "/" + UUID + ".jpg";

		while (true) {
			if (new File(newPath).exists()) {// 说明有重复的
				UUID = UuidUtil.getUUID();
				newPath = pre_mulu + "/" + UUID + ".jpg";
			} else {
				break;
			}
		}

		String prepath = imagejson.substring(0, imagejson.lastIndexOf("/") + 1);
		System.out.println("prepath:" + prepath);

		IMOperation op = new IMOperation();
		op.addImage(srcPath);
		op.addImage(newPath);
		System.out.println("newPath:===" + newPath);
		ConvertCmd convert = new ConvertCmd();
		// linux下不要设置此值，不然会报错
		if(OS.toLowerCase().startsWith("win")){
			convert.setSearchPath(imageMagickPath);
		}
		try {
			convert.run(op);
			if (new File(newPath).exists()) {
				System.out.println("success:" + newPath);
				compressJpg(newPath);// 压缩一下新生成的jpg
				return prepath + UUID + ".jpg";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	// 压缩jpg
	public static boolean compressJpg(String srcpath) {
		Process p;
		String cmd = "jpegoptim --max=90 " + srcpath; // d:/tools/jpegoptim1.2.4/jpegoptim64.exe
														// --max=90
		if (OS.toLowerCase().startsWith("win")) {
			cmd = "D:/Program Files/ImageMagick-6.9.1-Q16/jpegoptim 1.2.4 for Windows with libjpeg 8d/jpegoptim64.exe --max=90 " + srcpath; // d:/tools/jpegoptim1.2.4/jpegoptim64.exe
																					// --max=90
		}
		System.out.println(cmd);
		boolean b = true;
		try {
			p = Runtime.getRuntime().exec(cmd);
			System.out.println("===压缩jpg");
			p.waitFor();
		} catch (Exception e) {
			b = false;
			e.printStackTrace();
		}
		return b;
	}

	@Test
	public void testpngToJpg() {
		String path = "F:/filedown/a1e145bdfa9a4f3a9d454df6ccccc7b4/image/20150402/d77ru/4768e89b9c444ccdad28d37aac1d5c59.png";
		String ss = pngToJpg(path,
				"image/20150402/d77ru/4768e89b9c444ccdad28d37aac1d5c59.png");
		System.out.println(ss);
	}

	@Test
	public void testCompressJpg() {
		String srcpath = "E:/filedown/125ccf58a13241d1b77c1eec67d626f9/icon/2016/0801/78514/00de3c631dfe4276892c9f1b775ba4e3.png";
		compressJpg(srcpath);
	}

	@Test
	public void testYasuo() {
		String srcpath = "E:/filedown/000b3a9da4aa4073817ce74cec599775.png";
		compressPng(srcpath);
	}
	
	@Test
	public void testJpgFqq(){
		List<String> imagefiles = new ArrayList<String>();
		imagefiles.add("f:/filedown/test/5ea23ac0c16b490893c2bee2ec951a9c.jpg");
//		imagefiles.add("f:/filedown/test/1159111f99894327a44f063bb6c42d2c.jpg");
		String[] imagesjson = new String[]{"test/5ea23ac0c16b490893c2bee2ec951a9c.jpg"};
		resizeAllJpgForQQ(imagefiles,imagesjson);
	}
	/**
	 * 生成高中低的分辨率
	 * 适用于qq,通过原图的大小去生成缩略图，高清的会生成中，低的，中的会生成低的
	 * @return
	 */
	public static Map<String, String> resizeAllJpgForQQ(List<String> imagefiles,
			String[] imagesjson) {
		List<String> image_h = new ArrayList<String>();
		List<String> image_m = new ArrayList<String>();
		List<String> image_l = new ArrayList<String>();
		Map<String, String> jsonMap = new HashMap<String, String>();
		for (int i = 0; i < imagefiles.size(); i++) {

			String srcPath = imagefiles.get(i);
			String jsonStr = imagesjson[i];

			// 把png的格式转为jpg的
			String tmp = pngToJpg(srcPath, jsonStr);
			System.out.println("tmp==" + tmp);
			if (tmp != null && tmp != "") {// 说明转换成功
				System.out.println(srcPath);
				srcPath = new File(srcPath).getParentFile().getAbsolutePath()+ "/"+ tmp.substring(tmp.lastIndexOf("/") + 1, tmp.length());
				jsonStr = tmp;
			}
			System.out.println(srcPath);
			//获取原图的大小
			BufferedImage image = null;
			try {
				image = ImageIO.read(new File(srcPath));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			int imageWidth = image.getWidth();// 原始图标的宽高
			int imageHeight = image.getHeight();
			System.out.println("宽："+imageWidth+" 高："+imageHeight);
			String type ="";
			if(imageWidth<imageHeight){//不是宽屏的
				if(imageWidth>480-50){
					type = ConfigUtil.Image_h;//说明原图为高清图
				}else if(imageWidth>330-20){
					type = ConfigUtil.Image_m;
				}else {
					type = ConfigUtil.Image_l;
				}
			}else {//宽屏
				if(imageWidth>800-50){
					type = ConfigUtil.Image_h;
				}else if(imageWidth>520-20){
					type = ConfigUtil.Image_m;
				}else {
					type = ConfigUtil.Image_l;
				}
			}
			if (ConfigUtil.Image_h.equals(type)) {// 原图是高分辨率的
				image_h.add(jsonStr);
				compressJpg(srcPath);// 先压缩一下原图
				// 生成缩略图，并且是已经经过压缩的
				String tmp_image_m = resizeJpg(srcPath, 320, 533, jsonStr);// 普通分辨的图片json路径
				image_m.add(tmp_image_m);
				String tmp_image_l = resizeJpg(srcPath, 240, 400, jsonStr);
				image_l.add(tmp_image_l);
			} else if (ConfigUtil.Image_m.equals(type)) {
				image_m.add(jsonStr);
				compressJpg(srcPath);// 先压缩一下原图
				String tmp_image_l = resizeJpg(srcPath, 240, 400, jsonStr);
				image_l.add(tmp_image_l);
			} else if (ConfigUtil.Image_l.equals(type)) {
				image_l.add(jsonStr);
				compressJpg(srcPath);// 先压缩一下原图
			}
			jsonMap.put("image_h", ImageUtil.makeJson(image_h));
			jsonMap.put("image_m", ImageUtil.makeJson(image_m));
			jsonMap.put("image_l", ImageUtil.makeJson(image_l));
			System.out.println("高:"+ImageUtil.makeJson(image_h));
			System.out.println("中:"+ImageUtil.makeJson(image_m));
			System.out.println("低:"+ImageUtil.makeJson(image_l));

		}

		return jsonMap;
	}
	
	/**
	 * 生成壁纸的小缩略图
	 * @param args
	 */

	public static void resizePaper(String srcPath,String savepath,int minwidth){
		
		BufferedImage image = null;
		boolean flag = false;
		try {
			image = ImageIO.read(new File(srcPath));
		} catch (IOException e) {			
			e.printStackTrace();
		}
		int imageWidth = image.getWidth();//原始图标的宽高
		int imageHeight  = image.getHeight();
		System.out.println("宽："+imageWidth+" 高："+imageHeight);
		IMOperation op = new IMOperation();
		op.addImage(srcPath);
		if(imageWidth<imageHeight){//不是宽屏的
			op.resize(minwidth, null);
		}else {
			op.resize(null, minwidth);
		}						
		op.addImage(savepath);
		File dir = new File(savepath).getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		ConvertCmd convert = new ConvertCmd();
		// linux下不要设置此值，不然会报错
		System.out.println(OS);
		if(OS.toLowerCase().startsWith("win")){
			System.out.println(imageMagickPath);
			convert.setSearchPath(imageMagickPath);
		}
		try {
			convert.run(op);
			if(new File(savepath).exists()){
				System.out.println("success:"+savepath);
				compressJpg(savepath);				
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("======="+srcPath);			
		}
	}
	
	public static int[] getImageInfo(String imagePath){
		BufferedImage image = null;
		boolean flag = false;
		int[] info = new int[2];
		try {
			image = ImageIO.read(new File(imagePath));
			int imageWidth = image.getWidth();//原始图标的宽高
			int imageHeight  = image.getHeight();
			info[0] = imageWidth;
			info[1] = imageHeight;
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		return info;
	}
	public static void main(String[] args) {
//		String srcpath = "/data/filedown/test/73d78e783ecf4c608a7cb21b40aa833e.png";
//		System.out.println("png to jpg");
//		String name = pngToJpg(srcpath,
//				"test/73d78e783ecf4c608a7cb21b40aa833e.png");
//		compressJpg("/data/filedown/" + name);
//		resizeJpg("C:/test/1404279971053.jpg", 256, 256, "sdas.jpg");
		
		resizePaper("F:/filedown/6e396073ac8b460cba97d98785fe2892/2015/0722/5264o/20150722194813.jpg","F:/filedown/6e396073ac8b460cba97d98785fe2892/min/2015/0722/5264o/20150722194813.jpg",256);
	}
}
