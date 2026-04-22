package net.vova.epicenchantments.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.data.FilterData;
import net.vova.epicenchantments.enchantments.ExperienceEnchantment;
import net.vova.epicenchantments.enchantments.ModEnchantments;
import net.vova.epicenchantments.enchantments.VampirismEnchantment;

import java.util.Iterator;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID)
public class CombatEvents {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (player.level().isClientSide()) return;

            ItemStack weapon = player.getMainHandItem();

            // Проверяем зачарование Experience
            int experienceLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.EXPERIENCE.get(), weapon);

            if (experienceLevel > 0) {
                handleExperienceBonus(player, event.getEntity(), experienceLevel);
            }

            // Проверяем зачарование Vampirism
            int vampirismLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.VAMPIRISM.get(), weapon);

            if (vampirismLevel > 0) {
                handleVampirismHeal(player, vampirismLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        // Альтернативная реализация Vampirism - работает при любом уроне, не только при убийстве
        if (event.getSource().getEntity() instanceof Player player &&
                event.getSource().getDirectEntity() == player) {

            if (player.level().isClientSide()) return;

            ItemStack weapon = player.getMainHandItem();
            int vampirismLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.VAMPIRISM.get(), weapon);

            if (vampirismLevel > 0) {
                // Шанс лечения при каждом ударе (30%/50%/70%)
                float chance = 0.3f + (vampirismLevel - 1) * 0.2f;

                if (player.getRandom().nextFloat() < chance) {
                    handleVampirismHeal(player, vampirismLevel);
                }
            }
        }
    }

    private static void handleExperienceBonus(Player player, LivingEntity killedEntity, int enchantLevel) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (killedEntity.level().isClientSide()) return;

        // Получаем базовый опыт за моба
        int baseExperience = killedEntity.getExperienceReward();

        if (baseExperience > 0) {
            // Применяем множитель
            float multiplier = ExperienceEnchantment.getExperienceMultiplier(enchantLevel);
            int bonusExperience = Math.round(baseExperience * multiplier) - baseExperience;

            if (bonusExperience > 0) {
                // Создаем орбы опыта (кружочки)
                spawnExperienceOrbs(killedEntity, bonusExperience);
            }
        }
    }

    private static void spawnExperienceOrbs(LivingEntity entity, int experienceAmount) {
        if (entity.level().isClientSide()) return;

        // Делим опыт на орбы (как в Minecraft)
        while (experienceAmount > 0) {
            int orbValue = net.minecraft.world.entity.ExperienceOrb.getExperienceValue(experienceAmount);
            experienceAmount -= orbValue;

            // Создаем орб опыта
            net.minecraft.world.entity.ExperienceOrb xpOrb = new net.minecraft.world.entity.ExperienceOrb(
                    entity.level(),
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() / 2,
                    entity.getZ(),
                    orbValue
            );

            // Настраиваем орб
            xpOrb.setPosRaw(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ());

            // Добавляем в мир
            entity.level().addFreshEntity(xpOrb);
        }
    }

    private static void handleVampirismHeal(Player player, int enchantLevel) {
        if (player.level().isClientSide()) return;

        float healAmount = VampirismEnchantment.getHealAmount(enchantLevel);

        // Лечим игрока
        player.heal(healAmount);
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                net.minecraft.sounds.SoundEvents.WITHER_BREAK_BLOCK,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.3f,
                1.0f
        );
    }
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (player.level().isClientSide()) return;

            ItemStack weapon = player.getMainHandItem();

            // Проверяем зачарование Filter
            int filterLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.FILTER.get(), weapon);

            if (filterLevel > 0) {
                handleFilterDrop(event, weapon);
            }
        }
    }

    private static void handleFilterDrop(LivingDropsEvent event, ItemStack weapon) {
        // Получаем данные фильтра с меча
        FilterData filterData = FilterData.fromItemStack(weapon);

        if (filterData.getFilteredItems().isEmpty()) return;

        // Проходим по всем дропам и удаляем отфильтрованные
        Iterator<ItemEntity> iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            net.minecraft.world.entity.item.ItemEntity itemEntity = iterator.next();
            ItemStack dropStack = itemEntity.getItem();

            // Получаем ID предмета
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(dropStack.getItem());
            String itemIdString = itemId.toString();

            // Если предмет в фильтре - удаляем его
            if (filterData.isItemFiltered(itemIdString)) {
                iterator.remove();
            }
        }
    }
}