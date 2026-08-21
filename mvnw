#!/bin/sh
# Minimal mvnw shim — delegates to system mvn if wrapper jar is absent
set -e
if [ -f .mvn/wrapper/maven-wrapper.jar ]; then
  exec java -jar .mvn/wrapper/maven-wrapper.jar "$@"
else
  exec mvn "$@"
fi
