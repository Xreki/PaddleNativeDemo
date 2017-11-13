/* Copyright (c) 2016 PaddlePaddle Authors. All Rights Reserve.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License */

#include <paddle/capi.h>
#include <string.h>
#include "image_utils.h"

static const char* paddle_error_string(paddle_error status) {
  switch (status) {
    case kPD_NULLPTR:
      return "nullptr error";
    case kPD_OUT_OF_RANGE:
      return "out of range error";
    case kPD_PROTOBUF_ERROR:
      return "protobuf error";
    case kPD_NOT_SUPPORTED:
      return "not supported error";
    case kPD_UNDEFINED_ERROR:
      return "undefined error";
  };
}

#define CHECK(stmt)                                            \
  do {                                                         \
    paddle_error __err__ = stmt;                               \
    if (__err__ != kPD_NO_ERROR) {                             \
      const char* str = paddle_error_string(__err__);          \
      fprintf(stderr, "%s (%d) in " #stmt "\n", str, __err__); \
      exit(__err__);                                           \
    }                                                          \
  } while (0)

static void* read_config(const char* filename, long* size) {
  FILE* file = fopen(filename, "rb");
  if (file == NULL) {
    fprintf(stderr, "Open %s error\n", filename);
    return NULL;
  }
  fseek(file, 0L, SEEK_END);
  *size = ftell(file);
  fseek(file, 0L, SEEK_SET);
  void* buf = malloc(*size);
  fread(buf, 1, *size, file);
  fclose(file);
  return buf;
}

void init_paddle() {
  // Initalize Paddle
  static bool called = false;
  if (!called) {
    char* argv[] = {const_cast<char*>("--use_gpu=False")};
    CHECK(paddle_init(1, (char**)argv));
    called = true;
  }
}

paddle_gradient_machine init(const char* merged_model_path) {
  // Step 1: Reading merged model.
  long size;
  void* buf = read_config(merged_model_path, &size);

  // Step 2:
  //    Create a gradient machine for inference.
  paddle_gradient_machine machine;
  CHECK(paddle_gradient_machine_create_for_inference_with_parameters(
      &machine, buf, size));

  free(buf);
  buf = nullptr;
  return machine;
}

void infer(paddle_gradient_machine machine,
           const float* pixels,
           const uint64_t height,
           const uint64_t width,
           const uint64_t channel,
           float*& result,
           uint64_t& result_height,
           uint64_t& result_width) {
  // Step 3: Prepare input Arguments.
  paddle_arguments in_args = paddle_arguments_create_none();

  // There is only one input of this network.
  CHECK(paddle_arguments_resize(in_args, 1));

  // Create input matrix.
  // Set the value.
  paddle_matrix mat = paddle_matrix_create(
      /* sample_num */ 1,
      /* size */ channel * height * width,
      /* useGPU */ false);
  CHECK(paddle_arguments_set_value(in_args, 0, mat));

  // Get First row.
  paddle_real* array;
  CHECK(paddle_matrix_get_row(mat, 0, &array));
  memcpy(array, pixels, channel * height * width * sizeof(float));

  // Set the frame shape.
  CHECK(paddle_arguments_set_frame_shape(in_args, 0, height, width));

  // Step 4: Do inference.
  paddle_arguments out_args = paddle_arguments_create_none();
  CHECK(paddle_gradient_machine_forward(machine,
                                        in_args,
                                        out_args,
                                        /* isTrain */ false));

  // Step 5: Get the result.
  paddle_matrix probs = paddle_matrix_create_none();
  CHECK(paddle_arguments_get_value(out_args, 0, probs));

  CHECK(paddle_matrix_get_shape(probs, &result_height, &result_width));
  CHECK(paddle_matrix_get_row(probs, 0, &result));

  // Step 6: Release the resources.
  CHECK(paddle_arguments_destroy(in_args));
  CHECK(paddle_matrix_destroy(mat));
  CHECK(paddle_arguments_destroy(out_args));
  CHECK(paddle_matrix_destroy(probs));
}

void release(paddle_gradient_machine& machine) {
  if (machine != nullptr) {
    CHECK(paddle_gradient_machine_destroy(machine));
  }
}

int main() {
  init_paddle();

  const char* merged_model_path = "models/eng_reg_96_trans.paddle";
  paddle_gradient_machine machine = init(merged_model_path);

  const uint64_t height = 48;
  const uint64_t width = 214;
  const uint64_t channel = 1;

  // Read the raw pixels from image
  float* raw_pixels = (float*)malloc(channel * height * width * sizeof(float));
  ImageReader()("images/00000_0000.jpg", raw_pixels, height, width, channel, kHWC);

  // Substract the mean value.
  float* pixels = (float*)malloc(channel * height * width * sizeof(float));
  for (uint64_t i = 0; i < channel * height * width; ++i) {
    pixels[i] = raw_pixels[i] - 127.0;
  }

  float* result = nullptr;
  uint64_t result_height = 0;
  uint64_t result_width = 0;
  infer(machine, pixels, height, width, channel, result, result_height, result_width);

  const char* labels[] = {
      "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*",
      "+", ",", "-", ".", "/", "0", "1", "2", "3", "4",
      "5", "6", "7", "8", "9", ":", ";", "<", "=", ">",
      "?", "@", "A", "B", "C", "D", "E", "F", "G", "H",
      "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
      "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\",
      "]", "^", "_", "`", "a", "b", "c", "d", "e", "f",
      "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
      "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
      "{", "|", "}", "~", " "};

  printf("Probs: %lld x %lld\n", result_height, result_width);
  uint64_t blank = result_width - 1;
  for (uint64_t i = 0; i < result_height; ++i) {
    float max_probs = 0;
    uint64_t maxid = 0;
    for (uint64_t j = 0; j < result_width; ++j) {
      if (result[i * result_width + j] > max_probs) {
        max_probs = result[i * result_width + j];
        maxid = j;
      }
    }
    if (maxid != blank) {
      printf("%s", labels[maxid]);
    }
  }
  printf("\n");

  release(machine);

  free(raw_pixels);
  raw_pixels = nullptr;
  free(pixels);
  pixels = nullptr;

  return 0;
}
