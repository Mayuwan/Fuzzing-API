package interact_nativemethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        @SuppressLint("MissingPermission") String phoneNumber = telephonyManager.getLine1Number();//source
        //Log.v("sensitive info",phoneNumber);
        Data d = new Data();
        d.str =phoneNumber;
        boolean choice = false;//false, we can design some input to decide this "choice"
        Eavesdropper ev=new Eavesdropper();

        propagateData(d, ev, choice);//choice为true，使用日志泄漏，否则使用Eavesdropper的vulnerableMethod方法

 //       leak("tag",String.valueOf(phoneNumber));//sink

    }

    public native void propagateData(Data d, Eavesdropper ev, boolean choice);
}
