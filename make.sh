#!/bin/sh

##### 宣告環境變數
export LANG=zh_TW.UTF-8
export JAVA_HOME=/opt/jvm/openjdk8

##### Patch_lib目錄所在位址
LIB_DIR=/opt/AP/Patch_lib

##### Patch_program目錄所在位址
AP_PROG=/opt/AP/Patch_program

##### AP編號
APID=95

##### java 程式名稱
JAVA_NAME=AP95.java

##### 設定classpath變數
CLASSPATH=.
CLASSPATH=$CLASSPATH:$LIB_DIR/lib/tools.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/lib/xerces.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/activation.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/HTUtil.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/jdom.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/mysql.jar
CLASSPATH=$CLASSPATH:$JAVA_NAME.jar


# [不需修改]額外使用的jar檔
CLASSPATH=$CLASSPATH$EXT_CLASSPATH

##### 編譯程式
cd $AP_PROG/$APID
/usr/bin/sudo -u root $JAVA_HOME/bin/javac -classpath $CLASSPATH *.java
