package github.nighter.ravenspawners.regression;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GuiResourcesTest {
    private YamlConfiguration yaml(String path) throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream,path);
            yaml.loadFromString(new String(stream.readAllBytes(),StandardCharsets.UTF_8));
        }
        return yaml;
    }
    @Test void everyStorageLayoutExposesBothKindsOfFilter() throws Exception {
        for (String style : List.of("default","DonutSMP","DonutSMP_v2")) {
            YamlConfiguration yaml = yaml("gui_layouts/"+style+"/storage_gui.yml");
            assertEquals("open_filter",yaml.getString("slot_6.click.action"));
            assertEquals("cycle_storage_filter",yaml.getString("slot_8.click.action"));
            assertEquals("previous_page",yaml.getString("slot_1.click.action"));
            assertEquals("next_page",yaml.getString("slot_9.click.action"));
        }
    }
    @Test void sellConfirmationIsSparseAndPaneBased() throws Exception {
        for (String style : List.of("default","DonutSMP","DonutSMP_v2")) {
            YamlConfiguration yaml = yaml("gui_layouts/"+style+"/sell_confirm_gui.yml");
            assertEquals(3,yaml.getKeys(false).stream().filter(key -> key.startsWith("slot_")).count());
            assertEquals("LIME_STAINED_GLASS_PANE",yaml.getString("slot_12.material"));
            assertEquals("PLAYER_HEAD",yaml.getString("slot_14.material"));
            assertEquals("RED_STAINED_GLASS_PANE",yaml.getString("slot_16.material"));
        }
    }
    @Test void allBundledEnglishMenusHaveRedNamesAndCleanLore() throws Exception {
        for (String locale : List.of("en_US","en_US_DonutSMP","en_US_DonutSMP_v2")) {
            for (String file : List.of("gui.yml","command_gui.yml")) {
                YamlConfiguration yaml = yaml("language/"+locale+"/"+file);
                for (String key : yaml.getKeys(true)) {
                    if (key.endsWith(".name") && !yaml.getString(key,"").isEmpty())
                        assertTrue(yaml.getString(key).startsWith("&#FF1F1F"),key);
                    if (yaml.isList(key)) for (String line : yaml.getStringList(key)) {
                        assertFalse(line.toLowerCase().contains("click to"),key+": "+line);
                        assertFalse(line.contains("ɪɴꜰᴏʀᴍᴀᴛɪᴏɴ:"),key+": "+line);
                        assertFalse(line.toLowerCase().contains("description:"),key+": "+line);
                    }
                }
            }
        }
    }
    @Test void migrationManifestContainsOnlyPresentationResources() throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("raven-style/files.txt")) {
            List<String> resources = new String(stream.readAllBytes(),StandardCharsets.UTF_8).lines().toList();
            assertEquals(19,resources.size());
            for (String resource : resources) {
                assertTrue(resource.startsWith("gui_layouts/") || resource.startsWith("language/"));
                assertFalse(resource.endsWith("config.yml"));
                yaml(resource);
            }
        }
    }
}
