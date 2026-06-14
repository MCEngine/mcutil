# MCUtil

A core library providing shared utilities for various [MCEngine](https://github.com/MCEngine) projects.

MCUtil wraps common git-host operations behind a single `IGit` interface:

- **Latest tag** — ask a git host (GitHub or GitLab) for the latest released tag,
  e.g. to check whether a newer version of your project exists.
- **Issues** — create an issue and fetch an existing one.

This is handy for Minecraft plugins (or any app) that want to notify users about
available updates or open issues programmatically.

## Modules

MCUtil is a multi-module Gradle project. The provider-specific code lives in its own
sub module so you only pull in what you need, while sharing the same build, versioning
and publishing configuration from the root.

| Module          | Artifact         | Description                                                                 |
|-----------------|------------------|-----------------------------------------------------------------------------|
| `core`          | `mcutil-core`    | Shared `IGit` contract, `GitIssue` model and tag/JSON helpers.              |
| `github`        | `mcutil-github`  | GitHub provider — tags & issues via the GitHub REST API.                    |
| `gitlab`        | `mcutil-gitlab`  | GitLab provider — tags & issues via the GitLab REST API.                    |

Both providers depend on `core` and implement the same `IGit` interface, so they are
interchangeable — use `MCUtilGitHub` or `MCUtilGitLab` directly. The root project is a
pure aggregator and ships no artifact of its own.

All artifacts are published to the group `io.github.mcengine`.

## Requirements

- Java 21+
- Gradle 8.13 (a wrapper is included — use `./gradlew`)

## Installation

Artifacts are published to **GitHub Packages** at
`https://maven.pkg.github.com/MCEngine/mcutil`.

### Gradle

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/MCEngine/mcutil")
        credentials {
            username = System.getenv("USER_GITHUB_NAME")
            password = System.getenv("USER_GITHUB_TOKEN") // token with read:packages
        }
    }
}

dependencies {
    // Pick only the provider(s) you need
    implementation 'io.github.mcengine:mcutil-github:<version>'
    implementation 'io.github.mcengine:mcutil-gitlab:<version>'
    // (mcutil-core comes in transitively)
}
```

### Maven

```xml
<dependency>
  <groupId>io.github.mcengine</groupId>
  <artifactId>mcutil-github</artifactId>
  <version>VERSION</version>
</dependency>
```

## Usage

Use the provider class directly — `MCUtilGitHub` or `MCUtilGitLab`. Both implement the
same `IGit` interface, so they are interchangeable.

### Latest tag

```java
import io.github.mcengine.mcutil.git.IGit;
import io.github.mcengine.mcutil.github.MCUtilGitHub;

IGit git = new MCUtilGitHub();

// Fetch the latest tag.
String latest = git.getLatestTag(
        "MCEngine",      // organization / owner
        "mcutil",        // repository name
        null             // personal access token (null for public repos)
);
System.out.println("Latest tag: " + latest);
```

To compare it against your own version, use the helpers in `GitTagUtil`
(`normalizeTag`, `isNewer`):

```java
import io.github.mcengine.mcutil.git.GitTagUtil;

boolean updateAvailable = GitTagUtil.isNewer("2026.0.3-2", GitTagUtil.normalizeTag(latest));
```

GitLab works the same way via `io.github.mcengine.mcutil.gitlab.MCUtilGitLab`:

```java
import io.github.mcengine.mcutil.git.IGit;
import io.github.mcengine.mcutil.gitlab.MCUtilGitLab;

IGit git = new MCUtilGitLab();
String latest = git.getLatestTag("my-group", "my-repo", null);
```

### Issues

Create and fetch issues with the same provider. Both calls return a `GitIssue` record
— `number()` (GitHub `number` / GitLab `iid`), `title()`, `body()`, `url()` and
`state()`. Creating an issue requires a `token` with write access.

```java
import io.github.mcengine.mcutil.git.GitIssue;
import io.github.mcengine.mcutil.git.IGit;
import io.github.mcengine.mcutil.github.MCUtilGitHub;

IGit git = new MCUtilGitHub();
String token = System.getenv("GITHUB_TOKEN");

// Create an issue.
GitIssue created = git.createIssue(
        "MCEngine",                 // organization / owner
        "mcutil",                   // repository name
        token,                      // token with write access (required)
        "Something is broken",      // title
        "Steps to reproduce..."     // body / description
);
System.out.println("Opened #" + created.number() + " -> " + created.url());

// Fetch an existing issue by its number (GitHub) / iid (GitLab).
GitIssue issue = git.getIssue("MCEngine", "mcutil", token, created.number());
System.out.println(issue.title() + " [" + issue.state() + "]");
```

`MCUtilGitLab` exposes the identical API; pass the GitLab group/project and a token
sent as `PRIVATE-TOKEN`.

### Authentication

The `token` argument is optional and only needed for private repositories or to lift
rate limits:

- **GitHub** — a personal access token, sent as a `Bearer` token.
- **GitLab** — a project/personal access token, sent as the `PRIVATE-TOKEN` header.

Pass `null` (or a blank string) for public repositories.

### Version format

Tags are compared using the `yyyy.m.m[-iteration]` scheme (e.g. `2026.0.3-2`). A
leading `v`/`V` is ignored, so `v2026.1.0` and `2026.1.0` are treated the same.

## Building

```bash
# Build every module
./gradlew build

# Build a single module
./gradlew :github:build

# Publish all artifacts to GitHub Packages
#   requires GITHUB_ACTOR and GITHUB_TOKEN environment variables
#   (provided automatically inside GitHub Actions)
./gradlew publish
```

### Versioning

The published version is derived at build time by
`buildSrc/.../VersionCalculator.groovy` from `gradle.properties`:

- **Release** — when `RELEASE_VERSION` is set: `yyyy.m.m`.
- **CI** — when `BUILD_NUMBER` / `DEV_RELEASE_VERSION` is set: `yyyy.m.m-build.<n>`.
- **Local** — otherwise: `project-version`-`project-iteration` (e.g. `2026.0.3-2`).

## Project layout

```
mcutil/
├── build.gradle            # root aggregator + shared SCM/repository publishing config
├── settings.gradle         # includes the core / github / gitlab sub modules
├── gradle.properties       # shared project identity & version
├── buildSrc/               # 'mcutil.logic' convention plugin + VersionCalculator
├── core/                   # io.github.mcengine.mcutil.git (IGit, GitIssue, GitTagUtil, GitJson)
├── github/                 # MCUtilGitHub (central) + GetLatestTag / CreateIssue / GetIssue
└── gitlab/                 # MCUtilGitLab (central) + GetLatestTag / CreateIssue / GetIssue
```

## License

Released under the [MIT License](LICENSE).
