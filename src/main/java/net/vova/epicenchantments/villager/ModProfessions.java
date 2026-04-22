package net.vova.epicenchantments.villager;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.vova.epicenchantments.EpicEnchantments;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class ModProfessions {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, EpicEnchantments.MODID);

    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, EpicEnchantments.MODID);

    // Создаем PoiType для стола зачарований
    public static final RegistryObject<PoiType> ENCHANTER_POI = POI_TYPES.register("enchanter_poi",
            () -> {
                Set<BlockState> matchingStates = new HashSet<>();
                // Получаем все состояния блока стола зачарований
                for (BlockState state : Blocks.ENCHANTING_TABLE.getStateDefinition().getPossibleStates()) {
                    matchingStates.add(state);
                }
                return new PoiType(matchingStates, 1, 1);
            });

    // Профессия "Мастер зачарований"
    public static final RegistryObject<VillagerProfession> ENCHANTER_MASTER = PROFESSIONS.register(
            "enchanter_master",
            () -> {
                // Получаем ResourceKey для нашего PoiType
                ResourceKey<PoiType> enchanterPoiKey = ResourceKey.create(
                        ForgeRegistries.POI_TYPES.getRegistryKey(),
                        new ResourceLocation(EpicEnchantments.MODID, "enchanter_poi")
                );

                // Предикат для проверки PoiType
                Predicate<Holder<PoiType>> poiPredicate = holder ->
                        holder.is(enchanterPoiKey);

                return new VillagerProfession(
                        EpicEnchantments.MODID + ":enchanter_master",
                        poiPredicate,
                        poiPredicate,
                        ImmutableSet.of(Items.BOOK, Items.ENCHANTED_BOOK, Items.EXPERIENCE_BOTTLE),
                        ImmutableSet.of(Blocks.BOOKSHELF),
                        SoundEvents.ENCHANTMENT_TABLE_USE
                );
            }
    );

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        PROFESSIONS.register(eventBus);
    }
}