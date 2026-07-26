# Cài Đặt API

Thêm RavenSpawners làm dependency qua [JitPack](https://jitpack.io/#Ravenmc-live/RavenSpawner/).

> **Phiên bản mới nhất:** [![Latest Release](https://img.shields.io/github/v/release/Ravenmc-live/RavenSpawner?label=version)](https://github.com/Ravenmc-live/RavenSpawner/releases/latest)

## Maven

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.OpenVdra</groupId>
    <artifactId>RavenSpawners</artifactId>
    <version>LATEST</version>
    <scope>provided</scope>
</dependency>
```

## Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.OpenVdra:RavenSpawners:LATEST'
}
```

::: tip
Thay `LATEST` bằng tag phiên bản cụ thể khi build production, ví dụ `v3.2.1`.
:::

## plugin.yml

Khai báo RavenSpawners để nó tải trước plugin của bạn:

```yaml
name: YourPlugin
version: 1.0.0
main: com.yourpackage.YourPlugin
depend: [RavenSpawners]
# — hoặc tích hợp tùy chọn —
softdepend: [RavenSpawners]
```

## Thiết Lập Cơ Bản

```java
import github.nighter.ravenspawners.api.RavenSpawnersAPI;
import github.nighter.ravenspawners.api.RavenSpawnersProvider;

public class YourPlugin extends JavaPlugin {

    private RavenSpawnersAPI api;

    @Override
    public void onEnable() {
        api = RavenSpawnersProvider.getAPI();
        if (api == null) {
            getLogger().warning("RavenSpawners not found — disabling integration.");
            return;
        }
        getLogger().info("RavenSpawners API connected.");
    }

    public RavenSpawnersAPI getAPI() {
        return api;
    }
}
```
