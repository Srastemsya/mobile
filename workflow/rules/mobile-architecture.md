# Архитектура мобильного приложения (KMP)

Применяется при любой работе в модулях `shared`, `androidApp`, `iosApp`, `uikit`. Clean Architecture с тремя слоями на фичу, инверсия зависимостей, DI, единый сетевой клиент.

## Слои на фичу

Каждая фича в `shared/src/commonMain/.../<feature>/` делится на три слоя:

```
<feature>/
├── domain/          # модели, интерфейсы репозиториев, use cases, мапперы
├── data/            # реализации репозиториев, backend-клиенты, DTO, мапперы DTO → domain
└── presentation/    # Compose-экраны, ViewModel, State, Event
```

### Domain
- Без зависимостей от внешних библиотек (кроме Kotlin stdlib и coroutines) и без Android/iOS-кода.
- Без сетевых вызовов и работы с хранилищем — только интерфейсы репозиториев.
- Один use case = одна операция (Single Responsibility), вызов через `operator fun invoke`:

```kotlin
class GetUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(userId: String): Result<User> =
        userRepository.getUserById(userId)
}
```

### Data
- Реализует интерфейсы из domain. DTO отделены от domain-моделей, мапперы обязательны.
- Ошибки ловятся здесь и оборачиваются в `Result`; транспортные типы наружу не утекают.
- Кэширование реализуется в этом слое.

### Presentation
- ViewModel держит `StateFlow<State>` (неизменяемый state) и обрабатывает `Event` от UI.
- Использует только use cases — не репозитории и не сеть напрямую. Бизнес-логики нет.
- Структура экрана: `XxxScreen.kt`, `XxxViewModel.kt`, `XxxState.kt`, `XxxEvent.kt`.

## Инверсия зависимостей

Интерфейсы объявляет domain, реализует data; presentation зависит только от абстракций. Платформенный код (`androidMain`, `iosMain`) ограничен адаптерами: выбор изображений, карты, host-интеграция.

## DI

Koin — целевая библиотека: `factory` для use cases, `single` для репозиториев и API, `viewModel` для ViewModel; модули делятся по ответственности (`appModule`, `networkModule`, по фичам). Пока проект собирает зависимости через `di/AppContainer` — новые фичи регистрируются там же; миграция на Koin выполняется отдельной задачей, не попутно.

## Сеть

Целевой стек — Retrofit + OkHttp (Android) за общим KMP-интерфейсом; сейчас роль единого клиента выполняет `BackendApi`. Требования к клиенту независимо от реализации:

- таймауты обязательны (connect/read 30s);
- Bearer-авторизация, refresh на 401;
- стабильные `Idempotency-Key` для повторяемых POST;
- логирование запросов только в debug;
- API-интерфейсы и DTO — в `data/remote`, мапперы DTO ↔ domain обязательны.

## UI и Liquid Glass

Экраны собираются из компонентов `:uikit` (`ru.srastemsya.uikit`) и токенов `Theme.kt`; новые цвета, радиусы и отступы мимо токенов не вводятся. Стеклянные поверхности — через стиль Liquid Glass UI Kit; переиспользуемые обёртки эффекта живут в `uikit`, на платформе без поддержки эффекта используется читаемая подложка `color/surface/solid`. Соответствие токенов Figma и кода — в [design.md](design.md).

## Именование

- Domain-модели: `User`; DTO: `UserDto`; use cases: `GetUserUseCase`; репозитории: `UserRepository` / `BackendUserRepository`; ViewModel/State/Event: `HomeViewModel`, `HomeState`, `HomeEvent`.
- Один класс на файл (кроме простых data class), имя файла = имя класса.

## Нельзя

- Бизнес-логика во ViewModel или Compose-функциях
- Обращение presentation к data-реализациям напрямую
- Android/iOS-типы в `commonMain` domain
- Общий «utils/helpers» слой вместо пакета фичи
- Хардкод URL, строк окружения и цветов в компонентах

## Тесты

Обязательны для use cases, мапперов, репозиториев (с моками) и ViewModel — в `shared/src/commonTest` зеркально структуре слоёв. Оформление прогонов и отчётов — по [testing.md](testing.md).
