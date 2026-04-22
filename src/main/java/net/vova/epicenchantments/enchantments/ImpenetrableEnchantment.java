package net.vova.epicenchantments.enchantments;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;

import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID)
public class ImpenetrableEnchantment extends Enchantment {

    public ImpenetrableEnchantment() {
        super(
                Rarity.VERY_RARE,
                EnchantmentCategory.ARMOR,
                new EquipmentSlot[] {
                        EquipmentSlot.HEAD,
                        EquipmentSlot.CHEST,
                        EquipmentSlot.LEGS,
                        EquipmentSlot.FEET
                }
        );
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 40;
    }

    @Override
    public int getMaxLevel() {
        return 5; // I-V уровни
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    @Override
    public boolean isTreasureOnly() {
        return true; // Только из сокровищниц/жителей
    }

    @Override
    public boolean isTradeable() {
        return true; // Можно купить у жителей
    }

    @Override
    public boolean isDiscoverable() {
        return true; // Можно найти в сундуках
    }

    // Конфликтует с другими защитными зачарованиями
    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return !(other instanceof ThornsEnchantment);
    }

    // Обработчик события получения урона
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        DamageSource source = event.getSource();
        if (!entity.level().isClientSide && entity instanceof net.minecraft.world.entity.player.Player) {
            if (!source.is(DamageTypes.FALL) && !source.is(DamageTypes.LAVA) && !source.is(DamageTypes.MAGIC)) {

                int totalLevel = 0;

                // Суммируем уровни зачарования со всей брони
                for (ItemStack armor : entity.getArmorSlots()) {
                    if (armor.isEnchanted()) {
                        totalLevel += armor.getEnchantmentLevel(
                                ModEnchantments.IMPENETRABLE.get()
                        );
                    }
                }

                if (totalLevel > 0) {
                    // Шанс = уровень * 4% (I=4%, V=20%)
                    double chance = totalLevel * 0.02;
                    if (ThreadLocalRandom.current().nextDouble() < chance) {
                        event.setCanceled(true); // Полный игнор урона!

                        // Визуальный эффект
                        entity.level().playSound(
                                null,
                                entity.getX(), entity.getY(), entity.getZ(),
                                net.minecraft.sounds.SoundEvents.SHIELD_BLOCK,
                                net.minecraft.sounds.SoundSource.PLAYERS,
                                1.0F, 1.0F
                        );

                        // Частицы щита
                        spawnShieldParticles(entity);
                    }
                }
            }
        }
    }

    private static void spawnShieldParticles(LivingEntity entity) {
        if (entity.level().isClientSide) {
            for (int i = 0; i < 20; i++) {
                double x = entity.getX() + (Math.random() - 0.5) * 2;
                double y = entity.getY() + Math.random() * 2;
                double z = entity.getZ() + (Math.random() - 0.5) * 2;

                entity.level().addParticle(
                        net.minecraft.core.particles.ParticleTypes.ENCHANT,
                        x, y, z,
                        0, 0, 0
                );
            }
        }
    }
}