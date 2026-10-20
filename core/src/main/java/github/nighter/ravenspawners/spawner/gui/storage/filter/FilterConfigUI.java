package github.nighter.ravenspawners.spawner.gui.storage.filter;

import github.nighter.ravenspawners.RavenSpawners;
import github.nighter.ravenspawners.language.LanguageManager;
import github.nighter.ravenspawners.spawner.lootgen.loot.LootItem;
import github.nighter.ravenspawners.spawner.properties.SpawnerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FilterConfigUI implements Listener {
    private final RavenSpawners plugin;
    private final LanguageManager languageManager;
    private final Map<UUID, Long> lastItemClickTime = new ConcurrentHashMap<>();

    public FilterConfigUI(RavenSpawners plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void reload() {
        lastItemClickTime.clear();
    }

    public void openFilterConfigGUI(Player player, SpawnerData spawner) {
        FilterConfigHolder holder = new FilterConfigHolder(spawner);
        Inventory inventory = Bukkit.createInventory(holder, 54,
                languageManager.getGuiTitle("gui_title_filter_config"));
        holder.setInventory(inventory);
        setupFilterInventory(inventory, holder);
        player.openInventory(inventory);
    }

    private void setupFilterInventory(Inventory inventory, FilterConfigHolder holder) {
        SpawnerData spawner = holder.getSpawnerData();
        Map<Material, ItemStack> examples = new HashMap<>();
        if (spawner.getLootConfig() != null) {
            for (LootItem loot : spawner.getLootConfig().getAllItems()) {
                ItemStack sample = loot.createItemStack();
                if (sample != null && !sample.getType().isAir()) {
                    examples.putIfAbsent(sample.getType(), sample);
                }
            }
        }
        for (Material material : spawner.getFilteredItems()) {
            if (material.isItem() && !material.isAir()) examples.computeIfAbsent(material, ItemStack::new);
        }
        holder.setMaterials(examples.keySet().stream().sorted(Comparator.comparing(Material::name)).toList());
        int pages = StorageViewFilter.pages(holder.getMaterials().size());
        holder.setPage(Math.max(1, Math.min(holder.getPage(), pages)));
        inventory.clear();
        for (int slot = 0; slot < 45; slot++) {
            Material material = holder.materialAt(slot);
            if (material == null) break;
            ItemStack display = examples.get(material).asQuantity(1);
            String prefix = spawner.getFilteredItems().contains(material) ? "filter_item_blocked" : "filter_item_allowed";
            Map<String, String> placeholders = Map.of("item_name", languageManager.getVanillaItemName(material));
            display.editMeta(meta -> {
                meta.setDisplayName(languageManager.getGuiItemName(prefix + ".name", placeholders));
                meta.setLore(languageManager.getGuiItemLoreAsList(prefix + ".lore", placeholders));
            });
            inventory.setItem(slot, display);
        }
        inventory.setItem(45, navigation("previous", Math.max(1, holder.getPage() - 1)));
        ItemStack back = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        back.editMeta(meta -> {
            meta.setDisplayName(languageManager.getGuiItemName("filter_divider.name"));
            meta.setLore(languageManager.getGuiItemLoreAsList("filter_divider.lore"));
        });
        inventory.setItem(49, back);
        inventory.setItem(53, navigation("next", Math.min(pages, holder.getPage() + 1)));
    }

    private ItemStack navigation(String direction, int target) {
        ItemStack item = new ItemStack(Material.ARROW);
        Map<String, String> placeholders = Map.of("target_page", String.valueOf(target));
        item.editMeta(meta -> {
            meta.setDisplayName(languageManager.getGuiItemName("navigation_button_" + direction + ".name", placeholders));
            meta.setLore(languageManager.getGuiItemLoreAsList("navigation_button_" + direction + ".lore", placeholders));
        });
        return item;
    }

    @EventHandler
    public void onFilterInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof FilterConfigHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;
        if (!event.getClick().isLeftClick() && !event.getClick().isRightClick()) return;
        SpawnerData spawner = holder.getSpawnerData();
        if (plugin.getSpawnerManager().getSpawnerById(spawner.getSpawnerId()) != spawner) {
            player.closeInventory();
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastItemClickTime.put(player.getUniqueId(), now);
        if (last != null && now - last < 200) return;
        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
            player.openInventory(plugin.getSpawnerStorageUI().createStorageInventory(player, spawner, 1, -1));
            return;
        }
        if (slot == 45 || slot == 53) {
            int pages = StorageViewFilter.pages(holder.getMaterials().size());
            int target = Math.max(1, Math.min(pages, holder.getPage() + (slot == 45 ? -1 : 1)));
            if (target != holder.getPage()) {
                holder.setPage(target);
                setupFilterInventory(event.getInventory(), holder);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
            }
            return;
        }
        Material material = holder.materialAt(slot);
        if (material == null) return;
        boolean filtered = spawner.toggleItemFilter(material);
        spawner.markStorageDirty();
        plugin.getSpawnerManager().queueSpawnerForSaving(spawner.getSpawnerId());
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
        setupFilterInventory(event.getInventory(), holder);
        if (plugin.getSpawnerActionLogger() != null) {
            plugin.getSpawnerActionLogger().log(
                    github.nighter.ravenspawners.logging.SpawnerEventType.SPAWNER_ITEM_FILTER,
                    builder -> builder.player(player.getName(), player.getUniqueId())
                            .location(spawner.getSpawnerLocation()).entityType(spawner.getEntityType())
                            .metadata("item_type", material.name()).metadata("is_filtered", filtered));
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof FilterConfigHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastItemClickTime.remove(event.getPlayer().getUniqueId());
    }
}
