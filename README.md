# EmissorJava

Sistema desktop de recebimento de itens do aplicativo mobile. Desenvolvido em Java 21 com Spring Boot e JavaFX.

## Características

- **Servidor REST**: Porta 8084 para receber dados do app mobile
- **Interface JavaFX**: Visualização, busca, edição e exportação de itens
- **Banco de dados**: SQLite embarcado
- **Autenticação**: Token simples via header HTTP

## Requisitos

- OpenJDK 21.0.7 LTS ou superior
- Gradle 8.x (wrapper incluído)

## Execução

### Windows
```bash
run.bat
```

### Linux/Mac
```bash
./gradlew run
```

### Manualmente
```bash
.\gradlew.bat run
```

## Configuração

Edite `src/main/resources/application.properties` para alterar:

- **Porta do servidor**: `server.port=8084`
- **Token de autenticação**: `app.security.token=emissor-token-2026`
- **Banco de dados**: `spring.datasource.url=jdbc:sqlite:emissor.db`

## API REST

### Endpoints

#### Health Check
```http
GET http://localhost:8084/api/health
```

#### Criar Item (do Mobile)
```http
POST http://localhost:8084/api/items
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

#### Listar Todos os Itens
```http
GET http://localhost:8084/api/items
Headers:
  X-API-Token: emissor-token-2026
```

#### Buscar Itens
```http
GET http://localhost:8084/api/items?search=ABC
Headers:
  X-API-Token: emissor-token-2026
```

#### Obter Item por ID
```http
GET http://localhost:8084/api/items/{id}
Headers:
  X-API-Token: emissor-token-2026
```

#### Atualizar Item
```http
PUT http://localhost:8084/api/items/{id}
Headers:
  X-API-Token: emissor-token-2026
  Content-Type: application/json

Body:
{
  "codigoReferencia": "ABC123",
  "quantidade": 10,
  "descricao": "Produto atualizado"
}
```

#### Excluir Item
```http
DELETE http://localhost:8084/api/items/{id}
Headers:
  X-API-Token: emissor-token-2026
```

#### Contar Itens
```http
GET http://localhost:8084/api/items/count
Headers:
  X-API-Token: emissor-token-2026
```

## Funcionalidades da Interface

- **Visualização**: Tabela com todos os itens recebidos
- **Busca**: Filtro por código de referência ou descrição
- **Adicionar**: Inserir novos itens manualmente
- **Editar**: Modificar itens existentes
- **Excluir**: Remover itens do banco
- **Exportar**: Gerar arquivo CSV com os dados
- **Atualização automática**: Refresh manual via botão

## Estrutura do Projeto

```
EmissorJava/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/emissor/
│   │   │       ├── config/          # Configurações (Autenticação)
│   │   │       ├── controller/      # REST Controllers
│   │   │       ├── dto/             # Data Transfer Objects
│   │   │       ├── model/           # Modelos de dados
│   │   │       ├── repository/      # Acesso ao banco
│   │   │       ├── service/         # Lógica de negócio
│   │   │       ├── ui/              # Interface JavaFX
│   │   │       └── EmissorApplication.java
│   │   └── resources/
│   │       ├── fxml/                # Layouts JavaFX
│   │       ├── application.properties
│   │       └── schema.sql
│   └── test/
├── build.gradle
├── settings.gradle
└── README.md
```

## Build

Para gerar distribuível:
```bash
.\gradlew.bat build
```

Os artefatos estarão em `build/libs/`

## Troubleshooting

### Erro de porta em uso
Altere a porta em `application.properties` ou finalize o processo que está usando a porta 8084.

### JavaFX não carrega
Verifique se está usando OpenJDK 21 e se o plugin JavaFX está configurado corretamente no `build.gradle`.

### Banco de dados não inicializa
Verifique permissões de escrita no diretório do projeto. O arquivo `emissor.db` será criado automaticamente.

## Licença

Projeto interno - Todos os direitos reservados

## Suporte

Para dúvidas ou problemas, consulte a documentação do código ou entre em contato com a equipe de desenvolvimento.
