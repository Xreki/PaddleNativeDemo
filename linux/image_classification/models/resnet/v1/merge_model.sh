#!/bin/sh

PADDLE_ROOT=/home/liuyiqun01/github/Paddle/build_paddle/dist/opt/paddle

export is_test=True
export is_predict=True
export layer_num=50

MODEL_DIR=resnet_$layer_num
CONFIG_FILE=resnet.py
MODEL_FILE=../../resnet_$layer_num.paddle
$PADDLE_ROOT/bin/paddle_merge_model \
    --use_gpu=0 \
    --model_dir="$MODEL_DIR" \
    --config_file="$CONFIG_FILE" \
    --model_file="$MODEL_FILE"
