package test;

import java.lang.reflect.Array;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


public class testClonedObject {
	public static void main(String[] args){
		/**测试Employee的代理类*/
		 myEmployee em = new myEmployee("mayuwan",9000,2018,1,9);
	   try{
			Method cloneMethod = em.getClass().getMethod("clone", new Class[] {});
			System.out.println(em.person.getClass());//class test.GrandChild
			Object clonedf= cloneMethod.invoke(em);
			myEmployee my = (myEmployee)clonedf;
			//System.out.println(clonedf.getClass());
			System.out.println(em.person == my.person);//false
			System.out.println(em.person.getClass());//class test.myGrandChild
			System.out.println(em.person.rand == my.person.rand );//false
			System.out.println(em.equals(my));//true
			//修改person
			em.person= new myGrandChild(10);
			System.out.println(em.equals(my));//false
			
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
