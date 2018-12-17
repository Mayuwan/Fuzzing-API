package test;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.*;


class Father{
	public int[] ints ;
	//public Object obj;
	public Random rand;
	public Father(){
		ints = new int[] {1,2,3,4,5};
		rand= new Random();
		//Arrays.fill(ints, rand.nextInt(10));
	}
}
class Child extends Father{
	public int id;
	//public Father superObj;
	public Child(int id){
		super();
		//this.id = rand.nextInt(10);
		this.id= id;
		}
}
class GrandChild extends Child{
	public GrandChild(int id){
		super(id);
		}
}  
public class Employee{
	   public String name;//不可变对象类型
	  //private Manager manager;
	   public int[] intArr = new int[] {9,8,7};  //数组引用类型
	   public GrandChild person ;//对象引用类型
	   public double salary;//基本类型
	
	   
	   private  Date hireDay;

	   public Employee(String n, double s, int year, int month, int day)
	   {
	      name = n;
	      salary = s;
	      GregorianCalendar calendar = new GregorianCalendar(year, month - 1, day);
	      hireDay = calendar.getTime();
	      person = new GrandChild(1);
	      //manager = new Manager(124);
	   }
	  /* public String getManger()
	   {
	      return manager.toString();
	   }*/

	  
	   
	   public Employee(String n, double s, Date date)
	   {
	      name = n;
	      salary = s;
	      hireDay = date;
	   }
	   public void setDate(Date date){
		   hireDay = date;
	   }
	   public String getName()
	   {
	      return name;
	   }

	   public double getSalary()
	   {
	      return salary;
	   }

	   public Date getHireDay()
	   {
	      return hireDay;
	   }

	   public void raiseSalary(double byPercent)
	   {
	      double raise = salary * byPercent / 100;
	      salary += raise;
	   }
	   
	   public boolean equals(Object otherObject)
	   {
	      // a quick test to see if the objects are identical
	      if (this == otherObject) return true;

	      // must return false if the explicit parameter is null
	      if (otherObject == null) return false;

	      // if the classes don't match, they can't be equal
	      if (getClass() != otherObject.getClass()) return false;

	      // now we know otherObject is a non-null Employee
	      Employee other = (Employee) otherObject;

	      // test whether the fields have identical values  字段是否有相同值
	      return Objects.equals(name, other.name) && salary == other.salary && Objects.equals(hireDay, other.hireDay);
	   }

	   public int hashCode()
	   {
	      return Objects.hash(name, salary, hireDay); 
	   }

	   public String toString()
	   {
	      return getClass().getName() + "[name=" + name + ",salary=" + salary + ",hireDay=" + hireDay
	            + "]";
	   }
	   public static void main(String[] args){
		   Employee e = new Employee("mayuwan",214356,2018,10,23);
		   System.out.println(e.getHireDay());
		   e.setDate(new GregorianCalendar(2000,10,28).getTime());
		   System.out.println(e.getHireDay());
	   
	   }
}
