package github.nighter.ravenspawners.regression;

import github.nighter.ravenspawners.spawner.gui.storage.utils.StorageTransfer;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageTransferTest {
    @Test void staleViewCannotGiveAnyItems() {
        PlayerInventory target = inventory(new ItemStack[36]);
        var result = StorageTransfer.take(target, List.of(TestItems.item(Material.BONE, 64)), items -> false, items -> fail());
        assertTrue(result.stale());
        assertEquals(0, result.moved());
        verify(target, never()).setStorageContents(any());
    }
    @Test void removalHappensBeforeAnyPlayerInventoryWrite() {
        PlayerInventory target = inventory(new ItemStack[36]);
        var result = StorageTransfer.take(target, List.of(TestItems.item(Material.BONE, 64)), items -> {
            verify(target, never()).setStorageContents(any());
            assertEquals(64, items.getFirst().getAmount());
            return true;
        }, items -> fail());
        assertEquals(64, result.moved());
        verify(target).setStorageContents(argThat(items -> items[0].getAmount() == 64));
    }
    @Test void fullInventoryDoesNotRemoveStorageItems() {
        ItemStack[] contents = new ItemStack[36];
        Arrays.setAll(contents, slot -> TestItems.item(Material.DIAMOND, 64));
        PlayerInventory target = inventory(contents);
        var result = StorageTransfer.take(target, List.of(TestItems.item(Material.BONE, 64)), items -> {fail(); return true;}, items -> fail());
        assertEquals(0, result.moved());
        verify(target, never()).setStorageContents(any());
    }
    @Test void partialTransferReservesOnlyTheAmountThatFits() {
        ItemStack[] contents = new ItemStack[36];
        Arrays.setAll(contents, slot -> TestItems.item(Material.DIAMOND, 64));
        contents[0] = TestItems.item(Material.BONE, 60);
        PlayerInventory target = inventory(contents);
        var result = StorageTransfer.take(target, List.of(TestItems.item(Material.BONE, 64)), items -> {
            assertEquals(4, items.getFirst().getAmount()); return true;
        }, items -> fail());
        assertEquals(4, result.moved());
        assertEquals(60, contents[0].getAmount());
    }
    @Test void existingStacksAreFilledBeforeEmptySlots() {
        ItemStack[] contents = new ItemStack[36];
        contents[20] = TestItems.item(Material.BONE, 60);
        PlayerInventory target = inventory(contents);
        StorageTransfer.take(target, List.of(TestItems.item(Material.BONE, 8)), items -> true, items -> fail());
        verify(target).setStorageContents(argThat(items -> items[20].getAmount() == 64 && items[0].getAmount() == 4));
        assertEquals(60, contents[20].getAmount());
    }
    @Test void repeatedStaleTakeCannotDuplicateTheSameStack() {
        PlayerInventory target = inventory(new ItemStack[36]);
        AtomicInteger stock = new AtomicInteger(64);
        Predicate<List<ItemStack>> reserve = items -> stock.compareAndSet(64, 0);
        List<ItemStack> requested = List.of(TestItems.item(Material.BONE, 64));
        assertEquals(64, StorageTransfer.take(target, requested, reserve, items -> fail()).moved());
        assertTrue(StorageTransfer.take(target, requested, reserve, items -> fail()).stale());
        verify(target, times(1)).setStorageContents(any());
    }
    @Test void failedPlayerInventoryWriteRestoresTheReservedItems() {
        PlayerInventory target = inventory(new ItemStack[36]);
        doThrow(new IllegalStateException("write failed")).doNothing().when(target).setStorageContents(any());
        AtomicInteger restored = new AtomicInteger();
        assertThrows(IllegalStateException.class, () -> StorageTransfer.take(target,
                List.of(TestItems.item(Material.BONE, 9)), items -> true,
                items -> restored.set(items.getFirst().getAmount())));
        assertEquals(9, restored.get());
    }
    private PlayerInventory inventory(ItemStack[] contents) {
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getStorageContents()).thenReturn(contents);
        return inventory;
    }
}
