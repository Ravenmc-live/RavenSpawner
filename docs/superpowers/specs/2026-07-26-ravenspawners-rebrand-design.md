# RavenSpawners Rebrand Design

## Goal

Replace the RavenSpawners identity throughout the project with the canonical
RavenSpawners identity. This is an intentional breaking rebrand: the project
will not retain compatibility aliases, deprecated API shims, or migrations for
the former name.

## Canonical Naming

- Product, plugin, Java type, and artifact name: `RavenSpawners`
- Lowercase namespace, package segment, command, and permission root:
  `ravenspawners`
- Snake-case configuration and persistence identifier: `raven_spawners`
- Human-readable phrase: `RavenSpawners`

The canonical product name and every derived identifier use the plural form.

## Scope

The rebrand covers every project-owned occurrence of the former identity,
including:

- Java package declarations, imports, source directories, public API types,
  implementation types, fields, methods, and constants whose names contain the
  old brand
- Gradle root project, archive, module publication, and generated documentation
  names
- Bukkit and Paper plugin descriptors, main class paths, plugin names, prefixes,
  commands, aliases, and permissions
- Configuration keys, persistent-data namespaces, database names and tables,
  metrics identifiers, integration lookups, and other runtime identifiers
- Default configuration comments and values, localized messages, GUI copy,
  logging text, and generated output
- Repository documentation, contribution material, changelogs, issue templates,
  CI workflows, deployment conditions, artifact paths, and project URLs
- Project-owned filenames and directory names containing the former brand

Neutral uses of the adjective “smart” that do not identify RavenSpawners are
outside the rename. Every ambiguous occurrence will be classified by whether it
refers to the former product or one of its named runtime concepts.

## Compatibility

This is a hard break. The implementation will not:

- preserve `/ravenspawners` as a command or alias
- preserve `ravenspawners.*` permissions
- retain `github.nighter.ravenspawners` packages
- retain `RavenSpawners` API or implementation type names
- read old product-named configuration keys or persistent-data namespaces
- migrate old product-named database names or tables
- retain old plugin lookup names or artifact names

Existing installations must be treated as new RavenSpawners installations or
have their data converted externally.

## Implementation Strategy

The rename will be case-aware and performed in controlled groups:

1. Rename Java symbols, package declarations, imports, and matching source
   directories.
2. Rename build identity, plugin descriptors, commands, permissions, and
   runtime namespaces.
3. Rename persistence and configuration identifiers.
4. Rename player-facing text, translations, documentation, workflows, and URLs.
5. Scan file contents and paths case-insensitively for former-brand residue and
   classify any remaining matches.

Pre-existing working-tree changes belong to the user. Rebrand edits will be
limited to the requested substitutions, and unrelated modified or deleted files
will not be restored or rewritten.

## Error and Risk Handling

- Case-only and directory renames will be verified through package-to-path
  consistency checks because the workspace is on a case-insensitive filesystem.
- Java renames will be compiled after path and import updates to expose missed
  symbols.
- YAML descriptors and configuration files will be inspected in the built JAR,
  not only in the source tree.
- References to external RavenSpawners URLs will be converted to the equivalent
  RavenSpawners location where the mapping is deterministic. A broken or
  unknowable external destination will be reported rather than invented.
- Generated build output and Git internals are excluded from residue decisions.

## Verification

Completion requires all of the following:

- A case-insensitive content scan finds no project-owned former-brand
  identifiers outside explicitly excluded historical or generated material.
- A case-insensitive path scan finds no project-owned former-brand filenames or
  directories.
- Java packages match their source directories and the project compiles.
- The available Gradle test and build tasks pass.
- Produced JAR names use `RavenSpawners`.
- Packaged Bukkit and Paper descriptors identify `RavenSpawners`, point to the
  renamed main class, and expose only `ravenspawners` commands and permissions.
- Git diff review confirms unrelated user changes were preserved.

## Success Criteria

The repository builds and operates under the RavenSpawners identity without
requiring or exposing RavenSpawners-compatible APIs, commands, permissions,
storage identifiers, artifacts, or branding.
