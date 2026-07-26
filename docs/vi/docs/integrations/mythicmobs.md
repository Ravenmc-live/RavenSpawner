---
title: MythicMobs
---

# MythicMobs

**Tải về:** [Modrinth](https://modrinth.com/plugin/mythicmobs)

RavenSpawners đăng ký drop MythicMobs tên `ravenspawners`. Dùng nó trong bảng drop MythicMobs để tạo vật phẩm RavenSpawners:

```yaml
ExampleDrops:
  Drops:
    - ravenspawners ZOMBIE 1
    - ravenspawners SKELETON 1-3
```

Cú pháp:

```text
ravenspawners <ENTITY_TYPE> [amount|minimum-maximum]
```

Mob phải tương ứng với một `EntityType` Bukkit hợp lệ. Tên mob hoặc khoảng giá trị không hợp lệ sẽ bị từ chối và ghi vào log.
