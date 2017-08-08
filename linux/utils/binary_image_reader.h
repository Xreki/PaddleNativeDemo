#pragma once

#include <iostream>
#include "image_reader.h"

namespace utils {

template <typename T>
struct BinaryImageReader {
  bool operator()(std::string& image_path,
                  T* data,
                  size_t& height,
                  size_t& width,
                  size_t& channel) {
    if (data == NULL || image_path.empty()) {
      std::cerr << "invalid arguments." << std::endl;
      return false;
    }

    FILE* fp = fopen(image_path.c_str(), "rb");
    if (fp == NULL) {
      return false;
    }

    fseek(fp, 0L, SEEK_END);
    long size = ftell(fp);
    printf("size = %d, %d elements\n", size, size / sizeof(T));

    fseek(fp, 0L, SEEK_SET);
    fread(data, sizeof(T), height * width * channel, fp);
    fclose(fp);

    return true;
  }
};

}  // namespace utils
