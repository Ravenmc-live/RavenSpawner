package github.nighter.ravenspawners.spawner.gui.storage.filter;

import github.nighter.ravenspawners.spawner.properties.SpawnerData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

@Getter
public class FilterConfigHolder implements InventoryHolder {
    private final SpawnerData spawnerData;
    @Setter private Inventory inventory;
    @Setter private List<Material> materials = List.of();
    @Setter private int page = 1;

    public FilterConfigHolder(SpawnerData spawnerData) {
        this.spawnerData = spawnerData;
    }

    public Material materialAt(int slot) {
        if (slot < 0 || slot >= 45) return null;
        int index = (page - 1) * 45 + slot;
        return index < materials.size() ? materials.get(index) : null;
    }
}
