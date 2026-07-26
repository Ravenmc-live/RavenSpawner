package github.nighter.ravenspawners.hooks.protections;

import github.nighter.ravenspawners.RavenSpawners;
import github.nighter.ravenspawners.hooks.protections.api.WorldGuard;
import github.nighter.ravenspawners.hooks.IntegrationManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckStackBlock {
    public static boolean CanPlayerPlaceBlock(@NotNull final Player player, @NotNull Location location) {
        if (player.isOp() || player.hasPermission("*")) return true;

        IntegrationManager integrationManager = RavenSpawners.getInstance().getIntegrationManager();

        if (integrationManager.isHasWorldGuard() && !WorldGuard.canPlayerStackBlockInRegion(player, location)) return false;
        return true;
    }
}
