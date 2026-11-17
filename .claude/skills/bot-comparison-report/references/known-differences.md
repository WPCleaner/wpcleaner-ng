# Known differences

Causes already established in earlier comparisons. Check a new difference against this list first:
if the pattern matches, cite it instead of investigating again, and re-check only that it still applies
(it may have been fixed). Update this file at the end of each comparison: add new causes, mark fixed ones.

Format: `algo – pattern` → classification, how to recognize it, first seen.

## NG defects

- 004 – `<A, B>`, `<a, d>` (math notation) reported as an `<a>` tag → NG false positive. frwiki 2026-11.
- 008 – unbalanced title `=== Notes ==`, `== Histoire =` not reported (NG skips any line MediaWiki parses as a heading; legacy reports closing level < opening level) → NG miss. frwiki 2026-11.
- 010 – FIXED: mixed-case file namespace `[[FIchier:…]]`, `[[IMage:…]]` not recognized as image → false unclosed `[[`. frwiki 2026-11.
- 010 – template in a link target `[[Lee Enfield {{n°|4}}|…]]`, `[[{{1er|régiment}} …]]` accepted by NG, reported by legacy → NG miss (legacy "only" pages with text `[[`). frwiki 2026-11.
- 002 – `<hr width="100">` not reported (NG checks attributes on `<br>` only); `<hr width="100" / >` reported with truncated text `<hr ` → NG miss / bad text. frwiki 2026-11.
- 064 – `&nbsp;`, thin or narrow space in link text treated as a space (`[[12 mars|12&nbsp;mars]]`), replacement drops it → NG false positive. frwiki 2026-11.
- 090 – revision link `index.php?oldid=…` without title not reported → NG miss. frwiki 2026-11.
- 091 – piped external link `[https://en.wikipedia.org/wiki/X|Text]`, `lien=https://de.wikipedia.org/…` in gallery, URL without protocol not reported → NG miss. frwiki 2026-11.

## NG right, legacy wrong

- 002 – tag inside `<nowiki>` reported by legacy only.
- 005 – `<!--->`, `<!-->` do not close the comment (MediaWiki searches `-->` after `<!--`) → NG only.
- 010 – line break inside a link target `[[Altair 8800⏎|Altair]]` → NG only (bulk of NG-only 010).
- 017 – duplicate category written with an entity (`R&amp;B` vs `R&B`) → NG only.
- 048 – self-link with leading colon on category pages `[[:Catégorie:X|…]]` (legacy compares with the colon) → NG only, namespace 14.
- 048 – self-link in a tag attribute (`<gallery caption="…[[X]]…">`) → NG only.
- 064 – `<br>` at the end of link text `[[X|X<br>]]` (legacy off-by-one `length() - 3`) → NG only.

## Scope decisions (to ask the maintainer, not to fix)

- 002 – line break inside a void tag `<br⏎/>`, `<br /⏎>` → NG only.
- 064 – link to another namespace / leading colon `[[:Catégorie:X|Catégorie:X]]`; comment inside the link; category sort key equal to the category name → NG only.
- 067 – `?` or `!` before a reference (astronomy infoboxes `Sdm?<ref name="ned"/>`); punctuation between references in a list-defined block `{{Références|références=…}}` → NG only, Check Wiki lists almost none.
- 086/090 – wikitext pasted from rendered HTML `[[//fr.wikipedia.org/wiki/Fichier:…|lien=…]]` reported under both.
- 087 – URL parameters outside parsed external links (`&number=`, `&level=`, `&pid=`, `&region=`), file names, plain text `Text&Music` → same prefix logic as legacy, but legacy reports them as warnings and NG has no level.
- 091 – file description pages (`Source = https://en.wikipedia.org/wiki/File:…`), ~80% of NG pages on frwiki.
- Whitelists – legacy does not apply whitelist entries to titles with a namespace (category pages listed by legacy, whitelisted for NG: "page whitelisted for NG").

## Timing and stale data

- Check Wiki 003 – listed pages without any `<ref>`: stale Check Wiki entries. frwiki 2026-11.
- Check Wiki 011 – entity inside a URL; NG and legacy both ignore URLs. frwiki 2026-11.
- "page edited after NG run", "no error at all in content file (never re-checked)", "page absent from content file", "fixed since", "legacy listed it first, removed at a later re-check", "error introduced after the dump" → timing, no investigation needed.

## Reporting format (same page, different text)

- 010 – `[[Catégorie:X|Y]`: NG reports the broken link, legacy the next one; NG reports the whole image when its caption has an unpaired bracket.
- 017 – NG reports only the duplicate occurrence, legacy every occurrence.
- 064 – legacy extends the text to following punctuation and quotes (`[[X|''X'']]'',''`).
- 090 – legacy includes a trailing `.` or `)` in the URL, and the `text:` part of timeline entries.
