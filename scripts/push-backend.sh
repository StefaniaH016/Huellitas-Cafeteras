#!/bin/bash
set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuración
REGISTRY_USERNAME=${DOCKER_REGISTRY_USERNAME}
IMAGE_NAME="${REGISTRY_USERNAME}/huellitas-backend"
TAG="latest"

# Validar que el usuario de registry esté configurado
if [ -z "$REGISTRY_USERNAME" ]; then
    echo -e "${RED}❌ Error: DOCKER_REGISTRY_USERNAME no está configurado${NC}"
    echo "Ejecuta: export DOCKER_REGISTRY_USERNAME=tu_usuario_dockerhub"
    exit 1
fi

echo -e "${YELLOW}📦 Construyendo imagen: ${IMAGE_NAME}:${TAG}${NC}"
docker build -t "${IMAGE_NAME}:${TAG}" ./huellitas-backend

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error al construir la imagen${NC}"
    exit 1
fi

echo -e "${YELLOW}🚀 Empujando a Docker Hub...${NC}"
docker push "${IMAGE_NAME}:${TAG}"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Imagen ${IMAGE_NAME}:${TAG} empujada exitosamente${NC}"
else
    echo -e "${RED}❌ Error al empujar la imagen${NC}"
    exit 1
fi
