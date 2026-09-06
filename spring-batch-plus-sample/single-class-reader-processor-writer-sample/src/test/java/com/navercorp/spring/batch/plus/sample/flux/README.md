# Flux Reader Adapter Samples

## Invariants

- Samples should cover a single delegate whose reader produces items as a `Flux`.
- Every supported combination of reader, processor, and writer roles should have
  a runnable sample.
- Lifecycle callback support should have a runnable sample.
