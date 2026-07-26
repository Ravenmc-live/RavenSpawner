---
title: Bedrock Support
---

# Bedrock Support

**Download:** [Geyser](https://modrinth.com/plugin/geyser) and [Floodgate](https://modrinth.com/plugin/floodgate)

Install both Geyser and Floodgate when Bedrock players connect without a Java account. RavenSpawners uses Floodgate for player detection.

```yaml
bedrock_support:
  enable_formui: true
```

When enabled:

- Bedrock players receive mobile-friendly forms for supported spawner menus.
- Java players continue to use inventory GUIs.
- RavenSpawners falls back to the normal GUI if Floodgate is unavailable.
