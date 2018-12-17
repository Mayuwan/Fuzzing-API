package overidedSubclass;

import android.content.Intent;
import android.util.Log;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class myIntent extends Intent {
	public myIntent(){super();}
	public myIntent(android.content.Intent SmYYooC2o8){super(SmYYooC2o8);}
	public myIntent(java.lang.String WzY0qRcKBg){super(WzY0qRcKBg);}
	public myIntent(java.lang.String Nh8QBvoY5M, android.net.Uri YGzgxVFLYl){super(Nh8QBvoY5M, YGzgxVFLYl);}
	public myIntent(android.content.Context J05zI9jZqR, java.lang.Class JSs3eDQ6do){super(J05zI9jZqR, JSs3eDQ6do);}
	public myIntent(java.lang.String SzZabIJ70h, android.net.Uri PYuEkHF1xz, android.content.Context KDAU0LiJ1o, java.lang.Class QKjiGXMkWH){super(SzZabIJ70h, PYuEkHF1xz, KDAU0LiJ1o, QKjiGXMkWH);}

	private ArrayList compareFields = new ArrayList();

	@Override public boolean equals(Object other){
        if(this == other){return true;}
        if(getClass() != other.getClass()){return false;}
        myIntent otherObj = (myIntent) other;

        int equalNum=0;
        try{
            for(Field field:getClass().getFields()){
                if( Modifier.isFinal(field.getModifiers())){continue;}

                Object fieldThisValue = field.get(this);
                Object fieldOtherValue = field.get(otherObj);

                compareFields.add(fieldThisValue);
                Log.v("compare myIntent Field",Modifier.toString(field.getModifiers())+" "+field.getType().getName()+" "+field.getName());
                Log.v("compare myIntent Field",fieldThisValue+"---"+fieldOtherValue);

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