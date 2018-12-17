package nativemethod.fuzzing;


import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import static android.util.Log.v;

/**
 * Created by myw on 18-5-21.
 */

public class ReadConfig {
    private  Context mcontext;

    public ReadConfig(){
        mcontext = MyApplication.getContext();
    }

    public  HashMap read(){
        HashMap<String,String> map = new HashMap();

        BufferedReader reader = null;
        try {
            InputStream in = mcontext.getResources().openRawResource(R.raw.configure);
            reader = new BufferedReader(new InputStreamReader(in));
            String tempString = null;
            int line = 0;
            while ((tempString = reader.readLine()) != null) {
                String[] tempStr = tempString.split("=");
                v("item",tempStr[0]+":"+tempStr[1]);
                map.put(tempStr[0],tempStr[1]);
                line++;
            }
            reader.close();
            Log.v("line",String.valueOf(line));
        } catch (FileNotFoundException e) {
            Log.e("无法找到文件",e.getMessage());
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e1) {
                }
            }
        }
        return map;
    }


}
