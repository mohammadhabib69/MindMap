#!/bin/sh
# MindMap Maven wrapper script
# Uses IntelliJ's bundled Maven 3.9 if system mvn is not in PATH,
# and ensures JAVA_HOME points to JDK 26 if available.

if [ -d "/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home"
fi

IDEA_MAVEN="/Applications/IntelliJ IDEA.app/Contents/plugins/maven-plugin/lib/maven3/bin/mvn"
if [ -x "$IDEA_MAVEN" ]; then
    exec "$IDEA_MAVEN" "$@"
else
    exec mvn "$@"
fi
