package org.silicon.hotbarutil.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
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

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        PlayerInventory playerInventory = client.player.getInventory();
        ItemStack cursorStack = handler.getCursorStack();

        // Block clicking outside inventory (-999) to drop items from locked slots
        if (slotId == -999 && !cursorStack.isEmpty()) {
            if (isFromLockedHotbarSlot(playerInventory, cursorStack)) {
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
        if (actionType == SlotActionType.SWAP) {
            // button is the hotbar slot index (0-8) when using number keys
            if (button >= 0 && button < 9 && SlotLockManager.INSTANCE.isSlotLocked(button)) {
                ci.cancel();
                return;
            }
        }

        // Block throw action on locked slots (Q key while hovering)
        if (actionType == SlotActionType.THROW && slot != null) {
            if (isLockedHotbarSlot(playerInventory, slot)) {
                ci.cancel();
                return;
            }
        }
    }

    @Unique
    private boolean isLockedHotbarSlot(PlayerInventory inventory, Slot slot) {
        if (inventory == null || slot == null || slot.inventory == null) return false;

        if (slot.inventory == inventory) {
            int slotIndex = slot.getIndex();
            if (slotIndex >= 0 && slotIndex < 9) {
                return SlotLockManager.INSTANCE.isSlotLocked(slotIndex);
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