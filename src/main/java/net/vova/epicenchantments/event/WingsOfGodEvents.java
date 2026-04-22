package net.vova.epicenchantments.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.enchantments.ModEnchantments;
import net.vova.epicenchantments.enchantments.WingsOfGodEnchantment;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID)
public class WingsOfGodEvents {
    private static final UUID WINGS_ARMOR_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        Player player = event.player;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WINGS_OF_GOD.get(), chest);

        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;

        // Удаляем старый модификатор
        armorAttr.removeModifier(WINGS_ARMOR_UUID);

        if (level == 0) return;

        // Собираем остальные элементы брони
        ItemStack[] armorPieces = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET)
        };

        // Считаем общую защиту (броня + EPF от зачарований)
        float totalArmor = 0;
        int validPieces = 0;

        for (ItemStack piece : armorPieces) {
            if (!piece.isEmpty()) {
                totalArmor += getArmorValue(piece);
                totalArmor += getEnchantmentProtection(piece);
                validPieces++;
            }
        }

        if (validPieces == 0) return;

        float average = totalArmor / validPieces;
        float bonus = average * WingsOfGodEnchantment.getMultiplier(level);

        // Добавляем модификатор брони (чтобы показывалось в интерфейсе)
        AttributeModifier modifier = new AttributeModifier(
                WINGS_ARMOR_UUID,
                "Wings of God bonus",
                bonus,
                AttributeModifier.Operation.ADDITION
        );
        armorAttr.addTransientModifier(modifier);
    }

    private static float getArmorValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        // Получаем предмет
        net.minecraft.world.item.Item item = stack.getItem();

        // Если предмет — броня, получаем его защиту
        if (item instanceof net.minecraft.world.item.ArmorItem armorItem) {
            return armorItem.getDefense();
        }

        return 0;
    }
    private static float getEnchantmentProtection(ItemStack stack) {
        float epf = 0;
        var enchantments = EnchantmentHelper.getEnchantments(stack);

        for (var entry : enchantments.entrySet()) {
            var ench = entry.getKey();
            int level = entry.getValue();

            if (ench == Enchantments.ALL_DAMAGE_PROTECTION) {
                epf += 1 * level;
            } else if (ench == Enchantments.FIRE_PROTECTION) {
                epf += 2 * level;
            } else if (ench == Enchantments.BLAST_PROTECTION) {
                epf += 2 * level;
            } else if (ench == Enchantments.PROJECTILE_PROTECTION) {
                epf += 2 * level;
            } else if (ench == Enchantments.FALL_PROTECTION) {
                epf += 3 * level;
            }
        }

        // Каждый EPF примерно = 0.5–1 ед. защиты, берём 0.75 как среднее
        return epf * 0.75f;
    }
}