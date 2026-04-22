package net.vova.epicenchantments.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.vova.epicenchantments.data.FilterData;
import net.vova.epicenchantments.enchantments.ModEnchantments;

import java.util.Collection;

public class FilterCommand {

    private static final SuggestionProvider<CommandSourceStack> ITEM_SUGGESTIONS =
            (context, builder) -> {
                Collection<String> itemIds = BuiltInRegistries.ITEM.keySet().stream()
                        .map(id -> id.toString())
                        .toList();
                return SharedSuggestionProvider.suggest(itemIds, builder);
            };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("filter")
                .requires(source -> source.getEntity() instanceof Player)
                .then(Commands.literal("add")
                        .then(Commands.argument("item", StringArgumentType.greedyString()) // Изменено здесь!
                                .suggests(ITEM_SUGGESTIONS)
                                .executes(FilterCommand::addToFilter)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("item", StringArgumentType.greedyString()) // И здесь!
                                .suggests(ITEM_SUGGESTIONS)
                                .executes(FilterCommand::removeFromFilter)))
                .then(Commands.literal("list")
                        .executes(FilterCommand::listFilter))
                .then(Commands.literal("clear")
                        .executes(FilterCommand::clearFilter))
        );
    }

    private static int addToFilter(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getEntity();
        String itemId = StringArgumentType.getString(context, "item");

        if (player == null) return 0;

        ItemStack weapon = player.getMainHandItem();

        // Проверяем наличие зачарования Filter
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FILTER.get(), weapon) == 0) {
            context.getSource().sendFailure(
                    Component.literal("У вас нет зачарования 'Фильтр' на оружии в руке!")
            );
            return 0;
        }

        // Проверяем, существует ли такой предмет
        if (!BuiltInRegistries.ITEM.containsKey(net.minecraft.resources.ResourceLocation.tryParse(itemId))) {
            context.getSource().sendFailure(
                    Component.literal("Предмет '" + itemId + "' не найден!")
            );
            return 0;
        }

        // Добавляем в фильтр
        FilterData filterData = FilterData.fromItemStack(weapon);
        filterData.addItem(itemId);
        FilterData.saveToItemStack(weapon, filterData);

        context.getSource().sendSuccess(() ->
                Component.literal("§aПредмет §e" + itemId + "§a добавлен в фильтр!"), true);

        return 1;
    }

    private static int removeFromFilter(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getEntity();
        String itemId = StringArgumentType.getString(context, "item");

        if (player == null) return 0;

        ItemStack weapon = player.getMainHandItem();

        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FILTER.get(), weapon) == 0) {
            context.getSource().sendFailure(
                    Component.literal("У вас нет зачарования 'Фильтр' на оружии в руке!")
            );
            return 0;
        }

        FilterData filterData = FilterData.fromItemStack(weapon);
        filterData.removeItem(itemId);
        FilterData.saveToItemStack(weapon, filterData);

        context.getSource().sendSuccess(() ->
                Component.literal("§aПредмет §e" + itemId + "§a удален из фильтра!"), true);

        return 1;
    }

    private static int listFilter(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getEntity();

        if (player == null) return 0;

        ItemStack weapon = player.getMainHandItem();

        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FILTER.get(), weapon) == 0) {
            context.getSource().sendFailure(
                    Component.literal("У вас нет зачарования 'Фильтр' на оружии в руке!")
            );
            return 0;
        }

        FilterData filterData = FilterData.fromItemStack(weapon);
        Collection<String> filtered = filterData.getFilteredItems();

        if (filtered.isEmpty()) {
            context.getSource().sendSuccess(() ->
                    Component.literal("§7Фильтр пуст"), true);
        } else {
            context.getSource().sendSuccess(() ->
                    Component.literal("§6Отфильтрованные предметы:"), true);

            for (String itemId : filtered) {
                context.getSource().sendSuccess(() ->
                        Component.literal("  §e- " + itemId), true);
            }
        }

        return 1;
    }

    private static int clearFilter(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getEntity();

        if (player == null) return 0;

        ItemStack weapon = player.getMainHandItem();

        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FILTER.get(), weapon) == 0) {
            context.getSource().sendFailure(
                    Component.literal("У вас нет зачарования 'Фильтр' на оружии в руке!")
            );
            return 0;
        }

        FilterData filterData = FilterData.fromItemStack(weapon);
        filterData.clear();
        FilterData.saveToItemStack(weapon, filterData);

        context.getSource().sendSuccess(() ->
                Component.literal("§aФильтр очищен!"), true);

        return 1;
    }
}