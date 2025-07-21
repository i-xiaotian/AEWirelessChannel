package com.xiaotian.ae.wirelesscable.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public interface ITileWithWireless {
    Class<? extends TileEntity> getTileEntityClass();
    String getTileEntityId();
    boolean needInitTagFromItemStack();
    void initAEConnectionFromItemStackTag(@Nonnull NBTTagCompound tagCompound, @Nonnull TileEntity tileEntity);
}
