package tisiry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import tisiry.mixin.MinecraftClientMixin;

public class click implements ClientModInitializer {
    public static boolean active = false;
    public static KeyBinding targetKey = null;
    public static int delay = 5;
    private static int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("click")
                    .executes(ctx -> {
                        // Используем твой файл langs.java для помощи
                        langs.sendHelp(ctx);
                        return 1;
                    })
                    .then(ClientCommandManager.literal("off").executes(ctx -> {
                        active = false;
                        targetKey = null;
                        // Сообщение о выключении из твоего JSON через langs.java
                        ctx.getSource().sendFeedback(langs.getOffMessage());
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("attack")
                            .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                                    .executes(ctx -> start(ctx.getSource().getClient().options.attackKey, IntegerArgumentType.getInteger(ctx, "delay"), ctx))))
                    .then(ClientCommandManager.literal("use")
                            .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                                    .executes(ctx -> start(ctx.getSource().getClient().options.useKey, IntegerArgumentType.getInteger(ctx, "delay"), ctx))))
                    .then(ClientCommandManager.literal("custom")
                            .then(ClientCommandManager.argument("keyName", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        for (KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
                                            // Исправлено для маппингов 1.21.1
                                            builder.suggest(k.getBoundKeyTranslationKey().replace("key.", ""));
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                                            .executes(ctx -> {
                                                String name = StringArgumentType.getString(ctx, "keyName");
                                                KeyBinding key = findKey(name);
                                                if (key != null) return start(key, IntegerArgumentType.getInteger(ctx, "delay"), ctx);
                                                // Сообщение об ошибке из JSON через langs.java
                                                ctx.getSource().sendFeedback(langs.getErrorMessage(name));
                                                return 0;
                                            }))))
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Оптимизация производительности (важно для Vulkan)
            if (!active || targetKey == null || client.player == null || client.currentScreen != null) return;
            if (client.player.isUsingItem()) return;

            tickCounter++;
            if (tickCounter >= delay) {
                MinecraftClientMixin accessor = (MinecraftClientMixin) client;

                if (targetKey == client.options.attackKey) {
                    // Прямой вызов через миксин (быстрее и стабильнее)
                    client.execute(accessor::invokeDoAttack);
                } else if (targetKey == client.options.useKey) {
                    client.execute(accessor::invokeDoItemUse);
                } else {
                    // Для кастомных кнопок оставляем симуляцию
                    KeyBinding.onKeyPressed(targetKey.getDefaultKey());
                }
                tickCounter = 0;
            }
        });
    }

    private int start(KeyBinding key, int d, com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx) {
        targetKey = key;
        delay = d;
        active = true;
        tickCounter = 0;

        // Получаем красивое название (напр. "Левая кнопка мыши")
        String localizedName = Text.translatable(key.getBoundKeyTranslationKey()).getString();

        // Передаем данные в langs.java, чтобы он взял фразу из твоего ru_ru.json или en_us.json
        ctx.getSource().sendFeedback(langs.getOnMessage(localizedName, d));
        return 1;
    }

    private KeyBinding findKey(String name) {
        for (KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
            // Исправлено для маппингов 1.21.1
            if (k.getBoundKeyTranslationKey().toLowerCase().contains(name.toLowerCase())) return k;
        }
        return null;
    }
}
