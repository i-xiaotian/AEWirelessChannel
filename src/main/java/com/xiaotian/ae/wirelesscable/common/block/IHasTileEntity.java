package com.xiaotian.ae.wirelesscable.common.block;

import net.minecraft.tileentity.TileEntity;

public interface IHasTileEntity {
    Class<? extends TileEntity> getTileEntityClass();
    String getTileEntityId();
}
