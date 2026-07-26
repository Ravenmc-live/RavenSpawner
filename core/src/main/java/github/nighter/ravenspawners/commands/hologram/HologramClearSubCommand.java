package github.nighter.ravenspawners.commands.hologram;

import com.mojang.brigadier.context.CommandContext;
import github.nighter.ravenspawners.RavenSpawners;
import github.nighter.ravenspawners.commands.BaseSubCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class HologramClearSubCommand extends BaseSubCommand {

    public HologramClearSubCommand(RavenSpawners plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getPermission() {
        return "ravenspawners.command.hologram";
    }

    @Override
    public String getDescription() {
        return "Clear all text display holograms";
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        try {
            // Execute the Minecraft command to kill all text_display entities
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:kill @e[type=text_display]");
            
            // Send success message to player
            plugin.getMessageService().sendMessage(sender, "hologram.cleared");

            return 1;
        } catch (Exception e) {
            plugin.getLogger().severe("Error clearing holograms: " + e.getMessage());
            plugin.getMessageService().sendMessage(sender, "hologram.clear_error");
            return 0;
        }
    }
}
