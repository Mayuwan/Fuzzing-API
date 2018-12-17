package test;
import java.lang.reflect.*;
import java.util.*;
public class testPrivateMethod {
	
	public static void main(String[]  args){
		try{
			/**用自己编写的类测试*/
			/*Class clazz = Class.forName("test.AccessibleTest");  
			Object obj = clazz.newInstance();
			 Method mth = clazz.getDeclaredMethod("getId");//getId为private访问权限
			 mth.setAccessible(true);
			 AccessibleTest at = new AccessibleTest();  
			 at.setId(1);  
	         Object  returnObj  = mth.invoke(at);
	         System.out.println(returnObj);//1,可以运行private方法
	         */
			/*AccessibleTest at = new AccessibleTest();  
	        at.setId(1);  
	        at.setName("AT");  
	        for (Field f : clazz.getDeclaredFields()) {  
	            f.setAccessible(true);//AccessibleTest类中的成员变量为private,故必须进行此操作  
	            System.out.println(f.get(at));//获取当前对象中当前Field的value  
	        } */
			/**ues java API test private method*/
			/*Class clazz = Class.forName("java.util.ArrayList");  
			ArrayList obj =(ArrayList) clazz.newInstance();
			 Method mth = clazz.getDeclaredMethod("ensureExplicitCapacity",int.class);//getId为private访问权限
			 mth.setAccessible(true);
			Object returnObj = mth.invoke(obj,10);//可以运行private方法
			*/
			/*Class clazz = Class.forName("java.lang.ProcessEnvironment");  
			
			 Method mth = clazz.getDeclaredMethod("environ");// private static native byte[][] environ()
			 mth.setAccessible(true);
			Object returnObj = mth.invoke(null);//可以运行private static native方法
			 System.out.println(returnObj);
			 */
			/**test protected method*/
			Class clazz = Class.forName("test.TestProtected");
			Object obj = clazz.newInstance();
			 Method mth = clazz.getMethod("setId",int.class);//getName为protected访问权限
			
			 mth.setAccessible(true);
			 AccessibleTest at = new AccessibleTest();  
			 at.setId(1);  
	         Object  returnObj  = mth.invoke(at,100);
	       
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}catch(IllegalArgumentException e){
			e.printStackTrace();
		}catch(IllegalAccessException e){
			e.printStackTrace();
		}catch(SecurityException e){
			e.printStackTrace();
		}catch(InvocationTargetException e){
			e.printStackTrace();
		}catch(InstantiationException e){
			e.printStackTrace();
		}catch(NoSuchMethodException e){
			e.printStackTrace();
		}
		
        
		
		ArrayList<Integer> list = new ArrayList();
		list.add(10);
		
	}
	
}
class AccessibleTest {  
	  
    private int id;  
    private String name;  
  
    public AccessibleTest() {  
  
    }  
  
    private int getId() {  
        return id;  
    }  
  
    public void setId(int id) {  
        this.id = id;  
    }  
  
    protected String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
}  
class TestProtected extends AccessibleTest{
	public TestProtected(){super();}
}