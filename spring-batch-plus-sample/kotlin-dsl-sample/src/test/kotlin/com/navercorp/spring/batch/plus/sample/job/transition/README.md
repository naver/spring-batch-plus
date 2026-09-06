# Job Transition Samples

## Invariants

- Every transition scenario documented by the Kotlin DSL job flow guide should
  have a runnable sample in this package.

## Packages

- [`step`](step) demonstrates selecting a step as the next state and continuing
  from it.
- [`flow`](flow) demonstrates selecting a flow as the next state and continuing
  from it.
- [`decider`](decider) demonstrates routing execution through a decision state.
- [`terminal`](terminal) demonstrates completing a transition without selecting
  another state.
- [`stopandrestart`](stopandrestart) demonstrates stopping a job while defining
  where the next execution resumes.
