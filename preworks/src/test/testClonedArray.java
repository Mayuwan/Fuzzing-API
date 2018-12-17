package test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.*;

public class testClonedArray {
	
	/*比较数组的测试用例
	 * 测试DeepClone的 cloneArr方法和compareArr方法
	 **/
	public static void main(String[] args){
		/**一维数组的克隆和比较*/
		/**使用java库类：integer*/
		/**使用java 1.7 提供的数组克隆方法，Integer的clone方法为根类Object的clone方法*/
		Integer[] test={1,2,3,4,5,6};
        Integer[] b=test.clone();
        //System.out.println(test==b);//false
        System.out.println(test.equals(b));//false
        b[0]=10;
        System.out.println("test:"+b[0]+"  "+test[0]);//10 1
        System.out.println(test.equals(b)); System.out.println();//false
        
        
        /**使用反射对数组的元素逐个克隆，然后逐个比较*/
        Object cloned = Array.newInstance(test.getClass().getComponentType(),  Array.getLength(test));
        try{
        	DeepClone.cloneArr(test,cloned);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("cloned: "+cloned.getClass().getName());//[Ljava.lang.Integer;
        System.out.println("比较修改前的数组: "+DeepClone.compareArr(test,cloned));//true
        test[0]=14;
        System.out.println("比较修改后的数组: "+DeepClone.compareArr(test,cloned));//false
        System.out.println();
        
        /**基本类型：int*/
        /**使用java 1.7 提供的数组克隆方法*/
        int[]  primitiveArray= {10,11,12,13,14,15};
        int[] clonedPrimitive = primitiveArray.clone();
        System.out.println(primitiveArray==clonedPrimitive);//false
        System.out.println("equals:"+clonedPrimitive.equals(primitiveArray));//false
        clonedPrimitive[0] = 100;
        System.out.println("test:"+primitiveArray[0]+"  "+clonedPrimitive[0]);//10 100
        System.out.println("equals:"+clonedPrimitive.equals(primitiveArray));//false
        System.out.println();
        
        /**使用反射对数组的元素逐个克隆，然后逐个比较*/
        Object clonedPrimitiveObj = Array.newInstance(primitiveArray.getClass().getComponentType(),  Array.getLength(primitiveArray));
        try{
        	DeepClone.cloneArr(primitiveArray,clonedPrimitiveObj);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("clonedPrimitiveObj: "+clonedPrimitiveObj.getClass().getName());//[I
        System.out.println("比较修改前的数组: "+DeepClone.compareArr(clonedPrimitiveObj,primitiveArray));//true
        primitiveArray[0]=14;
        System.out.println("比较修改后的数组: "+DeepClone.compareArr(clonedPrimitiveObj,primitiveArray));//false
        System.out.println();
        
        /**自定义类：Employee，有重写clone方法*/
        /**使用java 1.7 提供的数组克隆方法*/
        Employee[] employees = new Employee[5];
        Arrays.fill(employees, new Employee("mayuwan",134.1,2018,10,12));
        Employee[] clonedEmployees = employees.clone();
        System.out.println(employees==clonedEmployees);//false
        System.out.println("equals:"+employees.equals(clonedEmployees));//false
        employees[0] = new Employee("mayuwan",100000.1,2018,11,25);
        System.out.println("test:"+employees[0]+"  "+clonedEmployees[0]);//test.Employee[name=mayuwan,salary=100000.1,hireDay=Sun Nov 25 00:00:00 CST 2018]  test.Employee[name=mayuwan,salary=134.1,hireDay=Fri Oct 12 00:00:00 CST 2018]
        System.out.println("equals:"+employees.equals(clonedEmployees));//false
        System.out.println();
        
        /**使用反射对数组的元素逐个克隆，然后逐个比较*/
        Object clonedEmployeess = Array.newInstance(employees.getClass().getComponentType(),  Array.getLength(employees));
        try{
        	DeepClone.cloneArr(employees,clonedEmployeess);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("clonedEmployeess: "+clonedEmployeess.getClass().getName());//[Ltest.Employee;
        System.out.println("比较修改前的数组: "+DeepClone.compareArr(employees,clonedEmployeess));//true
        employees[0]=new Employee("liweiwei",100000.1,2018,10,12);
        System.out.println("比较修改后的数组: "+DeepClone.compareArr(employees,clonedEmployeess));//false
        System.out.println();
        
        
        /**二维数组的克隆和比较*/
        /**自定义类：Employee，有重写clone方法*/
        /**初始化employees[][]*/
        Employee[][] employees2 = new Employee[3][3];
        for(int i=0;i<employees2.length;i++){
        	for(int j=0;j<employees2[i].length;j++){
        		employees2[i][j]=  new Employee("liweiwei",100000.1,2018,10,12);
        	}
        }
        /**使用java 1.7 提供的数组克隆方法*/
        Employee[][] clonedEmployees2 = employees2.clone();
        System.out.println(employees2==clonedEmployees2);//false
        System.out.println("equals:"+employees2.equals(clonedEmployees2));//false
        employees2[0][0].setDate(new GregorianCalendar(1994,5,14).getTime());
        System.out.println("test:"+employees2[0]+"  "+clonedEmployees2[0]);//10 100
        System.out.println("equals:"+employees2.equals(clonedEmployees2));//false
        System.out.println();
     
        /**使用反射对数组的元素逐个克隆，然后逐个比较*/
        Object cloneArr = Array.newInstance(employees2.getClass().getComponentType(),   Array.getLength(employees2));
        try{
        	DeepClone.cloneArr(employees2,cloneArr);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("cloneArr: "+cloneArr.getClass().getName());//[[Ltest.Employee;
        System.out.println("比较修改前的数组: "+DeepClone.compareArr(employees2,cloneArr));//true
        employees2[0][0]=new Employee("mayuwan",100000.1,2018,11,25);
        System.out.println("比较修改后的数组: "+DeepClone.compareArr(employees2,cloneArr));//false
        System.out.println();
        
        
  
	}
}
