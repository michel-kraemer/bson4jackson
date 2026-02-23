# Contributing to bson4jackson

Thank you for your interest in contributing to bson4jackson! Your help is very
much appreciated, whether it's reporting a bug, suggesting an improvement, or
submitting a pull request. This guide explains how to get involved.

## Legal Notice

By contributing to this project, you agree that your contributions will be
licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
This is the same license that covers the rest of bson4jackson. Please make sure
you have the right to submit any code or content you contribute.

## Reporting Bugs

If you have found a bug, please [open an issue](https://github.com/michel-kraemer/bson4jackson/issues)
on GitHub and include the following information:

* A clear and concise description of the problem
* The version of bson4jackson (and Jackson) you are using
* A **minimal reproducer** — the smallest possible code example that
  demonstrates the bug. This greatly reduces the time needed to investigate and
  fix the issue.
* The expected behavior and what actually happens instead
* Any relevant stack traces or error messages

## Submitting Pull Requests

Contributions via pull requests are welcome. Please follow these steps:

1. Fork the repository and create a new branch for your change.
2. Make your changes, keeping them focused and minimal.
3. Add or update tests to cover your change.
4. Build the project and run the test suite to make sure everything passes:

   ```bash
   ./gradlew test
   ```

   The Gradle wrapper will download the correct Gradle version automatically —
   no additional setup is required.

5. Open a pull request against the `master` branch with a clear description of
   what you changed and why.

All tests must pass before a pull request can be merged.
