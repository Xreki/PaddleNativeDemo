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

#ifndef ANDROID_COMMON_H
#define ANDROID_COMMON_H

#include <android/log.h>
#include <android/asset_manager.h>

#define TAG  "PaddlePaddle"

#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG, \
        format, ##__VA_ARGS__)
#define LOGI(format, ...) __android_log_print(ANDROID_LOG_INFO, TAG, \
        format, ##__VA_ARGS__)
#define LOGW(format, ...) __android_log_print(ANDROID_LOG_WARN, TAG, \
        format, ##__VA_ARGS__)
#define LOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR, TAG, \
        "Error: " format, ##__VA_ARGS__)

#define CHECK(stmt, ret)                                                \
  {                                                                     \
    paddle_error __err__ = stmt;                                        \
    if (__err__ != kPD_NO_ERROR) {                                      \
      LOGE("invoke paddle error %d, in " #stmt, __err__);               \
      return ret;                                                       \
    }                                                                   \
  }

static void* read_config_external(const char* filename, long* size) {
  FILE* file = fopen(filename, "r");
  if (file == NULL) {
    return NULL;
  }

  fseek(file, 0L, SEEK_END);
  *size = ftell(file);
  fseek(file, 0L, SEEK_SET);

  void* buf = malloc(*size);
  if (buf == NULL) {
    return NULL;
  }

  fread(buf, 1, *size, file);

  fclose(file);
  file = NULL;

  return buf;
}

static void* read_config_asset(AAssetManager *aasset_manager,
                               const char* filename,
                               long* size) {
  if (aasset_manager != NULL) {
    AAsset *asset = AAssetManager_open(aasset_manager, filename, AASSET_MODE_UNKNOWN);

    if (asset != NULL) {
      *size = AAsset_getLength(asset);

      void *buf = (char *) malloc(*size);
      if (buf == NULL) {
        return NULL;
      }

      if (AAsset_read(asset, buf, *size) > 0) {
        return buf;
      }
    }

    AAsset_close(asset);
    asset = NULL;
  }

  return NULL;
}

static void* read_config(AAssetManager *aasset_manager,
                         const char* filename,
                         long* size) {
  void* buf = read_config_asset(aasset_manager, filename, size);
  if (buf == NULL) {
    buf = read_config_external(filename, size);
  }
  return buf;
}

#endif // ANDROID_COMMON_H