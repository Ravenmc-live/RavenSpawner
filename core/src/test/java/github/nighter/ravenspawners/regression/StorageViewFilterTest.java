package github.nighter.ravenspawners.regression;

import github.nighter.ravenspawners.spawner.gui.storage.filter.StorageViewFilter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StorageViewFilterTest {
    @Test void allRetainsOriginalOrderingAndMetadata() {
        Map<Integer, ItemStack> items = Map.of(0, TestItems.item(Material.BONE, 64));
        assertSame(items, StorageViewFilter.apply(items, null));
    }
    @Test void filteredStacksAreCompactedAndKeepTheirOriginalItems() {
        ItemStack bone = TestItems.item(Material.BONE, 12);
        ItemStack second = TestItems.item(Material.BONE, 3);
        Map<Integer, ItemStack> items = Map.of(1, bone, 0, TestItems.item(Material.ROTTEN_FLESH, 2), 5, second);
        Map<Integer, ItemStack> filtered = StorageViewFilter.apply(items, Material.BONE);
        assertEquals(2, filtered.size());
        assertSame(bone, filtered.get(0));
        assertSame(second, filtered.get(1));
        assertEquals(3, items.size());
    }
    @Test void emptyFilterStillHasOnePage() {
        assertTrue(StorageViewFilter.apply(Map.of(), Material.BONE).isEmpty());
        assertEquals(1, StorageViewFilter.pages(0));
        assertEquals(1, StorageViewFilter.pages(45));
        assertEquals(2, StorageViewFilter.pages(46));
    }
    @Test void cyclingSupportsBothDirectionsAndAll() {
        List<Material> materials = List.of(Material.ROTTEN_FLESH, Material.BONE, Material.BONE);
        assertEquals(Material.BONE, StorageViewFilter.cycle(materials, null, false));
        assertEquals(Material.ROTTEN_FLESH, StorageViewFilter.cycle(materials, null, true));
        assertNull(StorageViewFilter.cycle(materials, Material.ROTTEN_FLESH, false));
        assertNull(StorageViewFilter.cycle(materials, Material.BONE, true));
    }
    @Test void emptyAndRemovedMaterialsCannotBreakCycling() {
        assertNull(StorageViewFilter.cycle(List.of(), null, false));
        assertEquals(Material.BONE, StorageViewFilter.cycle(List.of(Material.BONE), Material.DIAMOND, false));
    }
}
