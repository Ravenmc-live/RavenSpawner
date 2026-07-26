package github.nighter.ravenspawners.language.file;

import org.bukkit.configuration.file.YamlConfiguration;

public record LocaleData(YamlConfiguration messages, YamlConfiguration gui, YamlConfiguration commandGui,
                         YamlConfiguration formatting, YamlConfiguration items,
                         YamlConfiguration commandMessages, YamlConfiguration hologram) {
}
