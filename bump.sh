#!/bin/bash


BUILD_GRADLE_PATH="./app/build.gradle"

LATEST_COMMIT_HASH=$(git rev-parse --short HEAD)

TIME_STAMP=$(date +%Y%m%d%H)

sed -i"" -E "s/versionName \"[0-9.]+\"/versionName \"$TIME_STAMP.${LATEST_COMMIT_HASH}\"/" $BUILD_GRADLE_PATH

echo "Updated versionName to $1 in $BUILD_GRADLE_PATH"