package tisiry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
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
                    .executes(ctx -> { langs.sendHelp(ctx); return 1; })

                    .then(ClientCommandManager.literal("off").executes(ctx -> {
                        active = false;
                        targetKey = null;
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
                                    // ВОТ ТУТ ПОДСКАЗКИ:
                                    .suggests((ctx, builder) -> {
                                        for (KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
                                            builder.suggest(k.getTranslationKey().replace("key.", ""));
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                                            .executes(ctx -> {
                                                String name = StringArgumentType.getString(ctx, "keyName");
                                                KeyBinding key = findKey(name);
                                                if (key != null) return start(key, IntegerArgumentType.getInteger(ctx, "delay"), ctx);
                                                ctx.getSource().sendFeedback(langs.getErrorMessage(name));
                                                return 0;
                                            }))))
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!active || targetKey == null || client.player == null || client.currentScreen != null) return;
            if (client.player.isUsingItem()) return;

            tickCounter++;
            if (tickCounter >= delay) {
                KeyBinding.onKeyPressed(targetKey.getDefaultKey());
                if (targetKey == client.options.attackKey) {
                    client.execute(() -> ((MinecraftClientMixin)client).invokeDoAttack());
                } else if (targetKey == client.options.useKey) {
                    client.execute(() -> ((MinecraftClientMixin)client).invokeDoItemUse());
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
        String name = key.getTranslationKey().replace("key.", "");
        ctx.getSource().sendFeedback(langs.getOnMessage(name, d));
        return 1;
    }

    private KeyBinding findKey(String name) {
        for (KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
            if (k.getTranslationKey().toLowerCase().contains(name.toLowerCase())) return k;
        }
        return null;
    }
}
