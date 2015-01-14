package local.cni.utils.ziputils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 
 * 功能： 
 * 1 、实现把指定文件夹下的所有文件压缩为指定文件夹下指定 zip 文件 
 * 2 、实现把指定文件夹下的 zip 文件解压到指定目录下
 * 
 * @author simon.jia
 * 
 */
public class ZipUtils {
	/**
	 * 
	 * 方法名：Zip 描述： 日期：2012-4-1 下午2:10:13
	 * 
	 * @param sourceDir
	 *            //指定要压缩的文件夹路径
	 * @param outZipFile
	 *            //指定压缩后文件夹输出路径
	 * @return void
	 */
	public static void Zip(String sourceDir, String zipFile) {
		//创建一个输出流
		OutputStream os = null;
		try {
			//打开一个写ZIP文件的输出流
			os = new FileOutputStream(zipFile);
			//接上一个缓冲流、不用频繁读取文件
			BufferedOutputStream bos = new BufferedOutputStream(os);
			//读取ZIP文件当然要用到ZIP的文件打印流
			ZipOutputStream zos = new ZipOutputStream(bos);
			//打开要压缩的文件目录
			File file = new File(sourceDir);
			//基本路径
			String basePath = null;
			//如果有子目录就获得基本路径没有就获得上一节点路径
			if (file.isDirectory()) {
				basePath = file.getPath();
			} else {
				basePath = file.getParent();
			}
			//调用创建ZIP文件方法
			createZip(file, basePath, zos);
			//关掉当前ZIP写入流
			zos.closeEntry();
			//关掉ZIP流和过滤流
			zos.close();
			System.out.println("["+zipFile+"] zip successful! ");
		} catch (Exception e) {
			e.printStackTrace();
			writeLog("zip file "+zipFile+" Exception "+e.toString());
		}

	}

	/**
	 * 
	 * 方法名：createZip 描述： 作者： 日期：2012-4-1 下午2:14:30
	 * 
	 * @param @param source
	 * @param @param basePath
	 * @param @param zos
	 * @return void
	 */
	private static void createZip(File source, String basePath,
			ZipOutputStream zos) {

		// 创建文件对象用于装载要压缩文件
		File[] files = new File[0];

		String pathName;

		int length = 0;
		// 检测是否有子目录
		if (source.isDirectory()) {
			// 获取目录下所有文档
			files = source.listFiles();
		} else {
			files = new File[1];
			files[0] = source;
		}
		byte[] buf = new byte[1024];
		try {
			for (File file : files) {
				if (file.isDirectory()) {
					// 获得文件路径+“/”表示还有子目录
					pathName = file.getPath().substring(basePath.length() + 1)
							+ "/";
					// 创建一个Zip文件目录进行传递给输出流
					zos.putNextEntry(new ZipEntry(pathName));
					// 递归压缩到没有子目录
					createZip(file, basePath, zos);
				} else {

					pathName = file.getPath().substring(basePath.length());
					// 打开文件输入通道
					InputStream is = new FileInputStream(file);
					// 使用缓冲流对接压缩流
					BufferedInputStream bis = new BufferedInputStream(is);
					// 创建压缩目录
					zos.putNextEntry(new ZipEntry(pathName));
					// 循环写入压缩流
					while ((length = bis.read(buf)) > 0) {
						zos.write(buf, 0, length);
					}
					// 关闭文件流
					is.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * 方法名：unZip 描述：不能解压rar 日期：2012-4-1 下午5:33:19
	 * 
	 * @param @param zipFile
	 * @param @param descDir
	 * @return void
	 */
	private static void unZip(String descDir,String zipfile) {
		// 验证目录正确性
		descDir = descDir.endsWith("//") ? descDir : descDir + "//";
		byte b[] = new byte[1024];
		int length;
		ZipFile zipFile;

		try {
			// 打开ZIP压缩文件
			zipFile = new ZipFile(new File(zipfile));
			// 返回文件目录枚举
			Enumeration enumeration = zipFile.entries();
			// 创建Zip目录
			ZipEntry zipEntry = null;
			// 遍历目录枚举
			while (enumeration.hasMoreElements()) {
				// 获得压缩目录
				zipEntry = (ZipEntry) enumeration.nextElement();
				// 创建压缩目录文件
				File loadFile = new File(descDir + zipEntry.getName());

				if (zipEntry.isDirectory()) {
					loadFile.mkdirs();
				} else {

					if (!loadFile.getParentFile().exists()) {
						loadFile.getParentFile().mkdirs();
					}
					OutputStream os = new FileOutputStream(loadFile);
					InputStream zis = zipFile.getInputStream(zipEntry);
					while ((length = zis.read(b)) > 0) {
						os.write(b, 0, length);
					}
					os.close();
				}
			}
			System.out.println("["+zipfile+"] unzip successful! ");
		} catch (IOException e) {
			e.printStackTrace();
			writeLog("unzip file "+zipfile+" Exception "+e.toString());
		}

	}


	static char hexdigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String getMD5(File file) {
		FileInputStream fis = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(file);
			byte[] buffer = new byte[2048];
			int length = -1;
			long s = System.currentTimeMillis();
			while ((length = fis.read(buffer)) != -1) {
				md.update(buffer, 0, length);
			}
			byte[] b = md.digest();
			return byteToHexString(b);
			// 16位加密
			// return buf.toString().substring(8, 24);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				fis.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 把byte[]数组转换成十六进制字符串表示形式 *
	 * 
	 * @param tmp
	 *            要转换的byte[]
	 * @return 十六进制字符串表示形式
	 */
	private static String byteToHexString(byte[] tmp) {
		String s;
		// 用字节表示就是 16 个字节
		char str[] = new char[16 * 2];
		// 每个字节用 16 进制表示的话，使用两个字符，
		// 所以表示成 16 进制需要 32 个字符
		int k = 0;
		// 表示转换结果中对应的字符位置
		for (int i = 0; i < 16; i++) {
			// 从第一个字节开始，对 MD5 的每一个字节
			// 转换成 16 进制字符的转换
			byte byte0 = tmp[i];
			// 取第 i 个字节
			str[k++] = hexdigits[byte0 >>> 4 & 0xf];
			// 取字节中高 4 位的数字转换,
			// >>> 为逻辑右移，将符号位一起右移
			str[k++] = hexdigits[byte0 & 0xf];
			// 取字节中低 4 位的数字转换
		}
		s = new String(str);
		// 换后的结果转换为字符串
		return s;
	}
	
	
	 public static void writeLog(String str) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				File f = new File("C:\\ziputils\\backup.log");
				if (!f.exists()) {
					RandomAccessFile rf = new RandomAccessFile(
							"C:\\ziputils\\backup.log", "rw");
					rf.writeUTF("Datetime		Status			\r\n");
					rf
							.writeUTF("---------------------------------------------------------------------------------------------------\r\n");
					rf.writeUTF(sdf.format(new Date()) + "	" +str+  "\r\n");
					rf.close();
				} else {
					RandomAccessFile rf = new RandomAccessFile(
							"C:\\ziputils\\backup.log", "rw");
					rf.seek(rf.length());
					String res = sdf.format(new Date()) + "	" +str+  "\r\n";
					rf.write(res.getBytes());
					rf.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    
	 
	 public static void usage(){
		 	System.out.println("command ziputils usage:");
		 	System.out.println();
			System.out.println("zip file  :java -jar ziputils.jar zip \\path\\waitingZipFile.bak \\path\\zipFile.zip");
			System.out.println("zip file  :java -jar ziputils.jar zip \\waitingZipPath\\ \\path\\zipFile.zip");
			System.out.println("unzip file:java -jar ziputils.jar unzip \\waitingUnzipPath \\path\\waitingUnzipFile.zip");
			System.out.println();
			System.out.println("please try again!");
	 }
	 
	public static void main(String[] args) {

		try {

			String type = args[0].trim();
			String path = args[1].trim();
			String file = args[2].trim();

//			 String type = "zip";
//			 String path = "D:\\12.java";
//			 String file = "D:\\12.zip";
			

			// String type = "unzip";
			// String path = "D:\\ziptest\\";
			// String file = "D:\\glass_mesdb20140320.zip";

			if (type.equalsIgnoreCase("zip")) {
				System.out.println("[" + path + "] zipping...");

				writeLog(path + " zip start ");

				Zip(path, file);

				writeLog(path + " zip end ");

				System.out.println("[" + file + "] MD5 calculating...");

				String md5 = getMD5(new File(file));

				writeLog(file + " MD5= " + md5+"\n");

				System.out.println("[" + file + "] MD5 = " + md5);

			} else if (type.equalsIgnoreCase("unzip")) {
				System.out.println("[" + file + "] unzipping...");

				writeLog(file + " unzip start ");

				unZip(path, file);

				writeLog(file + " unzip end \n");
			} else {
				usage();
			}

		} catch (Exception e) {
			usage();
		}

	}
}
