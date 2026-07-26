# RavenSpawners Rebrand Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace every project-owned RavenSpawners identity with the canonical, intentionally incompatible RavenSpawners identity.

**Architecture:** Apply a case-aware mechanical rename from the outside in: first Java packages and symbols, then build/runtime identifiers, then persistence and text. Verify each boundary independently, then build the complete project and inspect the packaged plugin descriptors.

**Tech Stack:** Java, Gradle Kotlin DSL, Bukkit/Paper plugin descriptors, YAML, GitHub Actions, PowerShell

## Global Constraints

- Product, plugin, Java type, and artifact name: `RavenSpawners`
- Lowercase namespace, package segment, command, and permission root: `ravenspawners`
- Snake-case configuration and persistence identifier: `raven_spawners`
- The product name and every derived identifier must remain plural.
- Do not preserve RavenSpawners commands, permissions, packages, APIs, configuration keys, database identifiers, or aliases.
- Preserve all unrelated pre-existing working-tree changes.
- Exclude `.git/` and generated build output when evaluating brand residue.

---

### Task 1: Rename Java Packages, Paths, and Product Types

**Files:**
- Rename: `api/src/main/java/github/nighter/ravenspawners/` to `api/src/main/java/github/nighter/ravenspawners/`
- Rename: `core/src/main/java/github/nighter/ravenspawners/` to `core/src/main/java/github/nighter/ravenspawners/`
- Modify: all Java sources under both renamed directories

**Interfaces:**
- Consumes: existing Java APIs rooted at `github.nighter.ravenspawners`
- Produces: equivalent Java APIs rooted at `github.nighter.ravenspawners`, including `RavenSpawners`, `RavenSpawnersAPI`, `RavenSpawnersPlugin`, and `RavenSpawnersProvider`

- [ ] **Step 1: Record the expected failing package residue check**

Run:

```powershell
rg -n 'github\.nighter\.ravenspawners|RavenSpawners' api/src core/src -g '*.java'
```

Expected: matches in package declarations, imports, and branded type names.

- [ ] **Step 2: Rename Java content case-sensitively**

For every tracked and untracked `*.java` file beneath `api/src` and `core/src`,
apply these exact substitutions in order:

```text
github.nighter.ravenspawners -> github.nighter.ravenspawners
RavenSpawnersProvider -> RavenSpawnersProvider
RavenSpawnersPlugin -> RavenSpawnersPlugin
RavenSpawnersAPIImpl -> RavenSpawnersAPIImpl
RavenSpawnersAPI -> RavenSpawnersAPI
RavenSpawners -> RavenSpawners
ravenSpawners -> ravenSpawners
ravenspawners -> ravenspawners
```

- [ ] **Step 3: Rename the package directories**

Use filesystem moves that preserve every file:

```text
api/src/main/java/github/nighter/ravenspawners
  -> api/src/main/java/github/nighter/ravenspawners
core/src/main/java/github/nighter/ravenspawners
  -> core/src/main/java/github/nighter/ravenspawners
```

Rename branded Java files to match their public types:

```text
RavenSpawnersProvider.java -> RavenSpawnersProvider.java
RavenSpawnersPlugin.java -> RavenSpawnersPlugin.java
RavenSpawnersAPI.java -> RavenSpawnersAPI.java
RavenSpawnersAPIImpl.java -> RavenSpawnersAPIImpl.java
RavenSpawners.java -> RavenSpawners.java
```

- [ ] **Step 4: Verify package and type residue is gone**

Run:

```powershell
rg -n 'github\.nighter\.smart[a-z]*|Smart[A-Za-z]*Spawner|smart[A-Za-z]*Spawner' api/src core/src -g '*.java'
Get-ChildItem api/src,core/src -Recurse | Where-Object Name -Match 'RavenSpawners|ravenspawners'
```

Expected: no output.

- [ ] **Step 5: Compile Java**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: `BUILD SUCCESSFUL`.

### Task 2: Rename Build, Plugin, Command, and Permission Identity

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `build.gradle.kts`
- Modify: `api/build.gradle.kts`
- Modify: `core/build.gradle.kts`
- Modify: `core/src/main/resources/plugin.yml`
- Modify: `core/src/main/resources/paper-plugin.yml`
- Modify: `.github/workflows/*.yml`
- Modify: Java command registration and plugin-name lookup sites under `core/src/main/java/github/nighter/ravenspawners/`

**Interfaces:**
- Consumes: renamed Java main class `github.nighter.ravenspawners.RavenSpawners`
- Produces: plugin/artifact name `RavenSpawners`, `/ravenspawners` command, and `ravenspawners.*` permission tree

- [ ] **Step 1: Record expected identity residue**

Run:

```powershell
rg -n -i 'ravenspawners|RavenSpawners' settings.gradle.kts build.gradle.kts api core .github -g '*.kts' -g '*.yml' -g '*.yaml' -g '*.java'
```

Expected: old build, plugin, command, and permission identity matches.

- [ ] **Step 2: Update build and workflow identity**

Apply these exact substitutions to Gradle scripts and GitHub workflows:

```text
RavenSpawners-API -> RavenSpawners-API
RavenSpawners-Javadoc -> RavenSpawners-Javadoc
RavenSpawners -> RavenSpawners
ravenspawners -> ravenspawners
OpenVdra/RavenSpawners -> OpenVdra/RavenSpawners
openvdra.github.io/RavenSpawners -> openvdra.github.io/RavenSpawners
```

- [ ] **Step 3: Update Bukkit and Paper descriptors**

Set descriptor identity to:

```yaml
name: RavenSpawners
main: github.nighter.ravenspawners.RavenSpawners
prefix: RavenSpawners
```

Rename the command key and all usage text from `ravenspawners` to
`ravenspawners`. Rename every permission root from `ravenspawners.` to
`ravenspawners.`. Do not add an old command alias.

- [ ] **Step 4: Update matching Java runtime strings**

In Java sources, replace command, permission, plugin-lookup, metrics, and
persistent namespace strings according to:

```text
"RavenSpawners" -> "RavenSpawners"
"ravenspawners" -> "ravenspawners"
"raven_spawners" -> "raven_spawners"
```

- [ ] **Step 5: Verify descriptors and compile**

Run:

```powershell
rg -n -i 'ravenspawners|RavenSpawners' settings.gradle.kts build.gradle.kts api/build.gradle.kts core/build.gradle.kts core/src/main/resources/plugin.yml core/src/main/resources/paper-plugin.yml .github/workflows
.\gradlew.bat compileJava processResources
```

Expected: residue scan produces no output and Gradle reports `BUILD SUCCESSFUL`.

### Task 3: Rename Persistence, Configuration, Localization, and Documentation

**Files:**
- Modify: `core/src/main/resources/**/*.yml`
- Modify: `core/src/main/resources/language/CHANGELOG.txt`
- Modify: project Markdown and text files outside generated output
- Modify: `.github/ISSUE_TEMPLATE/*`
- Modify: project-owned files whose names contain the former brand

**Interfaces:**
- Consumes: runtime namespace `ravenspawners` and snake-case identifier `raven_spawners`
- Produces: clean RavenSpawners configuration, schema identifiers, copy, documentation, and URLs

- [ ] **Step 1: Capture all remaining content and path residue**

Run:

```powershell
rg -n -i --hidden -g '!.git/**' -g '!**/build/**' 'smart[ _.-]*spawner|ravenspawners' .
rg --files --hidden -g '!.git/**' -g '!**/build/**' | Select-String -Pattern 'RavenSpawners|ravenspawners|raven_spawners'
```

Expected: matches in resources, documentation, configuration, SQL, translations,
or paths.

- [ ] **Step 2: Rename machine identifiers**

Across project-owned resources and documentation, apply:

```text
raven_spawners -> raven_spawners
raven_spawners -> raven_spawners
ravenspawners -> ravenspawners
RavenSpawners -> RavenSpawners
RavenSpawners -> RavenSpawners
RavenSpawners -> RavenSpawners
RavenSpawners -> RavenSpawners
```

Use the same replacement in translated files only for the product name; retain
the surrounding translated language.

- [ ] **Step 3: Rename branded paths**

Rename every project-owned file or directory containing `RavenSpawners`,
`ravenspawners`, or `raven_spawners` using the canonical casing rules. Do not
touch `.git/` or any `build/` directory.

- [ ] **Step 4: Classify any residue**

Run the Step 1 commands again. Every remaining match must be either:

```text
none
```

Historical references are not exempt because the approved design requires a
complete rebrand.

- [ ] **Step 5: Validate resources**

Run:

```powershell
.\gradlew.bat processResources
```

Expected: `BUILD SUCCESSFUL`.

### Task 4: Full Verification and Packaged Artifact Inspection

**Files:**
- Verify: entire repository excluding `.git/` and generated build output
- Verify: `core/build/libs/RavenSpawners-*.jar`

**Interfaces:**
- Consumes: all renamed source, resource, build, and documentation files
- Produces: verified RavenSpawners build with no former-brand residue

- [ ] **Step 1: Run the full test and build suite**

Run:

```powershell
.\gradlew.bat clean test build
```

Expected: `BUILD SUCCESSFUL` and no failed tests.

- [ ] **Step 2: Run final content and path scans**

Run:

```powershell
rg -n -i --hidden -g '!.git/**' -g '!**/build/**' 'smart[ _.-]*spawner|ravenspawners' .
rg --files --hidden -g '!.git/**' -g '!**/build/**' | Select-String -Pattern 'RavenSpawners|ravenspawners|raven_spawners'
rg -n --hidden -g '!.git/**' -g '!**/build/**' 'Raven' . | Select-String -Pattern 'singular product identifier'
```

Expected: no output from all three scans.

- [ ] **Step 3: Inspect produced artifacts**

Run:

```powershell
Get-ChildItem core/build/libs -Filter 'RavenSpawners-*.jar'
jar tf (Get-ChildItem core/build/libs -Filter 'RavenSpawners-*.jar' | Select-Object -First 1).FullName | Select-String 'plugin.yml|paper-plugin.yml|github/nighter/ravenspawners/RavenSpawners.class'
```

Expected: a RavenSpawners JAR containing both descriptors and the renamed main
class.

- [ ] **Step 4: Review descriptors from the JAR**

Extract the built JAR into a temporary directory and confirm both descriptors
contain:

```yaml
name: RavenSpawners
main: github.nighter.ravenspawners.RavenSpawners
```

Confirm command and permission roots contain `ravenspawners` and no
`ravenspawners`.

- [ ] **Step 5: Review the Git diff**

Run:

```powershell
git status --short
git diff --check
git diff --stat
```

Expected: no whitespace errors; rebrand edits are limited to project identity,
and unrelated pre-existing changes remain present.
