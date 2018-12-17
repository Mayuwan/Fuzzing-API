package nativemethod.fuzzing;

import android.content.Context;

import org.json.JSONArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by myw on 18-6-12.
 */

public class SaveFile {
    private Context mContext;
    private String fileName;

    public SaveFile(String s){
        mContext = MyApplication.getContext();
        fileName = s;
    }
    public void save(ArrayList<Relation> arr){
        JSONArray JsonArr = new JSONArray();
        for (Relation re:arr) JsonArr.put(re.toJson());

        //write the file to disk
        Writer writer = null;
        try {
            OutputStream out = mContext.openFileOutput(fileName,Context.MODE_APPEND);
            writer = new OutputStreamWriter(out);
            writer.write(JsonArr.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
