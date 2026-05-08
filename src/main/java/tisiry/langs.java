package tisiry;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.context.CommandContext;

public class langs {
    // Метод, который проверяет текущий язык игры
    private static String getLang() {
        return Minecraft.getInstance().options.languageCode;
    }

    public static Component getOffMessage() {
        if (getLang().equals("ru_ru")) return Component.literal("§c[VupsClick] Автокликер ВЫКЛЮЧЕН!");
        // Если не русский, пишем на английском
        return Component.literal("§c[VupsClick] AutoClicker DISABLED!");
    }

    public static Component getOnMessage(String keyName, int delay) {
        if (getLang().equals("ru_ru"))
            return Component.literal("§a[VupsClick] Запущен на: §f" + keyName + " §a(пауза: §f" + delay + " т.)");

        return Component.literal("§a[VupsClick] Started on: §f" + keyName + " §a(delay: §f" + delay + " t.)");
    }

    public static Component getErrorMessage(String name) {
        if (getLang().equals("ru_ru"))
            return Component.literal("§c[VupsClick] Клавиша §f" + name + " §cне найдена!");

        return Component.literal("§c[VupsClick] Key §f" + name + " §cnot found!");
    }

    public static void sendHelp(CommandContext<FabricClientCommandSource> ctx) {
        if (getLang().equals("ru_ru")) {
            ctx.getSource().sendFeedback(Component.literal("§6--- [VupsClick Помощь] ---"));
            ctx.getSource().sendFeedback(Component.literal("§f/click attack <тики> §7- Бить"));
            ctx.getSource().sendFeedback(Component.literal("§f/click off §7- Выключить"));
        } else {
            ctx.getSource().sendFeedback(Component.literal("§6--- [VupsClick Help] ---"));
            ctx.getSource().sendFeedback(Component.literal("§f/click attack <ticks> §7- Attack"));
            ctx.getSource().sendFeedback(Component.literal("§f/click off §7- Turn off"));
        }
    }
}
