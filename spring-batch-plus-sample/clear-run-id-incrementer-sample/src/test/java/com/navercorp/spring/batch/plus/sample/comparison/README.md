# Run ID Incrementer Comparison Samples

## Invariants

- Comparison samples should start from equivalent completed job metadata
  containing both run-id and non-run-id parameters.
- The [`bad`](bad) sample should show `RunIdIncrementer` carrying previous
  non-run-id parameters forward.
- The [`good`](good) sample should show `ClearRunIdIncrementer` discarding those
  parameters.
- This comparison represents legacy or externally populated metadata rather
  than metadata produced by a clean Spring Batch 6 launch sequence.
