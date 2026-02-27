#!/bin/bash

set -e

echo "========================================="
echo "  JudgeCore Docker Build Script"
echo "========================================="

PROJECT_VERSION=${1:-"0.0.1-SNAPSHOT"}
IMAGE_NAME=${2:-"judgecore"}
REGISTRY=${3:-""}

echo "Project Version: $PROJECT_VERSION"
echo "Image Name: $IMAGE_NAME"
echo "Registry: $REGISTRY"

echo ""
echo "[1/4] Building Java application..."
cd "$(dirname "$0")"
mvn clean package -DskipTests -Pprod

if [ ! -f "JudgeCore-app/target/JudgeCore-app-${PROJECT_VERSION}.jar" ]; then
    echo "Error: JAR file not found!"
    exit 1
fi

echo ""
echo "[2/4] Preparing Docker build context..."
mkdir -p docker-build
cp JudgeCore-app/target/JudgeCore-app-${PROJECT_VERSION}.jar docker-build/
cp Dockerfile docker-build/
cd docker-build

echo ""
echo "[3/4] Building Docker image..."
FULL_IMAGE_NAME="${REGISTRY}${IMAGE_NAME}:${PROJECT_VERSION}"
docker build -t "${IMAGE_NAME}:latest" -t "$FULL_IMAGE_NAME" .

echo ""
echo "[4/4] Cleanup..."
cd ..
rm -rf docker-build

echo ""
echo "========================================="
echo "  Build Complete!"
echo "========================================="
echo "Latest Image: ${IMAGE_NAME}:latest"
echo "Versioned Image: $FULL_IMAGE_NAME"
echo ""
echo "To run the container:"
echo "  docker run -d -p 8080:8080 -p 9000:9000 ${IMAGE_NAME}:latest"
echo ""
echo "Or use docker-compose:"
echo "  docker-compose up -d"
