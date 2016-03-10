//
// Created by 东东 on 16/3/7.
//
#include "com_dd_jni_common_StringChild.h"
#include "stdio.h"
#include "android/log.h"
#include "string.h"
#include <pthread.h>
#include <unistd.h>


#define TAG "JNI[StringChild]"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)

jfieldID TAG_id;
jfieldID count_id;
jfieldID str_id;
jmethodID echo_id;
jmethodID setArgs_id;
jmethodID thowed_id;
jmethodID nativeCallBack_id;
jclass callBackClazz;

JavaVM* gJVM;
int thread_id;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    gJVM = vm;
    LOGE("onLoad");
    return JNI_VERSION_1_4;
}


JNIEXPORT jint JNICALL Java_com_dd_jni_common_StringChild_print
        (JNIEnv *env, jobject obj, jstring jstr) {
    const char *buf = (*env)->GetStringUTFChars(env, jstr, JNI_FALSE);
    if(buf == NULL)
        return -1;
    LOGE("buf:%s", buf);
    (*env)->ReleaseStringUTFChars(env, jstr, buf);
    return 0;
}

JNIEXPORT void JNICALL Java_com_dd_jni_common_StringChild_initNative
        (JNIEnv *env, jobject obj) {
    jclass jclazz = (*env)->FindClass(env, "com/dd/jni/common/StringChild");
    if(jclazz != NULL) {
        TAG_id = (*env)->GetStaticFieldID(env, jclazz, "TAG", "Ljava/lang/String;");
        count_id = (*env)->GetFieldID(env, jclazz, "count", "I");
        str_id = (*env)->GetFieldID(env, jclazz, "str", "Ljava/lang/String;");
        echo_id = (*env)->GetMethodID(env, jclazz, "echo", "()V");
        setArgs_id = (*env)->GetMethodID(env, jclazz, "setArgs", "(ILjava/lang/String;F)V");
        thowed_id = (*env)->GetMethodID(env, jclazz, "throwd", "()V");


        callBackClazz = (*env)->FindClass(env, "com/dd/jni/common/StringCallBack");
        LOGE("dddddd3");
        nativeCallBack_id = (*env)->GetStaticMethodID(env, callBackClazz, "nativeCallBack", "(I)V");
        LOGE("dddddd2");
    }
}

jstring CharTojstring(JNIEnv* env, char* str)  {
    jsize len = strlen(str);

    jclass clsstring = (*env)->FindClass(env, "java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env,"GB2312");
    jmethodID mid = (*env)->GetMethodID(env,clsstring, "<init>", "([BLjava/lang/String;)V");
    jbyteArray barr = (*env)-> NewByteArray(env,len);
    (*env)->SetByteArrayRegion(env, barr, 0, len, (jbyte*)str);
    return (jstring)(*env)-> NewObject(env,clsstring,mid,barr,strencode);
}

JNIEXPORT jint JNICALL Java_com_dd_jni_common_StringChild_getIntField
        (JNIEnv *env, jobject obj, jstring jstr) {
    const char *attr = (*env)->GetStringUTFChars(env, jstr, JNI_FALSE);
    if(!strcmp(attr, "count")) {
        (*env)->ReleaseStringUTFChars(env, jstr, attr);
        jint countV = (*env)->GetIntField(env, obj, count_id);
        return countV+5;
    }
    (*env)->ReleaseStringUTFChars(env, jstr, attr);
    return -1;
}


JNIEXPORT void JNICALL Java_com_dd_jni_common_StringChild_setStringFiled
        (JNIEnv *env, jobject obj, jstring jstr) {
    const char *attr = (*env)->GetStringUTFChars(env, jstr, JNI_FALSE);
    if(!strcmp(attr, "str")) {
         char *buf = "Hello JNI";
        jstring jBuf = CharTojstring(env, buf);
        (*env)->SetObjectField(env, obj, str_id, jBuf);
    }
    (*env)->ReleaseStringUTFChars(env, jstr, attr);
}

JNIEXPORT void JNICALL Java_com_dd_jni_common_StringChild_callJavaMethod
        (JNIEnv *env, jobject obj, jstring jstr) {
    const char *method = (*env)->GetStringUTFChars(env, jstr, JNI_FALSE);
    if(!strcmp(method, "echo")) {
        (*env)->CallVoidMethod(env, obj, echo_id);
    }else if(!strcmp(method, "setArgs")) {
        jint count = 2;
        jstring st = CharTojstring(env, "call Set Args"); //don't like this:jstring st = "call Set Args"
        jfloat num = 4.3f;
        (*env)->CallVoidMethod(env, obj, setArgs_id, count, st, num);
    }else if(!strcmp(method, "Fecho")) {
        jclass  clazz_father = (*env)->FindClass(env, "com/dd/jni/common/StringFather");
        jmethodID echo_father_id = (*env)->GetMethodID(env, clazz_father, "echo", "()V");
        (*env)->CallNonvirtualVoidMethod(env, obj, clazz_father, echo_father_id);
    }else if(!strcmp(method, "thowd")) {
        (*env)->CallVoidMethod(env, obj, thowed_id);
        if( (*env)->ExceptionOccurred(env)) {
            (*env)->ExceptionClear(env);

            jclass clazz = (*env)->FindClass(env, "java/lang/NullPointerException");
            (*env)->ThrowNew(env, clazz, "Null PointerException from c");
        }

    }
    (*env)->ReleaseStringUTFChars(env, jstr, method);
}

JNIEXPORT void JNICALL Java_com_dd_jni_common_StringChild_NewJavaObject
        (JNIEnv *env, jobject obj, jstring jstr) {
    const char *class = (*env)->GetStringUTFChars(env, jstr, JNI_FALSE);
    if(!strcmp(class, "StringChild")) {
        jclass stringChild_class = (*env)->FindClass(env, "com/dd/jni/common/StringChild");
        jmethodID stringChildInit_id = (*env)->GetMethodID(env, stringChild_class, "<init>", "()V");
        jobject stringChild = (*env)->NewObject(env,stringChild_class, stringChildInit_id);

        //call toString
        jmethodID toString_id = (*env)->GetMethodID(env, stringChild_class, "toString", "()Ljava/lang/String;");
        jstring jstr = (*env)->CallObjectMethod(env, stringChild, toString_id);
        const char *str = (*env)->GetStringUTFChars(env, jstr, JNI_FALSE);
        LOGE("toString:%s", str);
        (*env)->ReleaseStringUTFChars(env, jstr, str);
    }
    (*env)->ReleaseStringUTFChars(env, jstr, class);
}

JNIEXPORT jbyteArray JNICALL Java_com_dd_jni_common_StringChild_CallBasisArray
        (JNIEnv *env, jobject obj, jstring jtype) {
    const char *type = (*env)->GetStringUTFChars(env, jtype, JNI_FALSE);
    if(!strcmp(type, "new Basic")) {
        (*env)->ReleaseStringUTFChars(env, jtype, type);
        char *c = "1234";
        jbyteArray byteArray = (*env)->NewByteArray(env, 4);
        LOGE("%s[%d]", __FUNCTION__, __LINE__);
        (*env)->SetByteArrayRegion(env, byteArray, 0, strlen(c), c);
        LOGE("%s[%d]", __FUNCTION__, __LINE__);
        return byteArray;
    }
    (*env)->ReleaseStringUTFChars(env, jtype, type);
}

JNIEXPORT void JNICALL Java_com_dd_jni_common_StringChild_CallObjectArray
        (JNIEnv *env, jobject obj, jstring jtype, jobjectArray array) {
    const char *type = (*env)->GetStringUTFChars(env, jtype, JNI_FALSE);
    if(!strcmp(type, "Object")) {
        int row = (*env)->GetArrayLength(env, array);
        char const *jniData[row];
        int i = 0;
        for (i  = 0; i < row; ++i) {
            jniData[i] = (*env)->GetStringUTFChars(env, (*env)->GetObjectArrayElement(env, array, i), JNI_FALSE);
            LOGE("i:%d, value:%s", i, jniData[i]);
        }
        (*env)->SetObjectArrayElement(env, array, row/2, CharTojstring(env, "AA"));
    }
    (*env)->ReleaseStringUTFChars(env, jtype, type);
}


int value = 100;
JNIEXPORT void JNICALL Java_com_dd_jni_common_StringChild_nativeAddOne
        (JNIEnv *env, jobject obj) {
    value++;
}

JNIEXPORT void JNICALL Java_com_dd_jni_common_StringChild_nativeMinusOne
        (JNIEnv *env, jobject obj) {
    value--;
}

JNIEXPORT jint JNICALL Java_com_dd_jni_common_StringChild_getNativeValue
        (JNIEnv *env, jobject obj) {
    return value;
}


void *thread_fun(void *arg) {
    int time = 60;
    JNIEnv *env;
    (*gJVM)->AttachCurrentThread(gJVM, &env, NULL);


    while (time--) {
        LOGE("current time:%d", time);
        (*env)->CallStaticVoidMethod(env, callBackClazz, nativeCallBack_id, time);
        sleep(1);
    }
    (*gJVM)->DetachCurrentThread(gJVM);
}

JNIEXPORT void JNICALL Java_com_dd_jni_common_StringChild_createNativeThread
        (JNIEnv *env, jobject obj) {

    pthread_create(&thread_id, NULL, thread_fun, NULL);
}

