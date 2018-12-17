package nativemethod.fuzzing;

/**
 * Created by myw on 18-5-14.
 */

import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Random;

import static nativemethod.fuzzing.InitialRandom.getRandomChar;
import static nativemethod.fuzzing.InitialRandom.getRandomString;

/***/
public class Flag
{
    private  int M;/**返回值个数*/
    private int N;/**参数个数*/
    private int[][] flags ;
    private ArrayList ParaArr;/**所有显式参数+this*/
    //private ArrayList mutableArr;/**可变参数+this*/
    //private String classN;/**仅仅类名*/
    private String methodName;/**仅仅方法名:比如onstart*/
    private String retType;/**返回类型*/
    /**我自己生成的子类的类名，如果是final和static方法，则是原始类名*/
    private String myClassName;/** 代理类的类名初始化为原始类类名*/
    public ImmutableType immutable ;/**读取immutabla文件中的final类型的类的名字集合.immutable的定义为是final类，字段全为final*/
    private InitialRandom random;/**读取configure文件的内容并根据参数类型，赋予随机值*/

    private int lenOfEachDim;

    private boolean classIsfinal = false;
    private boolean mthIsfinal = false;
    private boolean mthIsStatic = false;
    private boolean mthHasReturn = false;
    private boolean mthIsNative = false;

    private boolean classhasCloneMth = false;
    private boolean classImpleSerializable = false;
    private boolean classCanCompare = false;


    public Flag(){
        M=0;N=0;
        ParaArr = new ArrayList();
        //mutableArr = new ArrayList();
        random = new InitialRandom();//初始化随机数类
        immutable = new ImmutableType();
        lenOfEachDim = random.getLenOfEachDim();
    }
    /**
     * 根据方法签名生成参数以及this，设置M,N的值。
     * 如果是static方法，没有this
     * **/
    public int paramGeneration(){
        //根据方法签名得到：方法所在类名，方法名，方法参数类型列表，返回类型。
        String mthSign = random.getMthSig();
        String classN = mthSign.split(": ")[0].substring(1);
        //子类的类名初始化为原始类类名
        myClassName = classN;

        String[] sTemp = mthSign.split(": ")[1].split(" ");
        retType = sTemp[0];
        methodName = sTemp[1].substring(0,sTemp[1].indexOf("("));
        String paratemp = sTemp[1].substring(sTemp[1].indexOf("(")+1, sTemp[1].indexOf(")"));

        String[] paraType = paratemp.split(",");
        N = paraType.length;
        if(paratemp.length() == 0){N=0;}
        Log.i("paraType.length",String.valueOf(N));

        if(!retType.equals("void")){
            setMthHasReturn(true);
            M += 1;
        }
        //对显式参数进行随机生成.
        if(N != 0) generate(paraType);

        //生成this
        Object d = returnDestinateClassObj();

        if(d == null){
            return 0;
        }
        else if(d.getClass() == String.class){  // static native method  TODO
            Log.v("m and n",String.valueOf(M)+":"+String.valueOf(N));
            //initial flags array
            if(N==0){
                flags = new int[M][1];
            }else{
                flags =new int[M][N];
            }
            return 1;
        }
        else{//native method  TODO
            ParaArr.add(d);//最后为implicit参数(this)
            if(!immutable.isImmutable(myClassName)){
                M +=1;
               // mutableArr.add(d);
            }
            N = ParaArr.size();
            Log.v("m and n",String.valueOf(M)+":"+String.valueOf(N));
            //initial flags array
            if(N==1){
                flags = new int[M][1];
            }
            else{
                flags =new int[M][N];
            }
            return 2;
        }
    }
    /**在运行时使用反射可以修改成员的访问限制，根据类名和方法名得到声明的方法，设置方法的访问权限为可访问的。
     *         忽略：所要测试的方法必须为public，因为Class.getMethod方法reflects the specified public member method
     * 如果方法是静态的，没有this对象，直接返回原始类名。因为静态方法属于类方法，无需在子类中使用静态方法
     * 如果方法不是静态的，执行以下步骤：
     * 1.如果this是不可变对象（final），原始类不需要比较方法，只需要克隆方法,返回原始类的对象,或者原始类实现了序列化方法，否则返回null
     * 2.如果this是可变对象，方法为final方法，执行原始类的方法，原始类需要实现克隆方法和比较方法，返回原始类的对象，否则返回null
     * 3.如果方法的访问权限为private和protected,默认，只能使用Class.getDeclaredMethod来得到。
     * 只能使用原始类对象类调用方法。原始类对象this需要有克隆方法和比较方法来进行克隆和比较。
     * 4.返回子类的对象
     *
     * @return 值为null:无法生成this; 值为String :表明方法为静态方法，值为Object:返回正常的this对象
     * */
    public Object returnDestinateClassObj(){
        //先判断this类名是否是final修饰，如果是则不可继承，那要判断是否实现了cloneable接口，以及equals,hashcode方法，对于非静态方法而言
        //判断方法参数列表的class，用于使用java反射的getMethod方法
        Class[] paraClasses = new Class[ParaArr.size()];
        int i=0;
        for(Object o: ParaArr) {
            if (o instanceof Integer) {paraClasses[i] = int.class;}
            else if (o instanceof Short) {paraClasses[i] = short.class;}
            else if (o instanceof Long) {paraClasses[i] = long.class;}
            else if (o instanceof Byte) {paraClasses[i] = byte.class;}
            else if (o instanceof Double) {paraClasses[i] = double.class;}
            else if (o instanceof Float) {paraClasses[i] = float.class;}
            else if (o instanceof Character) {paraClasses[i] = char.class;}
            else if (o instanceof Boolean) {paraClasses[i] = boolean.class;}
            else if (immutable.isImmutable(o.getClass().getName())) {paraClasses[i] = o.getClass();}
            else if (o.getClass().isArray()) {paraClasses[i] = o.getClass();}
            //else if (classIsProwerful(o.getClass())){paraClasses[i] = o.getClass();}//原始类是全能的，那使用getclass
            else {paraClasses[i] = o.getClass().getSuperclass();}//可变引用类型，使用它的父类类型
            i++;
        }
        try {
            //设置 classIsfinal mthIsfinal  mthIsStatic  mthIsAccessible  mthIsNative classhasCloneMth  classCanCompare
            Class tempCLz = forName(myClassName);
            //Log.v("tempCLz",tempCLz.getName());
            if(immutable.isImmutable(myClassName)){
               setClassIsfinal(true);
               setMthIsfinal(true);//final类的方法都为final
            }

            Method mth = tempCLz.getDeclaredMethod(methodName,paraClasses);//Class.getMethod方法reflects the specified public member method
            mth.setAccessible(true);//private/default/protect/public方法都可以运行

            int mthModifiers = mth.getModifiers();
            //if((Modifier.isPublic(mthModifiers) || Modifier.isProtected(mthModifiers)) ){
            if(Modifier.isFinal(tempCLz.getModifiers())){setMthIsfinal(true);}
            if(Modifier.isStatic(mthModifiers)) {setMthIsStatic(true);}
            if(Modifier.isNative(mthModifiers)){setMthIsNative(true);}
            if(Modifier.isFinal(mthModifiers)) {setClassIsfinal(true);}

            //设置classhasCloneMth  classCanCompare, classImpleSerializable
            classIsProwerful(tempCLz);


            if(isMthIsStatic()){
                return myClassName;
            }

            if(isClassIsfinal() ){
                if(isClasshasCloneMth()){
                    Log.v("原始类被初始化",myClassName);
                    return simplestConstructorInit(myClassName);//实例化原始类
                }
                else if(isClassImpleSerializable()){
                    Log.v("原始类被初始化",myClassName);
                    return simplestConstructorInit(myClassName);//实例化原始类;
                }
            }
            else if(isMthIsfinal() && isClasshasCloneMth() && isClassCanCompare()){
                Log.v("原始类被初始化",myClassName);
                return simplestConstructorInit(myClassName);//实例化原始类
            }
            else if(Modifier.isPublic(mthModifiers)){//使用代理类
                myClassName = "overidedSubclass.my"+myClassName.substring(myClassName.lastIndexOf(".")+1);
                Log.v("代理类被初始化",myClassName);
                return simplestConstructorInit(myClassName);
            }
            else{

                if(isClassCanCompare()){
                    if(isClassImpleSerializable()){
                        Log.v("原始类被初始化",myClassName);
                        return simplestConstructorInit(myClassName);//实例化原始类;
                    }
                    else if(isClasshasCloneMth()){
                        Log.v("原始类被初始化",myClassName);
                        return simplestConstructorInit(myClassName);//实例化原始类;
                    }
                }
            }


            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**生成参数
     * 规则：基本类型，随机生成; 对象类型，用最简单的构造函数构造，可变类型使用子类，不可变类型使用原始类。
     * 注意：数组是可变对象
     * */
    public void generate(String[] paraType){
        int len = random.getLenOfEachDim();//数组每维度的长度
        Class clx = null;
        for (String para : paraType){
            //m = isPrimitive(para,m);
            //参数为数组类型
            if(para.contains("[]")){//数组还未初始化
                M += 1;//数组为可变类型
                int dim = numOfDimension(para,"[]");
                StringBuilder sb = new StringBuilder();
                for(int in=0;in<dim;in++){
                    sb.append("[");
                }
                //去掉[][][]
                para = para.substring(0,para.indexOf("["));
                //基本类型
                if(para.equals("int")){ sb.append("I");}//ParaArr.add(1);}
                else if(para.equals("short") ){sb.append("S");}
                else if(para.equals("long")){sb.append("J");}
                else if(para.equals("byte")){sb.append("B");}
                else if(para.equals("float")){sb.append("F");}
                else if(para.equals("double")){sb.append("D");}
                else if(para.equals("char")){sb.append("C");}
                else if(para.equals("boolean")){ sb.append("Z");}//初始值为false
                //java对象类型
                else{ sb.append("L"+para+";");}
                clx = forName(sb.toString());
            }
            //基本类型
            else if(para.equals("int")){ clx = int.class;}//ParaArr.add(1);}
            else if(para.equals("short") ){clx = short.class;}
            else if(para.equals("long")){clx = long.class;}
            else if(para.equals("byte")){clx = byte.class;}
            else if(para.equals("float")){clx = float.class;}
            else if(para.equals("double")){clx = double.class;}
            else if(para.equals("char")){clx = char.class;}
            else if(para.equals("boolean")){ clx = boolean.class;}//初始值为false
            else if(immutable.isImmutable(para)){clx = forName(para);}
            else{
                M += 1;
                clx = forName(para);
            }
            Object o = generateParas(clx);
            ParaArr.add(o);
        }
    }

    /**使用最简单的构造方法构造对象。  不分 可变和不可变类型
     * 最简单的构造方法规则：参数个数最少（无参）或参数全部为基本类型
     * 构造函数的参数如果有数组类型，这里只考虑了最多两维。
     *
     * @param className full name of reference type, such as android.graphics.Paint.
     * @return  object whose constructor has none para or all paras are primitive or has least paras
     * */
    public Object simplestConstructorInit(String className){
        try{
            Class tempCLz = forName(className);//Log.v("类名称：", tempCLz.getName());
            //tempCLz.getConstructor()
            Constructor[] Constructors = tempCLz.getConstructors();//all public constructor
            if(Constructors.length==0) {
                Log.e("没有public构造器",className);
                return null;
            }
            int minParaLen=20;//初始化构造函数参数个数为20
            Constructor primitiveParaCon = null,leastParaCon = null;
            for(Constructor cl: Constructors) {
                Class[] paratype = cl.getParameterTypes();
                if (paratype.length < minParaLen) {
                    minParaLen = paratype.length;
                    leastParaCon = cl;
                }
                int primitionNum = 0;
                for(Class pa : paratype){
                    if (pa.isPrimitive()){ primitionNum += 1;}
                }
                if(primitionNum == paratype.length && primitionNum != 0){primitiveParaCon = cl;}
            }
            if (minParaLen == 0){
                try{
                    Object tempObject = tempCLz.newInstance();
                    Log.v("被无参实例化",tempCLz.getName());
                    return tempObject;
                }catch(InstantiationException e){
                    Log.v("不能无参实例化",tempCLz.getName());
                }catch(IllegalAccessException r){
                    Log.v("非法访问",tempCLz.getName());
                }

            }
            else if(primitiveParaCon != null){//参数最简单的,最好全为基本类型
                Object[] initargs = new Object[primitiveParaCon.getParameterTypes().length];
                int i=0;
                for(Class cla:primitiveParaCon.getParameterTypes()){
                    if(cla == int.class){initargs[i] = random.randomInt();}
                    if(cla == double.class){initargs[i] = random.randomDouble();}
                    if(cla == short.class){initargs[i] = random.randomShort();}
                    if(cla == byte.class){initargs[i] = random.randombyte();}
                    if(cla == long.class){initargs[i] = random.randomLong();}
                    if(cla == float.class){initargs[i] = random.randomFloat();}
                    if(cla == char.class){initargs[i] = getRandomChar();}
                    if(cla == boolean.class){initargs[i] = random.randomBoolean();}
                    i++;
                }
                try {
                    Object object = primitiveParaCon.newInstance(initargs);
                    Log.v("被基本类型参数实例化",printConstructor(primitiveParaCon));
                    return object;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            else if(leastParaCon !=null) {//或参数个数最少
                Class[] paratype = leastParaCon.getParameterTypes();
                Object[] initargs = new Object[minParaLen];
                for(int i=0;i<paratype.length;i++){
                    initargs[i] = generateParas(paratype[i]);
                }
                try {
                    Object object = leastParaCon.newInstance(initargs);
                    Log.v("被构造器参数最少且参数有引用类型的构造函数实例化",printConstructor(leastParaCon));
                    return object;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.v("生成对象出错",className);
        }
        return null;
    }

    /**参数的生成，对于各个类型：基本类型，数组，可变与不可变对象
     * 基本类型：随机生成
     * 数组：根据数组的维度生成指定类型的数组
     * 可变对象：使用子类生成
     * 不可变对象：使用原始类生成
     * */
    public Object generateParas(Class clx){

        if(clx.isArray()){
            int dim = numOfDimension(clx.getName(),"[");
            Object allArr = Array.newInstance(clx.getComponentType(), dim);
            generateArr(allArr);
            return allArr;
        }
        //参数为基本类型
        else if(clx.isPrimitive()){
            if(clx == int.class){return random.randomInt();}
            else if(clx == double.class){return random.randomDouble();}
            else if(clx == short.class){return random.randomShort();}
            else if(clx == byte.class){return random.randombyte();}
            else if(clx == long.class){return random.randomLong();}
            else if(clx == float.class){return random.randomFloat();}
            else if(clx == char.class){return getRandomChar();}
            else {return random.randomBoolean();}
        }
        //参数为不可变类型,使用原始类构造
        else if(immutable.isImmutable(clx.getName())){
            if(clx.getName().equals("java.lang.String")) return getRandomString(random.getStringLen());
                //ParaArr.add("camera");
            else
                return simplestConstructorInit(clx.getName());
        }
        else {//可变引用类型
            String objName = clx.getName();
            String subClzName = "overidedSubclass.my"+objName.substring(objName.lastIndexOf(".")+1);
            return simplestConstructorInit(subClzName);
        }
    }
    /**数组的生成，可生成多维数组
     * 多维数组使用递归进行
     * */
    public void generateArr(Object arrObj){
        Class component;
        Object val;

        for (int i = 0; i < Array.getLength(arrObj); i++) {
            component = arrObj.getClass().getComponentType();
            val = Array.get(arrObj, i);
            if(component.isArray()){
                val = Array.newInstance(component.getComponentType(),lenOfEachDim);
                //如果元素是数组，则递归调用
                generateArr(val);
            }
            else{//非数组
                val = generateParas(component);
            }
            Array.set(arrObj, i, val);
        }
    }

    public void flipArr(Object arrObj){
        Object val;

        for (int i = 0; i < Array.getLength(arrObj); i++) {
            val = Array.get(arrObj, i);
            if(val == null) {
                val =null;
            }
            else if(val.getClass().isArray()){
                val = Array.newInstance(val.getClass().getComponentType(),lenOfEachDim);
                //如果元素是数组，则递归调用
                flipArr(val);
            }
            else{//非数组
                val = flipParas(val);
            }
            Array.set(arrObj, i, val);
        }
    }
    /**翻转参数时，考虑各种情况：
     * 1.基本类型（这里为包裹类）：重新随机生成
     * 2.数组(支持多维数组)：重新随机生成数组
     * 3.对象（可变及不可变对象）。不可变对象除了string,都是尽量新建一个对象，使用随机的构造函数构造新的对象
     * */
    public Object flipParas(Object para){
        //数组
        Object res = null;
        if(para.getClass().isArray()){
            int dim = numOfDimension(para.getClass().getName(),"[");
            Object allArr = Array.newInstance(para.getClass().getComponentType(), dim);
            flipArr(allArr);
            res = allArr;
        }
        //对象为基本类型
        else if(para.getClass().isPrimitive()){
            if(para instanceof Integer){res = random.randomInt();}
            else if(para instanceof Short){res = random.randomShort();}
            else if(para instanceof Byte){res = random.randombyte();}
            else if(para instanceof Long){res = random.randomLong();}
            else if(para instanceof Double){res = random.randomDouble();}
            else if(para instanceof Float){res = random.randomFloat();}
            else if(para instanceof Character){res = getRandomChar();}
            else if(para instanceof Boolean ){res = random.randomBoolean();}
        }
        //String类型
        else if(para.getClass()==String.class){res =getRandomString(random.getStringLen());}
        //对象类型
        else{//重新用随机构造函数新建对象
            res = randomConstructorInit(para);
        }
        return res;
    }


    /**随机选取一个构造函数来构造对象，包括可变和不可变类型
     *
     * */
    public Object randomConstructorInit(Object obj){
        //String类型
        if(obj.getClass() == String.class) {return getRandomString(random.getStringLen());}
        //其他对象类型，包括可变和不可变类型
        Constructor[] Constructors = obj.getClass().getConstructors();
        //Log.v("对象类型",para[i].getClass().getName());

        if(Constructors.length >=2 ){
            //随机选取一个构造函数
            Constructor randomConstructor = Constructors[new Random().nextInt(Constructors.length)];
            /*  //找到参数最多的
            int maxParaLen = 0;//初始化构造函数参数个数为0
            Constructor longParaCon = null;
            for(Constructor cl: Constructors) {
                Class[] paratype = cl.getParameterTypes();
                if (paratype.length > maxParaLen) {
                    maxParaLen = paratype.length;
                    longParaCon = cl;
                }
            }*/
            Class[] paratype = randomConstructor.getParameterTypes();
            //初始化参数值
            Object[] initargs = new Object[paratype.length];
            for(int ind=0;ind<paratype.length;ind++){
                initargs[ind] = generateParas(paratype[ind]);
            }

            try {
                Object object = randomConstructor.newInstance(initargs);
                obj = object;

                Log.v("被随机构造函数实例化",printConstructor(randomConstructor));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        else{//只有一个构造参数,修改基本类型字段，如果没有基本类型字段，则修改引用类型的基本类型字段
            onlyOneConstructorObjFilp(obj);
        }
        return obj;
    }
    /**对象只有一个构造函数时，修改对象的得到public字段（不包括final）
     * 1.如果对象的字段有基本类型，那就修改基本类型的值
     * 2.如果没有基本类型的字段，对于对象字段，修改对象字段的基本字段; 对于数组，翻转数组
    * */
    public void onlyOneConstructorObjFilp(Object obj){
        Class curClass = obj.getClass();
        int notFinalNum=0;
        int primitiveNum = 0;
        int all = curClass.getFields().length;
        Log.v("num of Fields",curClass.getName()+":"+String.valueOf(all));
        Log.v("Fields",curClass.getName()+":"+printFields(curClass));
        try{
            for(Field f : curClass.getFields()){//得到public 字段

                if(!Modifier.isFinal(f.getModifiers()) ){
                    notFinalNum += 1;
                    if(f.getType().isPrimitive()){//字段是基本类型
                        primitiveNum++;
                        if(f.getType() == int.class){f.set(obj,random.randomInt());}
                        else if(f.getType() == short.class){f.set(obj,random.randomShort());}
                        else if(f.getType() == long.class){f.set(obj,random.randomLong());}
                        else if(f.getType() == byte.class){f.set(obj,random.randombyte());}
                        else if(f.getType() == double.class){f.set(obj,random.randomDouble());}
                        else if(f.getType() == float.class){f.set(obj,random.randomFloat());}
                        else if(f.getType() == char.class){f.set(obj,getRandomChar());}
                        else if(f.getType() == boolean.class){f.set(obj,random.randomBoolean());}
                    }
                }
            }
            if((all-notFinalNum)  ==0) return;//全部为public final，无public其他字段
            if(primitiveNum == 0){//没有public基本类型
                if((notFinalNum-primitiveNum)!=0){//对public 非final，非基本类型的对象进行同样的改字段值
                    for(Field f : curClass.getFields()){
                        Object fieldObj = f.get(obj);
                        if(Modifier.isFinal(f.getModifiers())){
                            continue;
                        }
                        if(!f.getType().isArray()){
                            if(fieldObj!=null){
                                onlyOneConstructorObjFilp(fieldObj);
                            }
                        }
                        //数组字段
                        else{
                            flipArr(fieldObj);
                        }
                    }
                }
            }
        }
        catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }


    public static String printFields(Class cl)
    {
        Field[] fields = cl.getFields();
        StringBuilder sb = new StringBuilder();
        for (Field f : fields)
        {
            Class type = f.getType();
            String name = f.getName();
            sb.append("   ");
            String modifiers = Modifier.toString(f.getModifiers());
            if (modifiers.length() > 0) sb.append(modifiers + " ");
            sb.append(type.getName() + " " + name + ";\n");
        }
        return sb.toString();
    }
    /**@param c a given constructor
     * @return String,such as  public java.lang.String([B, java.nio.charset.Charset);
     * */
    public static String printConstructor(Constructor c)
    {
        StringBuilder str = new StringBuilder();
        String name = c.getName();
        str.append("   ");
        String modifiers = Modifier.toString(c.getModifiers());
        if (modifiers.length() > 0) str.append(modifiers + " ");
        str.append(name + "(");

        // print parameter types
        Class[] paramTypes = c.getParameterTypes();
        for (int j = 0; j < paramTypes.length; j++)
        {
            if (j > 0) str.append(", ");
            str.append(paramTypes[j].getName());
        }
        str.append(");");
        return str.toString();
    }
    public static void printMethods(Class cl)
    {
        Method[] methods = cl.getDeclaredMethods();

        for (Method m : methods)
        {
            Class retType = m.getReturnType();
            String name = m.getName();

            System.out.print("   ");
            // print modifiers, return type and method name
            String modifiers = Modifier.toString(m.getModifiers());
            if (modifiers.length() > 0) System.out.print(modifiers + " ");
            System.out.print(retType.getName() + " " + name + "(");

            // print parameter types
            Class[] paramTypes = m.getParameterTypes();
            for (int j = 0; j < paramTypes.length; j++)
            {
                if (j > 0) System.out.print(", ");
                System.out.print(paramTypes[j].getName());
            }
            System.out.println(");");
        }
    }
    /**Given a class, find all its constructors and print.
     * @param cl given a class
     * */
    public static void printConstructors(Class cl)
    {
        Constructor[] constructors = cl.getConstructors();

        for (Constructor c : constructors)
        {
            String name = c.getName();
            System.out.print("   ");
            String modifiers = Modifier.toString(c.getModifiers());
            if (modifiers.length() > 0) System.out.print(modifiers + " ");
            System.out.print(name + "(");

            // print parameter types
            Class[] paramTypes = c.getParameterTypes();
            for (int j = 0; j < paramTypes.length; j++)
            {
                if (j > 0) System.out.print(", ");
                System.out.print(paramTypes[j].getName());
            }
            System.out.println(");");
        }
    }

    /**得到参数对象中的可变引用对象
     * 参数为数组，数组是可变引用对象
     * */
    public ArrayList getRef(ArrayList paras){
        ArrayList copyList = new ArrayList();
        for(Object para: paras){
            Log.v("para",para.getClass().getName());
            //判断数组类型    Determines if this Class object represents an array class
            if(para.getClass().isArray()){
                copyList.add(para);
                /*
                int dim = numOfDimension(paraName,"[");//判断维数
                if(dim == 1){
                    Class element = para.getClass().getComponentType();
                    if(!(isPrimitive(element) || immutable.isImmutable(element.getName()))){
                        copyList.add(para);
                    }
                }
                else if(dim == 2){
                    Class element = para.getClass().getComponentType().getComponentType();
                    if(!(isPrimitive(element) || immutable.isImmutable(element.getName()))){
                        copyList.add(para);
                    }
                }
                */
            }
            //判断不是基本类型和不可变类型
            else if(!( isPrimitive(para.getClass()) || immutable.isImmutable(para.getClass().getName()) )){
                    copyList.add(para);
            }
        }
        return copyList;
    }
    /**根据class判断是否为基本类型
     * @Para clx
     *
     * */

    public boolean isPrimitive(Class clx){
        if(clx == Integer.class){
            return true;
        }
        else if(clx == Long.class){
            return true;
        }
        else if(clx == Short.class){
            return true;
        }
        else if(clx == Byte.class){
            return true;
        }
        else if(clx == Float.class){
            return true;
        }
        else if(clx == Double.class){
            return true;
        }
        else if(clx == Character.class){
            return true;
        }
        else if(clx == Boolean.class){
            return true;
        }
        else{
            return false;
        }
    }

    /*public static boolean isPrimitive(String type){
        if(type.equals("int")) return true;
        else if(type.equals("short")) return true;
        else if(type.equals("long")) return true;
        else if(type.equals("byte")) return true;
        else if(type.equals("float")) return true;
        else if(type.equals("double")) return true;
        else if(type.equals("char")) return true;
        else if(type.equals("boolean")) return true;
        else return false;
    }*/

    public boolean classIsProwerful(Class tempClx){
        //对象都继承了equals和hashCode,需要判断是否重写了这两个方法
        for(Method mth:tempClx.getDeclaredMethods()){
            if(mth.getName().equals("equals")){
                setClassCanCompare(true);
                break;
            }
        }

        for(Class interf : tempClx.getInterfaces()) {
            if (interf == Cloneable.class) {
                setClasshasCloneMth(true);
            }
            if(interf == Serializable.class){
                setClassImpleSerializable(true);
            }
        }

        return false;
    }
    /**返回数组维度*/
    public static int numOfDimension(String mainStr, String subString){
        int count = 0;
        int index = 0;
        int fromindex = 0;
        while((index = mainStr.indexOf(subString,fromindex))>=0){
            count += 1;
            fromindex = index + subString.length();
        }
        return count;
    }

    public static Class forName(String s){
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            Log.e("forname方法失败",e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public String getMyClassName() {return myClassName;}

    public String getRetType() {return retType;}

    public boolean isClassIsfinal() {return classIsfinal;}

    public void setClassIsfinal(boolean classIsfinal) {
        this.classIsfinal = classIsfinal;
    }

    public boolean isMthIsfinal() {
        return mthIsfinal;
    }

    public void setMthIsfinal(boolean mthIsfinal) {
        this.mthIsfinal = mthIsfinal;
    }

    public boolean isMthIsStatic() {
        return mthIsStatic;
    }

    public void setMthIsStatic(boolean mthIsStatic) {
        this.mthIsStatic = mthIsStatic;
    }

    public boolean isClasshasCloneMth() {
        return classhasCloneMth;
    }

    public void setClasshasCloneMth(boolean classhasCloneMth) {this.classhasCloneMth = classhasCloneMth;}

    public int getM(){return M;}

    public int getN(){return N;}

    public String getMethodName(){return methodName;}

    public boolean isClassCanCompare() {
        return classCanCompare;
    }

    public void setClassCanCompare(boolean classCanCompare) {this.classCanCompare = classCanCompare;}

    public boolean isMthHasReturn() {
        return mthHasReturn;
    }

    public void setMthHasReturn(boolean mthHasReturn) {
        this.mthHasReturn = mthHasReturn;
    }

    public void setClassImpleSerializable(boolean classImpleSerializable) {
        this.classImpleSerializable = classImpleSerializable;
    }

    public boolean isClassImpleSerializable() {
        return classImpleSerializable;
    }

    public boolean isMthIsNative() {
        return mthIsNative;
    }

    public void setMthIsNative(boolean mthIsNative) {
        this.mthIsNative = mthIsNative;
    }



    public int getTestNum(){ return random.getTestNum(); }

    public int[][] getFlags(){
        return flags;
    }

    public void setFlags(int i,int j,int value){
        this.flags[i][j] = value;
    }

    public ArrayList getParaArr(){
        return ParaArr;
    }
}
