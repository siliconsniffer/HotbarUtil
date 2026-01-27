package org.silicon.hotbarutil.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.silicon.hotbarutil.SlotLockManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Unique
    private static final Identifier SLOT_LOCK_TEXTURE = Identifier.of("hotbarutil", "textures/gui/slot_lock.png");

    @Shadow @Final protected ScreenHandler handler;
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected Slot focusedSlot;

    @ModifyVariable(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItemTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V"),
        ordinal = 0
    )
    private List<Text> modifyTooltip(List<Text> original) {
        if (focusedSlot != null && focusedSlot.inventory != null) {
            if (focusedSlot.inventory.getClass().getSimpleName().equals("PlayerInventory")) {
                int slotIndex = focusedSlot.getIndex();

                if (slotIndex >= 0 && slotIndex < 9 && SlotLockManager.INSTANCE.isSlotLocked(slotIndex)) {
                    List<Text> modified = new ArrayList<>();
                    for (int i = 0; i < original.size(); i++) {
                        Text line = original.get(i);
                        if (i == 0) {
                            modified.add(Text.literal("§c[LOCKED] §r").append(line));
                        } else {
                            modified.add(line);
                        }
                    }
                    return modified;
                }
            }
        }
        return original;
    }
}
