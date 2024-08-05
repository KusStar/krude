build_type="release"

adb install-multiple app/build/outputs/apk/${build_type}/app-${build_type}.apk \
  scanner/build/outputs/apk/${build_type}/scanner-${build_type}.apk
