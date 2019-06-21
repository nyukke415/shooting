#! /bin/bash

if [ "$1" = "-B" ]; then
  echo "compile && view"
  javac Shooting.java && appletviewer Shooting.java
else
  appletviewer Shooting.java
fi

