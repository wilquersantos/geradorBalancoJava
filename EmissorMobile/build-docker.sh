#!/bin/bash

echo "========================================"
echo "Emissor Mobile - Build via Docker"
echo "========================================"
echo ""

# Verificar se Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não encontrado!"
    echo "Instale Docker Desktop: https://www.docker.com/products/docker-desktop"
    exit 1
fi

# Verificar se Docker está rodando
if ! docker info &> /dev/null; then
    echo "❌ Docker não está rodando!"
    echo "Inicie o Docker Desktop e tente novamente."
    exit 1
fi

echo "✅ Docker detectado"
echo ""

# Construir imagem
echo "📦 Construindo imagem Docker..."
docker build -t emissor-mobile-builder .

if [ $? -ne 0 ]; then
    echo "❌ Erro ao construir imagem Docker"
    exit 1
fi

echo ""
echo "🔨 Compilando APK..."
docker run --rm \
    -v "$(pwd)/app:/app/app" \
    -v "$(pwd)/build:/app/build" \
    -v gradle-cache:/root/.gradle \
    emissor-mobile-builder

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "✅ APK gerado com sucesso!"
    echo "========================================"
    echo ""
    echo "Localização: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    # Copiar APK para raiz do projeto
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        cp app/build/outputs/apk/debug/app-debug.apk ./emissor-mobile.apk
        echo "✅ APK copiado para: emissor-mobile.apk"
        
        # Mostrar tamanho do arquivo
        SIZE=$(du -h emissor-mobile.apk | cut -f1)
        echo "📦 Tamanho: $SIZE"
    fi
else
    echo ""
    echo "❌ Erro ao compilar APK"
    echo "Verifique os logs acima para mais detalhes"
    exit 1
fi
