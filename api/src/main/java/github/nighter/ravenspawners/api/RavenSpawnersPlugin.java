package github.nighter.ravenspawners.api;

/**
 * Interface that the main RavenSpawners plugin class implements.
 * Provides access to the API from the plugin instance.
 */
public interface RavenSpawnersPlugin {

    /**
     * Gets the RavenSpawnersAPI instance.
     *
     * @return the API instance
     */
    RavenSpawnersAPI getAPI();
}