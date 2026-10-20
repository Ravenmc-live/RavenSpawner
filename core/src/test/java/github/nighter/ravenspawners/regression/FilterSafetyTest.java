package github.nighter.ravenspawners.regression;

import github.nighter.ravenspawners.RavenSpawners;
import github.nighter.ravenspawners.language.LanguageManager;
import github.nighter.ravenspawners.spawner.gui.storage.filter.*;
import github.nighter.ravenspawners.spawner.gui.storage.StoragePageHolder;
import github.nighter.ravenspawners.spawner.properties.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilterSafetyTest {
    @Test void clickingPlayerInventoryCannotToggleGenerationFilters() {
        RavenSpawners plugin = mock(RavenSpawners.class);
        Server server = mock(Server.class);
        PluginManager manager = mock(PluginManager.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(manager);
        when(plugin.getLanguageManager()).thenReturn(mock(LanguageManager.class));
        FilterConfigUI ui = new FilterConfigUI(plugin);
        SpawnerData spawner = mock(SpawnerData.class);
        FilterConfigHolder holder = new FilterConfigHolder(spawner);
        Inventory inventory = mock(Inventory.class);
        when(inventory.getHolder(false)).thenReturn(holder);
        when(inventory.getSize()).thenReturn(54);
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getInventory()).thenReturn(inventory);
        when(event.getWhoClicked()).thenReturn(mock(Player.class));
        when(event.getRawSlot()).thenReturn(54);
        ui.onFilterInventoryClick(event);
        verify(event).setCancelled(true);
        verifyNoInteractions(spawner);
    }
    @Test void generatedDropMenuRejectsDragInteractions() {
        RavenSpawners plugin = mock(RavenSpawners.class);
        Server server = mock(Server.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        FilterConfigUI ui = new FilterConfigUI(plugin);
        Inventory inventory = mock(Inventory.class);
        when(inventory.getHolder(false)).thenReturn(new FilterConfigHolder(mock(SpawnerData.class)));
        InventoryDragEvent event = mock(InventoryDragEvent.class);
        when(event.getInventory()).thenReturn(inventory);
        ui.onInventoryDrag(event);
        verify(event).setCancelled(true);
    }
    @Test void paginatedDropFiltersCannotSelectFooterOrBottomItems() {
        FilterConfigHolder holder = new FilterConfigHolder(mock(SpawnerData.class));
        List<Material> materials = new ArrayList<>(Collections.nCopies(45,Material.BONE));
        materials.add(Material.ROTTEN_FLESH);
        holder.setMaterials(materials);
        holder.setPage(2);
        assertEquals(Material.ROTTEN_FLESH,holder.materialAt(0));
        assertNull(holder.materialAt(1));
        assertNull(holder.materialAt(45));
        assertNull(holder.materialAt(54));
        assertNull(holder.materialAt(-1));
    }
    @Test void shrinkingPageCountAlwaysClampsTheCurrentPage() {
        SpawnerData spawner = mock(SpawnerData.class);
        when(spawner.getVirtualInventory()).thenReturn(mock(VirtualInventory.class));
        StoragePageHolder holder = new StoragePageHolder(spawner,8,8,null);
        holder.setTotalPages(2);
        assertEquals(2,holder.getCurrentPage());
        holder.setTotalPages(0);
        assertEquals(1,holder.getCurrentPage());
    }
    @Test void changingGenerationFilterInvalidatesCachedNoLootStatus() throws Exception {
        SpawnerData spawner = mock(SpawnerData.class,CALLS_REAL_METHODS);
        var field = SpawnerData.class.getDeclaredField("filteredItems");
        field.setAccessible(true);
        field.set(spawner,java.util.concurrent.ConcurrentHashMap.newKeySet());
        var loot = mock(github.nighter.ravenspawners.spawner.lootgen.loot.LootItem.class);
        var bone = TestItems.item(Material.BONE,1);
        when(loot.createItemStack()).thenReturn(bone);
        spawner.setLootConfig(new github.nighter.ravenspawners.spawner.lootgen.loot.EntityLootConfig(0,List.of(loot)));
        assertFalse(spawner.hasNoLootOrExperience());
        assertTrue(spawner.toggleItemFilter(Material.BONE));
        assertTrue(spawner.hasNoLootOrExperience());
        assertFalse(spawner.toggleItemFilter(Material.BONE));
        assertFalse(spawner.hasNoLootOrExperience());
    }
    @Test void prefetchedLootCannotBypassNewlyDisabledDrops() throws Exception {
        SpawnerData spawner = mock(SpawnerData.class,CALLS_REAL_METHODS);
        var field = SpawnerData.class.getDeclaredField("filteredItems");
        field.setAccessible(true);
        field.set(spawner,java.util.concurrent.ConcurrentHashMap.newKeySet());
        spawner.storePreGeneratedLoot(List.of(TestItems.item(Material.BONE,6),TestItems.item(Material.ROTTEN_FLESH,2)),10);
        spawner.toggleItemFilter(Material.BONE);
        var items = spawner.getAndClearPreGeneratedItems();
        assertEquals(1,items.size());
        assertEquals(Material.ROTTEN_FLESH,items.getFirst().getType());
        assertEquals(10,spawner.getAndClearPreGeneratedExperience());
        assertNull(spawner.getAndClearPreGeneratedItems());
    }
}
