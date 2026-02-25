# Guia Rápido - Emissor Mobile

## 🚀 Setup em 5 minutos

### 1. Abrir Projeto
```bash
# Abra o Android Studio
# File > Open > Selecione a pasta EmissorMobile
```

### 2. Conectar Dispositivo
- Conecte o celular via USB **OU**
- Inicie um emulador Android

### 3. Executar
- Clique no botão ▶️ (Run)
- Aguarde instalação

### 4. Configurar no App

Abra o app > ⚙️ Configurações:

- **IP do Servidor**: `192.168.1.136`
- **Porta**: `8084`
- **Token**: `emissor-token-2026`

Toque em **Testar Conexão**

### 5. Usar!

1. Toque no botão **📷** (escanear)
2. Aponte para código de barras
3. Item aparece na lista automaticamente
4. Se auto-sync estiver ON, já está no servidor!

---

## ✅ Checklist

- [ ] EmissorJava desktop rodando
- [ ] Android Studio aberto
- [ ] Dispositivo conectado
- [ ] App instalado
- [ ] IP configurado
- [ ] Permissão de câmera concedida
- [ ] Teste de conexão OK

---

## 🎯 Funcionalidades

| Função | Botão/Local |
|--------|-------------|
| Escanear | Botão flutuante 📷 |
| Editar item | Toque no item |
| Sincronizar | Ícone ☁️ no topo |
| Configurações | Ícone ⚙️ no topo |
| Limpar tudo | Ícone 🗑️ no topo |

---

## 🔧 Modos

### Quantidade Automática
- **ON**: Escanear o mesmo código → incrementa quantidade
- **OFF**: Cada scan cria novo item

### Sincronização Automática  
- **ON**: Envia para servidor imediatamente
- **OFF**: Sincroniza manualmente (☁️)

---

## 🐛 Problemas Comuns

**Erro de conexão**
- Mesma rede WiFi?
- IP correto?
- EmissorJava rodando?

**Câmera não abre**
- Permissão concedida?
- Configurações > Apps > Emissor Mobile > Permissões

**Código não detecta**
- Melhor iluminação
- Mantém estável
- Foco no código

---

## 📱 Testando

### Teste Rápido
```
1. Abra o app
2. Configure IP: 192.168.1.136
3. Teste conexão (deve ficar verde ✅)
4. Toque em 📷
5. Escaneie qualquer código de barras
6. Veja no EmissorJava desktop
```

### Exemplo de Código de Barras de Teste
Use qualquer produto com código de barras ou gere um QR Code online com o texto "TEST123".

---

**Pronto para usar!** 🎉
