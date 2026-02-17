package org.silicon.hotbarutil.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import org.silicon.hotbarutil.SlotLockManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow @Final protected ScreenHandler handler;

    @Unique
    private static final Identifier SLOT_LOCK_TEXTURE = Identifier.of("hotbarutil", "textures/gui/slot_lock.png");

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        PlayerInventory playerInventory = client.player.getInventory();

        // Block clicking outside inventory (-999) to drop items from locked slots
        if (slotId == -999 && !handler.getCursorStack().isEmpty()) {
            if (isFromLockedHotbarSlot(playerInventory, handler.getCursorStack())) {
                ci.cancel();
                return;
            }
        }

        // Block interactions with locked hotbar slots
        if (slot != null && isLockedHotbarSlot(playerInventory, slot)) {
            ci.cancel();
            return;
        }

        // Block swap keybind (number keys) that would affect locked slots
        if (actionType == SlotActionType.SWAP && button >= 0 && button < 9 && SlotLockManager.INSTANCE.isSlotLocked(button)) {
            ci.cancel();
        }
    }

    @Inject(
        method = "renderMain",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlightFront(Lnet/minecraft/client/gui/DrawContext;)V",
            shift = At.Shift.AFTER
        )
    )
    private void renderSlotLockIcons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        PlayerInventory inventory = client.player.getInventory();

        for (Slot slot : handler.slots) {
            if (slot.inventory != inventory) continue;
            int index = slot.getIndex();
            if (index < 0 || index > 8) continue;

            if (SlotLockManager.INSTANCE.isSlotLocked(index)) {
                // Inside renderMain we are in translated coordinate space, so slot.x/slot.y are directly usable
                context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_LOCK_TEXTURE, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    @Unique
    private boolean isLockedHotbarSlot(PlayerInventory inventory, Slot slot) {
        if (slot.inventory == inventory) {
            int index = slot.getIndex();
            return index >= 0 && index < 9 && SlotLockManager.INSTANCE.isSlotLocked(index);
        }
        return false;
    }

    @Unique
    private boolean isFromLockedHotbarSlot(PlayerInventory inventory, ItemStack cursorStack) {
        for (int i = 0; i < 9; i++) {
            if (SlotLockManager.INSTANCE.isSlotLocked(i)) {
                ItemStack hotbarStack = inventory.getStack(i);

                // If the locked slot is now empty, cursor likely came from there
                if (hotbarStack.isEmpty()) {
                    return true;
                }

                // Check if cursor stack matches a locked slot's item
                if (ItemStack.areItemsEqual(cursorStack, hotbarStack) &&
                    ItemStack.areItemsAndComponentsEqual(cursorStack, hotbarStack)) {
                    return true;
                }
            }
        }
        return false;
    }
}