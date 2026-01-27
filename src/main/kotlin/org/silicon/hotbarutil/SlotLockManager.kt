package org.silicon.hotbarutil

object SlotLockManager {
    private val lockedSlots = mutableMapOf<Int, SlotLockState>()

    fun toggleSlotLock(slotIndex: Int) {
        val currentState = lockedSlots[slotIndex]?.isLocked ?: false
        lockedSlots[slotIndex] = SlotLockState(!currentState)
    }

    fun isSlotLocked(slotIndex: Int): Boolean = lockedSlots[slotIndex]?.isLocked ?: false

    fun clearLocks() = lockedSlots.clear()

    fun getLockedSlots(): Map<Int, SlotLockState> = lockedSlots.toMap()

    fun getLockedSlot(slotIndex: Int): SlotLockState = lockedSlots[slotIndex] ?: SlotLockState(false)
}
