package nativemethod.fuzzing;

import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class RunMethod{
    private static Integer invokeFailedNUm=0;
    private Flag flg = null;

    public RunMethod(){
        flg = new Flag();
    }
    /**
     * 算法程序开始
     * */
    public void run()  {
        long startTime = System.currentTimeMillis();
        /**back == 1 : 方法为静态方法
         * back == 2 : 方法为对象方法
         * back == 0 : 无法分析方法
         * */
        int  back = flg.paramGeneration();
        ArrayList allParas = flg.getParaArr();

        int M =flg.getM();
        int N = flg.getN();

        //如果flag矩阵的m为0，直接返回不分析
        if(M == 0){
            Log.e("方法返回值的个数为0","m=0");
            return;
        }
        if(back== 0){
            Log.e("无法生成this,无法分析该方法",flg.getMyClassName());
            return;
        }

        else{
            //int invokeFailedNUm = 0;
            SaveFile saveF = new SaveFile("Flag");
            int testNum = flg.getTestNum();
            for(int count = 0;count<testNum;count++){//测试次数
                Log.v("循环次数",String.valueOf(count+1));
                int explicitLen=0;
                if(!flg.isMthIsStatic()) {
                    explicitLen = N-1;
                    if(explicitLen==0 && M != 0){//参数个数为0,返回值不为0,存在this
                        runNonParaMethod(flg);
                    }
                }
                else{//static方法
                    explicitLen =N;
                    if(explicitLen==0){
                        runNonParaMethod(flg);
                    }
                }
                for(int index=0; index < explicitLen; index++) {
                    int anM = M;
                    Log.v("第几个参数",String.valueOf(index+1));

                    //对象方法,去掉implicit参数,否则不变
                    Object thisObj=null;
                    if(back==2) {//非static方法
                        thisObj = allParas.remove(allParas.size()-1);
                        Log.v("thisObj",thisObj.getClass().getName());
                    }

                    /**备份参数中的可变引用类型*/
                    Object[] firstMutuableExplicitPara = flg.getRef(allParas).toArray();
                    //Log.v("firstReferencePara",firstReferencePara.toString());
                    Object[] copyParaMutableObjList = new Object[firstMutuableExplicitPara.length];
                    Log.v("copyObjList length",String.valueOf(copyParaMutableObjList.length));
                    for (int i = 0; i < firstMutuableExplicitPara.length; i++) {
                        //如果参数是数组类型,深度克隆数组
                        if(firstMutuableExplicitPara[i].getClass().isArray()){
                            Object cloneArr = Array.newInstance(firstMutuableExplicitPara[i].getClass().getComponentType(),
                                    Array.getLength(firstMutuableExplicitPara[i]));
                            try{
                                cloneArr(firstMutuableExplicitPara[i], cloneArr);
                            }catch (Exception e){
                                Log.v("克隆数组失败",firstMutuableExplicitPara[i].getClass().getName());
                                e.printStackTrace();
                            }
                            copyParaMutableObjList[i] = cloneArr;
                        }
                        else{//可变对象
                            copyParaMutableObjList[i] = cloneObject(firstMutuableExplicitPara[i]);
                        }
                        if((copyParaMutableObjList[i] == null)) {
                            Log.e("克隆失败", firstMutuableExplicitPara[i].getClass().getName());
                            return;
                        }
                    }
                    //判断是要克隆this还是使用序列化
                    Object copyThis = null;
                    if(flg.isClassImpleSerializable()){
                        copyThis = Utils.getObjbySerializable(thisObj);
                    }
                    else if(flg.isClasshasCloneMth()){
                        copyThis = cloneObject(thisObj);
                    }








                    /**判断方法参数列表的class，用于使用java反射的getMethod方法*/
                    Object[] FirstRealParasWithoutImplicit = allParas.toArray();
                    Class[] parasType = new Class[FirstRealParasWithoutImplicit.length];
                    for (int i = 0; i < FirstRealParasWithoutImplicit.length; i++) {
                        if (FirstRealParasWithoutImplicit[i] instanceof Integer) {parasType[i] = int.class;}
                        else if (FirstRealParasWithoutImplicit[i] instanceof Short) {parasType[i] = short.class;}
                        else if (FirstRealParasWithoutImplicit[i] instanceof Long) {parasType[i] = long.class;}
                        else if (FirstRealParasWithoutImplicit[i] instanceof Byte) {parasType[i] = byte.class;}
                        else if (FirstRealParasWithoutImplicit[i] instanceof Double) {parasType[i] = double.class;}
                        else if (FirstRealParasWithoutImplicit[i] instanceof Float) {parasType[i] = float.class;}
                        else if (FirstRealParasWithoutImplicit[i] instanceof Character) {parasType[i] = char.class;}
                        else if (FirstRealParasWithoutImplicit[i] instanceof Boolean) {parasType[i] = boolean.class;}
                        else if (flg.immutable.isImmutable(FirstRealParasWithoutImplicit[i].getClass().getName())) {
                            parasType[i] = FirstRealParasWithoutImplicit[i].getClass();}
                        else if (FirstRealParasWithoutImplicit[i].getClass().isArray()) {parasType[i] = FirstRealParasWithoutImplicit[i].getClass();}
                        //else if (flg.classIsProwerful(FirstRealParasWithoutImplicit[i].getClass() )){parasType[i] = FirstRealParasWithoutImplicit[i].getClass();}//原始类是全能的，那使用getclass
                        else {parasType[i] = FirstRealParasWithoutImplicit[i].getClass().getSuperclass();}//可变引用类型，使用它的父类类型
                    }
                    for(Object ff: FirstRealParasWithoutImplicit){Log.v("first allParas",ff.toString());}
                    Log.v("myclassName",flg.getMyClassName());




                    /**方法执行第一次*/
                    Object[] firstReturn = new Object[anM];
                    if(back ==1){//是静态方法
                        Object  returnObj;
                        Class clx = Flag.forName(flg.getMyClassName());
                        returnObj = invokeStaticMthByReflect(clx,flg.getMethodName(),parasType,FirstRealParasWithoutImplicit,firstReturn);
                        if(returnObj != null){
                            firstReturn[0] = returnObj;
                            Log.v("return",returnObj.getClass().getName()+": "+returnObj.toString());
                        }
                        //返回值不是void,调用失败，则将返回值置为空，防止后面程序失败
                        if(!flg.getRetType().equals("void") && returnObj == null){
                            anM = anM-1;
                        }
                        /*String returnName = "";
                        try {
                            Class clx = Flag.forName(flg.getMyClassName());
                            Method Mth = clx.getMethod(flg.getMethodSig(), parasType);
                            returnName  = Mth.getReturnType().getName();
                            Object returnObj  = Mth.invoke(null, FirstRealParasWithoutImplicit);
                            if(returnObj != null){
                                firstReturn[0] = returnObj;
                                Log.v("return",returnObj.getClass().getName()+":"+returnObj.toString());
                            }
                            Log.v("第一次执行方法完毕","success");
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {//如果方法执行失败，如果有返回值，不用比较返回值
                            if(!returnName.equals("void")){
                                firstReturn[0] = null;
                                anM = anM-1;////方法执行失败，anM-1
                                e.printStackTrace();
                            }
                            invokeFailedNUm++;
                        }*/
                    }
                    else if(back ==2){//非静态方法
                        Object returnObj=null;
                        returnObj = invokeMthByReflect(thisObj,flg.getMethodName(),parasType,FirstRealParasWithoutImplicit,firstReturn);
                        if(returnObj != null){
                            firstReturn[0] = returnObj;
                            Log.v("return",returnObj.getClass().getName()+":"+returnObj.toString());
                        }
                        if(!flg.getRetType().equals("void") && returnObj == null){
                            anM = anM-1;
                        }
                        /*
                        String returnName="";
                        try {
                            Method Mth;
                            Object returnObj;

                            Mth= thisObj.getClass().getMethod(flg.getMethodSig(), parasType);
                            returnObj  = Mth.invoke(thisObj, FirstRealParasWithoutImplicit);
                            returnName = Mth.getReturnType().getName();
                            //if(FirstRealParasWithoutImplicit.length ==0) FirstRealParasWithoutImplicit=null;

                            if(returnObj != null){
                                firstReturn[0] = returnObj;
                                Log.v("return",returnObj.getClass().getName()+":"+returnObj.toString());
                            }
                            Log.v("第一次执行方法完毕","success");
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            if(!returnName.equals("void")){
                                firstReturn[0] = null;
                                anM = anM-1;//方法执行失败，anM-1
                                e.printStackTrace();
                            }
                            invokeFailedNUm++;
                        }*/
                    }


                    //方法第一次执行完毕得到firstReturn[0]，将引用类型对象放入到firstReturn
                    int ind = 0;
                    if(flg.isMthHasReturn()) ind=1;//无论方法是否调用成功，都有返回值，即使调用方法失败返回值为null
                    for(Object obj : firstMutuableExplicitPara){
                        firstReturn[ind] = obj;
                        ind++;
                    }
                    if(back == 2){
                        firstReturn[ind] = thisObj;
                    }












                    /**第一次方法的显式参数在执行过程中可能变化，将之前copy的引用类型参数放入到第二次方法执行的参数列表里*/
                    Object[] SecondRealParasWithoutImplicit = FirstRealParasWithoutImplicit;//remove后的数组
                    ArrayList arry = new ArrayList();
                    if(FirstRealParasWithoutImplicit==null){//显式参数为空
                        SecondRealParasWithoutImplicit = FirstRealParasWithoutImplicit;
                    }
                    else {
                        for(Object obj:FirstRealParasWithoutImplicit){arry.add(obj);}
                        for(int i=0;i<firstMutuableExplicitPara.length-1;i++){
                            int dex = arry.indexOf(firstMutuableExplicitPara[i]);
                            SecondRealParasWithoutImplicit[dex] = copyParaMutableObjList[i];
                        }
                    }
                    /**翻转显式参数*/
                    if(SecondRealParasWithoutImplicit!=null){
                        for(Object ff: SecondRealParasWithoutImplicit){Log.v("未翻转之前",ff.toString());}
                        //Log.v("replace paras","ok!");
                        SecondRealParasWithoutImplicit[index] = flg.flipParas(SecondRealParasWithoutImplicit[index]);
                        Log.v("参数翻转","ok!");

                        for(Object ff: SecondRealParasWithoutImplicit){Log.v("翻转后参数",ff.toString());}
                    }

                    //建立参数之间的联系，结果验证我的算法没有错误
                    /*for(Object d:SecondRealParasWithoutImplicit){
                        Log.v("修改paint shadowColor",d.getClass().getName());
                        if(d.getClass() == myPaint.class){
                            try {
                                Field sdsd = d.getClass().getField("shadowColor");
                                sdsd.setInt(d, (Integer) SecondRealParasWithoutImplicit[2]);
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }

                        }
                    }*/



                    int teM = M;//避免方法执行失败，程序停止
                    /**第二次执行方法*/
                    Object[] secondReturn = new Object[M];

                    if(back ==1){//静态方法
                        Object  returnObj=null;
                        Class clx = Flag.forName(flg.getMyClassName());
                        returnObj = invokeStaticMthByReflect(clx,flg.getMethodName(),parasType,SecondRealParasWithoutImplicit,secondReturn);
                        if(returnObj != null){
                            secondReturn[0] = returnObj;
                            Log.v("return",returnObj.getClass().getName()+":"+returnObj.toString());
                        }
                        //返回值不是void,调用失败，则将返回值置为空，防止后面程序失败
                        if(!flg.getRetType().equals("void") && returnObj == null){
                            teM = teM-1;
                        }
                        /*try {
                            Class clx = Flag.forName(flg.getClassN());
                            Method Mth = clx.getMethod(flg.getMethodSig(), parasType);
                            returnName = Mth.getReturnType().getName();
                            Object returnObj  = Mth.invoke(null, SecondRealParasWithoutImplicit);
                            if(returnObj != null){
                                secondReturn[0] = returnObj;
                                Log.v("return",returnObj.getClass().getName()+":"+returnObj.toString());
                            }
                            Log.v("第二次执行方法完毕","success");
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            if(!returnName.equals("void")){
                                secondReturn[0] = null;
                                anM = anM - 1;
                                e.printStackTrace();
                            }
                            invokeFailedNUm++;
                        }*/
                    }
                    else if(back == 2){
                        Object  returnObj;

                        returnObj = invokeMthByReflect(copyThis,flg.getMethodName(),parasType,SecondRealParasWithoutImplicit,secondReturn);
                        if(returnObj != null){
                            secondReturn[0] = returnObj;
                            Log.v("return",returnObj.getClass().getName()+":"+returnObj.toString());
                        }
                        //有返回值且返回为空,调用失败，则将返回值置为空，防止后面程序失败
                        if(!flg.getRetType().equals("void") && returnObj == null){
                            teM = teM-1;
                        }
                        /*
                        String returnName="";
                        try {
                            Method Mth;
                            Object returnObj;

                            Mth= copyThis.getClass().getMethod(flg.getMethodSig(), parasType);
                            returnObj  = Mth.invoke(copyThis, FirstRealParasWithoutImplicit);
                            returnName = Mth.getReturnType().getName();

                            if(returnObj != null){
                                secondReturn[0] = returnObj;
                                Log.v("return",returnObj.getClass().getName()+":"+returnObj.toString());
                            }
                            Log.v("第二次执行方法完毕","success");
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            if(!returnName.equals("void")){
                                secondReturn[0] = null;
                                anM = anM - 1;
                                e.printStackTrace();
                            }
                            invokeFailedNUm++;
                        }*/
                    }
                    /**设置allParas为，使得下次循环继续*/
                    allParas.clear();
                    if(SecondRealParasWithoutImplicit != null){
                        //Log.v("SecondRealParasWithoutImplicit",SecondRealParasWithoutImplicit.toString());
                        for(Object obj:SecondRealParasWithoutImplicit){
                            allParas.add(obj);
                        }
                    }
                    if(copyThis !=null ){//添加this，不管是可变对象还是不可变对象，this都会在调用方法时使用
                        allParas.add(copyThis);
                    }
                    /*if(thisObj !=null && flg.immutable.isImmutable(thisObj.getClass().getName())){//this为不可变类型
                        allParas.add(thisObj);
                    }*/
                    Object[] secondRefPara = flg.getRef(allParas).toArray();
                    //Log.v("secondRefPara",secondRefPara.toString());
                    int tep = 0;
                    if(flg.isMthHasReturn()) tep=1;//无论方法是否调用成功，都有返回值，即使返回值为null
                    for(Object obj : secondRefPara){
                        secondReturn[tep] = obj;
                        tep++;
                    }




                    /**比较第一次的引用结果firstReturn和第二次的引用结果secondReturn*/
                    Log.v("返回比较结果","start");
                    ArrayList<Relation> relateArr = new ArrayList();
                    int i=0;
                    if(anM != M || teM != M) {
                        i=1; //没有方法返回值
                        Log.e("返回方法的返回值失败",flg.getRetType());
                    }

                    for(; i < M ; i++){
                        Log.v("reference",firstReturn[i].getClass().getName()+"---"+secondReturn[i].getClass().getName());
                        if(firstReturn[i].getClass().isArray()){//数组单独比较
                            if(compareArr(firstReturn[i],secondReturn[i])){
                                flg.setFlags(i,index,1);
                            }
                        }
                        else {
                            if(!firstReturn[i].equals(secondReturn[i])){//这里重点要实现每个对象的比较方法
                                flg.setFlags(i,index,1);
                                Log.v("值改变,flag为1",firstReturn[i].getClass().getName());
                                //Relation relate = new Relation(flg.getMethodSig(),SecondRealParasWithoutImplicit[index].getClass().getName(),SecondRealParasWithoutImplicit[index]
                                //      ,secondReturn[i].getClass().getName(),secondReturn[i]);//若两次执行结果不一样，则将改变的参数和第二次的返回值记录到文件。
                                //relateArr.add(relate);
                            }
                        }

                    }
                    Log.v("比较结果","finish");
                    saveF.save(relateArr);
                }
                /**输出flag*/
                int[][] flag = flg.getFlags();
                for(int i=0; i < M; i++){
                    if(N !=0){
                        for (int j=0; j < N; j++){
                            Log.v("flags",String.valueOf(flag[i][j]));
                        }
                    }
                    else{//一维
                        Log.v("一维flags",String.valueOf(flag[i][0]));
                    }
                }

            }
            Log.v("runtime",String.valueOf((double)(System.currentTimeMillis()-startTime)/1000)+"s");
            if(invokeFailedNUm/2 >= testNum*0.95) Log.e("95%调用失败","参数设置问题");
        }
    }


    /**对象方法，有this,通过object得到指定方法
     *
     * */
    public Object invokeMthByReflect(Object target,String mthName,Class[] parasType,Object[] realPara,Object[] Return){
        String returnName ="";
        Object returnObj;
        try {
            Method mth;
            if(flg.getMyClassName().contains("overidedSubclass.my")){
                mth = target.getClass().getMethod(mthName, parasType);
            }
            else{
                mth = target.getClass().getDeclaredMethod(mthName, parasType);
            }

            Log.v("target",target.getClass().getName());

             returnObj  = mth.invoke(target, realPara);
             Log.v("执行对象方法成功","success");

            returnName = mth.getReturnType().getName();
            Log.v("return name",returnName);

            if(returnObj != null){Log.v("return",returnObj.toString());}
            if(returnName.equals("void")){return null;}

            return returnObj;
        } catch (NoSuchMethodException e) {
            Log.e("没有该方法",mthName);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {//返回值不是void,调用失败，则将返回值置为空，防止后面程序失败
            if(!returnName.equals("void")){
                Return[0] = null;
                e.printStackTrace();
            }
            invokeFailedNUm++;
        }
        return null;
    }
    /**静态方法，没有this,通过class调用指定方法
     * */
    public Object invokeStaticMthByReflect(Class target,String mthName,Class[] parasType,Object[] realPara,Object[] Return){
        String returnName ="";
        Object returnObj;
        try {
            Method mth;
            if(flg.getMyClassName().contains("overidedSubclass.my")){
                mth = target.getMethod(mthName, parasType);
            }
            else{
                mth = target.getDeclaredMethod(mthName, parasType);
            }
            Log.v("target",target.getName());

            returnObj  = mth.invoke(null, realPara);
            Log.v("执行静态方法成功","success");

            returnName = mth.getReturnType().getName();
            Log.v("return name",returnName);

            if(returnObj != null){Log.v("return",returnObj.toString());}
            if(returnName.equals("void")){return null;}

            return returnObj;
        } catch (NoSuchMethodException e) {
            Log.e("没有该方法",mthName);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {//返回值不是void,调用失败，则将返回值置为空，防止后面程序失败
            if(!returnName.equals("void")){
                Return[0] = null;
                e.printStackTrace();
            }
            invokeFailedNUm++;
        }
        return null;
    }

    public Object cloneObject(Object originalObj){

        if (originalObj instanceof Cloneable) {
            Log.v("has clone method",originalObj.getClass().getName());
            Object copyObj = null;
            //copyObjList[i] = Flag.invokeMthNoParameterByReflect(firstReferencePara[i],"clone");
            try {
                Method clone = originalObj.getClass().getMethod("clone");
                copyObj = clone.invoke(originalObj);
                return copyObj;
            } catch (NoSuchMethodException e) {
                Log.e("没有clone方法",originalObj.getClass().getName());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                Log.e("clone方法调用失败",originalObj.getClass().getName());
                e.printStackTrace();
            }
        }
        return null;
    }

   /* public Object invokeMthNonParaByReflect(Class target,String mthName,Class[] parasType,Object[] realPara){
        Object returnObj=null;
        try {
            Method mth = target.getMethod(mthName, parasType);
            returnObj  = mth.invoke(target, realPara);
            if(returnObj != null){
                return returnObj;
            }
            Log.v("执行无参数方法成功","success");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {//返回值不是void,调用失败，则将返回值置为空，防止后面程序失败
            e.printStackTrace();
            invokeFailedNUm++;
        }
        return null;
    }*/

    /**对数组中的每个元素依次克隆
     *
     * 多维数组深层克隆，如果数组类型是实现了Cloneable接口的某个类，
     * 则会调用每个元素的clone方法实现深度克隆
     *
     * 虽然数组有clone方法，但我们不能使用反射来克隆数组，因为不能使用
     * 反射来获取数组的clone方法，这个方法只能通过数组对象本身来调用，
     * 所以这里使用了动态数组创建方法来实现。
     *
     * @param objArr
     * @param cloneArr 克隆得到的数组
     * @throws Exception
     */
     private void cloneArr(Object objArr, Object cloneArr) throws Exception {
         Object objTmp;
         Object val = null;
         for (int i = 0; i < Array.getLength(objArr); i++) {
             //注，如果是非数组的基本类型，则返回的是包装类型
             objTmp = Array.get(objArr, i);

             if (objTmp == null) {val = null;}
             else if (objTmp.getClass().isArray()) {//如果是数组
                 val = Array.newInstance(objTmp.getClass().getComponentType(), Array.getLength(objTmp));
                 //如果元素是数组，则递归调用
                 cloneArr(objTmp, val);
             }
             else {//否则非数组
	                /*
	                 * 如果为基本类型或者是非Cloneable类型的引用类型，则直接对拷值 或
	                 * 者是对象的地址。没有实现Cloneable的引用类型会实行浅复制， 这对
	                 * 于像String不可变类来说是没有关系的，因为它们可以多实例或 多线程
	                 * 共享，但如果即没有实现Cloneable，又是可变以的类，浅复制 则会带来
	                 * 危险，因为这些类实例不能共享 ，一个实例里的改变会影响到 另一个实
	                 * 例。所以在使用克隆方案的时候一定要考虑可变对象的可克隆性，即需要
	                 * 实现Cloneable。
	                 *
	                 * 注，这里不能使用 objTmp.getClass.isPrimitive()来判断是元素是
	                 * 否是基本类型，因为objTmp是通过Array.get获得的，而Array.get返
	                 * 回的是Object 类型，也就是说如果是基本类型会自动转换成对应的包
	                 * 装类型后返回，所以 我们只能采用原始的类型来判断才行。
	                 */
                 if (objTmp.getClass().isPrimitive() || !(objTmp instanceof Cloneable)) {//基本类型或非Cloneable引用类型
                     val = objTmp;
                 }
                 else if (objTmp instanceof Cloneable) {//引用类型，并实现了Cloneable
	                    /*
	                     *  用反射查找clone方法，注，先使用getDeclaredMethod获取自
	                     *  己类 中所定义的方法（包括该类所声明的公共、保护、默认访问
	                     *  及私有的 方法），如果没有的话，再使用getMethod，getMethod
	                     *  只能获取公有的方法，但还包括了从父类继承过来的公有方法
	                     */
                     Method cloneMethod;
                     try {
                         //先获取自己定义的clone方法
                         cloneMethod = objTmp.getClass().getDeclaredMethod("clone", new Class[] {});
                     } catch (NoSuchMethodException e) {
                         //如果自身未定义clone方法，则从父类中找，但父类的clone一定要是public
                         cloneMethod = objTmp.getClass().getMethod("clone", new Class[] {});
                     }
                     cloneMethod.setAccessible(true);
                     val = cloneMethod.invoke(objTmp, new Object[0]);

                 }
             }
             // 设置克隆数组元素值
             Array.set(cloneArr, i, val);
         }
    }
    /**比较数组中每个元素的内容是否相等，全部相等返回true，否则返回false
     * 1.比较数组的长度
     * 2.对数组的元素一个一个比较,如果比较相等的个数等于全部的元素个数，返回true，否则返回false
     * */
    public static boolean compareArr(Object array1,Object array2){
        if(! (Array.getLength(array1) ==Array.getLength(array2)) ){return false;}
        Pair pair = computeEqualNum(array1,array2,0,0);
        if(pair.getEqualNum() == pair.getAllNum()){return true;}
        return false;
    }
    /**计算两个数组中对应元素相等的个数*/
    public static Pair<Integer> computeEqualNum(Object array1, Object array2,int equalNum, int allNumbers){
        Object objTmp1,objTmp2;
        Object val1=null,val2 = null;

        for (int i = 0; i < Array.getLength(array1); i++) {
            //注，如果是非数组的基本类型，则返回的是包装类型
            objTmp1 = Array.get(array1, i);
            objTmp2 = Array.get(array2, i);

            if (objTmp1 == null) {val1 = null;}
            else if(objTmp2 == null){val2=null;}
            else if (objTmp1.getClass().isArray() && objTmp2.getClass().isArray()) {//如果是数组
                //如果元素是数组，则递归调用
                //allNumbers += Array.getLength(objTmp1);
                Pair pair= computeEqualNum(objTmp1, objTmp2,equalNum,allNumbers);
                equalNum = (int)pair.getEqualNum();
                allNumbers = (int)pair.getAllNum();
            }
            else {//否则非数组
	                /*比较基本类型及对象*/
                allNumbers += 1;
                if (objTmp1.getClass().isPrimitive() && objTmp1.getClass().isPrimitive()) {
                    if(objTmp1 == objTmp2){ equalNum+=1; }
                }
                else {//引用类型
	                    /*用反射查找equals方法 */
                    try {
                        //先获取自己定义的clone方法
                        Method  equalsMethod = objTmp1.getClass().getDeclaredMethod("equals", Object.class);
                        Object result = equalsMethod.invoke(objTmp1, objTmp2);
                        if(result.equals(true)){ equalNum+=1;}
                    } catch (NoSuchMethodException e) {
                        //如果自身未定义clone方法，则从父类中找，但父类的clone一定要是public
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return new Pair(equalNum,allNumbers);
    }

    /**N=0的情况(无显式参数)：
     * 1.返回值不为空 或 2.存在this*/
    public void runNonParaMethod( Flag flg){
        int M = flg.getM();
        int anM = M;
        Object copyThisObj=null,thisObj = null;
        Class target;
        Object[] firstreturnObj=new Object[M];
        Object[] secondreturnObj=new Object[M];
        //静态方法没有this，非静态方法克隆this
        if(!flg.isMthIsStatic()){//对象方法
            thisObj = flg.getParaArr().get(0);
            //clone this
            if (thisObj instanceof Cloneable) {
                Log.v("has method",thisObj.getClass().getName());
                try {
                    Method clone = thisObj.getClass().getMethod("clone");
                    copyThisObj = clone.invoke(thisObj);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                Log.v("no clone method", thisObj.getClass().getName());
                return;
            }
            ////方法执行第一次
            int tep=0;
            firstreturnObj[0] = invokeMthByReflect(thisObj,flg.getMethodName(),null,null,firstreturnObj);
            if(firstreturnObj[0] != null){
                tep++;
                Log.v("first return",firstreturnObj.getClass().getName()+":"+firstreturnObj[0].toString());
            }
            if(!flg.getRetType().equals("void") && firstreturnObj[0] == null){
                anM = anM-1;
            }
            //将this放入firstreturnObj中
            firstreturnObj[tep] = thisObj;
            //方法执行第二次
            int ans = 0;
            secondreturnObj[0] = invokeMthByReflect(copyThisObj,flg.getMethodName(),null,null,secondreturnObj);

            if(secondreturnObj[0] != null){
                ans++;
                Log.v("second return",secondreturnObj.getClass().getName()+":"+secondreturnObj.toString());
            }
            if(!flg.getRetType().equals("void") && secondreturnObj == null){
                anM = anM-1;
            }
            ////将this放入secondreturnObj中
            secondreturnObj[ans] = copyThisObj;
        }
        else{//静态方法
            ////方法执行第一次
            target = Flag.forName(flg.getMyClassName());
            firstreturnObj[0] = invokeStaticMthByReflect(target,flg.getMethodName(),null,null,firstreturnObj);
            if(firstreturnObj[0] != null){
                Log.v("static first return",firstreturnObj.getClass().getName()+":"+firstreturnObj[0].toString());
            }
            if(!flg.getRetType().equals("void") && firstreturnObj[0] == null){
                anM = anM-1;
            }
            //方法执行第二次
            secondreturnObj[0] = invokeStaticMthByReflect(target,flg.getMethodName(),null,null,secondreturnObj);
            if(secondreturnObj != null){
                Log.v("static second return",secondreturnObj.getClass().getName()+":"+secondreturnObj.toString());
            }
            if(!flg.getRetType().equals("void") && secondreturnObj == null){
                anM = anM-1;
            }

        }



        //比较返回结果和this
        Log.v("返回比较结果","start");
        ArrayList<Relation> relateArr = new ArrayList();
        int i=0;
        if(anM != M) i=1; //firstReturn[0]不是方法返回值
        for(; i < M ; i++){
            Log.v("reference",firstreturnObj[i].getClass().getName()+"---"+secondreturnObj[i].getClass().getName());
            if(!firstreturnObj[i].equals(secondreturnObj[i])){
                Log.v("值改变,flag为1",firstreturnObj[i].getClass().getName());
                flg.setFlags(i,0,1);
                //Log.v("Flag",String.valueOf(flg.getFlags()[i]));
                //Relation relate = new Relation(flg.getMethodSig(),SecondRealParasWithoutImplicit[index].getClass().getName(),SecondRealParasWithoutImplicit[index]
                //      ,secondReturn[i].getClass().getName(),secondReturn[i]);//若两次执行结果不一样，则将改变的参数和第二次的返回值记录到文件。
                //relateArr.add(relate);
            }
        }
        Log.v("比较返回结果","finish");
    }

}

