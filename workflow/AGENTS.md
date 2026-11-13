

<!-- spec-pipeline-routing-v1 -->
## Specification pipeline
When explicitly asked to process the specification queue, read workflow/QUEUE.md. Setup alone does not start development. Read workflow/rules/index.md for applicable project rules; load only the current role and relevant rules. Do not load source, diffs or Developer history into PM or Tester contexts.

## Дизайн и UX
Перед проектированием или изменением продуктового интерфейса прочитай [правила дизайна](workflow/rules/design.md) и [персоны и пользовательские маршруты](docs/design/ux-personas-scenarios.md). Описания текущих экранов и ссылки на макеты собраны в [документации дизайна](docs/design/README.md).

## Мобильная разработка
Перед изменением мобильного приложения прочитай применимые правила из [индекса правил](workflow/rules/index.md):

- [архитектура мобильного приложения](workflow/rules/mobile-architecture.md) — слои KMP, DI, сеть, UI Kit и тесты;
- [документация фич](workflow/rules/feature-docs.md) — обязательный `FEATURE.md` и его актуализация вместе с кодом;
- [правила дизайна](workflow/rules/design.md) — пользовательские сценарии, компоненты, токены и документация экранов;
- [тестирование](workflow/rules/testing.md) — принадлежность тестов и неизменяемые отчёты прогонов.
