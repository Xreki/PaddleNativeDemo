/* Copyright (c) 2016 PaddlePaddle Authors. All Rights Reserve.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

#include <jni.h>
#include <android/asset_manager_jni.h>

#include <string.h>
#include "paddle_inference.h"

#include "common.h"
#include "android_log_wrapper.h"

extern "C" {

JNIEXPORT jlong
Java_org_paddle_demo_OpticalCharacterRecognizer_init(JNIEnv *env,
                                                     jobject thiz,
                                                     jobject jasset_manager,
                                                     jstring jmerged_model_path) {
  if (jmerged_model_path == NULL) {
    return 0;
  }

  android_start_logger();

  LOGI("Initalize Paddle environment.");
  init_paddle();

  AAssetManager *aasset_manager = AAssetManager_fromJava(env, jasset_manager);

  LOGI("Reading merged model.");
  long size = 0;
  const char *merged_model_path = env->GetStringUTFChars(jmerged_model_path, 0);
  void *merged_model = read_binary(aasset_manager, merged_model_path, &size);
  env->ReleaseStringUTFChars(jmerged_model_path, merged_model_path);

  LOGI("Create a gradient machine for inference.");
  paddle_gradient_machine gradient_machine = init(merged_model, size);

  free(merged_model);
  merged_model = NULL;

  return reinterpret_cast<jlong>(gradient_machine);
}

JNIEXPORT jfloatArray
Java_org_paddle_demo_OpticalCharacterRecognizer_inference(JNIEnv *env,
                                               jobject thiz,
                                               jlong jgradient_machine,
                                               jfloatArray jmeans,
                                               jbyteArray jpixels,
                                               jint jheight,
                                               jint jwidth,
                                               jint jchannel) {
  if (jgradient_machine == 0 || jpixels == NULL ||
      jheight != 48 || jwidth <=0 || jchannel != 1) {
    LOGE("invalid arguments, jgradient_machine = %ld, jmeans = %p, jpixels = %p, "
             "jheight = %d, jwidth = %d, jchannel = %d",
         jgradient_machine, jmeans, jpixels, jheight, jwidth, jchannel);
    return NULL;
  }

  const unsigned char* pixels = (unsigned char *) env->GetByteArrayElements(jpixels, 0);
  const float* means = (const float *) env->GetFloatArrayElements(jmeans, 0);
  float* input = (float *) malloc(jheight * jwidth * jchannel * sizeof(float));
  for (int i = 0; i < jheight * jwidth; i++) {
    input[i] = (float) pixels[i] - means[0];
  }
  env->ReleaseByteArrayElements(jpixels, (jbyte *) pixels, 0);
  env->ReleaseFloatArrayElements(jmeans, (jfloat *) means, 0);

  LOGI("Do inference.");
  paddle_gradient_machine gradient_machine =
      reinterpret_cast<paddle_gradient_machine>(jgradient_machine);
  float* result = nullptr;
  uint64_t result_height = 0;
  uint64_t result_width = 0;
  infer(gradient_machine,
        input,
        static_cast<uint64_t>(jheight),
        static_cast<uint64_t>(jwidth),
        static_cast<uint64_t>(1),
        result,
        result_height,
        result_width);

  free(input);
  input = nullptr;

  LOGI("Copy the results back to java.");
  uint64_t result_size = result_height * result_width;
  jfloatArray jresult = env->NewFloatArray(result_size);
  jfloat *jresult_ptr = env->GetFloatArrayElements(jresult, 0);
  memcpy(jresult_ptr, result, result_size * sizeof(float));
  env->ReleaseFloatArrayElements(jresult, jresult_ptr, 0);

  return jresult;
}

JNIEXPORT jint
Java_org_paddle_demo_OpticalCharacterRecognizer_release(JNIEnv *env,
                                                        jobject thiz,
                                                        jlong jgradient_machine) {
  paddle_gradient_machine gradient_machine =
      reinterpret_cast<paddle_gradient_machine>(jgradient_machine);
  release(gradient_machine);
  return 0;
}

}
