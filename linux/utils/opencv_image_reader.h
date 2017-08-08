#pragma once

#include <vector>
#include <opencv2/opencv.hpp>
#include "image_reader.h"

namespace utils {

template <typename T>
struct OpencvImageReader {
  OpencvImageReader(const std::vector<T>& means) : means_(means) {}

  bool operator()(std::string& image_path,
                  T* data,
                  size_t& height,
                  size_t& width,
                  size_t& channel) {
    if (data == NULL || image_path.empty() || channel != means_.size()) {
      std::cerr << "invalid arguments." << std::endl;
      return false;
    }

    cv::Mat image;
    if (1) {
      image = cv::imread(image_path, CV_LOAD_IMAGE_COLOR);
    } else {
      image = cv::imread(image_path, CV_LOAD_IMAGE_GRAYSCALE);
    }

    if (image.empty()) {
      printf("image is empty.\n");
      return false;
    }

    width  = (size_t)image.cols;
    height = (size_t)image.rows;
    channel = (size_t)image.channels();
    printf("height = %d, width = %d, channel = %d\n", height, width, channel);

    // float means[3] = {103.939, 116.779, 123.680};

    if (channel == 3) {
      // Store the pixels in CHW, BGR order
      size_t k = 0;
      for (size_t c = 0; c < channel; c++) {
        for (size_t y = 0; y < height; y++) {
          for (size_t x = 0; x < width; x++) {
            data[k] = static_cast<float>(image.at<cv::Vec3b>(y, x)[c]) - means_[c];
            // printf("(%d, %d, %d): %f, %f\n", c, y, x,
            //        (float) image.at<cv::Vec3b>(y, x)[c],
            //        data[k]);
            k++;
          }
        }
      }
    } else /* gray-scale */ {
      size_t k = 0;
      for (size_t y = 0; y < height; y++) {
        for (size_t x = 0; x < width; x++) {
          data[k] = static_cast<T>(image.at<unsigned char>(y, x));
          k++;
        }
      }
    }

    return 0;
  }

private:
  std::vector<T> means_;
};

}  // namespace utils
