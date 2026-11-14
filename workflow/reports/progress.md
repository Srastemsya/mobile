# Первый релиз «Срастёмся»

| Пакет | Зависимости по спецификации | Статус | Причина |
|---|---|---|---|
| BE-D00 | Общая инфраструктура сервисов и Compose | PASS | Коммит 1c336da на be-d00, 7/7 через Nginx. [Отчёт](../../reports/BE-D00/test_report.md). |
| BE-D01 | BE-D00 | PASS | Коммит c754a31, основной прогон 10/10, управляемое истечение курсоров 1/1. [Отчёт](BE-D01/test_report.md). |
| BE-D02 | BE-D01, BE-05 для загрузки обложки | PASS | 779470e, 14/14; загрузка обложки требует BE-05. |
| BE-D03 | BE-D01, BE-D02 | PASS | 718755c, 15/15 на сборке 7875b58. |
| Сквозной прогон | BE-D00–BE-D03 | PASS | Сборка 7875b58, чистый Compose через Nginx на 18089. |

Источник порядка и зависимостей — [спецификация релиза](../../docs/specs/discovery-v1/README.md). Docker Engine 29.8.2 доступен при запуске команды вне песочницы.

# Комната v2 · весовой выбор в лобби

| Пакет | Зависимости по спецификации | Статус | Причина |
|---|---|---|---|
| BE-L01 | BE-D03 | READY | Блокеры устранены: ветка `dev` создана, в config.json выставлен `create_pr: false` (remote-авторизация на машине не настроена — локальные ветки без push), добавлены `docs/specs/lobby-v2/{README,implementation,acceptance}.md` и `docs/contracts/lobby-v2.md`. PM/Developer/Tester ещё не запускались. |
| BE-L02 | BE-L01 | BLOCKED | Зависит от выполнения BE-L01 (ветка `be-l02` от `be-l01`). Документы контракта готовы. |
| BE-L03 | BE-L02 | BLOCKED | Зависит от выполнения BE-L02 (ветка `be-l03` от `be-l02`). Документы контракта готовы. |

Источник порядка и зависимостей — [спецификация комнаты v2](../../docs/specs/lobby-v2/README.md).

# Лента v1 · мобильное приложение (KMP)

| Пакет | Зависимости по спецификации | Статус | Причина |
|---|---|---|---|
| FE-F00 | — | PASS | Повторная Android-приёмка пройдена; iOS исключён текущим scope. См. [test report](FE-F00/recheck-20261113/test_report.md). |
| FE-F01 | FE-F00 | PASS | Android-клиент: фильтры, валидация, восстановление и query mapping подтверждены; build/host tests и независимая приёмка PASS. Совпадение узких EVENT-фильтров на staging не утверждается. См. [test report](FE-F01/test_report.md) и [Developer report](FE-F01/dev_report.md). |
| FE-F02 | FE-F01 | READY | FE-F01 пройден; FE-F02 ожидает запуска. |
| FE-F03 | FE-F02, стенд BE-D03 | BLOCKED | Ожидает FE-F02. |
| FE-F04 | FE-F01, стенд BE-D03 | READY | FE-F01 пройден; FE-F04 ожидает запуска. |
| FE-F05 | FE-F02, FE-F04 | BLOCKED | Ожидает FE-F02 и FE-F04. |
| FE-F06 | FE-F03, FE-F05 | BLOCKED | Ожидает FE-F03 и FE-F05. |

Источник порядка и зависимостей — [спецификация ленты](../../docs/specs/feed-v1/README.md). Спеки пакетов — `workflow/specs/feed-v1/`. Для запуска этой очереди переключить `spec_dir` в config.json на `workflow/specs/feed-v1`.

# Поэкранный UI-прогон · swipe-ui

| Экран | Зависимость | Статус | Ветка / worktree | Отчёт |
|---|---|---|---|---|
| 01 Открытия | — | INTEGRATED; final TESTING | `codex/ui-01-discovery` → `codex/ui-swipe-integration` | Пользователь подтвердил, что первая страница выглядит нормально; прежний staging-блокер и отчёты сохранены. Общий финальный прогон ещё нужен. |
| 02–03 | 02 → 03; затем отдельные свайп-экраны 04/05 | READY_FOR_INTEGRATION | `codex/ui-02-device-choice` / `/private/tmp/mobile-ui-02` | Commit `dabe2764ba3cc0938388f576195196bda5feb102`; build/host tests pass. Runtime and AC evidence blocked at sign-in (no authenticated fixture). |
| 04–05 | После 02–03 | TODO | отдельная ветка/worktree на экран | Swipe screens remain individual assignments, build and independent integrated gesture check. |
| 06–09 | После соответствующих экранов выбора и свайпа | TODO | grouped simple-state batch | Acceptance and exact dependency commits to be prepared at assignment. |
| 10–13 | 10 → 11 → 12 → 13; then separate swipe screens 14/15 | READY_FOR_INTEGRATION (13 target BLOCKED) | `codex/ui-10-room-create` / `/private/tmp/mobile-ui-10` | Commit `dd6014ed62d107caf2f10c7de61d7efb6d776d8f`; build/host tests pass. Runtime blocked at sign-in; 13 participant submit/wait/aggregation lacks target design and server contract. |
| 14–15 | После 10–13 | TODO | отдельная ветка/worktree на экран | Swipe screens remain individual assignments, build and independent integrated gesture check. |
| 16–17 | После 14/15 | TODO | grouped simple-state batch | Acceptance and exact dependency commits to be prepared at assignment. |
