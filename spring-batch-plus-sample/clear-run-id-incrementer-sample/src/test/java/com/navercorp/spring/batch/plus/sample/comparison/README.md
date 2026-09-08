# Run ID Incrementer Comparison Samples

## Invariants

- Comparison samples should start from a completed run that used explicit
  parameters and no incrementer.
- The [`bad`](bad) sample should show `RunIdIncrementer` carrying those
  parameters into the next instance.
- The [`good`](good) sample should show `ClearRunIdIncrementer` discarding them.
