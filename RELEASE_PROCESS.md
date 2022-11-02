# Release Process

1. Make sure all build passes. You can check by `./gradlew clean build koverMergedVerify`
2. Make a prepare commit.
   - Remove `-SNAPSHOT` postfix of `gradle.properties`.
   - Update compatibility, example, ... of `README.md`.
   - Remove `-SNAPSHOT` of `CHANGELOG.md`.
   - Commit message: `Prepare for vx.x.x`.
3. Make sure `~/.gradle/gradle.properties` holds gpg setting. If you lost a key, regenerate and publish it.
   ```sh
   signing.keyId=...
   signing.password=...
   signing.secretKeyRingFile=...
   ```
4. Make sure `${projectRoot}/.envrc` holds maven information. You need [direnv](https://direnv.net/) to enable `.envrc`.
   ```sh
   export MAVEN_USER=...
   export MAVEN_PASSWORD=...
   ```
5. Install to local and test it with example projects.
6. Publish by `./gradlew publish`
7. Check Staging Repositories in a [sonatype](https://oss.sonatype.org/).
7. Start a new version
   - Update version of `gradle.properties` to `x.x.x-SHAPSHOT`.
   - Add `## x.x.x-SHAPSHOT` to `CHANGELOG.md`.
   - Commit message: `Start next iteration`

## See also

- [Sonatype Publish Guide](https://central.sonatype.org/publish/publish-guide/)
- [Sonatype Requirements](https://central.sonatype.org/publish/requirements/)
- [GPG Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Sonatype Release Guide](https://central.sonatype.org/publish/release/)