package net.vova.epicenchantments.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.entity.custom.AirEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EpicEnchantments.MODID);

    public static final RegistryObject<EntityType<AirEntity>> AIR =
            ENTITY_TYPES.register("air", () -> EntityType.Builder.of(AirEntity::new, MobCategory.MONSTER)
                    .sized(2.5f, 2.5f)
                    .clientTrackingRange(10)
                    .build("rhino"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}