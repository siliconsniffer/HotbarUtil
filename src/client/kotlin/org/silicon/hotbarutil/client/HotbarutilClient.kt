package org.silicon.hotbarutil.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import org.silicon.hotbarutil.SlotLockManager
import org.silicon.hotbarutil.mixin.client.HandledScreenAccessor

class HotbarutilClient : ClientModInitializer {
    private lateinit var hotbarUtilPrev: KeyBinding
    private lateinit var hotbarUtilNext: KeyBinding
    private lateinit var hotbarUtilToggLock: KeyBinding
    private var lastHoveredSlotIndex: Int? = null

    override fun onInitializeClient() {
        val category = KeyBinding.Category(Identifier.of("hotbarutil", "main"))
        hotbarUtilPrev = KeyBindingHelper.registerKeyBinding(
            KeyBinding("key.hotbarutil.hotbar_util_prev", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, category)
        )
        hotbarUtilNext = KeyBindingHelper.registerKeyBinding(
            KeyBinding("key.hotbarutil.hotbar_util_next", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, category)
        )
        hotbarUtilToggLock = KeyBindingHelper.registerKeyBinding(
            KeyBinding("key.hotbarutil.hotbar_util_togglock", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, category)
        )

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            handleKeyPresses()
        }

        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            if (screen is HandledScreen<*>) {
                ScreenEvents.afterRender(screen).register { _, _, mouseX, mouseY, _ ->
                    detectHoveredSlot(screen, mouseX, mouseY)
                }

                ScreenKeyboardEvents.afterKeyPress(screen).register { _, keyInput ->
                    if (hotbarUtilToggLock.matchesKey(keyInput)) {
                        val inventory = MinecraftClient.getInstance().player?.inventory ?: return@register
                        SlotLockManager.toggleSlotLock(lastHoveredSlotIndex ?: inventory.selectedSlot)
                    }
                }
            }
        }
    }

    private fun detectHoveredSlot(screen: HandledScreen<*>, mouseX: Int, mouseY: Int) {
        val player = MinecraftClient.getInstance().player ?: return
        val accessor = screen as HandledScreenAccessor
        val screenX = accessor.getX()
        val screenY = accessor.getY()

        lastHoveredSlotIndex = null

        for (slot in screen.screenHandler.slots) {
            if (slot.inventory != player.inventory) continue
            val slotIndex = slot.index
            if (slotIndex !in 0..8) continue

            val slotX = screenX + slot.x
            val slotY = screenY + slot.y

            if (mouseX in slotX until slotX + 16 && mouseY in slotY until slotY + 16) {
                lastHoveredSlotIndex = slotIndex
                break
            }
        }
    }

    private fun handleKeyPresses() {
        val client = MinecraftClient.getInstance()
        if (client.currentScreen != null) return
        val inventory = client.player?.inventory ?: return

        when {
            hotbarUtilPrev.wasPressed() -> inventory.selectedSlot = (inventory.selectedSlot + 8) % 9
            hotbarUtilNext.wasPressed() -> inventory.selectedSlot = (inventory.selectedSlot + 1) % 9
            hotbarUtilToggLock.wasPressed() -> SlotLockManager.toggleSlotLock(inventory.selectedSlot)
        }
    }

}