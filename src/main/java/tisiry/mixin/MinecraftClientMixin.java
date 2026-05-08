package tisiry.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftClientMixin {
    // В Mojang Mappings методы часто начинаются с "start"
    // Попробуй сменить название на точное из маппингов
    @Invoker("startAttack")
    boolean invokeDoAttack();

    @Invoker("startUseItem")
    void invokeDoItemUse();
}
