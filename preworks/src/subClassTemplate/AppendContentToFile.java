package subClassTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class AppendContentToFile {
	public void method1() {
		FileWriter fw = null;
		try {
		//如果文件存在，则追加内容；如果文件不存在，则创建文件
			File f=new File("E:\\dd.txt");
			fw = new FileWriter(f, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println("追加内容");
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void method2(String file, String conent) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(file, true)));
			out.write(conent+"\r\n");
		} catch (Exception e) {
		e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	public static void append(String fileName, String content) {
		try {
		// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.writeBytes(content+"\r\n");
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		AppendContentToFile a = new AppendContentToFile();
		a.method1();
		method2("/media/myw/Study/Master/Research/DroidSafe/运行日志/re.txt", "222222222222222");
		append("/media/myw/Study/Master/Research/DroidSafe/运行日志/re.txt", "33333333333");
	}
}
