package net.vova.epicenchantments.event;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID)
public class ExperienceBottleEvents {

    private static final int XP_COST = 9;

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        // Проверяем, что это пустая стеклянная бутылка
        if (stack.getItem() != Items.GLASS_BOTTLE) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // 🔥 ВАЖНО: true — учитываем жидкости
        double range = 5.0;
        net.minecraft.world.phys.HitResult hit = player.pick(range, 1.0F, true);

        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            BlockPos lookingAt = ((BlockHitResult) hit).getBlockPos();
            BlockState state = player.level().getBlockState(lookingAt);
            FluidState fluid = player.level().getFluidState(lookingAt);

            // Если это вода — выходим
            if (state.getBlock() == Blocks.WATER || !fluid.isEmpty()) {
                return; // Minecraft сам наполнит бутылку водой
            }
        }

        // Проверяем, достаточно ли опыта
        if (player.experienceLevel > 1 || player.totalExperience >= XP_COST) {

            // Забираем опыт
            player.giveExperiencePoints(-XP_COST);

            // Убираем одну пустую бутылку
            stack.shrink(1);

            // Даём бутылочку опыта
            ItemStack expBottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
            if (!player.getInventory().add(expBottle)) {
                player.drop(expBottle, false);
            }

            // Звук
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§a+1 бутылочка опыта (—9 XP)"),
                    true
            );

            event.setCanceled(true);
        } else {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§cНедостаточно опыта! Нужно 9 XP"),
                    true
            );
        }
    }
}