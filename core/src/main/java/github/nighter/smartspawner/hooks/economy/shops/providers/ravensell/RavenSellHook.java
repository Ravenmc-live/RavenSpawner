package github.nighter.smartspawner.hooks.economy.shops.providers.ravensell;

import com.kammoun.raven.api.RavenSellProvider;
import github.nighter.smartspawner.hooks.economy.shops.providers.ShopProvider;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RavenSellHook implements ShopProvider {

    @Override
    public String getPluginName() {
        return "RavenSell";
    }

    @Override
    public boolean isAvailable() {
        return RavenSellProvider.isLoaded();
    }

    @Override
    public double getSellPrice(Material material) {
        if (!RavenSellProvider.isLoaded()) return 0.0;
        return RavenSellProvider.get().getPrice(new ItemStack(material));
    }
}
