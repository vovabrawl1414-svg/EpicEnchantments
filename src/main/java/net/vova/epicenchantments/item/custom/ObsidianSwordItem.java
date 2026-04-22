package net.vova.epicenchantments.item.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.vova.epicenchantments.item.ModItems;

public class ObsidianSwordItem extends SwordItem {


    private static class ObsidianTier implements Tier {
        @Override
        public int getUses() {
            return 2000;
        }

        @Override
        public float getSpeed() {
            return 0;
        }

        @Override
        public float getAttackDamageBonus() {
            return 7.0f;
        }

        @Override
        public int getLevel() {
            return 4;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(net.minecraft.world.item.Items.OBSIDIAN);
        }
    }

    private static final Tier OBSIDIAN_TIER = new ObsidianTier();

    public ObsidianSwordItem(Properties properties) {
        super(OBSIDIAN_TIER, 2, -2.4f, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, net.minecraft.world.entity.player.Player player, net.minecraft.world.entity.Entity entity) {
        return super.onLeftClickEntity(stack, player, entity);
    }
}