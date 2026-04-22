package net.vova.epicenchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CoolEnchantment extends Enchantment {
    public CoolEnchantment(Rarity p_44676_, EnchantmentCategory p_44677_, EquipmentSlot[] p_44678_) {
        super(p_44676_, p_44677_, p_44678_);
    }
    public int getMaxLevel() {
        return 3;
    }

    public int getMinCost(int p_44679_) {
        return getMaxLevel() + p_44679_ * 10;
    }

    @Override
    public int getMaxCost(int p_44691_) {
        return this.getMinCost(p_44691_) + 5;
    }
    @Override
    public boolean isTreasureOnly() {
        return true;
    }
    @Override
    public boolean canEnchant(ItemStack p_44689_) {
        return p_44689_.getItem() instanceof ArmorItem;
    }
    @Override
    public boolean isTradeable() {
        return false;
    }
    @Override
    public boolean isDiscoverable() {
        return true;
    }
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }
}
