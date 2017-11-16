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

#include "image_utils.h"
#include "paddle_inference.h"

int main() {
  init_paddle(false);

  const char* merged_model_path = "models/resnet_50.paddle";
  paddle_gradient_machine machine = init(merged_model_path);

  const uint64_t height = 224;
  const uint64_t width = 224;
  const uint64_t channel = 3;

  // Read the raw pixels from image
  const char* image_path = "images/chicken_224x224.jpg";
  float* raw_pixels = (float*)malloc(channel * height * width * sizeof(float));
  ImageReader()(image_path, raw_pixels, height, width, channel, kCHW);

  // Substract the mean value.
  std::vector<float> means({103.939, 116.779, 123.680});
  float* pixels = (float*)malloc(channel * height * width * sizeof(float));
  for (uint64_t c = 0; c < channel; ++c) {
    for (uint64_t hw = 0; hw < height * width; ++hw) {
      pixels[c * height * width + hw] = raw_pixels[c * height * width + hw] - means[c];
    }
  }

  float* result = nullptr;
  uint64_t result_height = 0;
  uint64_t result_width = 0;
  infer(machine, pixels, height, width, channel, result, result_height, result_width);

  printf("Prob: %lld x %lld\n", result_height, result_width);
  for (uint64_t i = 0; i < result_height; i++) {
    int maxid = 0;
    for (uint64_t j = 0; j < result_width; j++) {
      if (result[i * width + j] > result[i * result_width + maxid]) {
        maxid = j;
      }
    }
    printf("max probs of %d-th: %d - %f\n", i, maxid, result[i * width + maxid]);
  }
  printf("\n");

  release(machine);

  free(raw_pixels);
  raw_pixels = nullptr;
  free(pixels);
  pixels = nullptr;

  return 0;
}
