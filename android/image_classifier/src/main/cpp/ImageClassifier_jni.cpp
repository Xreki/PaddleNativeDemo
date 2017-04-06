#include <jni.h>
#include <string>
#include <paddle/capi.h>

extern "C"
jstring
Java_org_paddle_demo_ImageClassifierActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from Paddle";
    return env->NewStringUTF(hello.c_str());
}
