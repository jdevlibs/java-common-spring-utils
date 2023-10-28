# Spring framework utilities
The utilities of spring framework library
# Setup JDK
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.15.1.jdk/Contents/Home

# Deploy
mvn clean deploy

# Release
mvn nexus-staging:release
