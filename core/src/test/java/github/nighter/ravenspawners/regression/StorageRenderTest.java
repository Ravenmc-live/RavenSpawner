package github.nighter.ravenspawners.regression;

import github.nighter.ravenspawners.RavenSpawners;
import github.nighter.ravenspawners.Scheduler;
import github.nighter.ravenspawners.language.LanguageManager;
import github.nighter.ravenspawners.spawner.gui.layout.*;
import github.nighter.ravenspawners.spawner.gui.storage.*;
import github.nighter.ravenspawners.spawner.properties.*;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageRenderTest {
    private RavenSpawners plugin() throws Exception {
        RavenSpawners plugin = mock(RavenSpawners.class);
        when(plugin.getConfig()).thenReturn(new org.bukkit.configuration.file.YamlConfiguration());
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("SpawnerRegression"));
        var instance = RavenSpawners.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null,plugin);
        when(plugin.getLanguageManager()).thenReturn(mock(LanguageManager.class));
        GuiLayoutConfig layout = mock(GuiLayoutConfig.class);
        when(layout.getCurrentStorageLayout()).thenReturn(new GuiLayout());
        when(plugin.getGuiLayoutConfig()).thenReturn(layout);
        return plugin;
    }
    @Test void displayFailureAlwaysReleasesTheInventoryLock() throws Exception {
        RavenSpawners plugin = plugin();
        try (var scheduler = mockStatic(Scheduler.class)) {
            SpawnerStorageUI ui = new SpawnerStorageUI(plugin);
            SpawnerData spawner = mock(SpawnerData.class);
            VirtualInventory storage = mock(VirtualInventory.class);
            ReentrantLock lock = new ReentrantLock();
            when(spawner.getInventoryLock()).thenReturn(lock);
            when(spawner.getVirtualInventory()).thenReturn(storage);
            when(storage.getDisplayInventory()).thenThrow(new IllegalStateException("display failed"));
            StoragePageHolder holder = new StoragePageHolder(spawner,1,1,new GuiLayout());
            Inventory inventory = mock(Inventory.class);
            when(inventory.getHolder(false)).thenReturn(holder);
            assertThrows(IllegalStateException.class,() -> ui.updateDisplay(inventory,spawner,1,-1));
            assertFalse(lock.isLocked());
        }
    }
    @Test void filtersCompactThePageAndDoNotExposeCachedStackReferences() throws Exception {
        RavenSpawners plugin = plugin();
        try (var scheduler = mockStatic(Scheduler.class)) {
            SpawnerStorageUI ui = new SpawnerStorageUI(plugin);
            SpawnerData spawner = mock(SpawnerData.class);
            VirtualInventory storage = mock(VirtualInventory.class);
            ReentrantLock lock = new ReentrantLock();
            when(spawner.getInventoryLock()).thenReturn(lock);
            when(spawner.getVirtualInventory()).thenReturn(storage);
            ItemStack bone = TestItems.item(Material.BONE,12);
            ItemStack flesh = TestItems.item(Material.ROTTEN_FLESH,2);
            when(storage.getDisplayInventory()).thenReturn(Map.of(0,flesh,1,bone));
            StoragePageHolder holder = new StoragePageHolder(spawner,8,8,new GuiLayout());
            holder.setViewFilter(Material.BONE);
            Inventory inventory = mock(Inventory.class);
            when(inventory.getHolder(false)).thenReturn(holder);
            when(inventory.getSize()).thenReturn(54);
            ui.updateDisplay(inventory,spawner,8,-1);
            assertEquals(1,holder.getTotalPages());
            assertEquals(1,holder.getCurrentPage());
            verify(inventory).setItem(eq(0),argThat(item -> item != bone && item.getType() == Material.BONE && item.getAmount() == 12));
            verify(inventory).setItem(1,null);
            assertFalse(lock.isLocked());
        }
    }
}
