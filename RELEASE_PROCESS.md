# Release Process

1. Make sure all build passes. You can check by `./gradlew clean build`.
2. Make a release branch by `git switch -c release/va.b.c`.
3. Make a prepare commit.
    - Remove `-SNAPSHOT` postfix of `gradle.properties`.
    - Commit message: `Prepare for va.b.c`.
4. Install to local by `./gradlew clean build install --no-build-cache`. Test it with example projects.
5. Renew the Central Portal user token.
    - Generate a user token with a short expiration in [user token](https://central.sonatype.com/usertoken).
    - Set its username and password to `MAVEN_USER` / `MAVEN_PASSWORD` in [actions secrets](https://github.com/naver/spring-batch-plus/settings/secrets/actions).
6. Make a tag `va.b.c`.
    - Make a tag by `git tag va.b.c`.
    - Push the tag by `git push origin va.b.c`.
7. Wait for [deploy action](https://github.com/naver/spring-batch-plus/actions/workflows/deploy.yml) to be completed.
8. Hand the staged files over to the Central Portal.
    - The deploy action uploads to the ossrh staging api, which only stores the files. They have to be handed over to the portal separately.
    - Use the user token renewed in step 5. Staging repositories are per user, so the ones uploaded by the deploy action may not be listed with another token.
    - Make an authorization token. The api expects the base64 encoded `user:password` of the user token.
        ```sh
        MAVEN_AUTH_TOKEN=$(printf '%s:%s' "${MAVEN_USER}" "${MAVEN_PASSWORD}" | base64 | tr -d '\n')
        ```
    - List the staging repositories. `profile_id` is the namespace in [central portal](https://central.sonatype.com/publishing/namespaces), not the groupId. `ip=any` is required since the files were uploaded from the CI runner.
        ```sh
        curl -H "Authorization: Bearer ${MAVEN_AUTH_TOKEN}" \
            "https://ossrh-staging-api.central.sonatype.com/manual/search/repositories?ip=any&profile_id=com.navercorp"
        ```
    - Make sure there is exactly one repository. Each one becomes a separate deployment, so handing over only one of many releases a partial artifact set. Drop a stale one by `DELETE /manual/drop/repository/<repository key>`.
    - Hand it over with its key.
        ```sh
        curl -X POST -H "Authorization: Bearer ${MAVEN_AUTH_TOKEN}" \
            "https://ossrh-staging-api.central.sonatype.com/manual/upload/repository/<repository key>"
        ```
    - Release the deployment in [central portal](https://central.sonatype.com/publishing/deployments).
9. Make sure the artifacts are on [maven central](https://repo1.maven.org/maven2/com/navercorp/spring/). It takes a few minutes after the deployment is published.
10. Start a new version
    - Update version of `gradle.properties` to `a.b.c-SNAPSHOT`.
    - Commit message: `Start next iteration`.
11. Merge release branch.
    - Merge release branch
        - main : `git switch main && git merge release/va.b.c`.
        - patch (a.b.x branch) : `git switch a.b.x && git merge release/va.b.c`.
    - Delete release branch by `git branch -d release/va.b.c`.
    - Push to origin
        - main : `git push origin main`.
        - patch (a.b.x branch) : `git push origin a.b.x`.
12. Update docs on main.
    - Switch to main by `git switch main`.
    - Update compatibility, example, user guide, ... of `README.md`. Boot starter compatibility can be checked in [here](https://github.com/spring-projects/spring-boot/blob/main/platform/spring-boot-dependencies/build.gradle).
    - Update [CHANGELOG](./CHANGELOG.md).
    - Commit message: `Update docs for va.b.c`.
    - Push main by `git push origin main`.
13. Make a release on [github](https://github.com/naver/spring-batch-plus/releases) based on [CHANGELOG](./CHANGELOG.md).

## See also

- [Sonatype Publish Guide](https://central.sonatype.org/publish/publish-guide/)
- [Sonatype Requirements](https://central.sonatype.org/publish/requirements/)
- [GPG Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Sonatype Release Guide](https://central.sonatype.org/publish/release/)
- [Portal OSSRH Staging API](https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/)
