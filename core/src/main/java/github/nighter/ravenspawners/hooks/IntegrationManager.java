package github.nighter.ravenspawners.hooks;

import github.nighter.ravenspawners.RavenSpawners;
import github.nighter.ravenspawners.hooks.bedrock.FloodgateHook;
import github.nighter.ravenspawners.hooks.economy.shops.providers.ravensell.RavenSellHook;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

@Getter
public class IntegrationManager {
    private final RavenSpawners plugin;

    private boolean hasWorldGuard = false;
    private boolean hasFloodgate = false;
    private boolean hasRavenSell = false;

    public FloodgateHook floodgateHook;
    private RavenSellHook ravenSellHook;

    public IntegrationManager(RavenSpawners plugin) {
        this.plugin = plugin;
    }

    public void initializeIntegrations() {
        hasWorldGuard = checkPlugin("WorldGuard", () -> {
            Plugin worldGuardPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
            return worldGuardPlugin != null && worldGuardPlugin.isEnabled();
        }, true);

        hasFloodgate = checkPlugin("Floodgate", () -> {
            Plugin floodgatePlugin = Bukkit.getPluginManager().getPlugin("floodgate");
            if (floodgatePlugin != null && floodgatePlugin.isEnabled()) {
                this.floodgateHook = new FloodgateHook(plugin);
                return this.floodgateHook.isEnabled();
            } else {
                this.floodgateHook = null;
                return false;
            }
        }, true);

        hasRavenSell = checkPlugin("RavenSell", () -> {
            this.ravenSellHook = new RavenSellHook();
            return this.ravenSellHook.isAvailable();
        }, true);
    }

    private boolean checkPlugin(String pluginName, PluginCheck checker, boolean logSuccess) {
        try {
            if (checker.check()) {
                if (logSuccess) {
                    plugin.getLogger().info(pluginName + " integration enabled successfully!");
                }
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to initialize " + pluginName + " integration", e);
        }
        return false;
    }

    public void reload() {}

    public RavenSellHook getRavenSellHook() {
        return ravenSellHook;
    }

    @FunctionalInterface
    private interface PluginCheck {
        boolean check();
    }
}
