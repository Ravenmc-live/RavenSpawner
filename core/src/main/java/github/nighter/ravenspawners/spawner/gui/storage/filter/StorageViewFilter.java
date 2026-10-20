package github.nighter.ravenspawners.spawner.gui.storage.filter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StorageViewFilter {
    private StorageViewFilter() {}

    public static Map<Integer, ItemStack> apply(Map<Integer, ItemStack> items, Material material) {
        if (material == null) return items;
        Map<Integer, ItemStack> result = new LinkedHashMap<>();
        items.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .filter(entry -> entry.getValue().getType() == material)
                .forEach(entry -> result.put(result.size(), entry.getValue()));
        return result;
    }

    public static Material cycle(List<Material> materials, Material current, boolean backwards) {
        List<Material> options = new ArrayList<>();
        options.add(null);
        options.addAll(materials.stream().distinct().sorted(Comparator.comparing(Material::name)).toList());
        int currentIndex = Math.max(0, options.indexOf(current));
        return options.get(Math.floorMod(currentIndex + (backwards ? -1 : 1), options.size()));
    }

    public static int pages(int slots) {
        return Math.max(1, (slots + 44) / 45);
    }
}
