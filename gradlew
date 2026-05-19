#!/bin/sh

DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_EXEC="java"

exec "$JAVA_EXEC" -Dorg.gradle.appname=gradlew -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
