package net.vova.epicenchantments.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.vova.epicenchantments.client.HttpClient;

public class ReportCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("report")
                .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("report", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String player = StringArgumentType.getString(context, "player");
                                    String report = StringArgumentType.getString(context, "report");

                                    CommandSourceStack sourceStack = context.getSource();
                                    String playerName = sourceStack.getTextName();

                                    if (sourceStack.getEntity() instanceof ServerPlayer) {
                                        String reportText = "имя: " + player + "\nпричина: " + report;

                                        // Сообщаем игроку, что отправка началась
                                        sourceStack.sendSuccess(
                                                () -> Component.literal("§6⏳ Отправка репорта..."),
                                                false
                                        );

                                        HttpClient.sendReport(playerName, reportText, new HttpClient.Callback() {
                                            @Override
                                            public void onSuccess(String message) {
                                                sourceStack.sendSuccess(
                                                        () -> Component.literal("§a✅ " + message),
                                                        false
                                                );
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                sourceStack.sendSuccess(
                                                        () -> Component.literal("§c❌ Ошибка! " + error),
                                                        false
                                                );
                                            }
                                        });
                                    }
                                    return 1;
                                }))));
    }
}