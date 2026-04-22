package net.vova.epicenchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.enchantments.ExcavatorEnchantment;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, EpicEnchantments.MODID);

    public static final RegistryObject<Enchantment> COOL = ENCHANTMENTS.register("cool",
            ()-> new CoolEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{ EquipmentSlot.HEAD}));

    public static final RegistryObject<Enchantment> EXCAVATOR =
            ENCHANTMENTS.register("web", ExcavatorEnchantment::new);

    public static final RegistryObject<Enchantment> REPLANTER =
            ENCHANTMENTS.register("replanter", ReplanterEnchantment::new);

    public static final RegistryObject<Enchantment> DRILL =
            ENCHANTMENTS.register("drill", DrillEnchantment::new);

    public static final RegistryObject<Enchantment> WINGS_OF_GOD =
            ENCHANTMENTS.register("wings_of_god", WingsOfGodEnchantment::new);

    public static final RegistryObject<Enchantment> FILTER =
            ENCHANTMENTS.register("filter", FilterEnchantment::new);

    public static final RegistryObject<Enchantment> MAGNET =
            ENCHANTMENTS.register("magnet", MagnetEnchantment::new);

    public static final RegistryObject<Enchantment> LUMBERJACK =
            ENCHANTMENTS.register("lumberjack", LumberjackEnchantment::new);

    public static final RegistryObject<Enchantment> AUTOMELTING =
            ENCHANTMENTS.register("automelting", AutoMelting::new);

    public static final RegistryObject<Enchantment> EXPERIENCE =
            ENCHANTMENTS.register("experience", ExperienceEnchantment::new);

    public static final RegistryObject<Enchantment> VAMPIRISM =
            ENCHANTMENTS.register("vampirism", VampirismEnchantment::new);

    public static final RegistryObject<Enchantment>   IMPENETRABLE =
            ENCHANTMENTS.register("impenetrable", ImpenetrableEnchantment::new);

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}