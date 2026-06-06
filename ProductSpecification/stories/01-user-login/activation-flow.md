# Activation Flow — Reference (Story 1: User Login)

> Как пользователь попадает со ссылки в письме на страницу активации, и как идут запросы.
> Зафиксировано при работе над Frontend Scenario 4.1. Все факты подтверждены по коду — источники указаны.

## Краткий ответ

По клику на ссылку из письма — это запрос на **FE-маршрут** (за HTML-оболочкой SPA), которую отдаёт
бэкенд внутренним `forward` на `index.html`. Это **НЕ** «BE + HTTP-редирект на FE» — редиректа нет.
Сама валидация токена и активация — это **отдельные AJAX-запросы из SPA** на `/api/auth/activate`
(GET при загрузке страницы, POST при сабмите пароля).

## Пошагово

### 1. Ссылка в письме
`SmtpEmailNotificationSender.activationLink()`:
```java
return frontendBaseUrl + "/activate?token=" + activationToken;
```
`application.yml`: `rpm.frontend-base-url: https://rpm-platform.com` → в письме:
```
https://rpm-platform.com/activate?token=<JWT>
```
Токен активации — это **JWT в query-параметре** (не header).
Источник: `iam/user/infrastructure/notification/SmtpEmailNotificationSender.java`, `application.yml`.

### 2. Клик по ссылке
Top-level навигация браузера: `GET https://rpm-platform.com/activate?token=...`.
Это запрос за **SPA-оболочкой**, не за данными, и **не** активация.

### 3. Кто отдаёт `/activate`
`SpaForwardingController`:
```java
@GetMapping(value = {"/", "/login", "/activate"})
String forwardSpaRoute() { return "forward:/index.html"; }
```
Это `forward:` (внутренний форвард Spring на статический `index.html`), **НЕ** 302-редирект.
URL в адресной строке остаётся `/activate?token=...`. Браузер грузит Vue SPA.
Источник: `shared/infrastructure/web/SpaForwardingController.java`.

> **Предполагается single-origin**: один бэкенд (`rpm-platform.com`) отдаёт и SPA-оболочку
> (через `SpaForwardingController` + `/assets/**`), и API под `/api/**`. Поэтому в `SecurityConfig`
> публичны и `GET /activate` (маршрут SPA), и `GET`/`POST /api/auth/activate` (API).

### 4. SPA загрузилась → валидация токена (AJAX)
Vue Router → `ActivationPage.vue` `onMounted` → `validateActivationToken(token)`:
```
GET /api/auth/activate?token=<JWT>     // fetch, credentials:'include'
```
Бэк (`AuthResource.validateActivationToken` → `ActivationService.validateToken`):
- **200** `{login, email}` → форма "Set Password" с подзаголовком *"For account {login} ({email})"* (Сценарий 4.1 ✅)
- **422** (истёкший/невалидный JWT) → "Link Expired" (Сценарий 5.2 — не реализован)

Источники: `features/auth/components/ActivationPage.vue`, `features/auth/logic/activation.api.ts`,
`iam/auth/infrastructure/web/AuthResource.java`.

### 5. Сабмит пароля (Сценарий 5.1 — впереди)
Ввод пароля + подтверждения → "Activate Account":
```
GET  /api/auth/csrf          // XSRF-TOKEN cookie (POST требует CSRF)
POST /api/auth/activate      // body {token, password} + X-XSRF-TOKEN
```
Успех → "Account Activated!" + "Go to Sign In" → клик ведёт на `/login` (Сценарий 6.1).

## Схема

```
Email: https://rpm-platform.com/activate?token=JWT
        │  (клик — top-level GET, за HTML)
        ▼
[BE] GET /activate ──forward──▶ index.html (SPA shell)   ← НЕ редирект, НЕ активация
        │
        ▼  (SPA загрузилась, Vue Router → ActivationPage)
[FE] fetch GET /api/auth/activate?token=JWT  (AJAX, валидация токена)
        │
        ├─ 200 {login,email} ──▶ форма Set Password (4.1)
        └─ 422 ──────────────▶ Link Expired (5.2)
        │
        ▼  (ввод пароля + сабмит)
[FE] GET /api/auth/csrf  →  POST /api/auth/activate {token,password}
        │
        ▼
   Account Activated! → "Go to Sign In" → /login  (6.1)
```

## ✅ РЕШЕНО (Сценарий 5.1): dev origin = Vite-proxy (same-origin)

**Решение (2026-06-06, inline-design):** в деве ходим через **Vite-proxy** — API-клиенты
используют **относительные** URL (`/api/...`), а не абсолютный `http://localhost:8080`.
Тогда в деве запрос идёт `браузер → :5173/api → proxy (changeOrigin) → :8080` и остаётся
**same-origin**, как в проде. CORS на бэке **не нужен**; `credentials:'include'` + CSRF работают
тривиально. Бэкенд не меняется.

Конкретно при реализации POST-логики 5.1:
- `BASE_URL` в `login.api.ts` / `activation.api.ts` → пустая строка по умолчанию (относительный путь);
  убрать дефолт `http://localhost:8080` из `VITE_API_URL` (proxy уже настроен в `vite.config.ts`).
- POST: `GET /api/auth/csrf` (взять `XSRF-TOKEN` cookie) → `POST /api/auth/activate` с заголовком
  `X-XSRF-TOKEN` + `credentials:'include'`.

> Историческая справка по open question — ниже.

### ⚠️ Open question (исходная формулировка, до решения): dev vs prod origin

Прод и дев используют **разные origin-модели** — это критично для POST-активации (CSRF + credentials).

| | prod | dev |
|---|---|---|
| Frontend | `rpm-platform.com` (бэк отдаёт SPA) | Vite `localhost:5173` |
| API | тот же origin `/api/**` | `activation.api.ts` хардкодит `VITE_API_URL=http://localhost:8080`, бьёт **напрямую** |
| Origin для API-запроса | **same-origin** | **cross-origin** (минуя Vite-proxy `/api` из `vite.config.ts`) |
| CSRF / `credentials:'include'` | тривиально | требует корректного CORS (`Access-Control-Allow-Credentials: true`, конкретный origin, не `*`) |

- `GET /api/auth/activate` (4.1) работает в обоих случаях, т.к. при ошибке fetch форма всё равно рендерится.
- `POST /api/auth/activate` (5.1) в деве **сломается** без CORS+credentials+CSRF, хотя в проде заработает.

**Решить в самом начале Сценария 5.1** (через `/architecture` или inline-design) ДО написания
POST-логики: либо ходить через Vite-proxy `/api` (same-origin в деве), либо настроить CORS на бэке.

## См. также
- `endpoints.md` — список эндпоинтов и правила авторизации/CSRF.
- `tests/02_UI_Tests.md` — сценарии 4.1 / 5.1 / 5.2 / 6.1.
