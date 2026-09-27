package net.example.teleportcrystals.item;

import net.example.teleportcrystals.TeleportCrystalsMod;
import net.fabricmc.fabric.api.itemgroup.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class ModItems {
    private ModItems() {}

    // Unbound copies can stack like ender pearls; once bound, differing
    // component data (the saved location) keeps them from stacking with
    // differently-bound copies automatically.
    public static final Item TELEPORT_STONE = register("teleport_stone",
            new TeleportStoneItem(new Item.Properties().stacksTo(16)));

    // Tool-like: one at a time, has durability, and can take Unbreaking /
    // Mending (see the enchantable/durability item tag).
    public static final Item TELEPORT_WAND = register("teleport_wand",
            new TeleportWandItem(new Item.Properties().stacksTo(1).durability(96)));

    private static Item register(String path, Item item) {
        return Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(TeleportCrystalsMod.MOD_ID, path), item);
    }

    public static void init() {
        // Registers the field initializers above and adds both items to
        // the creative "Tools & Utilities" tab.
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            output.accept(TELEPORT_STONE);
            output.accept(TELEPORT_WAND);
        });
    }
}
