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
