package net.vova.epicenchantments.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.enchantments.ModEnchantments;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RainEvent {
    @SubscribeEvent
    public static void onRain(TickEvent.PlayerTickEvent event){
        if (!event.player.level().isClientSide && event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            Level level = event.player.level();
            ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
            int levelE = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.COOL.get(), stack);

            if (!level.isClientSide){
                boolean isRain = level.isRaining();
                if (isRain){float damage = levelE * 2;
                    player.hurt(player.damageSources().freeze(), damage);}
            }
        }
        }

    }
