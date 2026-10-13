package com.xiaotian.ae.wirelesscable.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Keeps card bindings and portable connections in the 1.21 item component format. */
public final class ItemConnectionData {
    private ItemConnectionData() {
    }

    public static CompoundTag get(ItemStack stack, String key) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.contains(key, net.minecraft.nbt.Tag.TAG_COMPOUND) ? data.getCompound(key) : null;
    }

    public static void put(ItemStack stack, String key, CompoundTag value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.put(key, value.copy()));
    }
}
