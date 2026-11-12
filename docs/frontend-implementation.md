# Клиентская реализация handoff

Спецификация первого релиза находится в `contracts/discovery-v1.md`, экраны — в `design/`, приёмка — в `specs/discovery-v1/acceptance.md`. Репозиторий UI Kit подключён как локальный модуль [`:uikit`](../uikit/README.md); в `commonMain` лежат Compose-компоненты и токены, в `androidMain` и `iosMain` — ресурсы и адаптеры платформы. Основные версии Kotlin/Compose/AGP проекта сохранены, чтобы обновление UI Kit не меняло toolchain всего приложения.

## Слои приложения

- `shared/src/commonMain/.../<feature>/domain` содержит модели, интерфейсы репозиториев и use case.
- `.../<feature>/data` содержит текущие локальные реализации и проекции между feature-моделями.
- `.../<feature>/presentation` содержит Compose-экраны, ViewModel и `StateFlow` UI state.
- `shared/src/androidMain` и `shared/src/iosMain` содержат только платформенный выбор изображений, настройки MapLibre и Swift/UIKit host.
- `di/AppContainer` собирает реализации репозиториев и ViewModel. Когда появится API стенд, замена локальных репозиториев на HTTP-реализации выполняется через этот composition root.

## Локальные реализации для первого прогона

| Сценарий | Реализация | Контракт |
|---|---|---|
| Открытия | `home/data/MockHomeRepository` | `GET /home` |
| Вход и регистрация | `registration/data/SettingsMockRegistrationRepository` | challenge, verify, профиль; локальный dev-код `0427` |
| Афиша и публикация | `events/data/MockEventRepository` | list/detail/create/register/cancel/organizer |
| Лента и комнаты | `party/data/MockPartyRepository` + `MockPartyBackend` | cursor-подобная выдача, комната, приглашение, голоса и результат |
| События карты | `map/data/EventMapCatalogRepository` | карта проецирует те же ID и координаты, что и афиша |
| Профиль | `profile/data/SettingsProfileRepository` | локальные профиль, фото, сохранённое и мои события |

Mocks остаются в памяти процесса либо в локальных настройках и предназначены для демонстрации UI. Они не заменяют проверку настоящих HTTP-статусов, авторизации, идемпотентности, сохранения комнаты между перезапусками и long polling. До подключения сервера UI не должен показывать dev OTP; `0427` используется только локальным mock-репозиторием.

## События и единая идентичность

При создании события сохраняется выбранная точка карты. `EventMapCatalogRepository` превращает поток `EventRepository` в маркеры карты с тем же `id`; режим свайпов событий также читает тот же `EventRepository`. Результат голосования и маркер карты открывают детали по этому ID. Карточки ленты принимают изображение через слот UI Kit и показывают обложку события, если она выбрана.

`OPEN` отображается как свободный вход без вместимости. `REGISTRATION` имеет вместимость и атомарное ограничение места в mock. Платная запись, чат, платежи, уведомления и подбор по настроению остаются за пределами первого клиентского среза согласно README handoff.

## Сборка UI Kit

Исходники и ресурсы UI Kit из папки handoff скопированы в `uikit/src`. Android SVG/font ресурсы поставляются вместе с модулем, iOS использует Compose Multiplatform resources и UIKit glass adapters. Руководство по карте находится в [`uikit/MAPS.md`](../uikit/MAPS.md). Текущий проект использует свои plugin aliases и SDK значения из version catalog, сохраняя KMP targets Android, iOS ARM64 и iOS Simulator ARM64.
