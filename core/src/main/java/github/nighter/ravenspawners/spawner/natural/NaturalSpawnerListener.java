package github.nighter.ravenspawners.spawner.natural;

import github.nighter.ravenspawners.RavenSpawners;
import github.nighter.ravenspawners.spawner.properties.SpawnerData;
import github.nighter.ravenspawners.spawner.data.SpawnerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class NaturalSpawnerListener implements Listener {
    private final RavenSpawners plugin;
    private final SpawnerManager spawnerManager;

    public NaturalSpawnerListener(RavenSpawners plugin) {
        this.plugin = plugin;
        this.spawnerManager = plugin.getSpawnerManager();
    }

    // Improve server performance and ram allocation for spawners
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreSpawnerSpawn(PreSpawnerSpawnEvent event) {
        SpawnerData ravenSpawners = spawnerManager.getSpawnerByLocation(event.getSpawnerLocation());

        if (ravenSpawners != null) {
            event.setCancelled(true);
            event.setShouldAbortSpawn(true);
        } else {
            // This is a natural spawner - check if natural spawning is allowed
            if (!plugin.getConfig().getBoolean("natural_spawner.spawn_mobs", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (event.getSpawner() == null) return;

        SpawnerData ravenSpawners = spawnerManager.getSpawnerByLocation(event.getSpawner().getLocation());

        if (ravenSpawners != null) {
            event.setCancelled(true);
        } else {
            // This is a natural spawner - check if natural spawning is allowed
            if (!plugin.getConfig().getBoolean("natural_spawner.spawn_mobs", true)) {
                event.setCancelled(true);
            }
        }
    }

}