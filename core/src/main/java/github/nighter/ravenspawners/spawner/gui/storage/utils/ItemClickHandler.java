package github.nighter.ravenspawners.spawner.gui.storage.utils;

import github.nighter.ravenspawners.spawner.properties.SpawnerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface ItemClickHandler {
    void handle(Player player, Inventory inventory, int slot, ItemStack item, SpawnerData spawner);
}