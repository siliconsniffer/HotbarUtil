package org.silicon.hotbarutil.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
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
            KeyBinding(
                "key.hotbarutil.hotbar_util_prev",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                category
            )
        )
        hotbarUtilNext = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.hotbarutil.hotbar_util_next",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                category
            )
        )
        hotbarUtilToggLock = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.hotbarutil.hotbar_util_togglock",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                category
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            handleKeyPresses()
        }

        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            if (screen is HandledScreen<*>) {
                ScreenEvents.afterRender(screen).register { _, context, mouseX, mouseY, _ ->
                    detectHoveredSlotAndRender(screen, context, mouseX, mouseY)
                }

                ScreenKeyboardEvents.afterKeyPress(screen).register { _, keyInput ->
                    if (hotbarUtilToggLock.matchesKey(keyInput)) {
                        val inventory = MinecraftClient.getInstance().player?.inventory ?: return@register
                        val slotToToggle = lastHoveredSlotIndex ?: inventory.selectedSlot
                        SlotLockManager.toggleSlotLock(slotToToggle)
                    }
                }
            }
        }
    }

    private fun detectHoveredSlotAndRender(screen: HandledScreen<*>, context: DrawContext, mouseX: Int, mouseY: Int) {
        val client = MinecraftClient.getInstance()
        val handler = screen.screenHandler

        lastHoveredSlotIndex = null

        val lockTexture = Identifier.of("hotbarutil", "textures/gui/slot_lock.png")

        val screenPos = if (screen is HandledScreenAccessor) {
            screen.getX() to screen.getY()
        } else {
            (context.scaledWindowWidth - 176) / 2 to (context.scaledWindowHeight - 166) / 2
        }

        val (screenX, screenY) = screenPos

        for (slot in handler.slots) {
            if (slot.inventory != null && slot.inventory == client.player?.inventory) {
                val slotIndex = slot.index

                val slotX = screenX + slot.x
                val slotY = screenY + slot.y
                if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                    if (slotIndex in 0..8) {
                        lastHoveredSlotIndex = slotIndex
                    }
                }
                if (slotIndex in 0..8 && SlotLockManager.isSlotLocked(slotIndex)) {
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, lockTexture, slotX, slotY, 0f, 0f, 16, 16, 16, 16)
                }
            }
        }
    }

    private fun handleKeyPresses() {
        val inventory = MinecraftClient.getInstance().player?.inventory ?: return

        if (MinecraftClient.getInstance().currentScreen != null) return

        when {
            hotbarUtilPrev.wasPressed() -> inventory.selectedSlot = (inventory.selectedSlot + 8) % 9
            hotbarUtilNext.wasPressed() -> inventory.selectedSlot = (inventory.selectedSlot + 1) % 9
            hotbarUtilToggLock.wasPressed() -> SlotLockManager.toggleSlotLock(inventory.selectedSlot)
        }
    }
}
