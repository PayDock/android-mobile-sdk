# Golden fixtures (S1)

Each `*.input.json` is one event with its time, IDs and connection fixed. `time` is when it happened, or `null`
when the event carries no time of its own; then `builtTime`, when the SDK built the record, is sent instead (spec § 5.2). Each `*.<platform>.expected.json` is the exact
OTLP JSON that platform’s encoder must produce for it, with the device context from `context.<platform>.json`.

| Folder | Encoded by | Why |
|---|---|---|
| `shared/` | Android and iOS | Same input, same record; only the resource attributes differ |
| `android/` | Android only | Android-only events: in-process and exit-info crashes, Kotlin errors |
| `ios/` | iOS only | iOS-only events: Objective-C exceptions, MetricKit, Swift errors |

A5 and I5 copy this folder into their test resources and check every input against its expected file, compared as
parsed JSON, attribute order included (spec § 9.2).

The expected files are generated from the inputs by `generate.py`, which follows spec § 5. Change an input or the spec,
then run `python3 generate.py` and review the diff.
