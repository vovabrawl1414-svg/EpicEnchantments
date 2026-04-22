package net.vova.epicenchantments.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.vova.epicenchantments.EpicEnchantments;


import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, EpicEnchantments.MODID);

    public static final RegistryObject<Block> ENCHANTER_TABLE = registerBlock("enchanter_table",
            () -> new EnchanterTableBlock());

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        return toReturn;
    }
    public static final RegistryObject<Block> FERTILE_SOIL = BLOCKS.register("fertile_soil",
            () -> new FertileSoilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.6f)
                    .sound(SoundType.GRAVEL)
                    .randomTicks()
                    .speedFactor(1.0f)
                    .jumpFactor(1.0f)
            ));


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}