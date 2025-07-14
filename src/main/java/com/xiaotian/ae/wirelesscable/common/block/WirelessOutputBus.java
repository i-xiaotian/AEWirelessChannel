package com.xiaotian.ae.wirelesscable.common.block;

import com.xiaotian.ae.wirelesscable.AEWirelessCable;
import com.xiaotian.ae.wirelesscable.common.tile.WirelessOutputBusTile;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WirelessOutputBus extends Block implements ITileEntityProvider {

    public WirelessOutputBus() {
        super(Material.GLASS);
        this.setCreativeTab(CreativeTabs.MATERIALS);
        this.setRegistryName(new ResourceLocation(AEWirelessCable.modID, "network_hub"));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull final World worldIn, final int meta) {
        return new WirelessOutputBusTile();
    }

}
