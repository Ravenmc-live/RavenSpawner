package github.nighter.ravenspawners.logging.discord;

import github.nighter.ravenspawners.RavenSpawners;

import java.io.File;

public class DiscordConfigUpdater {

    private static final String FILE_NAME   = "discord_logging.yml";
    private static final String VERSION_KEY = "config_version";

    private final RavenSpawners plugin;

    public DiscordConfigUpdater(RavenSpawners plugin) {
        this.plugin = plugin;
    }

    /** Call this before {@link DiscordWebhookConfig} tries to load the file. */
    public void checkAndUpdate() {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
    }
}
