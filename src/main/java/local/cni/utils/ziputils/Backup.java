package local.cni.utils.ziputils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Backup {
	static final int BUFFER = 2048;
	
	
	/**
	 * @param args[0] waiting to compress files path
	 * @param args[1] compress to file name
	 * @param args[2] the path copy to
	 */
	public static void main(String args[]) {
		try {
			
			String sourcePath = args[0].trim();
			String zipFile = args[1].trim();
			String destPath = args[2].trim();
			
			writeLog(zipFile.substring(10)+" Backup start "); 
			
			compress(zipFile,sourcePath);
			writeLog(zipFile.substring(10)+" MD5= "+getMD5(new File(zipFile)));
			xcopy(zipFile,destPath);
			move(zipFile,sourcePath);						
			
			writeLog(zipFile.substring(10)+" Backup end \n");
		} catch (Exception e) {			
			writeLog(e.toString());
		}
	}
	

	public static void compress(String zipFile,String sourcePath) {   
        try {        	
        	BufferedInputStream origin = null;

			FileOutputStream dest = new FileOutputStream(zipFile);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];
			File f = new File(sourcePath);			
			File files[] = f.listFiles();
			
			for (int i = 0; i < files.length; i++) {
				FileInputStream fi = new FileInputStream(files[i]);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(files[i].getName());
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}				
				origin.close();
			}
			out.close();
			Thread.sleep(1000);			
            writeLog("Compress successful"); 
        } catch (Exception e) {   
        	writeLog(e.toString());  
        }   
    }  
	
	public static void xcopy(String zipFile,String destPath) {   
        try {        	
            java.lang.Process p = java.lang.Runtime.getRuntime().exec(   
            		  "xcopy "+zipFile+" "+destPath+" /y");   
            java.io.BufferedReader in = new java.io.BufferedReader(   
                    new java.io.InputStreamReader(p.getInputStream()));   
            String s;   
            String t = "copied";   
            boolean isOk = false;   
            while ((s = in.readLine()) != null) {   
                if (s.indexOf(t) != -1) {   
                	isOk = true;   
                    break;   
                }   
            }   
            writeLog("Copy " + (isOk ? "successful" : "unsuccessful")); 
        } catch (Exception e) {   
        	writeLog(e.toString());  
        }   
    }  
	
	public static void move(String zipFile,String sourcePath) {   
        try {        	
            java.lang.Process p = java.lang.Runtime.getRuntime().exec(   
            		  "cmd.exe /c move "+zipFile+" "+sourcePath);   
            java.io.BufferedReader in = new java.io.BufferedReader(   
                    new java.io.InputStreamReader(p.getInputStream()));   
            String s;   
            String t = " ";   
            boolean isOk = false;   
            while ((s = in.readLine()) != null) {   
                if (s.indexOf(t) != -1) {   
                	isOk = true;   
                    break;   
                }   
            }   
            writeLog("Move " + (isOk ? "successful" : "unsuccessful")); 
        } catch (Exception e) {   
        	writeLog(e.toString());  
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
				File f = new File("C:\\MES_BAT\\backup.log");
				if (!f.exists()) {
					RandomAccessFile rf = new RandomAccessFile(
							"C:\\MES_BAT\\backup.log", "rw");
					rf.writeUTF("Datetime		Status			\r\n");
					rf
							.writeUTF("---------------------------------------------------------------------------------------------------\r\n");
					rf.writeUTF(sdf.format(new Date()) + "	" +str+  "\r\n");
					rf.close();
				} else {
					RandomAccessFile rf = new RandomAccessFile(
							"C:\\MES_BAT\\backup.log", "rw");
					rf.seek(rf.length());
					String res = sdf.format(new Date()) + "	" +str+  "\r\n";
					rf.write(res.getBytes());
					rf.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    
}
