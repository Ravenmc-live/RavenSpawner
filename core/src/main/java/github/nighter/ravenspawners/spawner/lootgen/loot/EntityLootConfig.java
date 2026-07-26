package github.nighter.ravenspawners.spawner.lootgen.loot;

import java.util.List;

public record EntityLootConfig(int experience, List<LootItem> possibleItems) {

    public List<LootItem> getAllItems() {
        return possibleItems;
    }
}