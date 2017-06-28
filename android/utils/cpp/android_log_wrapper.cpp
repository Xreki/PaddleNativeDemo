//
// Created by liuyiqun01 on 2017/4/19.
//

#include "android_log_wrapper.h"
#include <stdio.h>
#include <unistd.h>
#include <pthread.h>
#include <android/log.h>

static int pfd[2];
static pthread_t thread;
static const char* TAG = "PaddlePaddle";

static void* thread_func(void *) {
  size_t rdsz;
  char buf[256];
  while ((rdsz = read(pfd[0], buf, sizeof(buf) - 1)) > 0) {
    if (buf[rdsz - 1] == '\n') {
      rdsz--;
    }
    buf[rdsz] = 0; // add null-terminator
    __android_log_write(ANDROID_LOG_DEBUG, TAG, buf);
  }
  return 0;
}

int android_start_logger() {
  /* _IOLBF: line-buffered
   * _IONBF: unbuffered
   * Make sure both stdout and stderr line-buffered
   */
  setvbuf(stdout, 0, _IOLBF, 0);
  setvbuf(stderr, 0, _IOLBF, 0);

  /* Create the pipe and redirect stdout and stderr */
  pipe(pfd);
  dup2(pfd[1], STDOUT_FILENO);
  dup2(pfd[1], STDERR_FILENO);

  /* Spawn the logging thread */
  if (pthread_create(&thread, 0, thread_func, 0) == -1) {
    return -1;
  }
  pthread_detach(thread);
  return 0;
}
