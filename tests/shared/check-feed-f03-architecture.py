#!/usr/bin/env python3
"""Source-level architecture checks for Android FE-F03 in the shared feed feature."""
from pathlib import Path
import re

root = Path("shared/src/commonMain/kotlin/fefu/prime/srastem")
feed = root / "feed"
domain = feed / "domain"
data = feed / "data"
presentation = feed / "presentation"
tests = Path("shared/src/commonTest/kotlin/fefu/prime/srastem/feed")


def read(path: Path) -> str:
    assert path.is_file(), f"missing required file: {path}"
    return path.read_text()


def require(source: str, path: Path, *tokens: str) -> None:
    missing = [token for token in tokens if token not in source]
    assert not missing, f"{path}: missing {missing}"


def imports(path: Path) -> list[str]:
    return [line.removeprefix("import ") for line in read(path).splitlines() if line.startswith("import ")]


# Domain owns abstractions and remains platform/transport independent.
repository = domain / "FeedRepository.kt"
repository_source = read(repository)
require(repository_source, repository, "interface FeedRepository", "getCandidates", "getCatalogDetails")
for path in domain.glob("*.kt"):
    for imported in imports(path):
        assert imported.startswith(("kotlin.", "kotlinx.coroutines.")), f"domain depends on external/platform layer: {path}: {imported}"

details_case = domain / "GetCatalogDetailsUseCase.kt"
details_case_source = read(details_case)
require(details_case_source, details_case, "class GetCatalogDetailsUseCase", "FeedRepository", "operator fun invoke")
assert details_case_source.count("operator fun invoke") == 1, details_case

# Data implements domain interfaces and owns HTTP/DTO mapping.
backend_feed = data / "BackendFeedRepository.kt"
backend_feed_source = read(backend_feed)
require(backend_feed_source, backend_feed, "BackendApi", ": FeedRepository", "FeedMappers", "getCatalogDetails", "/v1/catalog/", "api::resolveMediaUrl")
mapper = data / "FeedMappers.kt"
require(read(mapper), mapper, "fun details(json: JsonObject)", "CatalogDetails(")

# Presentation depends on use cases and UI Kit; it must not reach through to transport/data.
view_model = presentation / "FeedModeViewModel.kt"
view_model_source = read(view_model)
require(view_model_source, view_model, "StateFlow<FeedModeState>", "GetFeedCandidatesUseCase", "GetCatalogDetailsUseCase")
for path in presentation.glob("*.kt"):
    source = read(path)
    for imported in imports(path):
        assert ".feed.data." not in imported, f"presentation imports data implementation: {path}: {imported}"
        assert ".backend." not in imported, f"presentation imports transport: {path}: {imported}"
        assert not imported.endswith(".FeedRepository"), f"presentation imports repository boundary directly: {path}: {imported}"
    assert "/v1/" not in source, f"presentation contains endpoint path: {path}"

screen = presentation / "FeedModeScreen.kt"
screen_source = read(screen)
require(
    screen_source,
    screen,
    "SrTopBar",
    "SrButton",
    "SrChip",
    "SrVoteCard",
    "SrVoteActions",
    "SrColors",
    "SrShapes",
    "SrType",
    "OpenCatalogDetails",
    "FeedCover(cover, chosen.title",
    "AsyncImagePainter.State.Error",
    "Обновляем подборку",
    "Больше вариантов нет",
)
assert not re.search(r"Color\(0x[0-9A-Fa-f]+\)", screen_source), f"hardcoded color in {screen}"

# DI remains in the existing app container; tests mirror touched layers.
container = root / "di/AppContainer.kt"
require(read(container), container, "createFeedModeViewModel", "GetCatalogDetailsUseCase(feedRepository)", "getCandidates = getFeedCandidates")
for layer, name in (("domain", "FeedUseCasesTest.kt"), ("data", "FeedMappersTest.kt"), ("presentation", "FeedModeViewModelTest.kt")):
    path = tests / layer / name
    read(path)
require(read(tests / "presentation/FeedModeViewModelTest.kt"), tests / "presentation/FeedModeViewModelTest.kt", "firstLikeImmediatelyShowsChosenResult", "skippingLastTenCandidatesAutomaticallyLoadsNextCursorPage")
require(read(tests / "data/FeedMappersTest.kt"), tests / "data/FeedMappersTest.kt", "fullCatalogDetailsSupportsPlaceWrapperAndEventDto")

print("PASS: FE-F03 domain/data/presentation boundaries, catalog detail use case and mapping, AppContainer DI, UI Kit usage, and test placement/coverage checks")
