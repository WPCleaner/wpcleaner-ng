# WPCleaner-NG

Modular Spring Boot application running maintenance tasks on MediaWiki wikis (like Wikipedia).
Full rewrite of the original [WPCleaner](https://github.com/WPCleaner/wpcleaner).

## Tech stack

- Java 25 (see `.sdkmanrc`), Spring Boot 4.x, JavaFX GUI
- Gradle with Kotlin DSL; conventions and quality plugins live in `build-logic/`
- JUnit 5, AssertJ, Mockito
- Internationalization with gettext (`wpcleaner-translations`)

## Modules

Modules are discovered automatically by `settings.gradle.kts` and are **always addressed by their
folder name only**, even when nested:

- `:wpcleaner-api` – MediaWiki API client (`org.wpcleaner.api.api.*`)
- `:wpcleaner-application-base`, `:wpcleaner-application-gui` (under `wpcleaner-application/`)
- `:wpcleaner-lib-image` (under `wpcleaner-lib/`)
- `:wpcleaner-dependencies` – enforced platform (dependency versions)
- `:wpcleaner-translations`

Use `:wpcleaner-lib-image:test`, not `:wpcleaner-lib:wpcleaner-lib-image:test`.
If Gradle reports "ambiguous matches", the error lists the valid module names.

## Commands

- Single test: `./gradlew :<module>:test --tests <full.package.TestClass>`
- Tests calling a real MediaWiki server (`src/apiTest` source set, not run by `test` nor `build`):
  `./gradlew :wpcleaner-api:apiTest --tests <full.package.TestClass>`
- Format: `./gradlew :<module>:spotlessApply` (run before declaring work done)
- Static analysis (mandatory, must pass): `./gradlew :<module>:pmdMain :<module>:pmdTest`
- Full build: `./gradlew build`

## Coding standards

- **No `var`**: always declare explicit types.
- **`final` everywhere**: local variables and parameters.
- **Records over classes** for data carriers; immutable collections (`List.of`, `Map.of`).
- **No code comments**: express intent through names of variables, methods and types.
  The SPDX license header at the top of each file is the only exception — copy it from a sibling file.
- **Private by default**: a method used only in its own class must be `private`.
- **Composition over inheritance.**
- **Imports over fully qualified names**, even if a PMD warning must be suppressed.
- **Reuse existing vocabulary**: don't introduce new terms when refactoring; name things like the surrounding code.
- **Deduplicate** actively between related services.
- **Surgical changes**: modify only what the task requires; no unrelated refactoring.
- **Test method names**: `camelCase` only (e.g. `requestTokens`), never underscores.

## Testing

- Favor integration tests over unit tests.
- Use `BDDMockito` instead of `Mockito` (`given`/`willReturn`, `then(...).should()`).
- Share test data across modules with `testFixtures(project(...))` (see `wpcleaner-api/src/testFixtures`).

## Git

- Never commit and never push: leave changes in the working tree for the maintainer to review.

## Project skills

- `mediawiki-action-query-api` – implement a MediaWiki `action=query` module in `wpcleaner-api`
- `phabricator` – fetch and summarize a Phabricator task (`T123456`)
- `manage-images` – fetch SVGs from Wikimedia Commons, render PNGs, update `ImageCollection`
- `wiki-documentation` – check the user documentation on `Wikipedia:WPCleanerNG` against the code and draft wikitext
