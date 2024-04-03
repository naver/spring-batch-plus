# Release Process

1. Make sure all build passes. You can check by `./gradlew clean build koverMergedVerify`.
2. Make a release branch by `git switch -c release/va.b.c`.
3. Make a prepare commit.
   - Remove `-SNAPSHOT` postfix of `gradle.properties`.
   - Update compatibility, example, ... of `README.md`. Boot starter compatibility can be checked in [here](https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-dependencies/build.gradle).
   - Remove `-SNAPSHOT` of `CHANGELOG.md`.
   - Commit message: `Prepare for va.b.c`.
4. Install to local by `./gradlew clean build install --no-build-cache`. Test it with example projects.
5. Make a tag `va.b.c`.
   - Make a tag by `git tag va.b.c`.
   - Push the tag by `git push origin va.b.c`.
6. Wait for [deploy action](https://github.com/naver/spring-batch-plus/actions/workflows/deploy.yml) to be completed.
7. Check Staging Repositories in a [sonatype](https://oss.sonatype.org/).
   - Click Close.
   - Click Release.
8. Start a new version
   - Update version of `gradle.properties` to `a.b.c-SHAPSHOT`.
   - Add `## a.b.c-SHAPSHOT` to `CHANGELOG.md`.
   - Commit message: `Start next iteration`.
9. Merge release branch.
   - Merge release branch
      - main : `git switch main && git merge release/va.b.c`.
      - patch (a.b.x branch) : `git switch a.b.x && git merge release/va.b.c`.
   - Delete release branch by `git branch -d release/va.b.c`.
   - Push main to origin
      - main : `git push origin main`.
      - patch (a.b.x branch) : `git push origin a.b.x`.
10. Make a release on [github](https://github.com/naver/spring-batch-plus/releases) based on [CHANGELOG](./CHANGELOG.md).

## See also

- [Sonatype Publish Guide](https://central.sonatype.org/publish/publish-guide/)
- [Sonatype Requirements](https://central.sonatype.org/publish/requirements/)
- [GPG Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Sonatype Release Guide](https://central.sonatype.org/publish/release/)
