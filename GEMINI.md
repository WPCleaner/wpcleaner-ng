# Project Context: WPCleaner-NG

The WPCleaner-NG project is a modular Spring boot application that is responsible for running some maintenance tasks on wikis relying on MediaWiki (like Wikipedia).

## Tech Stack & Architecture
- **Language:** Java (JDK 25)
- **Framework:** Spring boot 4.x
- **Build Tool:** Gradle (Kotlin DSL)
- **Testing:** JUnit 5, AssertJ, Mockito

## Development Lifecycle
- **Project Structure:** This is a multi-module Gradle project. Always verify the exact module name in `settings.gradle.kts` before running tasks.
- **Run Tests:** `./gradlew :<module-name>:test --tests <full.package.TestClass>` or `./gradlew :<module-name>:integrationTest`.
- **Formatting & Linting:**
    - Run `./gradlew :<module-name>:spotlessApply` before committing.
    - `./gradlew :<module-name>:pmdTest` and `./gradlew :<module-name>:pmdMain` are mandatory.
- **Full Build:** `./gradlew build`.

## Coding Standards & Mandates
- **Naming Conventions:**
    - **TESTS:** Must use `camelCase` only (e.g., `requestTokens`). **NEVER use underscores** in test method names (PMD violation).
- **Code Principles:**
    - **Records over Classes:** Prefer Java `record` for POJO to enforce immutability.
    - **Immutability:** Use `final` for local variables and parameters. Use immutable collections (e.g., `Map.of`).
    - **Surgical Updates:** Only modify what is strictly necessary. Avoid unrelated refactoring.
    - **Aversion to Comments:** Never use code comments. Prefer naming variables or methods.
    - **No var:** Usage of `var` is prohibited.
    - **Private Visibility:** Any method only used in its own class must be `private`.
    - **Composition over Inheritance:** Always prefer composition to inheritance.
    - **Deduplication:** Actively reduce duplication between related services.
    - **Vocabulary Consistency:** Avoid introducing new terms during refactoring; reuse existing wording as much as possible.
- **Testing Strategy:** Favor integration tests over unit tests.

## Operational Guidelines
- Never push a branch
- Never do a commit
- **Project Resolution:** If a Gradle task fails with "ambiguous matches," check the error output for the list of valid candidate modules.
- **Test Fixtures:** Use `testFixtures(project(...))` for shared test data across modules.

## File edition guidelines
- Use only `printf` to write files
