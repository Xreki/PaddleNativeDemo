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

#include "paddle_inference.h"

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

  // Set the first row.
  CHECK(paddle_matrix_set_row(mat, 0, const_cast<paddle_real*>(pixels)));

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

  paddle_error err = paddle_matrix_get_row(probs, 0, &result);
  if (err == kPD_NO_ERROR) {
    CHECK(paddle_matrix_get_shape(probs, &result_height, &result_width));
  }

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
