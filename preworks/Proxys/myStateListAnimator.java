package overidedSubclass;

import android.animation.StateListAnimator;
import android.util.Log;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class myStateListAnimator extends StateListAnimator {
	public myStateListAnimator(){super();}

	private ArrayList compareFields = new ArrayList();

	@Override public boolean equals(Object other){
        if(this == other){return true;}
        if(getClass() != other.getClass()){return false;}
        myStateListAnimator otherObj = (myStateListAnimator) other;

        int equalNum=0;
        try{
            for(Field field:getClass().getFields()){
                if( Modifier.isFinal(field.getModifiers())){continue;}

                Object fieldThisValue = field.get(this);
                Object fieldOtherValue = field.get(otherObj);

                compareFields.add(fieldThisValue);
                Log.v("compare myStateListAnimator Field",Modifier.toString(field.getModifiers())+" "+field.getType().getName()+" "+field.getName());
                Log.v("compare myStateListAnimator Field",fieldThisValue+"---"+fieldOtherValue);

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