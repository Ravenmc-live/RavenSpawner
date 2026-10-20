package github.nighter.ravenspawners.regression;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.atomic.AtomicInteger;
import static org.mockito.Mockito.*;

final class TestItems {
    static ItemStack item(Material material, int count) {
        AtomicInteger amount = new AtomicInteger(count);
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(material);
        when(item.getMaxStackSize()).thenReturn(64);
        when(item.getAmount()).thenAnswer(call -> amount.get());
        doAnswer(call -> {amount.set(call.getArgument(0)); return null;}).when(item).setAmount(anyInt());
        when(item.clone()).thenAnswer(call -> item(material, amount.get()));
        when(item.asQuantity(anyInt())).thenAnswer(call -> item(material, call.getArgument(0)));
        when(item.isSimilar(any())).thenAnswer(call -> {
            ItemStack other = call.getArgument(0);
            return other != null && other.getType() == material;
        });
        return item;
    }
}
