# Клиентская реализация handoff

Спецификация первого релиза находится в `contracts/discovery-v1.md`, экраны — в `design/`, приёмка — в `specs/discovery-v1/acceptance.md`. Клиентская очередь спецификаций ленты — в [`specs/feed-v1`](specs/feed-v1/README.md). Репозиторий UI Kit подключён как локальный модуль [`:uikit`](../uikit/README.md); в `commonMain` лежат Compose-компоненты и токены, в `androidMain` и `iosMain` — ресурсы и адаптеры платформы. Основные версии Kotlin/Compose/AGP проекта сохранены, чтобы обновление UI Kit не меняло toolchain всего приложения.

## Слои приложения

- `shared/src/commonMain/.../<feature>/domain` содержит модели, интерфейсы репозиториев и use case.
- `.../<feature>/data` содержит backend-клиенты, локальные реализации и проекции между feature-моделями.
- `.../<feature>/presentation` содержит Compose-экраны, ViewModel и `StateFlow` UI state.
- `shared/src/androidMain` и `shared/src/iosMain` содержат только платформенный выбор изображений, настройки MapLibre и Swift/UIKit host.
- `di/AppContainer` собирает backend-репозитории и ViewModel через общий `BackendApi`.

## Реализации первого backend-среза

| Сценарий | Реализация | Контракт |
|---|---|---|
| Открытия | `home/data/BackendHomeRepository` | `GET /v1/home` |
| Вход и регистрация | `registration/data/BackendRegistrationRepository` | challenge, OTP verify, `GET /me`, `PUT /me/registration`, refresh |
| Афиша и публикация | `events/data/BackendEventRepository` | list/detail/create/register, organizer roster and participant removal |
| Лента и комнаты | `feed/data/BackendFeedRepository`, `feed/data/BackendRoomRepository`; унаследованный UI комнаты — `party/data/BackendPartyRepository` | paged candidate feed, room/invite/join, votes, completion and long polling |
| Карта | `map/data/BackendMapCatalogRepository` | `GET /v1/catalog/map` for published events, places and activities |
| Профиль | `profile/data/SettingsProfileRepository` | local profile editing, photos, saved items and event lists |

`di/AppContainer` binds the backend repositories by default. Use cases ленты (`GetFeedCandidatesUseCase`, `CreateRoomUseCase`, `CreateInviteUseCase`, `JoinRoomUseCase`, `StartRoomUseCase`, `SubmitVoteUseCase`, `CompleteRoundUseCase`, `ObserveRoomUseCase`) собраны в контейнере поверх `feed/data` реализаций и доступны платформам как поля; `createFeedModeViewModel()` связывает экран «Выбираем вместе» (FE-F00) с проверкой завершённости профиля (`GET /v1/me profileComplete`). `BackendApi` adds bearer authentication, refresh on 401, JSON handling and stable idempotency keys for retried POST operations. The current development API origin is selected per platform: Android Emulator `http://10.0.2.2:8080`, iOS Simulator `http://127.0.0.1:8080`. Debug Android allows local HTTP; release Android does not. A production HTTPS origin still needs build configuration.

Mocks remain available as explicit implementations, but they are no longer the default app binding. Profile editing and profile lists remain local because they are outside the backend's Discovery v1 runtime. The API also does not currently provide self-cancel registration, event deletion, real cover uploads, chat, payments or reviews; the UI must not report these operations as server successes. See [backend integration notes](backend-integration.md).

## События и единая идентичность

При публикации события сервер возвращает единый ID; афиша, карта и выдача свайпов читают объект с этим ID. Карта перечитывает каталог при открытии. Карточки ленты показывают серверную обложку, если у карточки есть доступный URL.

`OPEN` отображается как свободный вход без вместимости. `REGISTRATION` имеет вместимость и атомарную серверную запись. Платная запись, чат, платежи, уведомления и подбор по настроению остаются за пределами первого клиентского среза согласно README handoff.

## Сборка UI Kit

Исходники и ресурсы UI Kit из папки handoff скопированы в `uikit/src`. Android SVG/font ресурсы поставляются вместе с модулем, iOS использует Compose Multiplatform resources и UIKit glass adapters. Руководство по карте находится в [`uikit/MAPS.md`](../uikit/MAPS.md). Текущий проект использует свои plugin aliases и SDK значения из version catalog, сохраняя KMP targets Android, iOS ARM64 и iOS Simulator ARM64.
