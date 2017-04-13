#!/bin/bash

python -m paddle.utils.dump_config \
          resnet_50/resnet_50.py \
          'is_test=1,use_gpu=0,is_predict=1' \
          --binary > resnet_50/resnet_50.bin

#python -m paddle.utils.dump_config \
#          resnet_50/resnet_50.py \
#          'is_test=1,use_gpu=0,is_predict=1' \
#          --whole > resnet_50.prototxt

