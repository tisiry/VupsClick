package tisiry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
                    // Подсказка, если ввели просто /click
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(Text.literal("§6[VupsClick] Использование:").formatted(Formatting.GOLD));
                        ctx.getSource().sendFeedback(Text.literal("§f/click attack <тики> §7- авто-удар"));
                        ctx.getSource().sendFeedback(Text.literal("§f/click use <тики> §7- авто-использование"));
                        ctx.getSource().sendFeedback(Text.literal("§f/click custom <клавиша> <тики> §7- любая кнопка"));
                        ctx.getSource().sendFeedback(Text.literal("§f/click off §7- выключить"));
                        return 1;
                    })
                    // Команда OFF
                    .then(ClientCommandManager.literal("off").executes(ctx -> {
                        active = false;
                        targetKey = null;
                        ctx.getSource().sendFeedback(Text.literal("§c[VupsClick] Автокликер выключен!"));
                        return 1;
                    }))
                    // Команда ATTACK
                    .then(ClientCommandManager.literal("attack")
                            .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                                    .executes(ctx -> startClicker(ctx.getSource().getClient().options.attackKey, IntegerArgumentType.getInteger(ctx, "delay"), ctx))))
                    // Команда USE
                    .then(ClientCommandManager.literal("use")
                            .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                                    .executes(ctx -> startClicker(ctx.getSource().getClient().options.useKey, IntegerArgumentType.getInteger(ctx, "delay"), ctx))))
                    // Команда CUSTOM (для любых клавиш)
                    .then(ClientCommandManager.literal("custom")
                            .then(ClientCommandManager.argument("keyName", StringArgumentType.word())
                                    .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                                            .executes(ctx -> {
                                                String name = StringArgumentType.getString(ctx, "keyName");
                                                KeyBinding key = findKey(name);
                                                if (key != null) {
                                                    return startClicker(key, IntegerArgumentType.getInteger(ctx, "delay"), ctx);
                                                } else {
                                                    ctx.getSource().sendFeedback(Text.literal("§c[VupsClick] Клавиша §f" + name + " §cне найдена!"));
                                                    return 0;
                                                }
                                            }))))
            );
        });

        // Логика кликов
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!active || targetKey == null || client.player == null || client.currentScreen != null) return;
            if (client.player.isUsingItem()) return;

            tickCounter++;
            if (tickCounter >= delay) {
                // Эмуляция нажатия (совместимо с Vulkan)
                KeyBinding.onKeyPressed(targetKey.getDefaultKey());

                // Для атаки используем миксин, чтобы обойти private доступ
                if (targetKey == client.options.attackKey) {
                    client.execute(() -> ((MinecraftClientMixin)client).invokeDoAttack());
                } else if (targetKey == client.options.useKey) {
                    client.execute(() -> ((MinecraftClientMixin)client).invokeDoItemUse());
                }

                tickCounter = 0;
            }
        });
    }

    // Удобный метод запуска
    private int startClicker(KeyBinding key, int d, com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx) {
        targetKey = key;
        delay = d;
        active = true;
        tickCounter = 0;
        String keyName = key.getTranslationKey().replace("key.", "").replace("categories.", "");
        ctx.getSource().sendFeedback(Text.literal("§a[VupsClick] Запущен на: §f" + keyName + " §a(пауза: §f" + d + " §at.)"));
        return 1;
    }

    private KeyBinding findKey(String name) {
        for (KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
            if (k.getTranslationKey().toLowerCase().contains(name.toLowerCase())) return k;
        }
        return null;
    }
}
