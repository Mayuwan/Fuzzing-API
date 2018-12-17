package subClassTemplate;
import java.io.*;
import java.util.*;
public class Parse {
	String fileName;
	HashSet<String> allClassName = new HashSet();
	public Parse(String path){
		fileName = path;
	}
	public HashSet<String> getAllClassName(){
		return allClassName;
	}
	/**读fileName指定文件
	 * 
	 * */
	public void read(){
		try {
			BufferedReader in = new BufferedReader( new FileReader(fileName));
			String str;
			while((str = in.readLine())!=null){
				allClassName.add(str);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void save(String content,String filePath){
		PrintWriter out=null;
		try {
			out = new PrintWriter(filePath+".java");
			out.write(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(out!=null) out.close();
		}
	}
	public static void main(String[] args){
		Parse s = new Parse("/media/myw/Study/Master/Research/DroidSafe/运行日志/canUseMth");
		s.read();
		HashSet<String> allClass = s.getAllClassName();
		for(String clx: allClass) System.out.print(clx+"\n");
		System.out.println();
		System.out.println(allClass.size());
	}
}
