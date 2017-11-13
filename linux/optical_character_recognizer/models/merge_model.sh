#!/bin/sh

MODEL_DIR=eng_reg_96_trans
CONFIG_FILE=$MODEL_DIR/sample_trainer.conf
MODEL_FILE=$MODEL_DIR.paddle
~/github/Paddle/build_paddle/dist/opt/paddle/bin/paddle_merge_model \
    --model_dir="$MODEL_DIR" \
    --config_file="$CONFIG_FILE" \
    --model_file="$MODEL_FILE"
