# Emissor Mobile - Android App

Aplicativo Android para leitura de códigos de barras com sincronização automática com o servidor EmissorJava.

## 📱 Funcionalidades

### ✅ Leitura de Código de Barras
- Scanner de código de barras usando câmera
- Suporte para diversos formatos (EAN, UPC, QR Code, etc.)
- Feedback visual durante escaneamento
- Delay automático entre leituras

### ✅ Modos de Quantidade
- **Automático**: Incrementa quantidade automaticamente ao escanear o mesmo código
- **Manual**: Permite digitar quantidade manualmente

### ✅ Gestão de Itens
- Lista de itens escaneados
- Visualização de código, quantidade e descrição
- Edição de quantidade e descrição
- Exclusão individual de itens
- Limpeza total da lista
- Indicador visual de sincronização

### ✅ Sincronização com Servidor
- **Automática**: Envia automaticamente quando conectado ao servidor
- **Manual**: Botão para sincronizar todos os itens pendentes
- Armazenamento local com Room Database
- Indicadores visuais de status de sincronização

### ✅ Configurações
- IP e porta do servidor configuráveis
- Token de autenticação
- Modo quantidade automática/manual
- Modo sincronização automática/manual
- Teste de conexão com servidor

## 🛠️ Tecnologias Utilizadas

- **Kotlin** - Linguagem principal
- **Jetpack Compose** - UI moderna e declarativa
- **Room Database** - Persistência local
- **Retrofit + OkHttp** - Comunicação HTTP
- **ML Kit** - Leitura de código de barras
- **CameraX** - Acesso à câmera
- **Coroutines + Flow** - Programação assíncrona
- **DataStore** - Armazenamento de preferências
- **Material Design 3** - Interface moderna

## 📋 Requisitos

- Android 7.0 (API 24) ou superior
- Câmera traseira
- Conexão WiFi (mesma rede do servidor)
- Permissão de câmera

## 🚀 Instalação

### Método 1: Android Studio

1. Abra o projeto no Android Studio
2. Conecte o dispositivo ou inicie um emulador
3. Clique em "Run" (▶️)

### Método 2: Build via Gradle

```bash
# Windows
cd EmissorMobile
.\gradlew.bat assembleDebug

# Linux/Mac
cd EmissorMobile
./gradlew assembleDebug
```

O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`

## ⚙️ Configuração Inicial

### 1. Instalar o App
- Instale o APK no dispositivo Android
- Conceda permissão de câmera quando solicitado

### 2. Configurar Servidor
1. Abra o app e toque no ícone de configurações (⚙️)
2. Configure:
   - **IP do Servidor**: IP do computador com EmissorJava (ex: 192.168.1.136)
   - **Porta**: 8084 (padrão)
   - **Token**: emissor-token-2026 (padrão)
3. Toque em "Testar Conexão"
4. Deve aparecer "Servidor conectado!"

### 3. Configurar Comportamento
- **Quantidade Automática**: 
  - ✅ Ativado: Ao escanear o mesmo código novamente, incrementa a quantidade
  - ❌ Desativado: Cria um novo registro a cada scan
  
- **Sincronização Automática**:
  - ✅ Ativado: Envia automaticamente para o servidor após cada scan
  - ❌ Desativado: Armazena localmente, sincroniza manualmente

## 📖 Como Usar

### Escanear Código de Barras

1. Na tela principal, toque no botão flutuante (📷)
2. Aponte a câmera para o código de barras
3. Aguarde o código ser detectado
4. O item aparecerá na lista automaticamente

### Editar Item

1. Na lista, toque no item desejado
2. Edite a quantidade e/ou descrição
3. Toque em "Salvar"

### Sincronizar com Servidor

**Modo Automático (recomendado)**:
- Ative "Sincronização Automática" nas configurações
- Os itens serão enviados automaticamente após cada scan

**Modo Manual**:
- Toque no ícone de nuvem (☁️) no topo
- Todos os itens pendentes serão sincronizados

### Verificar Status de Sincronização

- 🟢 **Bolinha Verde**: Item sincronizado com servidor
- 🟠 **Bolinha Laranja**: Pendente de sincronização
- 📊 **Badge no ícone**: Número de itens não sincronizados

## 🔧 Solução de Problemas

### Câmera não abre
- Verifique se a permissão de câmera foi concedida
- Vá em Configurações do Android > Apps > Emissor Mobile > Permissões

### Erro de sincronização
1. Verifique se está na mesma rede WiFi do servidor
2. Confirme o IP nas configurações (⚙️)
3. Teste a conexão
4. Verifique se o EmissorJava está rodando no desktop

### "Servidor não disponível"
- Certifique-se que o EmissorJava está rodando
- Verifique o firewall do Windows
- Confirme que está na mesma rede WiFi
- Teste no navegador do celular: `http://[IP]:8084/api/health`

### Código de barras não é detectado
- Melhore a iluminação
- Mantenha a câmera estável
- Aproxime ou afaste até ficar nítido
- Limpe a lente da câmera

## 📂 Estrutura do Projeto

```
EmissorMobile/
├── app/
│   ├── src/main/
│   │   ├── java/com/emissor/mobile/
│   │   │   ├── data/
│   │   │   │   ├── local/         # Room Database
│   │   │   │   ├── remote/        # Retrofit API
│   │   │   │   ├── preferences/   # DataStore
│   │   │   │   └── repository/    # Repository pattern
│   │   │   ├── ui/
│   │   │   │   ├── scanner/       # Scanner de código de barras
│   │   │   │   ├── screens/       # Telas Compose
│   │   │   │   ├── theme/         # Tema Material 3
│   │   │   │   └── viewmodel/     # ViewModels
│   │   │   ├── EmissorApplication.kt
│   │   │   └── MainActivity.kt
│   │   ├── res/                   # Recursos (strings, colors, etc)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 🔐 Segurança

- Token de autenticação para comunicação com servidor
- Dados armazenados localmente no dispositivo
- Comunicação apenas em rede local
- Não expõe dados na internet

## 🎨 Interface

### Tela Principal
- Lista de itens escaneados
- Cards com código, quantidade e descrição
- Indicador de sincronização
- Botão flutuante para escanear
- Badges de notificação

### Tela de Scanner
- Preview da câmera em tela cheia
- Área de foco delimitada
- Instruções no rodapé
- Feedback visual após detecção

### Tela de Edição
- Código de barras (somente leitura)
- Campo de quantidade (numérico)
- Campo de descrição (texto livre)
- Botões: Excluir, Cancelar, Salvar

### Tela de Configurações
- Configurações do servidor
- Comportamento do app
- Teste de conexão
- Informações sobre o app

## 🔄 Fluxo de Uso

```
1. Usuário abre o app
2. Toca no botão de escanear (📷)
3. Scanner abre a câmera
4. Código de barras é detectado
5. Scanner fecha automaticamente
6. Item aparece/atualiza na lista
7. Se quantidade automática está ON:
   - Código existente: incrementa quantidade
   - Código novo: cria item com quantidade 1
8. Se sincronização automática está ON:
   - Envia para servidor imediatamente
   - Marca como sincronizado (🟢)
9. Usuário pode editar descrição/quantidade
10. Ao salvar edição, marca como não sincronizado (🟠)
11. Próxima sincronização atualiza no servidor
```

## 🧪 Testando o App

### Teste 1: Escanear Código
1. Abra o app
2. Toque no botão escanear
3. Escaneie um código de barras
4. Verifique se aparece na lista

### Teste 2: Quantidade Automática
1. Ative "Quantidade Automática" nas configurações
2. Escaneie o mesmo código 3 vezes
3. Verifique se a quantidade é 3

### Teste 3: Sincronização
1. Configure o IP do servidor
2. Ative "Sincronização Automática"
3. Escaneie um código
4. Verifique no EmissorJava desktop se o item apareceu

### Teste 4: Edição
1. Toque em um item da lista
2. Altere a quantidade e descrição
3. Salve
4. Verifique se as mudanças foram aplicadas

## 📝 Changelog

### v1.0.0 (2026-02-24)
- ✅ Scanner de código de barras com ML Kit
- ✅ Armazenamento local com Room
- ✅ Interface Jetpack Compose
- ✅ Sincronização com servidor EmissorJava
- ✅ Modos automático e manual
- ✅ Tela de configurações
- ✅ Edição de itens
- ✅ Indicadores visuais de status

## 🤝 Integração com EmissorJava

Este app se comunica com o servidor desktop EmissorJava através da API REST:

- **Endpoint**: `POST http://[IP]:8084/api/items`
- **Header**: `X-API-Token: emissor-token-2026`
- **Body**: 
```json
{
  "codigoReferencia": "7891234567890",
  "quantidade": 5,
  "descricao": "Produto teste"
}
```

Consulte `MOBILE_INTEGRATION.md` no projeto EmissorJava para mais detalhes.

## 📄 Licença

Projeto interno - Todos os direitos reservados

## 👨‍💻 Desenvolvimento

Para contribuir ou customizar:

1. Clone o repositório
2. Abra no Android Studio Arctic Fox ou superior
3. Configure um emulador ou dispositivo físico
4. Execute com `gradlew installDebug`

## 🆘 Suporte

Em caso de problemas:
1. Verifique a seção "Solução de Problemas"
2. Consulte os logs do Android Studio (Logcat)
3. Teste a conexão com servidor manualmente
4. Entre em contato com a equipe de desenvolvimento
