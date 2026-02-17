package org.silicon.hotbarutil.mixin.client;

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
    private void onDropItem(boolean dropAll, CallbackInfoReturnable<ItemStack> cir) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        if (SlotLockManager.INSTANCE.isSlotLocked(self.getInventory().getSelectedSlot())) {
            cir.cancel();
        }
    }
}