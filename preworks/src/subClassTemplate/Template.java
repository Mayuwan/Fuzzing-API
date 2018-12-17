package subClassTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

//import android.util.Log;
//import android.util.Log;
//import example.DeepClone;
//import example.myEmployee;
import soot.*;
import test.myEmployee;

/**
 * Created by myw on 18-7-26.
 */

public class Template {
    public static final String tabSpace = "\t";
    public static final char semicolon = ';';
    public static final char space  = ' ';
    public static final String lineBreak = "\n";

    private String packageName;
    private String superClassName;
    private SootClass myClass;
    private String className;
    /**父类的可访问的字段，用于深度克隆, 包括数组及对象*/
    //private HashMap<String,String> superClassAccessableObjField =  new HashMap<>();//字段名,字段类型
    //private ArrayList<String> superClassAccessableObjField = new ArrayList();
    
    private boolean accessibleConstructor = false;
    private boolean isCloneable = false;
    private boolean hasEqualsMethod = false;
    private boolean hasHashcodeMethod = false;
    
    String fieldName;
    /**
     * @param superClx
     * example:android.text.AndroidCharacter
     * */
    public Template(String packageName, SootClass superClx){
        this.packageName = packageName;
        this.superClassName = superClx.getName();
        this.isCloneable = superClx.implementsInterface("java.lang.Cloneable");
        String OnlySuperClx = superClassName.substring(superClassName.lastIndexOf(".")+1);
        className = "my" + OnlySuperClx;
        hasEqualsMethod = superClx.declaresMethodByName("equals");
        hasHashcodeMethod = superClx.declaresMethodByName("hashCode");
        myClass = superClx;
        
        //类有可访问的构造方法，则可写文件
        for (SootMethod mth : myClass.getMethods()){
        	String sig = mth.getSignature(myClass, mth.getName(), mth.getParameterTypes(), mth.getReturnType());
            //构造函数方法签名有<init>
        	if(sig.contains("<init>")){ 
        		int mod = mth.getModifiers();
        		if(Modifier.isPublic(mod) || Modifier.isProtected(mod)) accessibleConstructor = true;
        	}
        }
    }
    public boolean hasAccessibleConstructor(){
    	return accessibleConstructor;
    }
   
    /**
     * SootClass.getFields()得到的是不包括父类型在内的字段,包括Private，...
     * 打印输出 对于SootClass.getFields()中非final，public,protected字段，
     * */
    /*public StringBuilder printFields(){
        StringBuilder str = new StringBuilder();
        for(SootField fld : myClass.getFields()){
            int modifiers = fld.getModifiers();
            if(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)){
                if(!(Modifier.isFinal(fld.getModifiers()))){
                	Type FieldType = fld.getType();
                    String fldTypeName = FieldType.toString();
                    //System.out.println(fld.getType());
                    String mod =  Modifier.toString(modifiers);
                    str.append(tabSpace + mod);
                    if(mod.length() > 0){ str.append(space);}
                    str.append(fldTypeName + space + fld.getName());
                    str.append(semicolon+lineBreak);
                    //引用类型的成员变量记录下来，并生成它的子类（递归方式）
                    if(!isPrimitive(fldTypeName)){
                    	if(fldTypeName.contains("[")){//如果字段是对象数组，得到数组的元素类型
                    		fldTypeName = removeBracket(fldTypeName);
                    	}
                        System.out.printf("field Reference Class:%s\n",fldTypeName);
                        SootClass cla = Scene.v().getSootClass(fldTypeName);
                        Template childRefClass = new Template(packageName,cla);
                        if(childRefClass.isFinal()){
    						System.out.printf("%s是final类，不可写子类\n",fldTypeName);
    						//AppendContentToFile.append("/media/myw/Study/Master/Research/DroidSafe/script/immutableType.txt",className);
    						continue;
    					}
                        if(childRefClass.hasAccessibleConstructor()){
    						Parse.save(childRefClass.toString(), System.getProperty("user.dir")+/Proxys/"+childRefClass.getClassName());
    						System.out.printf("%s已写模板\n",childRefClass.getClassName());
    					}
    					else {
    						System.out.printf("%s没有可访问的构造方法\n",childRefClass.getClassName());
    					}
                       
                    }
                }
           }
        }
        return str;
    }*/
    /**
     * SootClass.getFields()得到的是不包括父类在内的所有字段,包括Private，...，但得不到父类的字段
     * 设置 对于SootClass.getFields()中非final,非static，public,protected字段
     * */
    public void fieldIsRefTemplate(){
        for(SootField fld : myClass.getFields()){
            int modifiers = fld.getModifiers();
            if(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)){
                if(! ( Modifier.isFinal(fld.getModifiers()) || Modifier.isStatic(fld.getModifiers())) ){
                    String fldTypeName = fld.getType().toString();
                    String mod =  Modifier.toString(modifiers);
                    
                    //引用类型的成员变量记录下来，并生成它的子类（递归方式）
                    if(!isPrimitive(fldTypeName)){
                    	//superClassAccessableObjField.put(fld.getName(),fldTypeName);//包括数组及对象
                    	if(fldTypeName.contains("[")){//如果字段是对象数组，得到数组的元素类型
                    		System.out.printf("数组类型字段:%s\n",fldTypeName);
                    		String componentName = removeBracket(fldTypeName);
                    		if(!isPrimitive(componentName)){
                    			fldTypeName = componentName;
                    		}
                    	}
                    	
                        System.out.printf("field Reference Class:%s\n",fldTypeName);
                        SootClass fieldRefSootClx = Scene.v().getSootClass(fldTypeName);
                        
                        if(Modifier.isInterface(fieldRefSootClx.getModifiers())){
        					System.out.printf("%s是接口，必须实现该接口，不能重写\n",fldTypeName);
        					continue;
        				}
        				if(Modifier.isAbstract(fieldRefSootClx.getModifiers())){
        					System.out.printf("%s是Abstract类，必须重写方法，不能重写\n",fldTypeName);
        					continue;
        				}
        				int i=0;
        				 for(SootField f : fieldRefSootClx.getFields()){
        					 if(Modifier.isFinal( f.getModifiers())){i++;}
        				 }
        				 if(Modifier.isFinal( fieldRefSootClx.getModifiers()) && (i != 0 && i == fieldRefSootClx.getFields().size())){
        					 System.out.printf("%s是immutable类\n",fldTypeName);
        					AppendContentToFile.append("/media/myw/Study/Master/Research/DroidSafe/script/newImmutableType.txt",className);
        					continue;
        				}
        				 if(Modifier.isFinal( fieldRefSootClx.getModifiers())){
        						System.out.printf("%s是final类，不可写子类\n",fldTypeName);
        						continue;
        				}
        				 
        				Template childRefClass = new Template(packageName,fieldRefSootClx);
        				System.out.printf("reference field original class:%s\n",fieldRefSootClx.getName());
        				if(childRefClass.hasAccessibleConstructor()){
        					Parse.save(childRefClass.toString(),System.getProperty("user.dir")+"/Proxys/"+childRefClass.getClassName());
        					System.out.printf("%s已写模板\n",fldTypeName);
        				}
        				else{
        					System.out.printf("%s没有可访问的构造方法\n",fldTypeName);
        				}
                    }
                }
           }
        }
      
    }
    
    public boolean isPrimitive(String str){
    	if(str.equals("int")){return true;}
    	else if(str.equals("byte")){return true;}
    	else if(str.equals("short")){return true;}
    	else if(str.equals("long")){return true;}
    	else if(str.equals("char")){return true;}
    	else if(str.equals("boolean")){return true;}
    	else if(str.equals("float")){return true;}
    	else if(str.equals("double")){return true;}
    	else{return false;}
    }
    
    //去除[]
    public String removeBracket(String str){
    	return str.substring(0,str.indexOf("["));
    }
    public String getClassName(){return className;}
    
    /**例子：public myOutputStreamWriter(OutputStream timezone,String str){ super(timezone,str);}
     * <java.io.OutputStreamWriter: void <init>(java.io.OutputStream,java.lang.String)>
     * */
    public StringBuilder printConstructor(){
        StringBuilder str = new StringBuilder();
        for (SootMethod mth : myClass.getMethods()){
        	String sig = mth.getSignature(myClass, mth.getName(), mth.getParameterTypes(), mth.getReturnType());
            //构造函数方法签名包含<init>
        	if(sig.contains("<init>")){
            	int cons = mth.getModifiers();
                if(Modifier.isPublic(cons) || Modifier.isProtected(cons)) {
                    str.append(tabSpace);
                    str.append(Modifier.toString(cons));
                    if (Modifier.toString(cons).length() > 0) {
                        str.append(space);
                    }
                    str.append(className + "(");
                    //print parameter types
                    List<Type> paratypes = mth.getParameterTypes();
                    ArrayList<String> values = new ArrayList<>();
                    for (int i = 0; i < paratypes.size(); i++) {
                        if (i > 0) str.append(", ");
                        String name = getRandomString(10);
                        values.add(name);
                        String types = paratypes.get(i).toString();
                        str.append(types + space + name);///name为自命名的变量名
                    }
                    str.append("){super(");
                    StringBuilder superPara = new StringBuilder();
                    int i=0;
                    for (String val : values) {
                        superPara.append(val);
                        if(i<values.size()-1) superPara.append(", ");
                        i++;
                    }
                    str.append(superPara);
                    str.append(");}" + lineBreak);
                }
            }
        }
        return str;
    }
    /**父类如果有实现克隆方法，则使用父类的克隆方法，不用再重写该克隆方法
     * 
     * 克隆从父类继承下来的字段，只有public以及protected字段可以克隆，但Class.getFields只返回包括父类子读啊在内的public字段，
     * 所以只能克隆父类的非final的public字段
     * 
     * 克隆方法返回值为所克隆的类对象，比如，Time.clone == Time
     * 如果没有引用字段则为浅克隆（object clone）:super.clone，浅克隆克隆基本类型的值
     * 如果有则为深度克隆.
     * 例子：
     * myTime cloned = (myTime) super.clone();
     * cloned.hireDay = (Date) hireDay.clone();
     * cloneArr()
     * 
     * cloned.
     * return cloned;
     * */
    public StringBuilder printCloneMth(){
        StringBuilder str = new StringBuilder();
        str.append(tabSpace+"public "+ className+space+"clone() throws CloneNotSupportedException{"+lineBreak);
        str.append(tabSpace+tabSpace+className+" cloned = ("+className+") super.clone();//浅克隆，克隆基本类型的字段\n"
        				+ "		Object cloneArrObj =null;\n"
        				+ "		Field[] fieldArr = getClass().getFields();\n"
        				+ "		try {\n"
        				+ "    		for(Field field: fieldArr){//该类所有的public字段\n"
        				+"				field.setAccessible(true);\n"
        				+ "				Object fieldValue = field.get(this);\n"
        				+ "				Class fieldType = field.getType();\n"
        				+ "				if ( fieldType.isPrimitive()) {continue;}\n"
        				+ "				Log.v(\"clone "+className+" Field\",Modifier.toString(field.getModifiers())+\" \"+field.getType().getName()+\" \"+field.getName());\n"
        				+ "				if(fieldType.isArray()){//数组字段\n"
        				+ "					cloneArrObj = Array.newInstance(fieldType.getComponentType(), Array.getLength(fieldValue));\n"
        				+ "					DeepClone.cloneArr(fieldValue, cloneArrObj);\n"
        				+ "					// 设置到克隆对象中\n"
        				+ "					field.set(cloned, cloneArrObj);\n"
        				+ "					}\n"
        				+ "				else{//对象字段,优先使用原始类的序列化，如果没有，使用clone方法\n"
        				+ "					if(fieldValue instanceof Serializable){\n"
        				+ "						//序列化\n"
        				+ "						ByteArrayOutputStream bout =  new ByteArrayOutputStream();\n"
        				+ "						ObjectOutputStream out2 = new ObjectOutputStream(bout);\n"
        				+ "						out2.writeObject(fieldValue);\n"
        				+ "						out2.flush();\n"
        				+ "						//反序列化\n"
        				+ "						ObjectInputStream in2 = new ObjectInputStream( new ByteArrayInputStream(bout.toByteArray()));\n"
        				+ "						//设置到克隆对象中\n"
        				+ "						field.set(cloned, in2.readObject());\n"
        				+ "						continue;\n"
        				+ "					}\n"
        				+ "					//原始类的克隆方法\n"
        				+ "					if (fieldValue instanceof Cloneable) {\n"
        				+ "						// 反射查找clone方法\n"
        				+ "						Method cloneMethod;\n"
        				+ "						try {\n"
        				+ "							cloneMethod = fieldType.getDeclaredMethod(\"clone\",  new Class[] {});\n"
						+ "						} catch (NoSuchMethodException e) {\n"
						+ "						cloneMethod = fieldType.getMethod(\"clone\", new Class[] {});\n"
						+ "						}\n"
						+ "						//调用克隆方法并设置到克隆对象中\n"
						+ "						field.set(cloned, cloneMethod.invoke(fieldValue, new Object[0]));\n"
						+ "					}\n"
						+ "					else{//调用子类的构造方法\n"
						+ "						if(Modifier.isFinal(fieldType.getModifiers())){\n"
						+ "							Log.v(\"该字段是不可变对象,没有子类,不可比较和克隆\",fieldType.getName());\n"
						+ "							continue;\n"
						+ "						}\n"
						+ "						String fieldTypeName = fieldType.getName();\n"
						+ "						int lastDot = fieldTypeName.lastIndexOf(\".\");\n"
						+ "						int firsdDot = fieldTypeName.indexOf(\".\");\n"
						+ "						fieldTypeName = \"overidedSubclass\"+fieldTypeName.substring(firsdDot,lastDot)+\".my\"+fieldTypeName.substring(lastDot+1);\n"
						+ "						Class subClass  = Class.forName(fieldTypeName);\n"
						+ "						subClass.asSubclass(fieldType);\n"
						+"						//新建一个子类对象\n"
						+ "						Object ProxyObj=null;\n"
						+ "						ProxyObj= generateProxyClass(fieldValue,fieldType,subClass);\n"
						+ "						field.set(cloned, ProxyObj);\n"
						+ "						this.getClass().getField(field.getName()).set(this, generateProxyClass(fieldValue,fieldType,subClass));\n"
						+ "					}\n"
						+ "				}\n"
						+ "			}\n"
						+ "		} catch (Exception e){\n"
						+ "			e.printStackTrace();\n"
						+ "		}\n"
						+ "		return cloned;\n");
        str.append(tabSpace+"}\n");
        return str;
    }
    /**重写hashcode方法：使用Objects.hash对所有public字段方法生成hash
     *  s.t.    return Objects.hash(name, salary, hireDay);
     *用于比较的字段就要用于hash
     * */
    public StringBuilder printHashcodeMth(){
        StringBuilder str = new StringBuilder();
        str.append(tabSpace+"@Override public int hashCode(){\n");
        //如果字段全为基本类型，返回super.hashcode();
        str.append(""
        		+ "	    if(compareFields.size() == 0){\n"
        		+ "				return super.hashCode();\n"
        		+ "		}else{\n"
        		+ "				return Objects.hash(compareFields.toArray());\n"
        		+ "		}\n");
        str.append("	}\n");
        return str;
    }
    /**如果父类实现了比较方法，则使用父类的比较方法
     * 
     * 比较克隆的字段
     * 比较方法比较public字段,protected字段不能比较，因为class.getFields()方法只返回public字段
     * 基本类型，对象类型，数组，都要进行比较
     * */
    public StringBuilder printEqualMth(){
        StringBuilder str = new StringBuilder();
        str.append(tabSpace + "@Override public boolean equals(Object other){"+lineBreak);
        
        str.append(
"        if(this == other){return true;}\n"+
"        if(getClass() != other.getClass()){return false;}\n"+
"        "+className+" otherObj = ("+className+") other;\n\n"+
"        int equalNum=0;\n"+
"        try{\n"+
"            for(Field field:getClass().getFields()){\n"+
"                if( Modifier.isFinal(field.getModifiers())){continue;}\n\n"+
"                Object fieldThisValue = field.get(this);\n"+
"                Object fieldOtherValue = field.get(otherObj);\n\n"+
"                compareFields.add(fieldThisValue);\n"+
"                Log.v(\"compare "+className+" Field\",Modifier.toString(field.getModifiers())+\" \"+field.getType().getName()+\" \"+field.getName());\n"+
"                Log.v(\"compare "+className+" Field\",fieldThisValue+\"---\"+fieldOtherValue);\n\n"+
"                if(field.getType().isArray()) {//数组字段\n"+
"                    if (DeepClone.compareArr(fieldThisValue, fieldOtherValue)) { equalNum += 1; }\n"+
"                }\n"+
"                else{//基本类型与对象类型\n"+
"                    if (Objects.equals(fieldThisValue,fieldOtherValue)) { equalNum+=1; }\n"+
"                }\n"+
"            }\n"+
"        }catch (IllegalAccessException e) {\n"+
"            e.printStackTrace();\n"+
"        }\n\n"+
"        if(compareFields.size() == equalNum){return true;}\n"+
"        return false;\n");
        str.append(tabSpace+"}\n");
        return str;
    }
    
    public StringBuilder printGenerateProxyClassMth(){
    	StringBuilder str = new StringBuilder();
    	str.append(tabSpace+"//根据父类对象状态信息构造子类对象\n");
    	str.append(tabSpace+"public Object generateProxyClass(Object fieldValue, Class fieldType, Class subClass ) throws IllegalArgumentException,InstantiationException, IllegalAccessException, InvocationTargetException{\n"+
    			"		Object ProxyObj = null;\n"
    			+"		ArrayList originFieldClxs= new ArrayList();\n"
    			+"		for(Field f:fieldType.getFields()){originFieldClxs.add(f.getType());}\n\n"
    			+"		for(Constructor con : subClass.getConstructors()){"
    			+"			Class[] conParas = con.getParameterTypes();\n"
    			+"			if(originFieldClxs.containsAll(Arrays.asList(conParas))){\n"
    			+"				Object[] ProxyConParas = new Object[conParas.length];\n"
    			+"				int i=0;\n"
    			+"				for(Field f:fieldType.getFields()){\n"
    			+"						if(i< conParas.length && f.getType() == conParas[i]){\n"
    			+"						ProxyConParas[i] = f.get(fieldValue);\n"
    			+"						i++;\n"
    			+"					}\n"
    			+"				}\n"
    			+"				ProxyObj =  con.newInstance(ProxyConParas);\n"
    			+"				break;\n"
    			+"			}\n"
    			+"		}\n"
    			+"		return ProxyObj;\n");
    	str.append(tabSpace+"}\n");		
    	return str;			
    }
    
    public String toString(){
    	//对原始类的引用成员生成代理类
    	fieldIsRefTemplate();
    	
        String OnlySuperClx = superClassName.substring(superClassName.lastIndexOf(".")+1);

        StringBuilder content = new StringBuilder();
        //print package and import statement
        content.append("package" + space+packageName + semicolon+lineBreak);content.append(lineBreak);
        content.append("import" + space + superClassName + semicolon+lineBreak);
        content.append("import android.util.Log;\n" +"import java.lang.reflect.*;\n" +
        "import java.util.*;\n" +"import java.io.*;\n");
        content.append(lineBreak);

        content.append("public class" + space + className + space + "extends" + space + OnlySuperClx+space);
        if(!isCloneable){
        	content.append("implements Cloneable"+space);
        }
        content.append("{"+lineBreak);

       
       
        
        //print fields 不能打印字段
        //content.append(printFields()); content.append(lineBreak);

        //print constructors
        content.append(printConstructor()); content.append(lineBreak);
        
        //记录用于比较的字段
        content.append(tabSpace+"private ArrayList compareFields = new ArrayList();\n");content.append(lineBreak);


        //如果原始类有实现clone方法，就不添加
      //print clone method
        if(!isCloneable){
            content.append(printCloneMth());content.append(lineBreak);
            content.append(printGenerateProxyClassMth());content.append(lineBreak);
        }

        //print equals method
        if(!hasEqualsMethod){
        	content.append(printEqualMth());content.append(lineBreak);
        }
        

        //print hashcode method
        if(!hasHashcodeMethod){
            content.append(printHashcodeMth());content.append(lineBreak);
        }

        content.append("}");
        return content.toString();
    }
    public static String getRandomString(int length) {
        //随机字符串的随机字符库,变量首字符为字母
        String KeyString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuffer sb = new StringBuffer();
        int len = KeyString.length();
        sb.append(getRandomUpperCaseLetter());
        for (int i = 0; i < length-1; i++) {
            sb.append(KeyString.charAt((int) Math.round(Math.random() * (len - 1))));
        }
        return sb.toString();
    }
    public static char getRandomCharacter(char ch1,char ch2){
        return (char)(ch1+Math.random()*(ch2-ch1+1));//因为random<1.0，所以需要+1，才能取到ch2
    }
    public static char getRandomUpperCaseLetter(){
        return getRandomCharacter('A','Z');
    }
}

