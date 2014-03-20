#include <jni.h>
#include <time.h>
#include <stdlib.h>

struct BIGNUM{unsigned long *d;int top;int dmax;int neg;int flags;};
void
Java_specular_systems_NativeDelete_nativeDelete( JNIEnv* env,
                                                  jobject thiz,jlong x,jint bitsize){
    int start,end = bitsize/(sizeof(long)*8);
    struct BIGNUM* b = x;
    unsigned long *y = b->d;
    srand(time(NULL));
    for(start=0;start<end;start++)
        y[start]=rand();
}
