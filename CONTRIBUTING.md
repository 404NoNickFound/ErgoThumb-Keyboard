# Contributing to ErgoThumb

Thank you for your interest! ErgoThumb is a small, privacy-first keyboard,
and contributions of any size are welcome.

## Ways to contribute

- **Report bugs or propose features** via
  [issues](https://github.com/404NoNickFound/ErgoThumb-Keyboard/issues).
  For bugs, include your Android version, device, and the steps to
  reproduce.
- **Translate** the interface (see below).
- **Propose layout presets** for your language or region (regional letter
  placements). Presets are planned as importable JSON variants; until then,
  open an issue describing the layout you need.
- **Contribute code** (see development setup).

## Translations

The default locale is **Catalan** (`app/src/main/res/values/`). Spanish and
English are maintained alongside it. Other languages, and partial
translations, are very welcome: missing strings simply fall back to the
default locale at runtime.

1. Copy `app/src/main/res/values/strings.xml` to
   `app/src/main/res/values-XX/strings.xml` (`XX` = ISO 639-1 code; use
   `values-XX-rRR` for regional variants).
2. Translate as many strings as you like. If you are unsure about a string,
   leave it untranslated rather than guessing.
3. Keep placeholders intact (`%1$s`, `%2$d`, …) and escape apostrophes as
   `\'` inside Android string resources.
4. Open a pull request titled `Add/Update XX translation`.

Notes for translators: the `MissingTranslation` lint check is disabled by
design (partial locales are supported); `ExtraTranslation` remains an error,
so never add a string to a locale without adding it to the default locale
first.

## Development setup

- JDK 17 and a recent Android Studio, or a command line with the Android SDK.
- Clone, build and install:
      git clone https://github.com/404NoNickFound/ErgoThumb-Keyboard.git
      cd ErgoThumb-Keyboard
      ./gradlew installDebug
- Then enable *ErgoThumb Keyboard* in *System settings → Languages & input →
  On-screen keyboard* and select it. The in-app harness ("Prova el teclat
  aquí") is the fastest way to test without leaving the app.

## Ground rules for code

- **No new Android permissions and no network code, ever.** This is a hard
  project rule; PRs that introduce either will not be accepted.
- Keep changes small and focused: one logical change per pull request.
- User-visible text goes into `strings.xml` (all maintained locales); code
  comments and documentation are in English.
- The keyboard draws everything itself (`onDraw`); when touching layout
  code, check both portrait and landscape, and the animation-scale-0 case.

## Pull request process

Fork the repository, create a branch, and open a PR against `main`.
Describe what changed and why; attach screenshots or a short screen record
for UI changes. The maintainer (@404NoNickFound) will review it.

## License

By contributing, you agree that your contributions are licensed under the
same terms as the project: GPL-3.0-or-later for code, SIL OFL 1.1 for the
bundled typeface.

© 2026 404NoNickFound
