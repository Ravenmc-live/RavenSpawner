package github.nighter.ravenspawners.spawner.gui.storage.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Consumer;

public final class StorageTransfer {
    private StorageTransfer() {}

    public record Result(int moved, boolean stale) {}

    private static boolean isAir(Material material) {
        return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR;
    }

    public static Result take(PlayerInventory target, List<ItemStack> requested,
                              Predicate<List<ItemStack>> removeFromStorage, Consumer<List<ItemStack>> restoreToStorage) {
        ItemStack[] original = target.getStorageContents();
        ItemStack[] planned = new ItemStack[original.length];
        for (int slot = 0; slot < original.length; slot++) {
            planned[slot] = original[slot] == null ? null : original[slot].clone();
        }
        List<ItemStack> removals = new ArrayList<>();
        int moved = 0;
        for (ItemStack source : requested) {
            if (source == null || isAir(source.getType()) || source.getAmount() <= 0) continue;
            int remaining = source.getAmount();
            for (ItemStack existing : planned) {
                if (remaining == 0) break;
                if (existing == null || !existing.isSimilar(source)) continue;
                int count = Math.min(remaining, Math.max(0, existing.getMaxStackSize() - existing.getAmount()));
                existing.setAmount(existing.getAmount() + count);
                remaining -= count;
            }
            for (int slot = 0; slot < planned.length && remaining > 0; slot++) {
                if (planned[slot] != null && !isAir(planned[slot].getType())) continue;
                int count = Math.min(remaining, source.getMaxStackSize());
                planned[slot] = source.asQuantity(count);
                remaining -= count;
            }
            int count = source.getAmount() - remaining;
            if (count > 0) {
                removals.add(source.asQuantity(count));
                moved += count;
            }
        }
        if (moved == 0) return new Result(0, false);
        if (!removeFromStorage.test(removals)) return new Result(0, true);
        try {
            target.setStorageContents(planned);
        } catch (RuntimeException exception) {
            // A failed player inventory write must not leave a partially written inventory.
            target.setStorageContents(original);
            restoreToStorage.accept(removals);
            throw exception;
        }
        return new Result(moved, false);
    }
}
