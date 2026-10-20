package github.nighter.ravenspawners.hooks.economy.shops.providers.ravensell;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RavenSellHookTest {
    public interface Api {
        double getPrice(ItemStack item);
        void recordSell(UUID uuid, ItemStack item, int amount);
    }

    public static class Provider {
        static boolean loaded;
        static Api api;
        public static boolean isLoaded() { return loaded; }
        public static Api get() { return api; }
    }

    public interface LongApi {
        double getPrice(ItemStack item);
        void recordSell(UUID uuid, ItemStack item, long amount);
    }

    public static class LongProvider {
        static LongApi api;
        public static boolean isLoaded() { return true; }
        public static LongApi get() { return api; }
    }

    public static class IncompatibleProvider {
        public static boolean isLoaded() { return true; }
        public static Object get() { return new Object(); }
    }

    @BeforeEach
    void resetProvider() {
        Provider.loaded = false;
        Provider.api = mock(Api.class);
    }

    @Test void absentOptionalPluginDoesNotPreventHookConstruction() {
        RavenSellHook hook = new RavenSellHook();
        assertFalse(hook.isAvailable());
        assertEquals(0.0, hook.getSellPrice(Material.BONE));
        assertDoesNotThrow(() -> hook.recordSell(UUID.randomUUID(), null, 1));
    }

    @Test void unloadedProviderIsNotCalled() {
        RavenSellHook hook = new RavenSellHook(Provider.class);
        assertFalse(hook.isAvailable());
        assertEquals(0.0, hook.getSellPrice(Material.BONE));
        hook.recordSell(UUID.randomUUID(), mock(ItemStack.class), 2);
        verifyNoInteractions(Provider.api);
    }

    @Test void priceUsesTheExistingRuntimeApi() {
        Provider.loaded = true;
        when(Provider.api.getPrice(any())).thenReturn(12.5);
        RavenSellHook hook = new RavenSellHook(Provider.class);
        try (var construction = mockConstruction(ItemStack.class)) {
            assertEquals(12.5, hook.getSellPrice(Material.BONE));
            assertEquals(1, construction.constructed().size());
            verify(Provider.api).getPrice(construction.constructed().getFirst());
        }
    }

    @Test void saleRecordingKeepsOriginalArguments() {
        Provider.loaded = true;
        RavenSellHook hook = new RavenSellHook(Provider.class);
        UUID player = UUID.randomUUID();
        ItemStack item = mock(ItemStack.class);
        hook.recordSell(player, item, 17);
        verify(Provider.api).recordSell(player, item, 17);
    }

    @Test void providerReloadUsesTheCurrentApiInstance() {
        Provider.loaded = true;
        RavenSellHook hook = new RavenSellHook(Provider.class);
        Api previous = Provider.api;
        Provider.api = mock(Api.class);
        ItemStack item = mock(ItemStack.class);
        UUID player = UUID.randomUUID();
        hook.recordSell(player, item, 3);
        verifyNoInteractions(previous);
        verify(Provider.api).recordSell(player, item, 3);
    }

    @Test void incompatibleOptionalApiIsUnavailable() {
        assertFalse(new RavenSellHook(IncompatibleProvider.class).isAvailable());
    }

    @Test void actualApiErrorsAreNotSilentlyConvertedIntoZeroPrices() {
        Provider.loaded = true;
        when(Provider.api.getPrice(any())).thenThrow(new IllegalStateException("Price service failed"));
        RavenSellHook hook = new RavenSellHook(Provider.class);
        try (var construction = mockConstruction(ItemStack.class)) {
            assertThrows(IllegalStateException.class, () -> hook.getSellPrice(Material.BONE));
        }
    }

    @Test void runtimeLongQuantityParameterIsSupported() {
        LongProvider.api = mock(LongApi.class);
        RavenSellHook hook = new RavenSellHook(LongProvider.class);
        UUID player = UUID.randomUUID();
        ItemStack item = mock(ItemStack.class);
        hook.recordSell(player, item, 31);
        verify(LongProvider.api).recordSell(player, item, 31L);
    }
}
