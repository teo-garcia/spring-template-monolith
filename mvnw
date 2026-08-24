#!/bin/sh
# Minimal mvnw shim — uses the wrapper jar when present, else delegates to system mvn
set -e
if [ -f .mvn/wrapper/maven-wrapper.jar ]; then
  exec java --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow \
    -Dmaven.multiModuleProjectDirectory="${MAVEN_PROJECTBASEDIR:-$(pwd)}" \
    -classpath .mvn/wrapper/maven-wrapper.jar \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
else
  exec mvn "$@"
fi
