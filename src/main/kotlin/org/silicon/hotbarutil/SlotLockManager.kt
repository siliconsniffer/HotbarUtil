package org.silicon.hotbarutil

object SlotLockManager {
    private val lockedSlots = mutableSetOf<Int>()

    fun toggleSlotLock(slotIndex: Int) {
        if (!lockedSlots.add(slotIndex)) {
            lockedSlots.remove(slotIndex)
        }
    }

    fun isSlotLocked(slotIndex: Int): Boolean = slotIndex in lockedSlots

    fun clearLocks() = lockedSlots.clear()

    fun getLockedSlots(): Map<Int, SlotLockState> = lockedSlots.associateWith { SlotLockState(true) }

    fun getLockedSlot(slotIndex: Int): SlotLockState = SlotLockState(slotIndex in lockedSlots)
}