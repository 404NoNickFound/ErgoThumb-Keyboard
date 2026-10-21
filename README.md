# ErgoThumb

A thumb-first Android keyboard: two 3×3 letter grids, a central space, and
gestures instead of a sea of keys. No permissions. No network. Everything
on-device.

## What it is

ErgoThumb splits the keys into two 3×3 grids, one per thumb, with a central
space pill. Every key holds a **center letter** (tap) and up to eight
**corner mini-letters** (swipe from the center toward a corner). A suggestion
strip, a clipboard history menu, a language capsule and a cursor slider
complete the layout. In landscape the space becomes two keys with a hole
between them, one per thumb.

## Features

- Dual 3×3 grid + central space; landscape variant with two space keys
- 8-direction swipe letters per key, with visible per-key hints
- Suggestion strip: swipe a suggestion right = personal dictionary,
  left = blacklist + purge from learning
- Clipboard history (expires after 1 h of disuse) with trash management
- Language capsule cycling through your chosen subtypes (ca / es / en)
- Cursor slider; double-tap opens number and symbol layers
- Local learning: letter-pair (bigram) stats and next-word prediction,
  stored on-device, each independently switchable
- Spelling corrector with undo-on-delete and a per-word no-autocorrect list
- Deep theming: per-mode colors, optional custom accent, geometry sliders
  (height, side margins, key gap), animation scale with 0 = animations off
- Onboarding + guided tutorial, replayable from Settings → Information
- Proportional landscape layout (compact metrics, centered content)

## Privacy

ErgoThumb requests **no permissions** and contains **no network code**.
Learning data (bigrams, next-word pairs, personal dictionary, blacklist,
clipboard history) lives in the app's private storage and is removed when
you uninstall it. Full policy:
[Privacy policy](https://github.com/404NoNickFound/ErgoThumb-Keyboard/blob/main/docs/PRIVACY.md)

## Install

- **GitHub Releases**: signed APKs —
  <https://github.com/404NoNickFound/ErgoThumb-Keyboard/releases>
- **IzzyOnDroid**: pending inclusion
- **F-Droid**: pending inclusion

After installing, enable *ErgoThumb Keyboard* in
*System settings → Languages & input → On-screen keyboard*, then select it.

## Build from source

Requires Android Studio (recent) or a JDK 17 command line.

    git clone https://github.com/404NoNickFound/ErgoThumb-Keyboard.git
    cd ErgoThumb-Keyboard
    ./gradlew installDebug

Then enable the keyboard as described above.

## Translations and layouts

Translations live in `app/src/main/res/values*/`; partial translations are
welcome (the default locale, Catalan, is the fallback for missing strings).
Community layout presets (regional letter placements) are planned as
importable JSON variants; see the
[roadmap](https://github.com/404NoNickFound/ErgoThumb-Keyboard/blob/main/docs/ROADMAP.md)
for the version they land in. See
[CONTRIBUTING.md](https://github.com/404NoNickFound/ErgoThumb-Keyboard/blob/main/CONTRIBUTING.md)
for how to propose one.

## Licenses

- Code: GPL-3.0-or-later — see [LICENSE](LICENSE)
- Outfit typeface: SIL Open Font License 1.1
- Word frequency lists: derived from the FrequencyWords project

© 2026 404NoNickFound

## Known quirks

- On MIUI/HyperOS you may need to allow the keyboard in
  *Settings → Apps → Permissions* and disable battery optimization for it,
  or the input-method picker may not appear reliably.
- The tutorial runs in portrait by design; the keyboard itself supports
  landscape.
