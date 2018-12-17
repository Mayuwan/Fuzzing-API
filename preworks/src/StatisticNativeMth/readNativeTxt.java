package StatisticNativeMth;

import java.io.*;
import java.util.*;
import java.lang.*;

public class readNativeTxt {
	public static String DSHome = System.getenv("DROIDSAFE_SRC_HOME");
	private Set<String> nativeClass;
	
	/*public readNativeTxt(){
		
	}*/
	public static String readToString(String fileName) {  
        String encoding = "UTF-8";  
        File file = new File(fileName);  
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(filecontent);  
            in.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        try {  
            return new String(filecontent, encoding);  
        } catch (UnsupportedEncodingException e) {  
            System.err.println("The OS does not support " + encoding);  
            e.printStackTrace();  
            return null;  
        }  
    }
	public static Map readFileByLine(String fileName) {
        Map map = new HashMap();
        String str = readToString(fileName);
        int m=0;
        for(String line : str.split("\n")){
        	m++;
            String[] tempStr = line.split(":");
            map.put(tempStr[0], tempStr[1]);

        }
        System.out.println("sr:"+m);
        return map;
    }
	
	public static Map readFileByLines(String fileName) {  
		Map map = new HashMap(); 
		Set<String> clzSet = new LinkedHashSet<String>();
		String str = readToString(fileName);
		
		for(String line : str.split("\n")){
			clzSet.add(line.split(": ")[0].substring(1));
		}
		
		for(String clz : clzSet){
			List<String> list = new ArrayList<>();
			for(String line : str.split("\n")){
				String[] line_split = line.split(": ");
				if (line_split[0].substring(1).equals(clz)){
					list.add(line_split[1].substring(0, line_split[1].length()-1));
				}
			}
			map.put(clz, list);
		}
	
	
		/*
		Iterator entries = map.entrySet().iterator(); 
		while (entries.hasNext()) { 
		  Map.Entry entry = (Map.Entry) entries.next(); 
		  String key = (String)entry.getKey(); 
		  String value = entry.getValue().toString(); 
		  System.out.println("Key = " + key + ", Value = " + value); 
		}*/
		/*
        File file = new File(fileName);  
        BufferedReader reader = null;  
        
       
        try {  
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;  
            int line = 1; 
            reader
            while ((tempString = reader.readLine()) != null) {
            	String[] tempArr = tempString.split(": ");
            	//System.out.println(tempArr[0].substring(1));
            	map.put(tempArr[0].substring(1), tempArr[1].substring(0,tempArr[1].length()-1));
                line++;  
            }  
            //System.out.println(map.toString());
            reader.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        } 
        */
        return map;
    }
	
	public static Set<String> readFile(String fileName) {  
		Set<String> set = new LinkedHashSet<String>();; 
        File file = new File(fileName);  
        BufferedReader reader = null;  
        try {  
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;  
            int line = 1;    
            while ((tempString = reader.readLine()) != null) {
            	set.add(tempString);
                line++;  
            }  
            reader.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        } 
        return set;
    }
	public static Map read(String filepath){
        Map map = new HashMap();
        File file = new File(filepath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                String[] tempStr = tempString.split(":");
                System.out.println(tempStr[0]);
                map.put(tempStr[0],tempStr[1]);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return map;
    }
	public static void main(String[] arg){
		readFileByLines("/home/myw/droidsafe/droidsafe-src-master/config-files/native.txt");
	}
}
