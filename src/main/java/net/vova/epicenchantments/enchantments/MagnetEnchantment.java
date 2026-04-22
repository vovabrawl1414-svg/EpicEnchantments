package net.vova.epicenchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class MagnetEnchantment extends Enchantment {

    private static final EnchantmentCategory MAGNET_CATEGORY = EnchantmentCategory.create("magnet_tools",
            item -> item instanceof PickaxeItem ||
                    item instanceof AxeItem ||
                    item instanceof ShovelItem ||
                    item instanceof HoeItem ||
                    item instanceof SwordItem);

    public MagnetEnchantment() {
        super(Rarity.VERY_RARE, MAGNET_CATEGORY, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 1; // Только 1 уровень
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof PickaxeItem ||
                item instanceof AxeItem ||
                item instanceof ShovelItem ||
                item instanceof HoeItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return canEnchant(stack);
    }

    @Override
    public boolean isTreasureOnly() {
        return true; // Только в сокровищницах, нельзя получить на столе зачарований
    }

    @Override
    public boolean isTradeable() {
        return true; // Можно получить у жителей
    }

    @Override
    public boolean isDiscoverable() {
        return true; // Можно найти в сундуках
    }
}