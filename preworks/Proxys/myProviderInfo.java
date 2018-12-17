package overidedSubclass;

import android.content.pm.ProviderInfo;
import android.util.Log;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class myProviderInfo extends ProviderInfo implements Cloneable {
	public myProviderInfo(){super();}
	public myProviderInfo(android.content.pm.ProviderInfo WnYwkAZ64N){super(WnYwkAZ64N);}

	private ArrayList compareFields = new ArrayList();

	public myProviderInfo clone() throws CloneNotSupportedException{
		myProviderInfo cloned = (myProviderInfo) super.clone();//浅克隆，克隆基本类型的字段
		Object cloneArrObj =null;
		Field[] fieldArr = getClass().getFields();
		try {
    		for(Field field: fieldArr){//该类所有的public字段
				field.setAccessible(true);
				Object fieldValue = field.get(this);
				Class fieldType = field.getType();
				if ( fieldType.isPrimitive()) {continue;}
				Log.v("clone myProviderInfo Field",Modifier.toString(field.getModifiers())+" "+field.getType().getName()+" "+field.getName());
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
							Log.v("该字段是不可变对象,没有子类,不可比较和克隆",fieldType.getName());
							continue;
						}
						String fieldTypeName = fieldType.getName();
						int lastDot = fieldTypeName.lastIndexOf(".");
						int firsdDot = fieldTypeName.indexOf(".");
						fieldTypeName = "overidedSubclass"+fieldTypeName.substring(firsdDot,lastDot)+".my"+fieldTypeName.substring(lastDot+1);
						Class subClass  = Class.forName(fieldTypeName);
						subClass.asSubclass(fieldType);
						//新建一个子类对象
						Object ProxyObj=null;
						ProxyObj= generateProxyClass(fieldValue,fieldType,subClass);
						field.set(cloned, ProxyObj);
						this.getClass().getField(field.getName()).set(this, generateProxyClass(fieldValue,fieldType,subClass));
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return cloned;
	}

	//根据父类对象状态信息构造子类对象
	public Object generateProxyClass(Object fieldValue, Class fieldType, Class subClass ) throws IllegalArgumentException,InstantiationException, IllegalAccessException, InvocationTargetException{
		Object ProxyObj = null;
		ArrayList originFieldClxs= new ArrayList();
		for(Field f:fieldType.getFields()){originFieldClxs.add(f.getType());}

		for(Constructor con : subClass.getConstructors()){			Class[] conParas = con.getParameterTypes();
			if(originFieldClxs.containsAll(Arrays.asList(conParas))){
				Object[] ProxyConParas = new Object[conParas.length];
				int i=0;
				for(Field f:fieldType.getFields()){
						if(i< conParas.length && f.getType() == conParas[i]){
						ProxyConParas[i] = f.get(fieldValue);
						i++;
					}
				}
				ProxyObj =  con.newInstance(ProxyConParas);
				break;
			}
		}
		return ProxyObj;
	}

	@Override public boolean equals(Object other){
        if(this == other){return true;}
        if(getClass() != other.getClass()){return false;}
        myProviderInfo otherObj = (myProviderInfo) other;

        int equalNum=0;
        try{
            for(Field field:getClass().getFields()){
                if( Modifier.isFinal(field.getModifiers())){continue;}

                Object fieldThisValue = field.get(this);
                Object fieldOtherValue = field.get(otherObj);

                compareFields.add(fieldThisValue);
                Log.v("compare myProviderInfo Field",Modifier.toString(field.getModifiers())+" "+field.getType().getName()+" "+field.getName());
                Log.v("compare myProviderInfo Field",fieldThisValue+"---"+fieldOtherValue);

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