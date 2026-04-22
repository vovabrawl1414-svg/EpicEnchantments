package net.vova.epicenchantments.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.block.ModBlocks;
import net.vova.epicenchantments.item.custom.GloItem;
import net.vova.epicenchantments.item.custom.ObsidianSwordItem;
import net.vova.epicenchantments.item.custom.SackItem;
import net.vova.epicenchantments.item.custom.SmerchItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EpicEnchantments.MODID);

    public static final RegistryObject<Item> SACK = ITEMS.register("sack",
            () -> new SackItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
            ));

    public static final RegistryObject<Item> SCRAP = ITEMS.register("scrap",
            () -> new Item(new Item.Properties()
                    .stacksTo(9)
                    .rarity(Rarity.UNCOMMON)
            ));

    public static final RegistryObject<Item> EX = ITEMS.register("ex",()-> new GloItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FERTILE_SOIL = ITEMS.register("fertile_soil",
            () -> new BlockItem(ModBlocks.FERTILE_SOIL.get(), new Item.Properties()));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final RegistryObject<Item> SMERCH = ITEMS.register("smerch",
            () -> new SmerchItem(new Item.Properties()));

    public static final RegistryObject<Item> OBSIDIAN_SWORD = ITEMS.register("obsidian_sword",
            () -> new ObsidianSwordItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).durability(2000).setNoRepair()));
}