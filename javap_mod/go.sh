#! /bin/bash

if [ "$1" = "-B" ]; then
  echo "compile && view"
  javac SampleApplet.java && appletviewer SampleApplet.java
else
  appletviewer SampleApplet.java
fi

