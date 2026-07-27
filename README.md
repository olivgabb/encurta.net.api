# ENCURTA.NET

API REST para encurtamento de URLs, com autenticação via JWT, rastreamento de cliques em tempo real (WebSocket/STOMP) e persistência em MongoDB.

## Tecnologias utilizadas

| Categoria | Tecnologia |
|---|---|
| Linguagem / Runtime | Java 21 |
| Framework | Spring Boot 4.0.4 (Spring Web MVC) |
| Autenticação/Autorização | Spring Security + JWT (`com.auth0:java-jwt`) |
| Hash de senha | BCrypt (`BCryptPasswordEncoder`) |
| Banco de dados | MongoDB (via `spring-boot-starter-data-mongodb`, `MongoTemplate`) |
| Comunicação em tempo real | WebSocket + STOMP (`spring-boot-starter-websocket`) |
| Utilitários | Lombok, Gson |
| Build | Maven (Maven Wrapper incluso) |

## Arquitetura

```
controllers/   -> endpoints REST (AuthController, RoutesController)
services/      -> regras de negócio (JWTService, AuthService, Base62, CounterService, IncrementClickService)
repositories/  -> acesso a dados (UserRepository, UrlRepository) e DTOs
documents/     -> entidades persistidas no MongoDB (User, UrlEntity)
filters/       -> filtro de autenticação por token (TokenFilter)
config/        -> configuração de segurança e WebSocket (SecurityConfig, WebSocketConfig, UserRoles)
```

## Configuração

As configurações ficam em `src/main/resources/application.properties`:

```properties
spring.application.name=url-shorter
spring.mongodb.uri=mongodb://admin:root@localhost:27017/url-shortener?authSource=admin
api.security.token.secret=${JWT_SECRET:xablau}
```

> ⚠️ O segredo do JWT possui um valor padrão (`xablau`) caso a variável de ambiente `JWT_SECRET` não seja definida. **Defina `JWT_SECRET` em produção**, pois um segredo previsível permite forjar tokens válidos. As credenciais do MongoDB também estão fixas no arquivo e devem ser externalizadas (variáveis de ambiente / secrets manager) antes de qualquer deploy real.

O front-end é esperado rodando em `http://localhost:5670` (origem liberada via `@CrossOrigin` nos controllers).

## Rotas e payloads

### Autenticação — `/auth`

#### `POST /auth/register`
Cria um novo usuário e já retorna um token JWT autenticado.

**Payload:**
```json
{
  "username": "string",
  "password": "string",
  "role": "ADMIN | USER"
}
```

**Resposta 200:**
```json
{ "token": "jwt-token" }
```

**Resposta 400** (usuário já existe):
```json
{ "erro": "usuario ja existe" }
```

#### `POST /auth/login`
Autentica um usuário existente.

**Payload:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Resposta 200:**
```json
{ "token": "jwt-token" }
```

#### `POST /auth/validate-token`
Verifica se um token JWT é válido.

**Payload:**
```json
{ "token": "jwt-token" }
```

**Resposta:** `200 OK` se válido, `400 Bad Request` se inválido/expirado.

#### `GET /auth/get-username`
Retorna o username do usuário autenticado na requisição atual (extraído do contexto de segurança, populado pelo `TokenFilter`).

**Header obrigatório:**
```
Authorization: Bearer <token>
```

**Resposta 200:**
```json
{ "username": "string" }
```

### Encurtamento de URLs

#### `POST /insert`
Cria uma URL curta a partir de uma URL original. Requer usuário autenticado (o `user_id` do dono é extraído do token).

**Header obrigatório:**
```
Authorization: Bearer <token>
```

**Payload:**
```json
{ "originalUrl": "https://exemplo.com/pagina-longa" }
```

**Resposta 200:**
```json
{ "shortUrl": "aB3xZ" }
```

#### `GET /r/{url}`
Redireciona (`301 Moved Permanently`) para a URL original correspondente ao código curto, e incrementa o contador de cliques de forma atômica. O evento de clique é publicado no tópico WebSocket `/topic/clicks`.

#### `GET /get-urls`
Lista todas as URLs pertencentes ao usuário autenticado, com contagem de cliques.

**Header obrigatório:**
```
Authorization: Bearer <token>
```

**Resposta 200:**
```json
{
  "urls": [
    { "shortUrl": "aB3xZ", "originalUrl": "https://exemplo.com", "clicks": 12 }
  ]
}
```

### WebSocket

- Endpoint STOMP: `/ws`
- Tópico de assinatura: `/topic/clicks` — recebe uma mensagem em tempo real (`{ "_id", "originalUrl", "clicks" }`) toda vez que um link é acessado via `/r/{url}`.

## Segurança

- **Autenticação stateless com JWT**: `JWTService` gera tokens assinados com **HMAC256**, contendo `issuer` (`auth-api`), `subject` (username) e expiração de **7 dias**. A validação verifica assinatura, emissor e expiração antes de aceitar o token.
- **Filtro de autenticação por request**: `TokenFilter` (um `OncePerRequestFilter`) lê o header `Authorization: Bearer <token>` em cada requisição, valida o JWT e popula o `SecurityContextHolder` com o usuário autenticado — sem uso de sessão/cookies.
- **Sessão stateless**: `SessionCreationPolicy.STATELESS`, adequado para uma API consumida por um front-end separado.
- **Hash de senha**: senhas nunca são armazenadas em texto puro — são processadas com **BCrypt** (`BCryptPasswordEncoder`) antes de persistir no MongoDB.
- **Autorização por papéis (roles)**: `UserRoles` (`ADMIN`, `USER`) define as `GrantedAuthority` do usuário (`ROLE_ADMIN`/`ROLE_USER`), usadas pelo Spring Security.
- **CORS restrito**: os controllers liberam explicitamente apenas a origem `http://localhost:5670`.
- **CSRF desabilitado**: aceitável neste modelo porque a API é stateless e autenticada via token (Bearer), não via cookies de sessão.

### Pontos de atenção

- A regra de autorização em `SecurityConfig` protege o path `POST /api/insert`, mas o endpoint real está mapeado em `POST /insert` (sem o prefixo `/api`). Vale revisar esse matcher para garantir que a rota de inserção esteja de fato protegida pela camada de configuração (hoje ela só fica "segura" porque o controller lê o usuário do contexto de segurança, o que geraria erro se não houver usuário autenticado).
- O segredo do JWT tem um valor padrão hardcoded no `application.properties` — recomenda-se remover o default e exigir a variável de ambiente em qualquer ambiente que não seja o local.

## Geração das URLs curtas (não é aleatória)

Diferente de muitos encurtadores que usam geração aleatória, este projeto gera os códigos curtos de forma **determinística e sequencial**, ofuscada por uma transformação matemática:

1. **Contador atômico no MongoDB** (`CounterService`): um documento na collection `id_counter` é incrementado via `findAndModify` com `$inc`, garantindo que cada chamada receba um ID sequencial único mesmo sob concorrência (operação atômica do MongoDB).
2. **Ofuscação do ID**: antes de codificar, o ID é multiplicado por uma constante fixa (`871837 + 871836 = 1743673`) em `Base62.encode`. Isso evita que os códigos gerados sejam sequências óbvias como `1`, `2`, `3`, dificultando a adivinhação do próximo/anterior código por um usuário externo.
3. **Codificação Base62** (`Base62`): o valor resultante é convertido para uma string usando um alfabeto de 62 caracteres (`0-9a-zA-Z`), produzindo códigos curtos e amigáveis para URL. A decodificação reverte o processo (Base62 → divisão pela mesma constante) para recuperar o ID original, se necessário.

**Importante:** essa abordagem é uma ofuscação de sequência, não uma fonte de aleatoriedade criptográfica — o mapeamento entre ID e código é determinístico e reversível por qualquer pessoa que conheça o algoritmo e a constante usada. Para cenários que exijam imprevisibilidade real (ex.: evitar enumeração de todas as URLs cadastradas), o ideal seria gerar os códigos com um gerador aleatório seguro (`SecureRandom`) ou UUIDs truncados, verificando colisão no banco antes de persistir.

## Como rodar localmente

Pré-requisitos: Java 21, MongoDB rodando localmente (ou ajustar `spring.mongodb.uri`).

```bash
# usando o Maven Wrapper incluso no projeto
./mvnw spring-boot:run
```

Defina o segredo do JWT antes de subir a aplicação:

```bash
export JWT_SECRET=sua-chave-secreta-forte
```
