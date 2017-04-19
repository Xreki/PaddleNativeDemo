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

#include <iostream>
#include <paddle/capi.h>

#include "../../../../utils/cpp/common.h"
#include "android_log_wrapper.h"

extern "C" {

JNIEXPORT jlong
Java_org_paddle_demo_OpticalCharacterRecognizer_init(JNIEnv *env,
                                                     jobject thiz,
                                                     jobject jasset_manager,
                                                     jstring jconfig_path,
                                                     jstring jparams_path) {
  if (jconfig_path == NULL || jparams_path == NULL) {
    return 0;
  }

  start_logger();

  LOGI("Initalize Paddle environment.");
  char *argv[] = {"--use_gpu=False"};
  CHECK(paddle_init(1, (char **) argv), 0);

  AAssetManager *aasset_manager = AAssetManager_fromJava(env, jasset_manager);

  LOGI("Reading config binary file. It is generated by `convert_protobin.sh`.");
  long size = 0;
  const char *config_path = env->GetStringUTFChars(jconfig_path, 0);
  void *config = read_binary(aasset_manager, config_path, &size);
  env->ReleaseStringUTFChars(jconfig_path, config_path);

  LOGI("Create a gradient machine for inference.");
  paddle_gradient_machine gradient_machine = 0;
  CHECK(paddle_gradient_machine_create_for_inference(&gradient_machine, config, (int) size), 0);

  LOGI("Loading parameter.");
  const char *params_path = env->GetStringUTFChars(jparams_path, 0);
  // CHECK(paddle_gradient_machine_load_parameter_from_disk(gradient_machine, params_path),
  //       0);
  char *params = static_cast<char *>(read_binary(aasset_manager, params_path, &size));
  long numFiles = 0;
  memcpy(&numFiles, params, sizeof(long));
  long position = sizeof(long);
  char a = params[position];
  char b = params[position+1];
  char c = params[position+2];
  LOGI("here: %s", params + position);
  LOGI("here: %d %d %d", int(a), int(b), int(c));
  long s = 0;
  memcpy(&s, params + 30, sizeof(long));
  LOGI("s: %ld", s);

  CHECK(paddle_gradient_machine_load_parameter_from_buffer(gradient_machine, params, size),
        0);
  env->ReleaseStringUTFChars(jparams_path, params_path);

  return reinterpret_cast<jlong>(gradient_machine);
}

JNIEXPORT jintArray
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

  LOGI("Prepare input Arguments, there is only one input of this network.");
  paddle_arguments in_args = paddle_arguments_create_none();
  CHECK(paddle_arguments_resize(in_args, 1), NULL);

  LOGI("Create input matrix.");
  paddle_matrix mat = paddle_matrix_create(/* sample_num */ 1,
                                           /* size */ jheight * jwidth,
                                           /* useGpu */ false);
  CHECK(paddle_arguments_set_value(in_args, 0, mat), NULL);

  LOGI("Get the first row and set the value.");
  paddle_real *input = NULL;
  CHECK(paddle_matrix_get_row(mat, 0, &input), NULL);

  const unsigned char* pixels = (unsigned char *) env->GetByteArrayElements(jpixels, 0);
  const float* means = (const float *) env->GetFloatArrayElements(jmeans, 0);
  for (int i = 0; i < jheight * jwidth; i++) {
    input[i] = (float) pixels[i] - means[0];
  }
  env->ReleaseByteArrayElements(jpixels, (jbyte *) pixels, 0);
  env->ReleaseFloatArrayElements(jmeans, (jfloat *) means, 0);

  LOGI("Do inference.");
  paddle_gradient_machine gradient_machine =
      reinterpret_cast<paddle_gradient_machine>(jgradient_machine);
  paddle_arguments out_args = paddle_arguments_create_none();
  CHECK(paddle_gradient_machine_forward(gradient_machine,
                                        in_args,
                                        out_args,
                                        /* isTrain */ false),
        NULL);

  LOGI("Get the result.");
  paddle_ivector ids = paddle_ivector_create_none();
  CHECK(paddle_arguments_ids(out_args, 0, ids), NULL);

  uint64_t size = 0;
  CHECK(paddle_ivector_get_size(ids, &size), NULL);

  int* labels = NULL;
  CHECK(paddle_ivector_get(ids, &labels), NULL);

  LOGI("Copy the results back to java.");
  jintArray jresults = env->NewIntArray(size);
  jint *jresults_ptr = env->GetIntArrayElements(jresults, 0);
  memcpy(jresults_ptr, labels, size * sizeof(int));
  env->ReleaseIntArrayElements(jresults, jresults_ptr, 0);

  CHECK(paddle_arguments_destroy(in_args), NULL);
  CHECK(paddle_matrix_destroy(mat), NULL);
  CHECK(paddle_arguments_destroy(out_args), NULL);
  CHECK(paddle_ivector_destroy(ids), NULL);

  return jresults;
}

JNIEXPORT jint
Java_org_paddle_demo_OpticalCharacterRecognizer_release(JNIEnv *env,
                                                        jobject thiz,
                                                        jlong jgradient_machine) {
  paddle_gradient_machine gradient_machine =
      reinterpret_cast<paddle_gradient_machine>(jgradient_machine);
  CHECK(paddle_gradient_machine_destroy(gradient_machine), -1);
  return 0;
}

}