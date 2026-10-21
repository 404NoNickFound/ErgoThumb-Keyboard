# Roadmap

This document lists **intentions**, not commitments. Versions are containers
of features, not dates: when a version is ready depends on testing, stability,
and free time — there are no ETAs here on purpose. If a feature moves to a
later version, that is normal and not a delay.

The roadmap exists so contributors can see whether a proposed change fits the
current plan, and so users can tell whether the project is alive: check the
commit history of this file. An updated roadmap means an active maintainer,
even between releases.

## 0.1.0 — current release

First public version. Everything listed in the release notes ships here.
No new features will be added to 0.1.x; only hotfixes for crashes, data loss,
or security issues.

## 0.2.0 — polish and comfort

- **Emojis.**
- **Improved landscape layout.**
- **Move the cursor slider to the center.**
- **Haptic feedback**, via `View.performHapticFeedback` (no permission).
  Custom vibration patterns are deliberately out of scope: they would require
  the `VIBRATE` permission, which breaks the project's zero-permission rule.
- **Faster erase** with an extra action (a stronger gesture or a dedicated
  control, to be designed).

## 0.3.0 — customization, portability, one-hand

- **New gestures** — currently: shake to move the keyboard side-to-side, and
  collapse the two 3×3 grids into a single 3×3 + space (one-hand mode: toggle,
  no timeout, accelerometer only, no permissions). The category may grow with
  more gestures later.
- **Layout editor**: rearrange any letter (centers and corners) across keys.
  Letters only, any language — no digits or symbols. Reset-to-default included.
- **Importable/exportable layout presets (JSON)**: community layouts for
  regional letter placements. The layout editor exports to this format, so the
  two land together.
- **Export/import of user data**, with an element picker: full configuration,
  appearance, functionality, and dictionary/learning data, chosen independently
  per action; on import, an optional "delete file after import".
  *Shipping this requires updating `docs/PRIVACY.md`, which today states the
  app has no export mechanism. Exported learning data contains the user's
  vocabulary; the policy must say so and place responsibility for the exported
  file on the user.*
- **Learning-management UI**: view, search, and clear the personal dictionary,
  blacklist, learned substitutions, demoted corrections, and bigram statistics.
  Today these are only reachable through suggestion-strip gestures at the
  moment a word appears; a transparent screen makes the privacy promise
  auditable and pairs naturally with the export/import above.
- **Extended character coverage**: audit which extended-ASCII / commonly used
  characters are present in the layout and fill any gaps.
- **Community website** for languages and presets.

## 0.4.0 — intelligence

- **Presets suggested by bigrams**: derive an optimal layout from the user's
  own typing statistics. Lands in 0.4.0, or splits into 0.5.0 if 0.4.0 grows
  heavier than expected.

## Backlog (no version assigned)

Ideas that fit the project but have no version yet. They get promoted to a
version when they are ready to be scoped; until then they are not promises.

- **Themed icons**: a proper monochrome layer for Material You tinting.
  Verifiable only on Android 13+; ships when someone with a compatible device
  can test it.
- **Mutable word scoring** in the spell engine: corrections lose weight when
  undone, personal words gain it. Today the undo block is binary (never /
  demoted). Candidate for 0.2.0 or 0.3.0; needs a data-format decision first.

## Pull requests

**Always welcome, no discussion needed:** translations, bug fixes with tests,
documentation, small UX tweaks that do not touch the engine.

**Discuss first (open an issue before coding):** anything touching
`SpellEngine`, the `Prefs` keys or file formats, the layout system, or the
gesture model. These are the parts where a well-meaning change can break data
compatibility or the interaction contract with users.

**Never:** new permissions, network code, analytics, or cloud sync. These are
identity constraints, not preferences — see [docs/PRIVACY.md](PRIVACY.md).

**Big ideas not on this roadmap** (for example, a plugin system or community
infrastructure): open an issue anyway. "Not now" is not a rejection; it is a
signal that the idea needs scoping before it can be scheduled.
