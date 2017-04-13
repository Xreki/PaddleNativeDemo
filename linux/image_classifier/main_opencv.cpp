#include <paddle/capi.h>
#include <time.h>
#include "../common/common.h"

#include <opencv2/opencv.hpp>

int read_data(std::string& image_path,
              paddle_real* data,
              size_t& height,
              size_t& width,
              size_t& channel) {
  if (data == NULL || image_path.empty()) {
    std::cerr << "invalid arguments." << std::endl;
    return -1;
  }

  FILE* fp = fopen(image_path.c_str(), "rb");
  if (fp == NULL) {
    return -1;
  }

  fseek(fp, 0L, SEEK_END);
  long size = ftell(fp);
  printf("size = %d, %d floats\n", size, size / sizeof(float));

  fseek(fp, 0L, SEEK_SET);
  fread(data, sizeof(float), height * width * channel, fp);
  fclose(fp);

  // for (int i = 0; i < size / sizeof(float); i++) {
  //   printf("%f\n", data[i]);
  // }

  return 0;
}

int read_image_pixels(std::string& image_path,
                      paddle_real* data,
                      size_t& height,
                      size_t& width,
                      size_t& channel) {
  if (data == NULL || image_path.empty()) {
    std::cerr << "invalid arguments." << std::endl;
    return -1;
  }

  cv::Mat image;
  if (1) {
    image = cv::imread(image_path, CV_LOAD_IMAGE_COLOR);
  } else {
    image = cv::imread(image_path, CV_LOAD_IMAGE_GRAYSCALE);
  }

  if (image.empty()) {
    printf("image is empty.\n");
    return -1;
  }

  width  = (size_t)image.cols;
  height = (size_t)image.rows;
  channel = (size_t)image.channels();
  printf("height = %d, width = %d, channel = %d\n", height, width, channel);
  
  float means[3] = {103.939, 116.779, 123.680};

  if (channel == 3) {
    // Store the pixels in CHW, BGR order
    size_t k = 0;
    for (size_t c = 0; c < channel; c++) {
      for (size_t y = 0; y < height; y++) {
        for (size_t x = 0; x < width; x++) {
          data[k] = (float) image.at<cv::Vec3b>(y, x)[c] - means[c];
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
        data[k] = (float) image.at<unsigned char>(y, x);
        k++;
      }
    }
  }

  return 0;
}

int virtual_image_pixels(paddle_real* data,
                         size_t& height,
                         size_t& width,
                         size_t& channel) {
  if (data == NULL) {
    std::cerr << "invalid arguments." << std::endl;
    return -1;
  }

  width = 224;
  height = 224;
  channel = 3;

  srand(time(0));
  for (size_t i = 0; i < height * width * channel; i++) {
    data[i] = (float) (i % 255);
  }

  return 0;
}

int main() {
  // Initalize Paddle
  char* argv[] = {"--use_gpu=False"};
  CHECK(paddle_init(1, (char**)argv));

  // Step 1: Reading config binary file. It is generated by `convert_protobin.sh`
  long size;
  void* buf = read_config("resnet_50/resnet_50.bin", &size);

  // Step 2: 
  //    Create a gradient machine for inference.
  paddle_gradient_machine machine;
  CHECK(paddle_gradient_machine_create_for_inference(&machine, buf, (int)size));
  CHECK(paddle_gradient_machine_randomize_param(machine));

  //    Loading parameter. Uncomment the following line and change the directory.
  CHECK(paddle_gradient_machine_load_parameter_from_disk(machine,
                                                         "resnet_50/resnet_50"));

  // Step 3: Prepare input Arguments
  paddle_arguments in_args = paddle_arguments_create_none();

  // There is only one input of this network.
  CHECK(paddle_arguments_resize(in_args, 1));

  // Create input matrix and set the value
  size_t IMAGE_HEIGHT = 224;
  size_t IMAGE_WIDTH = 224;
  size_t IMAGE_CHANNEL = 3;
  paddle_matrix mat = paddle_matrix_create(/* sample_num */ 1,
                                           /* size */ IMAGE_CHANNEL * IMAGE_HEIGHT * IMAGE_WIDTH,
                                           /* useGPU */ false);
  CHECK(paddle_arguments_set_value(in_args, 0, mat));

  printf("Get First row.\n");
  paddle_real* array;
  CHECK(paddle_matrix_get_row(mat, 0, &array));
  std::string image_path = "images/chicken_2_224x224.jpg";
  int ret = read_image_pixels(image_path,
                              array,
                              IMAGE_HEIGHT,
                              IMAGE_WIDTH,
                              IMAGE_CHANNEL);

  // std::string image_path = "images/data.bin";
  // int ret = read_data(image_path,
  //                     array,
  //                     IMAGE_HEIGHT,
  //                     IMAGE_WIDTH,
  //                     IMAGE_CHANNEL);

  printf("Do inference.\n");
  paddle_arguments out_args = paddle_arguments_create_none();
  CHECK(paddle_gradient_machine_forward(machine,
                                        in_args,
                                        out_args,
                                        /* isTrain */ false));

  // Step 5: Get the result.
  paddle_matrix prob = paddle_matrix_create_none();
  CHECK(paddle_arguments_value(out_args, 0, prob));

  uint64_t height = 0;
  uint64_t width = 0;
  CHECK(paddle_matrix_get_shape(prob, &height, &width));

  CHECK(paddle_matrix_get_row(prob, 0, &array));

  printf("Prob: %lld x %lld\n", height, width);
  int maxid = 0;
  for (int i = 0; i < 1000; i+=100) {
    for (int j = i; j < i + 100; j++) {
      // printf("%f ", array[j]);
      if (array[j] > array[maxid]) {
        maxid = j;
      }
    }
    // printf("\n");
  }
  // printf("\n");
  printf("max probs: %d - %f\n", maxid, array[maxid]);

  // Step 6: Release the resources.
  CHECK(paddle_gradient_machine_destroy(machine));
  CHECK(paddle_arguments_destroy(in_args));
  CHECK(paddle_matrix_destroy(mat));
  CHECK(paddle_arguments_destroy(out_args));
  CHECK(paddle_matrix_destroy(prob));

  return 0;
}
