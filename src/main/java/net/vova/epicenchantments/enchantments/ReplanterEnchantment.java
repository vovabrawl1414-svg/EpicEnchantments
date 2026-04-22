package net.vova.epicenchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class ReplanterEnchantment extends Enchantment {

    public ReplanterEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.DIGGER,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 20 + level * 10; // 30, 40, 50, 60
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // Может накладываться на любую мотыгу
        return stack.getItem() instanceof HoeItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // НЕЛЬЗЯ зачаровать в столе
        return false;
    }

    @Override
    public boolean isTreasureOnly() {
        return true; // Только сокровище
    }

    @Override
    public boolean isTradeable() {
        return false; // НЕЛЬЗЯ купить у жителей
    }

    @Override
    public boolean isDiscoverable() {
        return true; // Можно найти в сундуках
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true; // Книги существуют
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        // Несовместимо с шелковым касанием
        return super.checkCompatibility(other) &&
                other != Enchantments.SILK_TOUCH;
    }

    // Шанс срабатывания в зависимости от уровня
    public static double getChance(int level) {
        return switch (level) {
            case 1 -> 0.4;  // 40%
            case 2 -> 0.6;  // 60%
            case 3 -> 0.8;  // 80%
            case 4 -> 1.0;  // 100%
            default -> 0.0;
        };
    }
}