# ReFind — Intelligent Lost & Found

> From lost to found — automatically.

ReFind is a mobile application that turns lost-and-found from a *searching problem* into a
*matching problem*. Users report lost or found items with a photo, time and location; the
app uses **image recognition and contextual matching** (category, colour, location, time)
to pair reports automatically. Both parties are notified on a strong match, and the owner
must pass a **private verification question** before a return is arranged — so finders
never hand items to strangers who simply saw the post.

**Free for individual users. Organisations (campuses, malls, transport, events) subscribe
to a management console with analytics.**

## Why

Traditional lost-and-found is passive and fragmented: lost-property desks are warehouses
you have to call, and social-media posts drown in unrelated chatter with no matching and
no trust layer. Nobody connects "lost" and "found" reports of the same item — the burden
falls entirely on the person who already lost something. ReFind closes that loop with AI
matching plus an anti-fraud verification gate.

## Key Features

- **Photo-first reporting** — photo + auto-filled time/location + private detail notes,
  in under a minute.
- **AI matching engine** — pre-trained image embeddings (CLIP-style, on-device via
  TensorFlow Lite with server fallback) combined with contextual filters and a composite
  confidence score.
- **Proactive notifications** — the system tells both sides; the loser never has to
  search.
- **LLM-generated verification questions** — item-specific questions generated from the
  owner's *private* details gate every claim.
- **Incentive layer** — optional owner-set rewards held in escrow and released on verified
  handover, plus merchant-sponsored ReFind points. The escrow model closes both abuse
  levers: hostage pricing ("pay more or no phone") and theft-for-reward (anomaly detection
  and security-point settlement for high-value items).
- **In-app chat & handover confirmation** — with safe handover points on campus.
- **Organisation console (B2B)** — item intake, storage tracking, moderation workflow,
  return-rate analytics for campus security / venue staff.

## Roadmap

| Phase | Timing | Goal |
| --- | --- | --- |
| M1 — Design | Months 1–2 | Prototype + offline matching demo |
| M2 — MVP | Months 3–5 | Android app: report, match, notify |
| M3 — Trust layer | Months 6–7 | Verification questions, escrowed rewards, chat, org console v1 |
| M4 — Campus pilot | Month 8+ | Flagship campus live with security office |
| M5 — Commercialise | Year 2 | First paying organisations, iOS client |

## Repository Layout

```text
docs/
  business-plan.tex         # Business plan source (LaTeX)
  business-plan.pdf         # Compiled business plan
  presentation/             # Phase-2 slides, demo video, screenshots
app/                        # Android app — Jetpack Compose UI prototype
```

## Running the UI prototype (Phase 2)

```bash
ANDROID_HOME=$HOME/Library/Android/sdk ./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.refind.app/.MainActivity
```

The prototype is a single-Activity Compose app with a demo campus story:
sign in → browse the Lost/Found feed → file a report through the 3-step
wizard → see the AI match → verify through chat. All state is in-memory;
every screen is built with Material 3 on the ReFind teal palette
(`#0D7377`), light and dark.

Motion design highlights (focus of the phase-2 demo video):

- Direction-aware screen transitions (push slides right, pop slides left,
  the report wizard slides up as a modal)
- Staggered card entrance on the feed and profile lists
- Report wizard: horizontal step sliding + growing progress bar + spring
  check-mark reveal on submit
- **Match Found sequence** — the two reports fly in from opposite sides,
  link with a spring check badge, a confidence ring sweeps to 92%, and the
  matching-factor chips (Category / Colour / Location / Time) pop in one
  by one
- Press-scale feedback on cards, animated segmented tabs, count-up stats
  on the profile

## Building the Business Plan

```bash
# Option 1: tectonic (self-contained, recommended)
tectonic docs/business-plan.tex

# Option 2: TeX Live / MacTeX
latexmk -pdf -output-directory=docs docs/business-plan.tex
```

## Team

| Member | Role |
| --- | --- |
| Heng Liu | Product & growth |
| Honghui Zhang | Android engineer |
| Yuchen Huang | Backend & AI engineer |

## License

Released under the [Apache License 2.0](LICENSE).
