package test;
import java.lang.reflect.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.io.*;
public class test {
	public static void main(String[] args){
		
		/*myEmployee employee = new myEmployee("mayuwan",1234.657,1994,10,2);
		try{
			myEmployee cloned = employee.clone();
			System.out.println(employee.intArr==cloned.intArr);//true
			System.out.println(employee.person==cloned.person);//true
		}catch(Exception e){
			
		}*/
		//Cat cat = new Cat(0);
		myCat mycat = new myCat(1,new Jump(2));
		
		//cat = mycat;//cast up
		//mycat = cat; compile error
		//System.out.println( cat.getClass());//false
		//Object fieldValue = cat.getClass().cast(mycat);//cast up
		 
		System.out.println(int.class.getSimpleName());
		
		
		try{
			Method cloneMethod = mycat.getClass().getMethod("clone", new Class[] {});
			Object clonedf= cloneMethod.invoke(mycat);
			myCat my = (myCat)clonedf;
			System.out.println(clonedf.getClass());
			System.out.println(mycat.i == my.i);
			System.out.println(mycat.equals(my));
		}catch(NoSuchMethodException e){
			System.out.println("NoSucNoSuchMethodExceptionhMethodException");
			e.printStackTrace();
		}catch(IllegalAccessException e){
			System.out.println("IllegalAccessException");
			e.printStackTrace();
		}catch(IllegalArgumentException e){
			System.out.println("IllegalArgumentException");
			e.printStackTrace();
		}catch(InvocationTargetException e){
			System.out.println("InvocationTargetException");
			e.printStackTrace();
		}
		
	}
	
	
}
class Cat{
	public int i;
	public Jump j;
	public Cat(int i,Jump j){
		this.i = i;
		this.j = j;
	}
}
class Jump{
	public int jump;
	public Jump(int jump){this.jump = jump;}
}
class myCat extends Cat implements Cloneable {
	public myCat(int i,Jump j){super(i,j);} 
	private ArrayList compareFields = new ArrayList();
	
	public myCat clone() throws CloneNotSupportedException{
		myCat cloned = (myCat) super.clone();//浅克隆，克隆基本类型的字段
		//myCat cloned = new myCat(this.i);
		Object cloneArrObj =null;
		Field[] fieldArr = getClass().getFields();
		try {
    		for(Field field: fieldArr){//从父类继承下来的public字段
				Object fieldValue = field.get(this);
				Class fieldType = field.getType();
				if ( fieldType.isPrimitive()) {continue;}
				//Log.v("clone myFather Field",Modifier.toString(field.getModifiers())+" "+field.getType().getName()+" "+field.getName());
				if(fieldType.isArray()){//数组字段
					cloneArrObj = Array.newInstance(fieldType.getComponentType(), Array.getLength(fieldValue));
					DeepClone.cloneArr(fieldValue, cloneArrObj);
					// 设置到克隆对象中
					field.set(cloned, cloneArrObj);
					}
				else{//对象字段,优先使用原始类的序列化，如果没有，使用clone方法
					if(fieldValue instanceof Serializable){
						//序列化
						ByteArrayOutputStream bout =  new ByteArrayOutputStream();
						ObjectOutputStream out2 = new ObjectOutputStream(bout);
						out2.writeObject(fieldValue);
						out2.flush();
						//反序列化
						ObjectInputStream in2 = new ObjectInputStream( new ByteArrayInputStream(bout.toByteArray()));
						//设置到克隆对象中
						field.set(cloned, in2.readObject());
						continue;
					}
					//原始类的克隆方法
					if (fieldValue instanceof Cloneable) {
						// 反射查找clone方法
						Method cloneMethod;
						try {
							cloneMethod = fieldType.getDeclaredMethod("clone",  new Class[] {});
						} catch (NoSuchMethodException e) {
						cloneMethod = fieldType.getMethod("clone", new Class[] {});
						}
						//调用克隆方法并设置到克隆对象中
						field.set(cloned, cloneMethod.invoke(fieldValue, new Object[0]));
					}
					else{//调用子类的构造方法
						if(Modifier.isFinal(fieldType.getModifiers())){
							//Log.v("该字段是不可变对象,没有子类,不可比较和克隆",fieldType.getName());
							continue;
						}
						String fieldTypeName = fieldType.getName();
						int lastDot = fieldTypeName.lastIndexOf(".");
						int firsdDot = fieldTypeName.indexOf(".");
						fieldTypeName = "test"+fieldTypeName.substring(firsdDot,lastDot)+".my"+fieldTypeName.substring(lastDot+1);
						Class subClass  = Class.forName(fieldTypeName);
						subClass.asSubclass(fieldType);
						//构造子类对象，		//每个子类有字段为父类对象
						Object ProxyObj = null;
						ArrayList originFieldClxs= new ArrayList();
						for(Field f:fieldType.getFields()){
							originFieldClxs.add(f.getType());
						}
						for(Constructor con : subClass.getConstructors()){
							Class[] paras = con.getParameterTypes();
							if(originFieldClxs.containsAll(Arrays.asList(paras))){
								Object[] ProxyConParas = new Object[paras.length];
								int i=0;
								for(Class para:paras){
									Field f = fieldType.getField(para.getName());
									ProxyConParas[i] = f.get(fieldValue);
									i++;
								}
								ProxyObj =  con.newInstance(ProxyConParas);
								break;
							}
						}
						field.set(cloned, ProxyObj);
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return cloned;
	}
	@Override public boolean equals(Object other){
	      if(this == other){return true;}
	      if(getClass() != other.getClass()){return false;}
	      myCat otherObj = (myCat) other;

	      int equalNum=0;
	      try{
	          for(Field field:getClass().getFields()){
	              if( Modifier.isFinal(field.getModifiers())){continue;}

	              Object fieldThisValue = field.get(this);
	              Object fieldOtherValue = field.get(otherObj);

	              compareFields.add(fieldThisValue);
	              //Log.v("compare myEmployee Field",Modifier.toString(field.getModifiers())+" "+field.getType().getName()+" "+field.getName());
	              //Log.v("compare myEmployee Field",fieldThisValue+"---"+fieldOtherValue);
	              if(field.getType().isArray()) {//数组字段
	                  if (DeepClone.compareArr(fieldThisValue, fieldOtherValue)) { equalNum += 1; }
	              }
	              else{//基本类型与对象类型
	                  if (Objects.equals(fieldThisValue,fieldOtherValue)) { equalNum+=1; }
	              }
	          }
	      }catch (IllegalAccessException e) {
	          e.printStackTrace();
	      }

	      if(compareFields.size() == equalNum){return true;}
	      return false;
		}

		@Override public int hashCode(){
		    if(compareFields.size() == 0){
					return super.hashCode();
			}else{
					return Objects.hash(compareFields.toArray());
			}
		}
}