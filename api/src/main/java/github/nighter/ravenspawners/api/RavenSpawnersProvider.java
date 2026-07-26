package github.nighter.ravenspawners.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Utility class to get the RavenSpawnersAPI instance.
 */
public class RavenSpawnersProvider {

    private static final String PLUGIN_NAME = "RavenSpawners";

    /**
     * Gets the RavenSpawnersAPI instance.
     *
     * @return the API instance, or null if RavenSpawners is not loaded
     */
    public static RavenSpawnersAPI getAPI() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);

        if (plugin == null) {
            return null;
        }

        if (!(plugin instanceof RavenSpawnersPlugin)) {
            return null;
        }

        return ((RavenSpawnersPlugin) plugin).getAPI();
    }
}