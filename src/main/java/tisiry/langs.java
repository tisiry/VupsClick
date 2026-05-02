package tisiry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class langs {
    // Исправленный метод проверки языка
    public static boolean isRussian() {
        String lang = MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
        return lang.equals("ru_ru");
    }

    public static Text getOffMessage() {
        return Text.literal(isRussian() ? "§c[VupsClick] Автокликер ВЫКЛЮЧЕН!" : "§c[VupsClick] AutoClicker DISABLED!");
    }

    public static Text getOnMessage(String key, int delay) {
        if (isRussian()) {
            return Text.literal("§a[VupsClick] Запущен на: §f" + key + " §a(пауза: §f" + delay + " т.)");
        } else {
            return Text.literal("§a[VupsClick] Started on: §f" + key + " §a(delay: §f" + delay + " t.)");
        }
    }

    public static Text getErrorMessage(String key) {
        return Text.literal(isRussian() ? "§c[VupsClick] Клавиша §f" + key + " §cне найдена!" : "§c[VupsClick] Key §f" + key + " §cnot found!");
    }

    public static void sendHelp(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> ctx) {
        if (isRussian()) {
            ctx.getSource().sendFeedback(Text.literal("§6--- [VupsClick Помощь] ---"));
            ctx.getSource().sendFeedback(Text.literal("§f/click attack <тики> §7- Ударить"));
            ctx.getSource().sendFeedback(Text.literal("§f/click use <тики> §7- Использовать"));
            ctx.getSource().sendFeedback(Text.literal("§f/click custom <кнопка> <тики> §7- Любая"));
            ctx.getSource().sendFeedback(Text.literal("§f/click off §7- Выключить"));
        } else {
            ctx.getSource().sendFeedback(Text.literal("§6--- [VupsClick Help] ---"));
            ctx.getSource().sendFeedback(Text.literal("§f/click attack <ticks> §7- Attack"));
            ctx.getSource().sendFeedback(Text.literal("§f/click use <ticks> §7- Use item"));
            ctx.getSource().sendFeedback(Text.literal("§f/click custom <key> <ticks> §7- Any key"));
            ctx.getSource().sendFeedback(Text.literal("§f/click off §7- Turn off"));
        }
    }
}
