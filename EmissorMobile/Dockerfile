# Use a imagem oficial do Android SDK
FROM eclipse-temurin:17-jdk-jammy

# Variáveis de ambiente
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=${ANDROID_HOME}
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# Instalar dependências
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Baixar e instalar Android Command Line Tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Aceitar licenças e instalar componentes necessários
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Instalar Gradle
RUN wget -q https://services.gradle.org/distributions/gradle-8.2-bin.zip -O /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /opt && \
    rm /tmp/gradle.zip && \
    ln -s /opt/gradle-8.2/bin/gradle /usr/bin/gradle

# Criar diretório de trabalho
WORKDIR /app

# Copiar arquivos do projeto
COPY . .

# Gerar wrapper do Gradle
RUN gradle wrapper --gradle-version 8.2

# Garantir permissões de execução
RUN chmod +x ./gradlew

# Comando padrão: build do APK
CMD ["./gradlew", "assembleDebug", "--no-daemon"]
