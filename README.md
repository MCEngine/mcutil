# MCUtil

A core library providing shared utilities for various [MCEngine](https://github.com/MCEngine) projects.

Today MCUtil focuses on **version checking**: given a project's current version, it
asks a git host (GitHub or GitLab) for the latest released tag and tells you whether
a newer version exists. This is handy for Minecraft plugins (or any app) that want to
notify users about available updates.

## Modules

MCUtil is a multi-module Gradle project. The provider-specific code lives in its own
sub module so you only pull in what you need, while sharing the same build, versioning
and publishing configuration from the root.

| Module          | Artifact         | Description                                                                 |
|-----------------|------------------|-----------------------------------------------------------------------------|
| `core`          | `mcutil-core`    | Shared `IGit` contract plus git tag parsing / version comparison helpers.   |
| `github`        | `mcutil-github`  | GitHub provider — reads the latest tag via the GitHub REST API.             |
| `gitlab`        | `mcutil-gitlab`  | GitLab provider — reads the latest tag via the GitLab REST API.             |
| _root_          | `mcutil`         | Facade that aggregates every provider and exposes the `MCUtil` entry point. |

Both providers depend on `core` and implement the same `IGit` interface, so they are
interchangeable. The root `mcutil` artifact re-exports `core`, `github` and `gitlab`
transitively, so depending on it alone gives you everything.

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
    // Option A: pull everything through the facade
    implementation 'io.github.mcengine:mcutil:<version>'

    // Option B: pick only the provider(s) you need
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

### Quick start — the `MCUtil` facade

The simplest entry point is `MCUtil.compareVersion(...)`, which picks the provider for
you based on a `gitType` string (`"github"` or `"gitlab"`).

```java
import io.github.mcengine.mcutil.MCUtil;

// Returns true if a newer tag than your current version exists remotely.
boolean updateAvailable = MCUtil.compareVersion(
        "github",        // git provider: "github" or "gitlab"
        "2026.0.3-2",    // your current version
        "MCEngine",      // organization / owner
        "mcutil",        // repository name
        null             // personal access token (null for public repos)
);

if (updateAvailable) {
    System.out.println("A new version is available!");
}
```

### Using a provider directly

If you only need one host, depend on that module and use its implementation of `IGit`:

```java
import io.github.mcengine.mcutil.git.IGit;
import io.github.mcengine.mcutil.github.MCUtilGitHub;

IGit git = new MCUtilGitHub();

String latest = git.getLatestTag("MCEngine", "mcutil", null);
System.out.println("Latest tag: " + latest);

boolean newer = git.compareVersion("2026.0.3-2", "MCEngine", "mcutil", null);
```

GitLab works the same way via `io.github.mcengine.mcutil.gitlab.MCUtilGitLab`.

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
#   requires USER_GITHUB_NAME and USER_GITHUB_TOKEN environment variables
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
├── build.gradle            # root facade module + shared SCM/repository publishing config
├── settings.gradle         # includes the core / github / gitlab sub modules
├── gradle.properties       # shared project identity & version
├── buildSrc/               # 'mcutil.logic' convention plugin + VersionCalculator
├── core/                   # io.github.mcengine.mcutil.git (IGit, GitTagUtil)
├── github/                 # io.github.mcengine.mcutil.github (MCUtilGitHub)
├── gitlab/                 # io.github.mcengine.mcutil.gitlab (MCUtilGitLab)
└── src/                    # io.github.mcengine.mcutil (MCUtil facade)
```

## License

Released under the [MIT License](LICENSE).
