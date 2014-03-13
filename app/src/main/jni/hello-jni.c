#include <string.h>
#include <jni.h>

void
Java_specular_systems_NativeDelete_nativeDelete( JNIEnv* env,
                                                  jobject thiz,jlong x,jint bitsize)
{
    int start;
    int end = bitsize/sizeof(int);
    int* y= (int*)x;
    for(start=0;start<end;start++){
        y[start]=0;
    }
}
