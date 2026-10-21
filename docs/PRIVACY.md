# Privacy Policy — ErgoThumb

Last updated: February 2026

ErgoThumb is an Android keyboard built privacy-first. This policy explains,
in plain language, exactly what the app stores and what it never does.

## Permissions

ErgoThumb requests **no Android permissions** at all. You can verify this in
`app/src/main/AndroidManifest.xml`: there is no `<uses-permission>` element.

## Network access

ErgoThumb contains **no network code**. It never connects to the internet:
no analytics, no crash reporting, no advertising, no accounts, no cloud
sync. The app uses standard AndroidX/Material components for its user
interface; none of them is configured to transmit any data.

## Data stored on your device

All data lives in the app's private storage and is **removed when you
uninstall the app**. Each item below is stored only if you enable the
corresponding feature:

- **Letter-pair statistics (bigrams)** — counts of which letters you type
  after which, used to rank suggestions.
  Opt-in through an explicit warning dialog; stored as a plain text file.
- **Next-word prediction pairs** — which words you type after which.
  Opt-in through an explicit warning dialog.
- **Personal dictionary and blacklist** — words you swipe right (keep) or
  left (ban) on the suggestion strip.
- **Clipboard history** — recent copies shown in the third-cell menu.
  Entries expire automatically after one hour of disuse. Nothing is ever
  recorded from password fields or fields marked as sensitive by the
  system, and the on-screen notice "Sensitive field: nothing is stored"
  appears whenever such a field is focused.
- **Settings** — colors, geometry, animation scale, chosen languages.

## What never happens

- No data ever leaves your device.
- No keystrokes are logged beyond the on-device learning described above.
- No content from password or sensitive fields is stored or learned from.
- No third party (including the developer) can access any of this data.

## Changes to this policy

Any change will be reflected in this file with an updated "Last updated"
date, and mentioned in the release notes.

## Contact

Open an issue at
<https://github.com/404NoNickFound/ErgoThumb-Keyboard/issues>

© 2026 404NoNickFound
