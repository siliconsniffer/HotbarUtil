package org.silicon.hotbarutil.mixin.client;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.silicon.hotbarutil.SlotLockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final Identifier SLOT_LOCK_TEXTURE = Identifier.of("hotbarutil", "textures/gui/slot_lock.png");

    @Inject(method = "renderHotbar", at = @At("RETURN"))
    private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        int x = scaledWidth / 2 - 91;
        int y = scaledHeight - 22;

        for (int i = 0; i < 9; i++) {
            if (SlotLockManager.INSTANCE.isSlotLocked(i)) {
                int slotX = x + i * 20 + 3;
                int slotY = y + 3;
                context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_LOCK_TEXTURE, slotX, slotY, 0, 0, 16, 16, 16, 16);
            }
        }
    }
}
