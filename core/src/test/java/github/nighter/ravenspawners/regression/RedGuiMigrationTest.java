package github.nighter.ravenspawners.regression;

import github.nighter.ravenspawners.updates.RedGuiMigration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedGuiMigrationTest {
    @TempDir Path directory;
    private JavaPlugin plugin() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(directory.toFile());
        when(plugin.getResource(anyString())).thenAnswer(call -> getClass().getClassLoader().getResourceAsStream(call.getArgument(0)));
        return plugin;
    }
    @Test void existingStyleFilesAreBackedUpBeforeReplacement() throws Exception {
        Path existing = directory.resolve("language/en_US/gui.yml");
        Files.createDirectories(existing.getParent());
        Files.writeString(existing,"gui_title_stacker: old\n");
        RedGuiMigration.apply(plugin());
        assertEquals("gui_title_stacker: old\n",Files.readString(directory.resolve("backups/red-gui-ff1f1f-v1/language/en_US/gui.yml")));
        assertTrue(Files.readString(existing).contains("FF1F1F"));
        assertTrue(Files.exists(directory.resolve(".red-gui-ff1f1f-v1.applied")));
    }
    @Test void secondStartupPreservesLaterCustomizations() throws Exception {
        JavaPlugin plugin = plugin();
        RedGuiMigration.apply(plugin);
        Path existing = directory.resolve("language/en_US/gui.yml");
        Files.writeString(existing,"custom: retained\n");
        RedGuiMigration.apply(plugin);
        assertEquals("custom: retained\n",Files.readString(existing));
    }
    @Test void behaviorFlagsAndOperationalConfigurationArePreserved() throws Exception {
        Path layout = directory.resolve("gui_layouts/default/main_gui.yml");
        Files.createDirectories(layout.getParent());
        Files.writeString(layout,"skip_main_gui: true\nslot_1:\n  material: BARRIER\n");
        Path config = directory.resolve("config.yml");
        Files.writeString(config,"database: untouched\nlanguage: vi_VN\n");
        RedGuiMigration.apply(plugin());
        assertTrue(YamlConfiguration.loadConfiguration(layout.toFile()).getBoolean("skip_main_gui"));
        assertEquals("database: untouched\nlanguage: vi_VN\n",Files.readString(config));
    }
    @Test void failedMigrationDoesNotWriteACompletionMarker() throws Exception {
        JavaPlugin plugin = plugin();
        when(plugin.getResource("raven-style/files.txt")).thenReturn(null);
        assertThrows(java.io.IOException.class,() -> RedGuiMigration.apply(plugin));
        assertFalse(Files.exists(directory.resolve(".red-gui-ff1f1f-v1.applied")));
    }
}
