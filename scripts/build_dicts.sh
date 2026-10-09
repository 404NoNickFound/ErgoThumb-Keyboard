#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p app/src/main/assets/dicts tmp
base=https://raw.githubusercontent.com/hermitdave/FrequencyWords/master/content/2018
for L in ca es en; do
  curl -sL "$base/$L/${L}_50k.txt" -o "tmp/${L}_raw.txt"
done
python3 - <<'PY'
import gzip
for L in ["ca", "es", "en"]:
    freq = {}
    with open(f"tmp/{L}_raw.txt", encoding="utf-8") as f:
        for line in f:
            parts = line.split()
            if len(parts) < 2: continue
            w, c = parts[0].lower(), parts[1]
            if not all(ch.isalpha() or ch in "'·-" for ch in w): continue
            if len(w) < 2 or not any(ch.isalpha() for ch in w): continue
            try: n = int(c)
            except ValueError: continue
            freq[w] = freq.get(w, 0) + n
    items = sorted(freq.items(), key=lambda kv: -kv[1])
    with gzip.open(f"app/src/main/assets/dicts/{L}.txt.gz", "wt", encoding="utf-8") as out:
        for w, n in items:
            out.write(f"{w} {n}\n")
    print(L, "paraules:", len(items))
PY
rm -rf tmp
ls -lh app/src/main/assets/dicts/
