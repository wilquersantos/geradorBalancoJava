@echo off
echo ========================================
echo Emissor Mobile - Build via Docker
echo ========================================
echo.

REM Verificar se Docker esta instalado
docker --version >nul 2>&1
if %errorLevel% neq 0 (
    echo X Docker nao encontrado!
    echo Instale Docker Desktop: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

REM Verificar se Docker esta rodando
docker info >nul 2>&1
if %errorLevel% neq 0 (
    echo X Docker nao esta rodando!
    echo Inicie o Docker Desktop e tente novamente.
    pause
    exit /b 1
)

echo OK Docker detectado
echo.

REM Construir imagem
echo Construindo imagem Docker...
docker build -t emissor-mobile-builder .

if %errorLevel% neq 0 (
    echo X Erro ao construir imagem Docker
    pause
    exit /b 1
)

echo.
echo Compilando APK...
docker run --rm ^
    -v "%cd%\app:/app/app" ^
    -v "%cd%\build:/app/build" ^
    -v gradle-cache:/root/.gradle ^
    emissor-mobile-builder

if %errorLevel% equ 0 (
    echo.
    echo ========================================
    echo OK APK gerado com sucesso!
    echo ========================================
    echo.
    echo Localizacao: app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    REM Copiar APK para raiz do projeto
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        copy /Y "app\build\outputs\apk\debug\app-debug.apk" "emissor-mobile.apk" >nul
        echo OK APK copiado para: emissor-mobile.apk
        
        REM Mostrar tamanho do arquivo
        for %%A in (emissor-mobile.apk) do echo Tamanho: %%~zA bytes
    )
) else (
    echo.
    echo X Erro ao compilar APK
    echo Verifique os logs acima para mais detalhes
)

echo.
pause
