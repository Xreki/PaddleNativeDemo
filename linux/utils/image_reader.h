#pragma once

#include <string>

namespace utils {

template <typename T>
struct ImageReader {
  virtual bool operator()(std::string& image_path,
                          T* data,
                          size_t& height,
                          size_t& width,
                          size_t& channel);
};

}  // namespace utils
