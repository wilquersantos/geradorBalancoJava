# Build APK via Docker - Guia Completo

Este guia mostra como compilar o APK do Emissor Mobile usando Docker, **sem precisar instalar Android Studio**.

## 📋 Pré-requisitos

### 1. Instalar Docker Desktop

**Windows:**
1. Baixe: https://www.docker.com/products/docker-desktop
2. Execute o instalador
3. Reinicie o computador se solicitado
4. Abra Docker Desktop
5. Aguarde aparecer "Docker Desktop is running"

**Requisitos mínimos:**
- Windows 10 64-bit: Pro, Enterprise, ou Education (Build 19041 ou superior)
- WSL 2 habilitado
- 4GB RAM disponível
- 20GB espaço em disco

### 2. Verificar Instalação

Abra PowerShell e execute:
```powershell
docker --version
```

Deve retornar algo como:
```
Docker version 24.0.x, build xxxxx
```

## 🚀 Métodos de Build

### Método 1: Script Automático (Recomendado)

#### Windows:
```powershell
cd d:\PROJETOSDEV\EmissorJava\EmissorMobile
.\build-docker.bat
```

#### Linux/Mac:
```bash
cd d:\PROJETOSDEV\EmissorJava\EmissorMobile
chmod +x build-docker.sh
./build-docker.sh
```

O script irá:
1. ✅ Verificar se Docker está instalado e rodando
2. 📦 Construir a imagem Docker com Android SDK
3. 🔨 Compilar o projeto e gerar o APK
4. 📱 Copiar o APK para `emissor-mobile.apk`

**Tempo estimado:** 
- Primeira vez: 10-15 minutos (baixa Android SDK)
- Próximas vezes: 2-5 minutos

---

### Método 2: Docker Compose

```powershell
cd d:\PROJETOSDEV\EmissorJava\EmissorMobile
docker-compose up --build
```

O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`

---

### Método 3: Comandos Manuais

#### Passo 1: Construir Imagem
```powershell
docker build -t emissor-mobile-builder .
```

#### Passo 2: Compilar APK
```powershell
docker run --rm `
  -v "${PWD}/app:/app/app" `
  -v "${PWD}/build:/app/build" `
  -v gradle-cache:/root/.gradle `
  emissor-mobile-builder
```

#### Passo 3: Localizar APK
```
app\build\outputs\apk\debug\app-debug.apk
```

---

## 📱 Localização do APK

Após o build bem-sucedido, o APK estará em:

```
✅ Cópia na raiz: emissor-mobile.apk
✅ Original: app/build/outputs/apk/debug/app-debug.apk
```

## 📦 Instalar no Celular

### Opção 1: USB (Recomendado)

1. **Ativar Depuração USB no celular:**
   - Configurações > Sobre o telefone
   - Toque 7 vezes em "Número da versão"
   - Volta > Opções do desenvolvedor
   - Ative "Depuração USB"

2. **Conectar celular ao PC via USB**

3. **Instalar ADB (se não tiver):**
   ```powershell
   # Via Chocolatey
   choco install adb
   
   # Ou baixe: https://developer.android.com/studio/releases/platform-tools
   ```

4. **Instalar APK:**
   ```powershell
   adb install emissor-mobile.apk
   ```

### Opção 2: Transferência Manual

1. Copie `emissor-mobile.apk` para o celular
2. No celular, abra o arquivo
3. Permita "Instalar de fontes desconhecidas"
4. Toque em "Instalar"

### Opção 3: Compartilhamento

1. Envie o APK por:
   - Email
   - WhatsApp
   - Google Drive
   - Bluetooth
2. Abra no celular e instale

---

## 🔧 Troubleshooting

### Erro: "Docker não está rodando"

**Solução:**
1. Abra Docker Desktop
2. Aguarde inicializar completamente
3. Tente novamente

### Erro: "Cannot connect to Docker daemon"

**Windows:**
1. Verifique se WSL 2 está instalado
2. Execute: `wsl --install`
3. Reinicie o computador

### Erro: "No space left on device"

**Solução:**
```powershell
# Limpar containers e imagens antigas
docker system prune -a

# Limpar cache do Gradle
docker volume rm gradle-cache
```

### Build muito lento

**Otimizações:**
1. Alocar mais recursos ao Docker:
   - Docker Desktop > Settings > Resources
   - Aumente CPU e Memory
   
2. Use cache do Gradle:
   - O volume `gradle-cache` já está configurado
   - Builds subsequentes serão mais rápidos

### Permissões no Linux/Mac

```bash
chmod +x build-docker.sh
chmod +x gradlew
```

---

## ⚙️ Configurações Avançadas

### Gerar APK Release (Assinado)

1. Crie um keystore:
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

2. Configure `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("my-release-key.jks")
            storePassword = "senha"
            keyAlias = "my-key-alias"
            keyPassword = "senha"
        }
    }
}
```

3. Build release:
```bash
docker run --rm \
  -v "$(pwd):/app" \
  -v gradle-cache:/root/.gradle \
  emissor-mobile-builder \
  ./gradlew assembleRelease
```

### Limpar Build

```powershell
docker run --rm `
  -v "${PWD}:/app" `
  emissor-mobile-builder `
  ./gradlew clean
```

---

## 📊 Informações do APK

### Tamanho Esperado
- **Debug APK**: ~15-25 MB
- **Release APK (minificado)**: ~10-15 MB

### Verificar Informações

```powershell
# Listar conteúdo
unzip -l emissor-mobile.apk

# Info do APK (requer aapt)
aapt dump badging emissor-mobile.apk
```

---

## 🎯 Fluxo Completo

```
1. Instalar Docker Desktop
   ↓
2. Executar build-docker.bat
   ↓
3. Aguardar compilação (10-15 min primeira vez)
   ↓
4. APK gerado: emissor-mobile.apk
   ↓
5. Transferir para celular
   ↓
6. Instalar e usar!
```

---

## 🔄 CI/CD (Opcional)

Você pode automatizar o build usando GitHub Actions:

```yaml
# .github/workflows/android.yml
name: Android CI

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build APK
        run: |
          docker build -t emissor-mobile-builder .
          docker run --rm -v $PWD:/app emissor-mobile-builder
      
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

---

## ✅ Checklist Final

- [ ] Docker Desktop instalado
- [ ] Docker Desktop rodando
- [ ] Executou build-docker.bat
- [ ] APK gerado sem erros
- [ ] APK transferido para celular
- [ ] App instalado no celular
- [ ] Permissão de câmera concedida
- [ ] IP do servidor configurado
- [ ] Teste de conexão OK

---

## 📞 Suporte

**Logs detalhados:**
```powershell
docker run --rm `
  -v "${PWD}:/app" `
  -v gradle-cache:/root/.gradle `
  emissor-mobile-builder `
  ./gradlew assembleDebug --stacktrace --info
```

**Entrar no container:**
```powershell
docker run -it --rm `
  -v "${PWD}:/app" `
  emissor-mobile-builder `
  /bin/bash
```

---

## 🚀 Pronto!

Após executar `build-docker.bat`, você terá o APK pronto para instalar no celular Android!

**Arquivo gerado:** `emissor-mobile.apk`

**Próximo passo:** Transfira para o celular e instale!
