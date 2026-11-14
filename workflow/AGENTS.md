## Specification pipeline
When explicitly asked to process the specification queue, read `workflow/QUEUE.md`. Setup alone does not start development. Read `workflow/rules/index.md` for applicable project rules and load only the active role and relevant rules.

Only two active workflow roles exist: PM and Developer. PM owns scope, acceptance criteria, documentation review and final implementation review. Developer owns implementation, documentation updates and developer checks. Do not create or hand work to a Tester role; PM reviews the Developer's report and implementation directly. Historical reports that mention Tester remain historical evidence and are not active workflow instructions.

## Дизайн и UX
Перед проектированием или изменением продуктового интерфейса прочитай [правила дизайна](workflow/rules/design.md) и [персоны и пользовательские маршруты](docs/design/ux-personas-scenarios.md). Описания текущих экранов и ссылки на макеты собраны в [документации дизайна](docs/design/README.md).

## Мобильная разработка
Перед изменением мобильного приложения прочитай применимые правила из [индекса правил](workflow/rules/index.md):

- [архитектура мобильного приложения](workflow/rules/mobile-architecture.md) — слои KMP, DI, сеть, UI Kit и тесты;
- [документация фич](workflow/rules/feature-docs.md) — обязательный `FEATURE.md` и его актуализация вместе с кодом;
- [правила дизайна](workflow/rules/design.md) — пользовательские сценарии, компоненты, токены и документация экранов;
- [тестирование](workflow/rules/testing.md) — проверки Developer и сохранение неизменяемых отчётов.
