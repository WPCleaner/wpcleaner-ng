---
name: mediawiki-action-query-api
description: Implement a call to one module of the MediaWiki action query API (list=, prop=, meta=) in wpcleaner-api. Use when the user asks to implement or support a MediaWiki action=query API module.
argument-hint: <modulename>
---

# MediaWiki action query API module

Implement a call to the MediaWiki `action=query` API module `$ARGUMENTS` in `:wpcleaner-api`.
In these instructions, the example module name (`modulename`) is `users`.

## Before writing code

Read an existing, complete implementation and mirror its structure exactly (naming, annotations,
`ApiRestClient` / `ApiUriBuilder` usage, error handling, tests). Good references:

- `wpcleaner-api/src/main/java/org/wpcleaner/api/api/query/list/random/`
- `wpcleaner-api/src/test/java/org/wpcleaner/api/api/query/list/random/ApiRandomTest.java`
- `wpcleaner-api/src/main/java/org/wpcleaner/api/api/query/QueryParameters.java`

## Steps

1. **Fetch the documentation** with WebFetch:
   `https://www.mediawiki.org/w/api.php?action=help&modules=query%2B<modulename>`
   (e.g. `...modules=query%2Busers`). Extract the `h2` title, of the form
   `<actiontype>=<modulename> (<moduleprefix>)` — for the example `list=users (us)`:
   action type `list`, module prefix `us`. Also extract every parameter, its allowed values,
   and whether it is deprecated.

2. **Capitalized module name** `ModuleName`: split into words and capitalize each one
   (`users` → `Users`, `deletedrevisions` → `DeletedRevisions`).

3. **Package**: `org.wpcleaner.api.api.query.<actiontype>.<modulename>`
   (e.g. `org.wpcleaner.api.api.query.list.users`). Test classes go in the same package under `src/test`.

4. **Register the module** in the existing `QueryParameters`.

5. **Parameters enum** `<ModuleName>Parameters` (e.g. `UsersParameters`) listing all module parameters,
   with nested enums for parameters having a fixed list of values. Item naming:
   - remove the module prefix (`usattachedwiki` → `attachedwiki`)
   - split the words (`attached wiki`)
   - expand abbreviations when useful (`prop` → `PROPERTIES`)
   - convert to `UPPER_SNAKE_CASE` (`ATTACHED_WIKI`)

   Sort enum items alphabetically, and sort nested enums alphabetically.

   **Deprecated parameters must not appear anywhere**: not in enums, parameter records or the service.

6. **Response records** with Jackson annotations modeling the response.
   When the module has a properties parameter (like `usprop`), call the real API with every possible
   value: each one may add fields to the response. Order record components and methods alphabetically.

7. **Parameters record** when the module has more than 5 parameters: one record grouping all of them
   (components sorted alphabetically), with a nested `Builder` (fields sorted alphabetically, then setter
   methods sorted alphabetically). A limit parameter (like `uslimit`) uses `Limit`, never `Integer`,
   because the value can be a number or the literal `max`.

8. **Service class** `Api<ModuleName>` (e.g. `ApiUsers`).

9. **Integration tests** `Api<ModuleName>Test`, annotated with `@TestCallingMWApi`.

## Verify

```bash
./gradlew :wpcleaner-api:spotlessApply
./gradlew :wpcleaner-api:pmdMain :wpcleaner-api:pmdTest
./gradlew :wpcleaner-api:apiTest --tests org.wpcleaner.api.api.query.<actiontype>.<modulename>.Api<ModuleName>Test
```

`apiTest` calls a real wiki; `@TestCallingMWApi` tests are excluded from the regular `test` task.
Fix every failure before reporting the work as done, and report the test results.
