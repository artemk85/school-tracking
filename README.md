# school-tracking — система поощрения школьника

Расчёт недельного денежного поощрения за оценки по пятибалльной системе.
Backend: **Spring Boot 3 (Java 17) + PostgreSQL**. UI: **React (Vite)**.

## Правила расчёта

| Оценка | Базовая сумма (коэффициент 1.0) |
|--------|---------------------------------|
| 5      | +75 ₽                           |
| 4      | +50 ₽                           |
| 3      | −50 ₽                           |
| 2      | −100 ₽                          |

* **Веса предметов**: математика и русский язык — коэффициент **1.0**; остальные — **0.7**
  (настраивается в разделе «Настройки»).
* Итог по предмету за неделю = сумма начислений: `базовая сумма × коэффициент`.
* **Исправление двойки**: `−100 ₽` (двойка) + `75 ₽` + `75 ₽` (две пятёрки) по одному
  предмету за одну неделю = **+50 ₽**, то есть двойка фактически исправляется на четвёрку.
* Неделя считается с понедельника по воскресенье.

Пример: за неделю в математике оценки 2, 5, 5 → `−100 + 75 + 75 = +50 ₽`.
Обычный предмет, оценка 4 → `50 × 0.7 = +35 ₽`.

## Структура

```
backend/    Spring Boot: REST API, JPA, PostgreSQL
  src/main/resources/schema.sql   создание таблиц
  src/main/resources/data.sql     начальные предметы и настройки
frontend/   React + Vite UI
docker-compose.yml   PostgreSQL
```

API:
* `GET/POST/PUT/DELETE /api/grades` — оценки
* `GET/POST/PUT/DELETE /api/subjects` — предметы
* `GET /api/report/week?date=YYYY-MM-DD` — отчёт за неделю
* `GET/PUT /api/settings` — настройки сумм и коэффициентов

## Запуск

### Вариант A. Всё в Docker (рекомендуется)

Соберёт и поднимет три контейнера: `db`, `backend`, `frontend`.

```bash
cp .env.example .env          # при необходимости задайте DB_PASSWORD
docker compose up -d --build
```

* UI — https://localhost (nginx, HTTPS на порту 443; HTTP 80 редиректит на HTTPS)
* API — https://localhost/api/... (nginx проксирует на backend:8080)
* PostgreSQL — localhost:5432

Сертификат самоподписанный, поэтому браузер покажет предупреждение — примите его
(«Дополнительно» → «Перейти на сайт»). Проверка через `curl`: `curl -k https://localhost/`.

Остановить: `docker compose down` (с данными) или `docker compose down -v` (удалить том БД).

### Вариант B. Локальная разработка

#### 1. База данных

```bash
docker compose up -d db
```

#### 2. Backend (порт 8080)

```bash
cd backend
./mvnw spring-boot:run
```

Переменные окружения (по умолчанию): `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`.

#### 3. Frontend (порт 5173)

```bash
cd frontend
npm install
npm run dev
```

Откройте http://localhost:5173 — запросы к `/api` проксируются на backend.

## Docker

* `backend/Dockerfile` — multi-stage: `maven:3.9.9-eclipse-temurin-21` собирает jar,
  затем `eclipse-temurin:21-jre` запускает приложение от непривилегированного пользователя.
* `frontend/Dockerfile` — multi-stage: `node:20-alpine` собирает статику Vite,
  `alpine` генерирует самоподписанный TLS-сертификат на 10 лет,
  `nginx:1.27-alpine` раздаёт статику по HTTPS и проксирует `/api/` на сервис `backend`.
* `frontend/nginx.conf` — редирект HTTP → HTTPS, TLS 1.2/1.3, SPA-fallback
  (`try_files ... /index.html`) и reverse-proxy `/api/`.
* `docker-compose.yml` — сервисы `db` (с healthcheck), `backend` (ждёт готовности БД, свой healthcheck)
  и `frontend` (порты 80 и 443); пароль БД берётся из `.env` (`DB_PASSWORD`, по умолчанию `school`).

> Требуется **Java 21** (Spring Boot 3 совместим с 17+, но проект настроен на LTS 21).
> Сертификат генерируется при сборке образа; для продакшена замените его на
> сертификат от доверенного центра (например, Let's Encrypt) или смонтируйте свой
> в `/etc/nginx/certs/server.crt` и `/etc/nginx/certs/server.key`.

Собрать образы отдельно:

```bash
docker compose build            # оба образа
docker compose build backend    # только backend
docker compose build frontend   # только frontend
```

## Тесты

```bash
cd backend
./mvnw test
```

`RewardServiceTest` проверяет правило «две пятёрки исправляют двойку до +50 ₽»
и различие коэффициентов предметов.