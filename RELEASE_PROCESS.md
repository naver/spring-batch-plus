# Release Process

1. Make sure all build passes. You can check by `./gradlew clean build koverMergedVerify`.
2. Make a release branch by `git checkout -b release/vx.x.x`.
3. Make a prepare commit.
   - Remove `-SNAPSHOT` postfix of `gradle.properties`.
   - Update compatibility, example, ... of `README.md`.
   - Remove `-SNAPSHOT` of `CHANGELOG.md`.
   - Commit message: `Prepare for vx.x.x`.
4. Install to local by `./gradlew clean build install --no-build-cache`. Test it with example projects.
5. Make a tag `vx.x.x`.
   - Make a tag by `git tag vx.x.x`.
   - Push the tag by `git push origin vx.x.x`.
6. Wait for [deploy action](https://github.com/naver/spring-batch-plus/actions/workflows/deploy.yml) to be completed.
7. Check Staging Repositories in a [sonatype](https://oss.sonatype.org/).
   - Click Close.
   - Click Release.
8. Start a new version
   - Update version of `gradle.properties` to `x.y.x-SHAPSHOT`.
   - Add `## x.y.x-SHAPSHOT` to `CHANGELOG.md`.
   - Commit message: `Start next iteration`.
9. Merge release branch.
   - Merge release branch by `git checkout main && git merge release/vx.x.x`.
   - Delete release branch by `git branch -d release/vx.x.x`.
   - Push main to origin `git push origin main`.
10. Make a release on [github](https://github.com/naver/spring-batch-plus/releases) based on [CHANGELOG](./CHANGELOG.md).

## See also

- [Sonatype Publish Guide](https://central.sonatype.org/publish/publish-guide/)
- [Sonatype Requirements](https://central.sonatype.org/publish/requirements/)
- [GPG Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Sonatype Release Guide](https://central.sonatype.org/publish/release/)
