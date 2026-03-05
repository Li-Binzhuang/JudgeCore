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
echo "[1/2] Preparing Docker build..."
cd "$(dirname "$0")"

echo ""
echo "[2/2] Building Docker image..."
FULL_IMAGE_NAME="${REGISTRY}${IMAGE_NAME}:${PROJECT_VERSION}"
docker build -t "${IMAGE_NAME}:latest" -t "$FULL_IMAGE_NAME" .

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
