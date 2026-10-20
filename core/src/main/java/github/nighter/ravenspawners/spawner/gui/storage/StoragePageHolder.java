package github.nighter.ravenspawners.spawner.gui.storage;

import github.nighter.ravenspawners.spawner.gui.SpawnerHolder;
import github.nighter.ravenspawners.spawner.gui.layout.GuiLayout;
import github.nighter.ravenspawners.spawner.properties.SpawnerData;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class StoragePageHolder implements InventoryHolder, SpawnerHolder {
    @Getter
    private final SpawnerData spawnerData;
    @Getter
    private int currentPage;
    @Getter
    private int totalPages;
    @Getter
    private int oldUsedSlots;
    @Getter
    private final GuiLayout layout;

    // Make these public static final for better performance and accessibility
    public static final int ROWS_PER_PAGE = 5;
    public static final int MAX_ITEMS_PER_PAGE = 45; // Pre-calculated value
    private Inventory inventory;
    @Getter
    @lombok.Setter
    private org.bukkit.Material viewFilter;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public StoragePageHolder(SpawnerData spawnerData, int currentPage, int totalPages, GuiLayout layout) {
        this.spawnerData = spawnerData;
        this.currentPage = Math.max(1, Math.min(currentPage, totalPages));
        this.totalPages = Math.max(1, totalPages);
        this.oldUsedSlots = spawnerData.getVirtualInventory().getUsedSlots();
        this.layout = layout;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setCurrentPage(int page) {
        this.currentPage = Math.max(1, Math.min(page, totalPages));
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = Math.max(1, totalPages);
        this.currentPage = Math.max(1, Math.min(this.currentPage, this.totalPages));
    }

    public void updateOldUsedSlots() {
        this.oldUsedSlots = spawnerData.getVirtualInventory().getUsedSlots();
    }
}
