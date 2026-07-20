package github.nighter.smartspawner.hooks.protections;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.protections.api.WorldGuard;
import github.nighter.smartspawner.hooks.IntegrationManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckStackBlock {
    public static boolean CanPlayerPlaceBlock(@NotNull final Player player, @NotNull Location location) {
        if (player.isOp() || player.hasPermission("*")) return true;

        IntegrationManager integrationManager = SmartSpawner.getInstance().getIntegrationManager();

        if (integrationManager.isHasWorldGuard() && !WorldGuard.canPlayerStackBlockInRegion(player, location)) return false;
        return true;
    }
}
