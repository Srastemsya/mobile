# FE-F06

Мобильный пакет (KMP, Android/iOS). Обрабатывается только раздел FE-F06 в [implementation.md](../../../docs/specs/feed-v1/implementation.md#fe-f06--исчерпание-выдачи-и-ошибки) и общие требования этого документа (доменная модель, компоненты UI Kit, готовность пакета).

Порядок и зависимости описаны в [README ленты](../../../docs/specs/feed-v1/README.md). Пакет зависит от FE-F03 и FE-F05. Критерии F06-01 … F06-03 берутся из [acceptance.md](../../../docs/specs/feed-v1/acceptance.md).

Серверное поведение не реализуется: границей служат [контракт Discovery v1](../../../docs/contracts/discovery-v1.md) и [мобильный HTTP-контракт](../../../docs/contracts/mobile-api.md). Применяются правила [mobile-architecture.md](../../rules/mobile-architecture.md) и [feature-docs.md](../../rules/feature-docs.md).
