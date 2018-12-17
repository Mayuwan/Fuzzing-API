package interact_nativemethod;

import android.telephony.SmsManager;

public class Eavesdropper{
    String s;
    public void vulnerableMethod(){
        SmsManager smsManager =  SmsManager.getDefault();
        smsManager.sendTextMessage("123457",null,this.s,null,null);
    }
}