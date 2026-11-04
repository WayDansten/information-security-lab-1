# Secure API Lab

Учебное backend-приложение на Java 21 и Spring Boot для демонстрации базовых мер защиты OWASP Top 10.

## Стек

- Java 21, Spring Boot, Gradle
- SQLite и Spring Data JPA
- Spring Security, BCrypt и JWT
- SpotBugs (SAST) и OWASP Dependency-Check (SCA)

## Запуск

Нужны Java 21 и Gradle 8.13. Секрет JWT задаётся через переменную окружения:

```powershell
$env:JWT_SECRET = 'replace-with-a-long-random-secret-at-least-32-bytes'
gradle bootRun
```

При первом запуске создаётся SQLite-файл `secure-api.db` и учебный пользователь:

```text
username: student
password: student-password
```

Пароль в базе хранится только как BCrypt-хэш. Учебные credentials не использовать в production.

## API

### Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student","password":"student-password"}'
```

Ответ содержит `accessToken` и `refreshToken`. Access-токен передаётся в заголовке `Authorization: Bearer <token>`.

### Получение данных

```bash
curl http://localhost:8080/api/data \
  -H "Authorization: Bearer <access-token>"
```

Без токена или с недействительным токеном endpoint возвращает `401 Unauthorized`.

### Создание поста

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"My post","content":"Plain text content"}'
```

### Обновление токена

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

## Реализованные меры защиты

- **SQL injection:** доступ к SQLite выполняется через Spring Data JPA repositories. Пользовательский ввод не конкатенируется с SQL.
- **XSS:** входные поля ограничены DTO-валидацией, текст постов очищается `Jsoup Safelist.none()`, ответы отдаются как JSON.
- **Broken Authentication:** пароль хэшируется BCrypt; login выдаёт access/refresh JWT; middleware проверяет подпись, тип, subject и срок действия access-токена.
- **Session security:** API stateless, серверные сессии отключены, включены `X-Content-Type-Options` и `X-Frame-Options: DENY`.
- **Input validation:** логин, пароль, title и content имеют обязательность и ограничения длины.
- **Secrets:** JWT secret читается из environment variable и не хранится в репозитории.

## Проверки

```bash
gradle test
gradle spotbugsMain
gradle dependencyCheckAnalyze
```

GitHub Actions запускает эти проверки на каждом `push` и `pull_request`. Отчёты SpotBugs и Dependency-Check публикуются как artifacts workflow. Для сдачи нужно добавить в этот раздел ссылку на публичный репозиторий и последний успешный запуск Actions, а также приложить скриншоты из вкладки Actions.
