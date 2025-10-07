# Secure REST API (Spring Boot + JWT)

защищённый REST-API на Spring Boot с аутентификацией по JWT, хэшированием паролей и базовыми мерами защиты от OWASP Top 10 (SQLi/XSS/BA).

---

## Описание проекта и API

**Стек:** Java 21, Spring Boot 3 (Web, Validation, Security, OAuth2 Resource Server, Data JPA), JJWT 0.12.x (HS256), PostgreSQL (локально), H2 (в тестах).

### Эндпоинты

#### 1) POST `/auth/login` — аутентификация, выдача JWT

**Request**

```http
POST /auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "password123"
}
```

**Response 200**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
}
```

**Ошибки**

* `401 Unauthorized` — неверные учётные данные.

---

#### 2) GET `/api/data` — получение списка постов (только с JWT)

**Request**

```http
GET /api/data
Authorization: Bearer <JWT>
```

**Response 200**

```json
[
  {
    "id": 1,
    "content": "Hello <script>alert(1)</script> world",
    "createdAt": "2025-10-07T22:25:55.734530Z",
    "authorUsername": "alice"
  }
]
```

**Ошибки**

* `401 Unauthorized` — отсутствует/некорректный токен.

---

#### 3) POST `/api/posts` — создать пост (только с JWT)

**Request**

```http
POST /api/posts
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "content": "My first post!"
}
```

**Response 200**

```json
{
  "id": 2,
  "content": "My first post!",
  "createdAt": "2025-10-08T00:01:23Z",
  "authorUsername": "alice"
}
```

**Ошибки**

* `400 Bad Request` — валидация полей (пустой `content` и т.п.).
* `401 Unauthorized` — отсутствует/некорректный токен.

---

### Быстрые примеры cURL

```bash
# Логин → извлечь токен
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"password123"}' | jq -r .token)

# Без токена (ожидаем 401)
curl -i http://localhost:8080/api/data

# С токеном
curl -s http://localhost:8080/api/data -H "Authorization: Bearer $TOKEN"

# Создать пост
curl -s -X POST http://localhost:8080/api/posts \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"Hello world"}'
```

---

## Реализованные меры защиты

### 1) Защита от SQL Injection (SQLi)

**JPA/Hibernate**: все запросы выполняются через ORM и **параметризованные** Prepared Statements; ручная конкатенация SQL/JPQL с пользовательскими строками не используется.
**Репозитории**: `findByUsername(...)`, `save(...)` и т.д. генерируют безопасные SQL с биндингом параметров.
**Кастомные запросы (при необходимости)**: используются именованные параметры `:param`, без строковой конкатенации.


---

### 2) Защита от XSS

**API возвращает только JSON** (`Content-Type: application/json`), не рендерит HTML/JS и не вставляет пользовательские данные в HTML-шаблоны.

**Контент в ответах** — строки, которые безопасны до тех пор, пока фронтенд не вставляет их в DOM как HTML. Рекомендация фронту: использовать безопасный вывод (`textContent`, фреймворки экранируют по умолчанию; не применять `innerHTML`/`v-html`/`dangerouslySetInnerHTML` без санитайзера).

**HTTP-заголовки**: включена базовая CSP (`default-src 'none'`) на уровне Spring Security headers — это доп. страховка при случайной HTML-странице.

---

### 3) Аутентификация и хранение паролей (Broken Authentication)

**JWT (HS256)**: при успешном логине сервер выдаёт короткоживущий токен (время жизни настраивается в `app.jwt.ttl-minutes`). Проверка токена реализована стандартным механизмом **Spring Security OAuth2 Resource Server** — `JwtDecoder` валидирует подпись и сроки, доступ к `/api/**` открыт только с валидным `Bearer`-токеном.

**Хэширование паролей:** пароли никогда не хранятся в открытом виде. Используется **BCryptPasswordEncoder**.

**Статус-коды:** без токена — `401`, при нехватке прав — `403`.

**`open-in-view: false`** — снижает риски случайной сериализации ленивых прокси и утечек данных.


---

### 4) Валидация и корректность данных

Входные DTO помечены **Jakarta Validation** аннотациями (`@NotBlank`), ошибки валидации приводят к `400 Bad Request`.

Контроллеры возвращают **DTO-ответы** (а не JPA-сущности), чтобы исключить проблемы сериализации ленивых прокси и утечки внутренней модели.

---

## Качество и безопасность кода (CI)

**SpotBugs (SAST):** запускается в CI, отчёты прикладываются артефактами. Для ORM-сущностей заглушены «шумные» паттерны EI/EI2 (`config/spotbugs-exclude.xml`).

**OWASP Dependency-Check (SCA):** анализирует зависимости и валит сборку при CVSS ≥ 7.0.

---

## Профили и окружения

**Локально (PostgreSQL):** `application.yml` с JDBC-строкой до вашей БД.
**Тесты/CI (H2):** `src/test/resources/application-test.yml` с `jdbc:h2:mem:...` и `SPRING_PROFILES_ACTIVE=test` при запуске тестов.