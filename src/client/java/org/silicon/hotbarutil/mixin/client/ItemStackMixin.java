package org.silicon.hotbarutil.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.silicon.hotbarutil.SlotLockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void modifyTooltipForLockedSlot(Item.TooltipContext context, net.minecraft.entity.player.PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        ItemStack self = (ItemStack) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        PlayerInventory inventory = client.player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack slotStack = inventory.getStack(i);
            if (slotStack == self && SlotLockManager.INSTANCE.isSlotLocked(i)) {
                List<Text> tooltip = cir.getReturnValue();
                if (!tooltip.isEmpty()) {
                    Text originalName = tooltip.getFirst();
                    tooltip.set(0, Text.literal("§c[LOCKED] §r").append(originalName));
                }
                break;
            }
        }
    }
}
