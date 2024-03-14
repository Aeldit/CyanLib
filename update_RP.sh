#!/bin/sh

# Update the translations pack automatically

LANG_SRC_DIR=src/main/resources/assets/cyanlib/lang/

if [ ! -d "$LANG_SRC_DIR" ];then
  exit 1
fi

# Copies every lang file from the sources to the Resource Pack's lang directory
cp $LANG_SRC_DIR/[a-z][a-z]_[a-z][a-z].json CyanLib_lang/assets/cyanlib/lang/
