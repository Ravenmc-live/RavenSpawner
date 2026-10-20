package github.nighter.ravenspawners.hooks.economy.shops.providers.ravensell;

import github.nighter.ravenspawners.hooks.economy.shops.providers.ShopProvider;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class RavenSellHook implements ShopProvider {
    private final Methods methods;

    public RavenSellHook() {
        this(findProvider());
    }

    RavenSellHook(Class<?> provider) {
        this.methods = resolve(provider);
    }

    private record Methods(Method loaded, Method get, Method price, Method record) {}

    private static Class<?> findProvider() {
        try {
            return Class.forName("com.kammoun.raven.api.RavenSellProvider", false,
                    RavenSellHook.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError exception) {
            return null;
        }
    }

    private static Methods resolve(Class<?> provider) {
        if (provider == null) return null;
        try {
            Method loaded = provider.getMethod("isLoaded");
            Method get = provider.getMethod("get");
            Class<?> api = get.getReturnType();
            Method price = api.getMethod("getPrice", ItemStack.class);
            Method record;
            try {
                record = api.getMethod("recordSell", UUID.class, ItemStack.class, int.class);
            } catch (NoSuchMethodException exception) {
                record = api.getMethod("recordSell", UUID.class, ItemStack.class, long.class);
            }
            return new Methods(loaded, get, price, record);
        } catch (ReflectiveOperationException | LinkageError | SecurityException exception) {
            return null;
        }
    }

    @Override
    public String getPluginName() {
        return "RavenSell";
    }

    @Override
    public boolean isAvailable() {
        if (methods == null) return false;
        try {
            return Boolean.TRUE.equals(invoke(methods.loaded(), null));
        } catch (RuntimeException | LinkageError exception) {
            return false;
        }
    }

    @Override
    public double getSellPrice(Material material) {
        if (!isAvailable()) return 0.0;
        Object api = invoke(methods.get(), null);
        Object price = invoke(methods.price(), api, new ItemStack(material));
        if (!(price instanceof Number value)) {
            throw new IllegalStateException("RavenSell returned a non-numeric price");
        }
        return value.doubleValue();
    }

    public void recordSell(UUID uuid, ItemStack item, int amount) {
        if (!isAvailable()) return;
        Object api = invoke(methods.get(), null);
        Object quantity;
        if (methods.record().getParameterTypes()[2] == long.class) {
            quantity = Long.valueOf(amount);
        } else {
            quantity = Integer.valueOf(amount);
        }
        invoke(methods.record(), api, uuid, item, quantity);
    }

    private static Object invoke(Method method, Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("RavenSell API call failed", cause);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("RavenSell API is not accessible", exception);
        }
    }
}
