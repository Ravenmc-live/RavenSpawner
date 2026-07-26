---
title: MythicMobs
---

# MythicMobs

**Download:** [Modrinth](https://modrinth.com/plugin/mythicmobs)

RavenSpawners registers a MythicMobs drop named `ravenspawners`. Use it in a MythicMobs drop table to create a RavenSpawners item:

```yaml
ExampleDrops:
  Drops:
    - ravenspawners ZOMBIE 1
    - ravenspawners SKELETON 1-3
```

Syntax:

```text
ravenspawners <ENTITY_TYPE> [amount|minimum-maximum]
```

The mob must map to a valid Bukkit `EntityType`. Invalid mob names or ranges are rejected and logged.
