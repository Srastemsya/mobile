# Афиша и события

Точные поля, статусы и ограничения находятся в [`docs/contracts/discovery-v1.md`](docs/contracts/discovery-v1.md) и [`docs/contracts/mobile-api.md`](docs/contracts/mobile-api.md). Экранные сценарии описаны в [`docs/design/events.md`](docs/design/events.md) и [`docs/design/event.md`](docs/design/event.md).

## Клиентские слои

```text
EventScreen → EventViewModel → EventUseCases → EventRepository (domain)
                                                 ↑
                                      MockEventRepository (data)
```

`MockEventRepository` имитирует афишу, детали, создание, запись, снятие записи и действия организатора. Вместимость и конкурирующие изменения защищены `Mutex` внутри процесса. Новое событие записывает координаты и общий ID; карта проектирует его в маркер, а лента событий получает ту же модель из этого репозитория. Настоящий серверный HTTP-адаптер пока не подключён.

## Ограничения релиза

- `OPEN` — свободный вход без лимита мест.
- `REGISTRATION` — бесплатная запись с положительной вместимостью.
- В mock-клиенте платная запись не создаётся; оплата отсутствует.
- Чат, платежи и уведомления не реализованы в этой задаче. Чат отдельной задачей BE-05; UI показывает недоступное состояние.
- Обложка хранится как локальный URI для демо. Настоящий media upload будет подключён по протоколу из API handoff.

При подключении backend добавьте HTTP data-адаптер к `EventRepository`, DTO и мапперы в `events/data`; ошибки API переводите в доменные ошибки, не пропуская DTO в ViewModel. Сохраните mock как локальную конфигурацию через `AppContainer`.
