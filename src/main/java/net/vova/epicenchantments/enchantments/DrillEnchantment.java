package net.vova.epicenchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class DrillEnchantment extends Enchantment {

    public DrillEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.DIGGER,
                new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 15 + level * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof PickaxeItem ||
                stack.getItem() instanceof ShovelItem;
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        // Конфликтует с EXCAVATOR
        if (other == ModEnchantments.EXCAVATOR.get()) {
            return false;
        }
        // Совместим с MAGNET и AUTOMELTING
        return super.checkCompatibility(other);
    }

    public static int getDepth(int level) {
        return level; // 1, 2, 3 блока вглубь
    }
}