package org.silicon.hotbarutil.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.silicon.hotbarutil.SlotLockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void onDropItem(boolean dropAll, CallbackInfoReturnable<ItemStack> cir)
    {
        if (MinecraftClient.getInstance().player == null) return;
        if (SlotLockManager.INSTANCE.isSlotLocked(MinecraftClient.getInstance().player.getInventory().getSelectedSlot())) {
            cir.cancel();
        }
    }
}
