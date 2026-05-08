package tisiry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

public class click implements ClientModInitializer {
    public static boolean active = false;
    public static KeyMapping targetKey = null;
    public static int delay = 5;
    private static int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("click")
                    .executes(ctx -> {
                        langs.sendHelp(ctx);
                        return 1;
                    })
                    .then(ClientCommands.literal("off").executes(ctx -> {
                        active = false;
                        targetKey = null;
                        ctx.getSource().sendFeedback(langs.getOffMessage());
                        return 1;
                    }))
                    .then(ClientCommands.literal("attack")
                            .then(ClientCommands.argument("delay", IntegerArgumentType.integer(1))
                                    .executes(ctx -> start(ctx.getSource().getClient().options.keyAttack, IntegerArgumentType.getInteger(ctx, "delay"), ctx))))
                    .then(ClientCommands.literal("use")
                            .then(ClientCommands.argument("delay", IntegerArgumentType.integer(1))
                                    .executes(ctx -> start(ctx.getSource().getClient().options.keyUse, IntegerArgumentType.getInteger(ctx, "delay"), ctx))))
                    .then(ClientCommands.literal("custom")
                            .then(ClientCommands.argument("keyName", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        for (KeyMapping k : Minecraft.getInstance().options.keyMappings) {
                                            builder.suggest(k.getName().replace("key.", ""));
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(ClientCommands.argument("delay", IntegerArgumentType.integer(1))
                                            .executes(ctx -> {
                                                String name = StringArgumentType.getString(ctx, "keyName");
                                                KeyMapping key = findKey(name);
                                                if (key != null) return start(key, IntegerArgumentType.getInteger(ctx, "delay"), ctx);
                                                ctx.getSource().sendFeedback(langs.getErrorMessage(name));
                                                return 0;
                                            }))))
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!active || targetKey == null || client.player == null || client.screen != null) return;

            tickCounter++;
            if (tickCounter >= delay) {
                tisiry.mixin.MinecraftClientMixin accessor = (tisiry.mixin.MinecraftClientMixin) client;
                if (targetKey == client.options.keyAttack) {
                    accessor.invokeDoAttack();
                } else if (targetKey == client.options.keyUse) {
                    accessor.invokeDoItemUse();
                } else {
                    targetKey.setDown(true);
                    KeyMapping.click(targetKey.getDefaultKey());
                    targetKey.setDown(false);
                }
                tickCounter = 0;
            }
        });
    }

    private int start(KeyMapping key, int d, CommandContext<FabricClientCommandSource> ctx) {
        targetKey = key;
        delay = d;
        active = true;
        tickCounter = 0;

        // Получаем красивое название (ЛКМ, ПКМ или иностранное название)
        String localizedName = key.getTranslatedKeyMessage().getString();

        // Отправляем это в наш умный langs
        ctx.getSource().sendFeedback(langs.getOnMessage(localizedName, d));
        return 1;
    }


    private KeyMapping findKey(String name) {
        for (KeyMapping k : Minecraft.getInstance().options.keyMappings) {
            if (k.getName().toLowerCase().contains(name.toLowerCase())) return k;
        }
        return null;
    }
}

