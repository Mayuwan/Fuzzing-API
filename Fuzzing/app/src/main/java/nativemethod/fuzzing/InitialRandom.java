package nativemethod.fuzzing;


import java.util.HashMap;
import java.util.Random;

/**
 * Created by myw on 18-5-14.
 */

public class InitialRandom {
    public HashMap<String,String> map;
    public Random ran;

    public InitialRandom(){
        ReadConfig config = new ReadConfig();//读configure文件
        map = config.read();
        ran = new Random();
    }

    public static byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();//
//将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }
    public static char getRandomCharacter(char ch1,char ch2){
        return (char)(ch1+Math.random()*(ch2-ch1+1));//因为random<1.0，所以需要+1，才能取到ch2
    }
    public static char getRandomLowerCaseLetter(){
        return getRandomCharacter('a','z');
    }
    public static char getRandomUpperCaseLetter(){
        return getRandomCharacter('A','Z');
    }
    public static char getRandomDigitLetter(){
        return getRandomCharacter('0','9');
    }

    public String getMthSig(){
        return  map.get("mthSig");
    }
    public int getTestNum(){return Integer.valueOf(map.get("testNum")).intValue() ;}
    public int randomInt(){
        return ran.nextInt(Integer.valueOf(map.get("int")).intValue());
    }
    public short randomShort(){
        return new Integer(ran.nextInt(Short.valueOf(map.get("short")).intValue())).shortValue();
    }
    public byte randombyte(){
        return new Integer(ran.nextInt(Byte.valueOf(map.get("byte")).intValue())).byteValue();
    }
    public long randomLong(){
        Long max = new Long((String) map.get("long"));
        //if((long)max > Integer.MAX_VALUE){return ran.nextLong();}
        //else{return new Integer(ran.nextInt(max.intValue())).longValue();}
        return (long)(ran.nextDouble()*max);
    }
    public float randomFloat(){
        Integer max = new Integer( map.get("float"));
        return ran.nextFloat()*max;}
    public double randomDouble(){
        Long max = new Long((String) map.get("double"));
        return ran.nextDouble()*max;
    }
    public boolean randomBoolean(){
        return ran.nextBoolean();
    }
    public int getLenOfEachDim(){
        return new Integer(map.get("lenOfEachDim")).intValue();
    }
    public int getStringLen(){
        return new Integer((String) map.get("StringLen")).intValue();
    }

    public static char getRandomCharacter(){
        return getRandomCharacter('\u0000','\uFFFF');
    }

    public static String getRandomString(int length) {
        //随机字符串的随机字符库
        String KeyString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuffer sb = new StringBuffer();
        int len = KeyString.length();
        for (int i = 0; i < length; i++) {
            sb.append(KeyString.charAt((int) Math.round(Math.random() * (len - 1))));
        }
        return sb.toString();
    }
    public static char getRandomChar(){
        //随机字符串的随机字符库
        String KeyString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789,./;'[]=-!@#$%^&*";
        int len = KeyString.length();
        return KeyString.charAt((int) Math.round(Math.random() * (len - 1)));
    }

}
