#!/bin/bash
# download_libs.sh  —  Download Guice and its dependencies

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

MAVEN="https://repo1.maven.org/maven2"
LIB="lib"

echo "=== Downloading Guice 7.0.0 and dependencies into $LIB/ ==="

download() {
    local url="$1"
    local dest="$2"
    if [ -f "$LIB/$dest" ]; then
        echo "  [skip] $dest already exists"
    else
        echo "  [downloading] $dest ..."
        curl -fsSL "$url" -o "$LIB/$dest"
        echo "  [done] $dest"
    fi
}

download "$MAVEN/com/google/inject/guice/7.0.0/guice-7.0.0.jar"                           "guice-7.0.0.jar"
download "$MAVEN/javax/inject/javax.inject/1/javax.inject-1.jar"                           "javax.inject-1.jar"
download "$MAVEN/aopalliance/aopalliance/1.0/aopalliance-1.0.jar"                          "aopalliance-1.0.jar"
download "$MAVEN/com/google/guava/guava/32.1.3-jre/guava-32.1.3-jre.jar"                  "guava-32.1.3-jre.jar"
download "$MAVEN/com/google/errorprone/error_prone_annotations/2.23.0/error_prone_annotations-2.23.0.jar" "error_prone_annotations.jar"

echo ""
echo "✓ All Guice dependencies downloaded to lib/"
echo ""
echo "Now run: ./compile.sh"
