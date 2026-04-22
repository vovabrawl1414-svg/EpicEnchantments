package net.vova.epicenchantments.villager;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.enchantments.ModEnchantments;

import java.util.List;

@Mod.EventBusSubscriber(modid = EpicEnchantments.MODID)
public class ModTrades {

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        // Добавляем торговлю только нашему жителю
        if (event.getType() == ModProfessions.ENCHANTER_MASTER.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            // Уровень 1 (Новичок)
            trades.get(1).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 8), // Что дает игрок
                    new ItemStack(Items.BOOK, 1),     // Что получает игрок
                    12,                               // Макс использование
                    2,                                // Опыт
                    0.05f                             // Множитель цены
            ));

            // Уровень 2 (Ученик)
            trades.get(2).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 15),
                    EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                            ModEnchantments.MAGNET.get(), 1)),
                    8,
                    5,
                    0.1f
            ));

            trades.get(2).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 12),
                    EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                            ModEnchantments.AUTOMELTING.get(), 1)),
                    8,
                    5,
                    0.1f
            ));

            // Уровень 3 (Подмастерье)
            trades.get(3).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 20),
                    EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                            ModEnchantments.LUMBERJACK.get(), 1)),
                    6,
                    10,
                    0.1f
            ));

            trades.get(3).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                            ModEnchantments.IMPENETRABLE.get(), 1)),
                    6,
                    10,
                    0.1f
            ));

            trades.get(3).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 25),
                    EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                            ModEnchantments.EXPERIENCE.get(), 1)),
                    6,
                    10,
                    0.1f
            ));

            // Уровень 4 (Мастер)
            trades.get(4).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 30),
                    EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                            ModEnchantments.EXCAVATOR.get(), 1)),
                    4,
                    15,
                    0.1f
            ));

            trades.get(4).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.DIAMOND, 1),
                    new ItemStack(Items.EMERALD, 4),
                    12,
                    15,
                    0.05f
            ));

            // Уровень 5 (Грандмастер)
            trades.get(5).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 45),
                    EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                            ModEnchantments.VAMPIRISM.get(), 1)),
                    3,
                    30,
                    0.1f
            ));

            trades.get(5).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.NETHERITE_SCRAP, 1),
                    new ItemStack(Items.EMERALD, 10),
                    8,
                    30,
                    0.05f
            ));

            // Специальные комбинированные книги
            trades.get(5).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 60),
                    createCombinedBook(), // Книга с несколькими зачарованиями
                    2,
                    40,
                    0.2f
            ));
        }
    }

    // Книга со всеми зачарованиями для высшего уровня
    private static ItemStack createCombinedBook() {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);


        // Добавляем все зачарования в одну книгу
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(ModEnchantments.EXCAVATOR.get(), 1));
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(ModEnchantments.MAGNET.get(), 1));
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(ModEnchantments.LUMBERJACK.get(), 1));
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(ModEnchantments.AUTOMELTING.get(), 1));

        return book;

    }

    @SubscribeEvent
    public static void addWanderingTrades(WandererTradesEvent event) {
        // Иногда странствующий торговец может продавать наши книги
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();

        rareTrades.add((trader, rand) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 35),
                EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                        ModEnchantments.EXCAVATOR.get(), 1)),
                2,
                12,
                0.2f
        ));
    }
}