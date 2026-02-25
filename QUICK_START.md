# Guia Rápido - EmissorJava

## 🚀 Início Rápido (5 minutos)

### 1. Configurar Firewall
Execute como Administrador:
```
setup-firewall.bat
```

### 2. Iniciar Aplicação
```
run.bat
```

O aplicativo irá:
- ✅ Iniciar servidor REST na porta 8084
- ✅ Abrir interface JavaFX
- ✅ Criar banco de dados SQLite
- ✅ Exibir status na janela

### 3. Descobrir IP do Computador
No PowerShell:
```powershell
ipconfig
```
Anote o "Endereço IPv4" (ex: 192.168.1.100)

### 4. Testar API
No navegador:
```
http://localhost:8084/api/health
```

Deve retornar:
```json
{"status":"UP","service":"EmissorJava"}
```

### 5. Configurar App Mobile
Use o IP descoberto + porta 8084:
```
http://192.168.1.100:8084/api/items
```

Token de autenticação:
```
emissor-token-2026
```

---

## 📱 Envio do Mobile

```json
POST http://192.168.1.100:8084/api/items
Headers:
  X-API-Token: emissor-token-2026
  Content-Type: application/json

Body:
{
  "codigoReferencia": "ABC123",
  "quantidade": 5,
  "descricao": "Produto teste"
}
```

---

## 🎯 Funcionalidades

### Desktop JavaFX
- ✅ Visualizar itens recebidos em tabela
- ✅ Buscar por código ou descrição
- ✅ Adicionar itens manualmente
- ✅ Editar itens existentes
- ✅ Excluir itens
- ✅ Exportar para CSV
- ✅ Atualização em tempo real

### API REST
- ✅ Health check
- ✅ Criar item (mobile → desktop)
- ✅ Listar todos os itens
- ✅ Buscar itens
- ✅ Obter item por ID
- ✅ Atualizar item
- ✅ Excluir item
- ✅ Contar itens

---

## 📋 Endpoints da API

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/health | Verifica status |
| POST | /api/items | Cria novo item |
| GET | /api/items | Lista todos |
| GET | /api/items?search=X | Busca itens |
| GET | /api/items/{id} | Obtém por ID |
| PUT | /api/items/{id} | Atualiza item |
| DELETE | /api/items/{id} | Exclui item |
| GET | /api/items/count | Conta total |

---

## 🔧 Configurações

Arquivo: `src/main/resources/application.properties`

```properties
# Porta do servidor
server.port=8084

# Token de autenticação
app.security.token=emissor-token-2026

# Banco de dados
spring.datasource.url=jdbc:sqlite:emissor.db
```

---

## 🐛 Troubleshooting

### Problema: Porta 8084 em uso
**Solução:** Altere a porta em `application.properties`:
```properties
server.port=9090
```

### Problema: Mobile não conecta
**Checklist:**
- [ ] Desktop e mobile na mesma rede Wi-Fi
- [ ] Firewall configurado (executou setup-firewall.bat?)
- [ ] IP correto no app mobile
- [ ] Token correto: `emissor-token-2026`
- [ ] App desktop está rodando

**Teste rápido:** Acesse do celular:
```
http://[IP_DO_DESKTOP]:8084/api/health
```

### Problema: JavaFX não abre
**Verificar:**
- [ ] Java 21 instalado?
- [ ] Variável JAVA_HOME configurada?

**Comando:**
```powershell
java -version
```

Deve mostrar: `openjdk version "21.0.7"`

---

## 📁 Arquivos Importantes

| Arquivo | Descrição |
|---------|-----------|
| `run.bat` | Inicia aplicação |
| `setup-firewall.bat` | Configura firewall |
| `README.md` | Documentação completa |
| `MOBILE_INTEGRATION.md` | Guia integração mobile |
| `FIREWALL_CONFIG.md` | Guia detalhado firewall |
| `api-tests.http` | Testes da API |
| `emissor.db` | Banco de dados (criado automaticamente) |

---

## 📞 Verificação Passo a Passo

### Passo 1: Build OK?
```batch
.\gradlew.bat build -x test
```
✅ Deve mostrar: `BUILD SUCCESSFUL`

### Passo 2: Aplicação Inicia?
```batch
run.bat
```
✅ Janela JavaFX abre
✅ Console mostra: "EmissorJava iniciado com sucesso!"

### Passo 3: API Funciona?
Navegador: `http://localhost:8084/api/health`
✅ Retorna JSON com status UP

### Passo 4: Tabela Vazia?
✅ Interface mostra: "Nenhum item recebido ainda"

### Passo 5: Teste Manual
- Clique em "Adicionar Item"
- Preencha dados
- Clique em "Salvar"
✅ Item aparece na tabela

### Passo 6: Teste Via API (opcional)
Use Postman ou arquivo `api-tests.http`
✅ POST cria item
✅ Item aparece na interface

---

## 🎓 Próximos Passos

1. **Desenvolver o App Mobile**
   - Consulte: `MOBILE_INTEGRATION.md`
   - Exemplos para Android, iOS, React Native, Flutter

2. **Personalizar Configurações**
   - Alterar porta se necessário
   - Mudar token padrão (segurança)
   - Ajustar logs

3. **Deploy**
   - Considere criar instalador com jpackage
   - Ou distribua pasta `build/distributions/`

---

## 💡 Dicas

### Performance
- SQLite é rápido para < 100k registros
- Para mais, considere PostgreSQL

### Segurança
- ⚠️ Não exponha na internet sem HTTPS
- 🔒 Altere o token padrão
- 🔐 Use VPN para acesso remoto

### Manutenção
- Backup do `emissor.db` regularmente
- Logs em: console do aplicativo
- Erros aparecem em pop-ups JavaFX

---

## ✅ Status da Implementação

- [x] Estrutura do projeto Gradle
- [x] Modelo de dados e banco SQLite
- [x] Repositório com JDBC
- [x] Camada de serviço
- [x] API REST com endpoints
- [x] Autenticação via token
- [x] Interface JavaFX completa
- [x] Funcionalidades de CRUD
- [x] Busca e filtros
- [x] Exportação CSV
- [x] Integração Spring Boot + JavaFX
- [x] Scripts de execução
- [x] Documentação completa
- [x] Exemplos de integração mobile

**Sistema 100% funcional!** 🎉
