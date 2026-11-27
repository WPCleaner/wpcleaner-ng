# Known differences

Causes already established in earlier comparisons. Check a new difference against this list first:
if the pattern matches, cite it instead of investigating again, and re-check only that it still applies
(it may have been fixed). Update this file at the end of each comparison: add new causes, mark fixed ones.

Format: `algo – pattern` → classification, how to recognize it, first seen.

## NG defects

- 004 – FIXED (#234, tag name must end with whitespace, `/` or `>`): `<A, B>`, `<a, d>` (math notation) reported as an `<a>` tag → NG false positive. frwiki 2026-11.
- 013 – FIXED (#234): `#include <math.h>` reported as an unclosed `<math>` tag (MediaWiki renders it as text) → NG false positive. frwiki 2026-11 (2nd run).
- 022 – FIXED (#235): whitespace-only sort key `[[Catégorie:X| ]]` (idiom to sort first) reported: `stripTrailingWhitespace` turns `" "` into `""`; legacy skips keys without a non-space character → NG false positive, ~100% of NG pages. frwiki 2026-11 (2nd run).
- 025 – FIXED (#236): heading preceded by a comment on the same line `<!--x-->=== B ===` not seen (MediaWiki renders it as a heading) → false level jump on the next heading, NG false positive. frwiki 2026-11 (2nd run).
- 008 – FIXED (#237, longer closing run left to T446240): unbalanced title `=== Notes ==`, `== Histoire =` not reported (NG skips any line MediaWiki parses as a heading; legacy reports closing level < opening level) → NG miss. frwiki 2026-11.
- 010 – FIXED: mixed-case file namespace `[[FIchier:…]]`, `[[IMage:…]]` not recognized as image → false unclosed `[[`. frwiki 2026-11.
- 010 – FIXED (#238): template in a link target `[[Lee Enfield {{n°|4}}|…]]`, `[[{{1er|régiment}} …]]` accepted by NG, reported by legacy → NG miss (legacy "only" pages with text `[[`). frwiki 2026-11.
- 002 – FIXED (#240): `<hr width="100">` not reported (NG checked attributes on `<br>` only). Still open: `<hr width="100" / >` reported with truncated text `<hr ` → NG miss / bad text. frwiki 2026-11.
- 064 – `&nbsp;`, thin or narrow space in link text treated as a space (`[[12 mars|12&nbsp;mars]]`), replacement drops it → NG false positive. frwiki 2026-11.
- 090 – revision link `index.php?oldid=…` without title not reported → NG miss. frwiki 2026-11.
- 091 – piped external link `[https://en.wikipedia.org/wiki/X|Text]`, `lien=https://de.wikipedia.org/…` in gallery, URL without protocol not reported → NG miss. frwiki 2026-11.

## NG right, legacy wrong

- 002 – tag inside `<nowiki>` reported by legacy only.
- 005 – `<!--->`, `<!-->` do not close the comment (MediaWiki searches `-->` after `<!--`) → NG only.
- 014 – broken attribute quote `<syntaxhighlight lang="cpp>`…`</syntaxhighlight>` renders as paired tags; legacy reports the closing tag → legacy only. frwiki 2026-11 (2nd run).
- 010 – line break inside a link target `[[Altair 8800⏎|Altair]]` → NG only (bulk of NG-only 010).
- 017 – duplicate category written with an entity (`R&amp;B` vs `R&B`) → NG only.
- 018 – category starting with a letter without uppercase form (`[[Catégorie:ɮ]]`, `ʕ`) reported by legacy and Check Wiki → legacy only (inferred). frwiki 2026-11 (2nd run).
- 048 – self-link with leading colon on category pages `[[:Catégorie:X|…]]` (legacy compares with the colon) → NG only, namespace 14.
- 048 – self-link in a tag attribute (`<gallery caption="…[[X]]…">`) → NG only.
- 064 – `<br>` at the end of link text `[[X|X<br>]]` (legacy off-by-one `length() - 3`) → NG only.

## Scope decisions (to ask the maintainer, not to fix)

- 002 – line break inside a void tag `<br⏎/>`, `<br /⏎>` → NG only.
- 030 – deactivated on frwiki (priority 0 in `Projet:Correction syntaxique/Traduction`): no legacy page, Check Wiki 0, NG 135,000 pages (icons, flags, logos); NG also checked categories, legacy articles only (FIXED (#239): main namespace only). DECIDED: 030 removed from `check.yaml`. Other priority 0 algorithms may raise the same question. frwiki 2026-11 (2nd run).
- 064 – link to another namespace / leading colon `[[:Catégorie:X|Catégorie:X]]`; comment inside the link; category sort key equal to the category name → NG only.
- 067 – `?` or `!` before a reference (astronomy infoboxes `Sdm?<ref name="ned"/>`); punctuation between references in a list-defined block `{{Références|références=…}}` → NG only, Check Wiki lists almost none.
- 086/090 – wikitext pasted from rendered HTML `[[//fr.wikipedia.org/wiki/Fichier:…|lien=…]]` reported under both.
- 087 – URL parameters outside parsed external links (`&number=`, `&level=`, `&pid=`, `&region=`), file names, plain text `Text&Music` → same prefix logic as legacy, but legacy reports them as warnings and NG has no level.
- 091 – file description pages (`Source = https://en.wikipedia.org/wiki/File:…`), ~80% of NG pages on frwiki.
- Whitelists – legacy does not apply whitelist entries to titles with a namespace (category pages listed by legacy, whitelisted for NG: "page whitelisted for NG").

## Timing and stale data

- Check Wiki 003 – listed pages without any `<ref>`: stale Check Wiki entries. frwiki 2026-11.
- Check Wiki 011 – entity inside a URL; NG and legacy both ignore URLs. frwiki 2026-11.
- Check Wiki 027 – partial list (15 pages for 2,000+ NG detections of numeric entities `&#x2013;`, `&#201;`…). frwiki 2026-11 (2nd run).
- Check Wiki 028 – tables closed by templates (`{{Boîte déroulante/fin}}`, `{{Col-fin}}`, `{{…/fin}}`) counted as unclosed: Check Wiki false positives, ~1,800 pages. frwiki 2026-11 (2nd run).
- Check Wiki 566 – `action=list` returns HTTP 500 "invalid error id": write `[]` to `cw/566.json`. frwiki 2026-11 (2nd run).
- 003 – legacy reports the `<ref>` text, so a page fixed by adding `{{Références}}` after the legacy re-check is classified "NG DOES NOT DETECT": check the page history. frwiki 2026-11 (2nd run).
- "page edited after NG run", "no error at all in content file (never re-checked)", "page absent from content file", "fixed since", "legacy listed it first, removed at a later re-check", "error introduced after the dump" → timing, no investigation needed.

## Reporting format (same page, different text)

- 010 – `[[Catégorie:X|Y]`: NG reports the broken link, legacy the next one; NG reports the whole image when its caption has an unpaired bracket.
- 017 – NG reports only the duplicate occurrence, legacy every occurrence.
- 016 – legacy shows placeholders (`[SHY]`, `[BOM]`, `[NBSP]`), NG the raw character (also legacy `[NBSP]` in 003).
- 028 – legacy lists every `{|`, `|}` and closing template of the page, NG only the unclosed `{|`.
- 566 – NG also reports `<abbr>` containing a link, long `<abbr>`, and `title="A>B` attribute values.
- 064 – legacy extends the text to following punctuation and quotes (`[[X|''X'']]'',''`).
- 090 – legacy includes a trailing `.` or `)` in the URL, and the `text:` part of timeline entries.
