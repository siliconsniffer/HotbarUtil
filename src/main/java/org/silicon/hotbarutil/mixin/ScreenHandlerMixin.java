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
        PlayerInventory inventory = player.getInventory();

        // Block dropping items outside the inventory if they came from a locked slot
        if (slotIndex == -999 && !handler.getCursorStack().isEmpty()) {
            if (isFromLockedHotbarSlot(inventory, handler.getCursorStack())) {
                ci.cancel();
                return;
            }
        }

        // Block throw of the selected item (Q key outside a slot)
        if (actionType == SlotActionType.THROW && slotIndex == -1) {
            if (SlotLockManager.INSTANCE.isSlotLocked(inventory.getSelectedSlot())) {
                ci.cancel();
                return;
            }
        }

        // Block pickup, quick-move, and throw on locked hotbar slots
        if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            if (actionType == SlotActionType.PICKUP || actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.THROW) {
                if (isLockedHotbarSlot(inventory, getSlot(slotIndex))) {
                    ci.cancel();
                }
            }
        }
    }

    @Unique
    private boolean isLockedHotbarSlot(PlayerInventory inventory, Slot slot) {
        if (inventory == null) return false;
        if (slot.inventory == inventory) {
            int index = slot.getIndex();
            return index >= 0 && index < 9 && SlotLockManager.INSTANCE.isSlotLocked(index);
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