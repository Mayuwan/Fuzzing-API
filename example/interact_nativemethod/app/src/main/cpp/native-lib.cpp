#include <jni.h>
#include<android/log.h>
#define TAG "leakIMei" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型

extern "C"
JNIEXPORT void JNICALL
Java_interact_1nativemethod_MainActivity_propagateData(JNIEnv *env, jobject thisObj,
                                                       jobject data, jobject ev, jboolean choice) {
    jclass cd = env->GetObjectClass(data);
    jfieldID fd= env->GetFieldID(cd,"str","Ljava/lang/String;");
    jobject imei = env->GetObjectField(data,fd);

    if(choice == 0){//choice=0 means choice= false
        cd =env->GetObjectClass(ev);
        //将imei的值赋给ev的成员变量s
        fd = env->GetFieldID(cd,"s","Ljava/lang/String;");
        env->SetObjectField(ev,fd,imei);
        //获得ev的vulnerableMethod方法的id,并调用vulnerableMethod方法
        jmethodID md= env->GetMethodID(cd,"vulnerableMethod","()V");
        env->CallVoidMethod(ev,md);//调用ev的vulnerableMethod方法
    }
    else{
        jstring str = (jstring)imei;
        const char *imei_str = env->GetStringUTFChars(str, 0);
        //使用android/log.h打印日志
        LOGD("phoneNumber:%s",imei_str);
        env->ReleaseStringUTFChars(str, imei_str);
    }

}