# FE-F03

Мобильный пакет (KMP, Android/iOS). Обрабатывается только раздел FE-F03 в [implementation.md](../../../docs/specs/feed-v1/implementation.md#fe-f03--одно-устройство-пул-и-результат) и общие требования этого документа (доменная модель, компоненты UI Kit, готовность пакета).

Порядок и зависимости описаны в [README ленты](../../../docs/specs/feed-v1/README.md). Пакет зависит от FE-F02 и готового стенда BE-D03. Критерии F03-01 … F03-04 берутся из [acceptance.md](../../../docs/specs/feed-v1/acceptance.md).

Серверное поведение не реализуется: границей служат [контракт Discovery v1](../../../docs/contracts/discovery-v1.md) и [мобильный HTTP-контракт](../../../docs/contracts/mobile-api.md). Применяются правила [mobile-architecture.md](../../rules/mobile-architecture.md) и [feature-docs.md](../../rules/feature-docs.md).
