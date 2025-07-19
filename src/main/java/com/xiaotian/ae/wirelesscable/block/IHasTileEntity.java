package com.xiaotian.ae.wirelesscable.block;

import net.minecraft.tileentity.TileEntity;

public interface IHasTileEntity {
    Class<? extends TileEntity> getTileEntityClass();
    String getTileEntityId();
}
