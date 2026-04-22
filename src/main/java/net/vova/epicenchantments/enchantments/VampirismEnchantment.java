package net.vova.epicenchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class VampirismEnchantment extends Enchantment {

    public VampirismEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 40;
    }

    @Override
    public int getMaxLevel() {
        return 3; // Уровни: I (1 сердце), II (2 сердца), III (3 сердца)
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return canEnchant(stack);
    }

    @Override
    public boolean isTreasureOnly() {
        return true; // Только в сокровищницах/торговле
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    // Метод для получения количества здоровья
    public static float getHealAmount(int level) {
        return level * 1.0f; // 1 уровень = 2 здоровья (1 сердце), 3 уровень = 6 здоровья (3 сердца)
    }
}