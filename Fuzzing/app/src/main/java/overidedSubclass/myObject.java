package overidedSubclass;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class myObject extends Object implements Cloneable{

    public myObject(){super();}

    public myObject clone() throws CloneNotSupportedException{
        return (myObject) super.clone();
    }
    @Override public boolean equals(Object other){
        if(this == other){return true;}
        if(getClass() != other.getClass()){return false;}
        myObject otherObj = (myObject) other;
        int equalNum=0;int num = 0;
        for(Field f: myObject.class.getFields()){
            if( !Modifier.isFinal(f.getModifiers())){
                num += 1;
                Log.v("myObject Field",Modifier.toString(f.getModifiers())+" "+f.getType().getName()+" "+f.getName());
                try {
                    Object value1 = f.get(this);
                    Object value2 = f.get(otherObj);
                    Log.v("myObject value",value1+"---"+value2);
                    if (value1.equals(value2)){equalNum+=1;}
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if(num == equalNum){return true;}
        return false;
    }
    @Override public int hashCode(){return super.hashCode();}
}