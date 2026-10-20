package github.nighter.ravenspawners.regression;

import github.nighter.ravenspawners.RavenSpawners;
import github.nighter.ravenspawners.language.MessageService;
import github.nighter.ravenspawners.spawner.data.SpawnerManager;
import github.nighter.ravenspawners.spawner.gui.storage.*;
import github.nighter.ravenspawners.spawner.properties.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;
import java.util.concurrent.locks.ReentrantLock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageActionSafetyTest {
    @Test void failedDropReservationDoesNotDropOrClearDisplayedItems() throws Exception {
        RavenSpawners plugin = mock(RavenSpawners.class);
        SpawnerStorageUI ui = mock(SpawnerStorageUI.class);
        when(plugin.getSpawnerStorageUI()).thenReturn(ui);
        when(plugin.getMessageService()).thenReturn(mock(MessageService.class));
        SpawnerStorageAction action = new SpawnerStorageAction(plugin);
        SpawnerData spawner = mock(SpawnerData.class);
        VirtualInventory storage = mock(VirtualInventory.class);
        when(spawner.getVirtualInventory()).thenReturn(storage);
        ReentrantLock lock = new ReentrantLock();
        when(spawner.getInventoryLock()).thenReturn(lock);
        when(spawner.removeItemsAndUpdateSellValue(anyList())).thenReturn(false);
        StoragePageHolder holder = new StoragePageHolder(spawner,1,1,null);
        Inventory inventory = mock(Inventory.class);
        when(inventory.getHolder(false)).thenReturn(holder);
        var bone = TestItems.item(Material.BONE,12);
        when(inventory.getItem(0)).thenReturn(bone);
        Player player = mock(Player.class);
        var method = SpawnerStorageAction.class.getDeclaredMethod("handleDropPageItems",Player.class,SpawnerData.class,Inventory.class);
        method.setAccessible(true);
        assertFalse((boolean) method.invoke(action,player,spawner,inventory));
        verify(inventory,never()).setItem(anyInt(),any());
        verify(player,never()).getWorld();
        assertFalse(lock.isLocked());
        verify(ui).updateDisplay(inventory,spawner,1,-1);
    }
    @Test void aBrokenSpawnerCannotBeWithdrawnFromAnOldGui() {
        RavenSpawners plugin = mock(RavenSpawners.class);
        SpawnerManager manager = mock(SpawnerManager.class);
        when(plugin.getSpawnerManager()).thenReturn(manager);
        SpawnerStorageAction action = new SpawnerStorageAction(plugin);
        SpawnerData spawner = mock(SpawnerData.class);
        when(spawner.getSpawnerId()).thenReturn("removed-spawner");
        when(spawner.getVirtualInventory()).thenReturn(mock(VirtualInventory.class));
        StoragePageHolder holder = new StoragePageHolder(spawner,1,1,null);
        Inventory inventory = mock(Inventory.class);
        when(inventory.getHolder(false)).thenReturn(holder);
        Player player = mock(Player.class);
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getInventory()).thenReturn(inventory);
        when(event.getWhoClicked()).thenReturn(player);
        action.onInventoryClick(event);
        verify(event).setCancelled(true);
        verify(player).closeInventory();
        verify(spawner,never()).removeItemsAndUpdateSellValue(anyList());
    }
}
