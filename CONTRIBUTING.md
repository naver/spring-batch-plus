# Contributing

## Code of Conduct

This project is governed by the [Code of Conduct](./CODE_OF_CONDUCT.md). By participating you are expected to uphold
this code. Please report unacceptable behavior to [issue](https://github.com/naver/spring-batch-plus/issues).

## IDE

Use [Intellij Community](https://www.jetbrains.com/idea/download/#section=mac) (also Ultimate is possible).

Import code style schema from [naver-intellij-formatter](./buildSrc/config). You can see [how to import schemas](https://www.jetbrains.com/help/idea/configuring-code-style.html#import-export-schemes) in the Intellij guide.

## How to contribute

### Issue

Feel free to make an any issue.

### Pull request

We doesn't require a lot.

1. Make sure that all check passes (`./gradlew check` should pass)
2. Make sure to write test for all code changes.

You can check build guide in [build from source](./README.md#build-from-source).

## Scripts

### spring-batch-diff.sh

Compares two Spring Batch upstream version tags and shows the diff. Useful for understanding what changed between versions when planning migration work.

```bash
# Show diffstat summary between two versions
scripts/spring-batch-diff.sh 5.2.0 6.0.0 --stat

# Filter to specific module
scripts/spring-batch-diff.sh 5.2.0 6.0.0 --stat -p 'spring-batch-core/src/main/java/**'

# Full diff output to file
scripts/spring-batch-diff.sh 5.2.0 6.0.0 -o /tmp/batch-diff.patch

# List available tags
scripts/spring-batch-diff.sh --list-tags
```

Run `scripts/spring-batch-diff.sh --help` for all options.
