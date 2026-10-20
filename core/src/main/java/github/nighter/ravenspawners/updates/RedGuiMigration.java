package github.nighter.ravenspawners.updates;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class RedGuiMigration {
    private static final String REVISION = "red-gui-ff1f1f-v1";

    private RedGuiMigration() {}

    public static void apply(JavaPlugin plugin) throws IOException {
        Path root = plugin.getDataFolder().toPath();
        Path marker = root.resolve("." + REVISION + ".applied");
        if (Files.exists(marker)) return;
        List<String> resources;
        try (InputStream stream = plugin.getResource("raven-style/files.txt")) {
            if (stream == null) throw new IOException("Missing GUI style manifest");
            resources = new String(stream.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(line -> !line.isBlank()).toList();
        }
        for (String resource : resources) {
            Path destination = root.resolve(resource);
            Path backup = root.resolve("backups").resolve(REVISION).resolve(resource);
            if (Files.exists(destination) && !Files.exists(backup)) {
                Files.createDirectories(backup.getParent());
                Files.copy(destination, backup);
            }
            String contents;
            try (InputStream stream = plugin.getResource(resource)) {
                if (stream == null) throw new IOException("Missing GUI style resource: " + resource);
                contents = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (Files.exists(destination) && resource.startsWith("gui_layouts/")) {
                YamlConfiguration previous = YamlConfiguration.loadConfiguration(destination.toFile());
                YamlConfiguration replacement = new YamlConfiguration();
                try {
                    replacement.loadFromString(contents);
                } catch (Exception exception) {
                    throw new IOException("Invalid GUI style resource: " + resource, exception);
                }
                for (String key : List.of("skip_main_gui", "skip_sell_confirmation")) {
                    if (previous.contains(key)) replacement.set(key, previous.get(key));
                }
                contents = replacement.saveToString();
            }
            Files.createDirectories(destination.getParent());
            Path temporary = Files.createTempFile(destination.getParent(), ".raven-gui-", ".tmp");
            try {
                Files.writeString(temporary, contents, StandardCharsets.UTF_8);
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
        Files.createDirectories(root);
        Files.writeString(marker, REVISION, StandardCharsets.UTF_8);
    }
}
