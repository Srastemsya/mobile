# Срастёмся · HTTP API для фронтенда

Снимок реализации от 13.11.2026, коммит `a62274c`. Таблица ниже сверена с маршрутами FastAPI и конфигурацией шлюза. Для DTO и правил валидации используйте [мобильный контракт](mobile-api.md) и [уточнения первого релиза](discovery-v1.md). [Контракт комнаты v2](lobby-v2.md) описывает целевое поведение, статус реализации указан ниже.

## Подключение

- Staging по конфигурации деплоя — `https://srastemsya.nixbi.ru/v1`.
- Локальный Android Emulator — `http://10.0.2.2:8080/v1`. iOS Simulator — `http://127.0.0.1:8080/v1`.
- Пути в таблице относительны к этому базовому URL. Относительные URL медиа разрешайте от origin.
- Передавайте `Authorization: Bearer <accessToken>` везде, кроме трёх POST для OTP и refresh. Каталог, афиша и свайпы требуют завершённого профиля. Для POST передавайте `Idempotency-Key`, при сетевом повторе сохраняйте ключ и тело.
- Ошибки API имеют форму `{error: {code, message, requestId, details?}}`. Для страниц передавайте `nextCursor` предыдущего ответа как `cursor`. При `409 CURSOR_EXPIRED` начинайте выдачу заново.

## Эндпоинты

| Метод и путь | Вход | Успешный ответ |
|---|---|---|
| POST `/auth/challenges` | `{channel: SMS или EMAIL, destination}` | 201 `{challengeId, expiresAt, resendAvailableAt}` |
| POST `/auth/challenges/{id}/verify` | `{code}`, строка из четырёх цифр | 200 `Session` |
| POST `/auth/refresh` | `{refreshToken}` | 200 `Session` с новой парой токенов |
| DELETE `/auth/session` | Пустое тело | 204 |
| GET `/me` | — | 200 `Profile` |
| PUT `/me/registration` | `{displayName, birthDate}` | 200 `Profile` |
| GET `/home` | — | 200 `{quickCategories, collections, upcoming}` |
| GET `/catalog/categories` | — | 200 `{items: Category[]}` |
| GET `/catalog/search` | `q?`, фильтры каталога, `cursor?` | 200 страница `CatalogCard` |
| GET `/catalog/search/suggestions` | `q?` | 200 `{items: [{label, query}]}` |
| GET `/catalog/map` | `kind`, `bbox=west,south,east,north`, фильтры каталога, `cursor?` | 200 страница `CatalogCard` |
| GET `/catalog/{kind}/{id}` | `EVENT`, `PLACE` или `ACTIVITY` | 200 `Event` или `{card, description}` |
| GET `/events` | `categoryId?, localDate?, maxPriceMinor?, onlyWithSeats?, cursor?` | 200 страница `CatalogCard` |
| GET `/events/{id}` | — | 200 `Event` |
| POST `/events` | `CreateEvent`, `endsAt` можно опустить | 201 `Event` |
| POST `/events/{id}/registrations` | Пустое тело | 201 новая запись или 200 существующая, `{registration, admission}` |
| GET `/events/{id}/registrations` | `cursor?`, доступ организатору | 200 страница `{registration, participant}` |
| DELETE `/events/{id}/registrations/{registrationId}` | Пустое тело, доступ организатору | 200 `{registration, admission}` |
| GET `/feed/candidates` | `kind=EVENT` или `PLACE`, `categoryId?, localDate?, budgetTotalMinor?, partySize?, cursor?` | 200 страница `CatalogCard` |
| POST `/rooms` | `{name?}` или пустое тело | 201 `Room` |
| POST `/rooms/{id}/invites` | Пустое тело, владелец в `LOBBY` | 201 `{url, expiresAt}` |
| POST `/room-invites/join` | `{token}` из deep link | 200 `Room` |
| GET `/rooms/{id}` | `afterVersion?, waitSeconds?` | 200 `Room` или 204 по тайм-ауту ожидания |
| POST `/rooms/{id}/start` | `{filters: FeedFilters}`, владелец, минимум два участника | 200 `Room` |
| PUT `/rooms/{id}/rounds/{roundId}/votes/{kind}/{candidateId}` | `{choice: LIKE или PASS}` | 200 `Room` |
| PUT `/rooms/{id}/rounds/{roundId}/completion` | Пустое тело | 200 `Room` |

Фильтры каталога: `kind?, categoryId?, localDate?, maxPriceMinor?, onlyWithSeats?`. Суммы передаются в копейках. В одиночной выдаче бюджет задаётся через `budgetTotalMinor` вместе с `partySize`. При старте комнаты бюджет задаётся через `filters.budgetTotal` как `Money`, размер группы сервер берёт из состава.

## Комнаты в текущей реализации

- До 10 карточек в раунде. `round.number` начинается с 1. `round.expiresAt` задаёт 30 минут с создания каждого раунда. Просроченный незавершённый раунд даёт `CLOSED`.
- Сервер подводит итог, когда все участники завершили проход. Для матча нужно не меньше 70% лайков. При равенстве выигрывает более ранняя карточка пула. Новый результат содержит `{winner, basis: "THRESHOLD", likes, memberCount}`. Старые сохранённые результаты могут содержать только `winner`.
- Следующий пул берётся в порядке каталога из ещё не показанных карточек. При исчерпании сервер возвращает `EXHAUSTED`, `round=null`, `result=null`. Веса категорий и `TOP_SCORE` запланированы.
- Получите первый snapshot через GET без query. Затем держите один запрос с `afterVersion=<version>&waitSeconds=25`. При 204 повторяйте с той же версией, при 200 используйте новую. Настройте клиентский тайм-аут больше 25 секунд. Остановите опрос при уходе с экрана или в терминальном состоянии.
- Лайки и отмена в одиночном режиме хранятся на устройстве.

Чат, загрузка медиа, списки профиля, сохранения, отзывы и поддержка в базовом контракте относятся к проектируемым разделам. Для публикации через текущий API можно опустить `coverMediaId`. Внутренний контракт Kafka приложен для справки.
