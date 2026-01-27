package org.silicon.hotbarutil.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.silicon.hotbarutil.SlotLockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Shadow public abstract Slot getSlot(int index);

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (player == null) return;

        ScreenHandler handler = (ScreenHandler) (Object) this;
        ItemStack cursorStack = handler.getCursorStack();

        if (slotIndex == -999 && !cursorStack.isEmpty()) {
            if (player.getInventory() != null && isFromLockedHotbarSlot(player.getInventory(), cursorStack)) {
                ci.cancel();
                return;
            }
        }

        if (actionType == SlotActionType.PICKUP && slotIndex >= 0) {
            if (slotIndex < handler.slots.size()) {
                Slot slot = getSlot(slotIndex);
                if (isLockedHotbarSlot(player.getInventory(), slot)) {
                    ci.cancel();
                    return;
                }
            }
        }

        if (actionType == SlotActionType.QUICK_MOVE && slotIndex >= 0) {
            if (slotIndex < handler.slots.size()) {
                Slot slot = getSlot(slotIndex);
                if (isLockedHotbarSlot(player.getInventory(), slot)) {
                    ci.cancel();
                    return;
                }
            }
        }

        if (actionType == SlotActionType.THROW) {
            if (slotIndex >= 0) {
                if (slotIndex < handler.slots.size()) {
                    Slot slot = getSlot(slotIndex);
                    if (isLockedHotbarSlot(player.getInventory(), slot)) {
                        ci.cancel();
                        return;
                    }
                }
            }
            if (slotIndex == -1) {
                int selectedSlot = player.getInventory().getSelectedSlot();
                if (SlotLockManager.INSTANCE.isSlotLocked(selectedSlot)) {
                    ci.cancel();
                }
            }
        }
    }

    @Unique
    private boolean isLockedHotbarSlot(PlayerInventory inventory, Slot slot) {
        if (inventory == null) return false;

        if (slot.inventory == inventory) {
            int slotId = slot.getIndex();
            if (slotId >= 0 && slotId < 9) {
                return SlotLockManager.INSTANCE.isSlotLocked(slotId);
            }
        }
        return false;
    }

    @Unique
    private boolean isFromLockedHotbarSlot(PlayerInventory inventory, ItemStack cursorStack) {
        if (inventory == null) return false;

        for (int i = 0; i < 9; i++) {
            if (SlotLockManager.INSTANCE.isSlotLocked(i)) {
                ItemStack hotbarStack = inventory.getStack(i);

                if (hotbarStack.isEmpty()) {
                    return true;
                }

                if (ItemStack.areItemsEqual(cursorStack, hotbarStack) &&
                    ItemStack.areItemsAndComponentsEqual(cursorStack, hotbarStack)) {
                    return true;
                }
            }
        }
        return false;
    }
}
