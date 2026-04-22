package net.vova.epicenchantments.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.data.FilterData;
import net.vova.epicenchantments.enchantments.ModEnchantments;

import java.util.Iterator;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID)
public class DropEvents {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        DamageSource source = event.getSource();

        // Проверяем, убил ли игрок моба
        if (source.getEntity() instanceof Player player) {
            // Не обрабатываем на клиенте
            if (player.level().isClientSide()) return;

            ItemStack weapon = player.getMainHandItem();

            // Проверяем наличие зачарования Filter
            int filterLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.FILTER.get(), weapon);

            if (filterLevel > 0) {
                handleFilterDrop(event, weapon, player);
            }
        }
    }

    private static void handleFilterDrop(LivingDropsEvent event, ItemStack weapon, Player player) {
        FilterData filterData = FilterData.fromItemStack(weapon);

        // Проходим по всем дропам и удаляем отфильтрованные
        Iterator<net.minecraft.world.entity.item.ItemEntity> iterator = event.getDrops().iterator();
        int removedCount = 0;

        while (iterator.hasNext()) {
            net.minecraft.world.entity.item.ItemEntity itemEntity = iterator.next();
            ItemStack dropStack = itemEntity.getItem();

            // Получаем ID предмета
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(dropStack.getItem());
            String itemIdString = itemId.toString();

            // Также проверяем без namespace (minecraft:bone -> bone)
            String simpleId = itemId.getPath();

            // Если предмет в фильтре - удаляем его
            if (filterData.isItemFiltered(itemIdString) || filterData.isItemFiltered(simpleId)) {
                iterator.remove();
                removedCount++;
            }
        }
    }
}