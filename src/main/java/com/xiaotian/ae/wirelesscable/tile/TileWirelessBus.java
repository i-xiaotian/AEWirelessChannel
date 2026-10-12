package com.xiaotian.ae.wirelesscable.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import com.xiaotian.ae.wirelesscable.block.BlockBaseWirelessBus;
import com.xiaotian.ae.wirelesscable.config.AEWirelessCableConfig;
import com.xiaotian.ae.wirelesscable.chunk.NetworkChunkLoading;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

public abstract class TileWirelessBus extends TileEntity implements IGridProxyable, IActionHost, ITickable {

    private final AENetworkProxy proxy = new AENetworkProxy(this, "aeProxy", getVisualItemStack(), true);
    private boolean proxyReady;
    private boolean chunkLoadingEnabled;
    private int chunkLoadingRadius = -1;
    private int chunkLoadingExpected = -1;
    private int chunkLoadingHeld = -1;
    private int chunkLoadingShared = -1;
    private int chunkLoadingLimit = -1;

    public TileWirelessBus() {
        super();
    }

    public abstract ItemStack getVisualItemStack();

    @Override
    public AENetworkProxy getProxy() {
        return this.proxy;
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull final AEPartLocation aePartLocation) {
        return AECableType.SMART;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull final AEPartLocation aePartLocation) {
        return this.proxy.getNode();
    }

    @Override
    public void securityBreak() {
        this.getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {
    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return this.proxy.getNode();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        proxy.onChunkUnload();
    }

    @Override
    public void onLoad() {
        proxyReady = false;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        proxy.invalidate();
        proxyReady = false;
        NetworkChunkLoading.unregister(world, pos);
    }

    @Override
    public void validate() {
        super.validate();
        proxy.validate();
        proxyReady = false;
    }

    @Override
    protected void setWorldCreate(@Nonnull final World worldIn) {
        setWorld(worldIn);
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound compound) {
        super.writeToNBT(compound);
        proxy.writeToNBT(compound);
        compound.setBoolean("wirelessChunkLoadingEnabled", chunkLoadingEnabled);
        compound.setInteger("wirelessChunkLoadingRadius", chunkLoadingRadius);
        compound.setInteger("wirelessChunkLoadingExpected", chunkLoadingExpected);
        compound.setInteger("wirelessChunkLoadingHeld", chunkLoadingHeld);
        compound.setInteger("wirelessChunkLoadingShared", chunkLoadingShared);
        compound.setInteger("wirelessChunkLoadingLimit", chunkLoadingLimit);
        NBTTagCompound sidesTag = new NBTTagCompound();
        final EnumSet<EnumFacing> connectableSides = proxy.getConnectableSides();
        for (EnumFacing side : connectableSides) {
            sidesTag.setBoolean(String.valueOf(side.ordinal()), true);
        }
        compound.setTag("validSides", sidesTag);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull final NBTTagCompound compound) {
        super.readFromNBT(compound);
        proxy.readFromNBT(compound);
        chunkLoadingEnabled = compound.getBoolean("wirelessChunkLoadingEnabled");
        chunkLoadingRadius = compound.hasKey("wirelessChunkLoadingRadius")
                ? compound.getInteger("wirelessChunkLoadingRadius") : -1;
        chunkLoadingExpected = compound.hasKey("wirelessChunkLoadingExpected")
                ? compound.getInteger("wirelessChunkLoadingExpected") : -1;
        chunkLoadingHeld = compound.hasKey("wirelessChunkLoadingHeld")
                ? compound.getInteger("wirelessChunkLoadingHeld") : -1;
        chunkLoadingShared = compound.hasKey("wirelessChunkLoadingShared")
                ? compound.getInteger("wirelessChunkLoadingShared") : -1;
        chunkLoadingLimit = compound.hasKey("wirelessChunkLoadingLimit")
                ? compound.getInteger("wirelessChunkLoadingLimit") : -1;
        if (!compound.hasKey("validSides")) return;

        final NBTTagCompound sidesTag = compound.getCompoundTag("validSides");
        EnumSet<EnumFacing> newSides = EnumSet.noneOf(EnumFacing.class);
        final EnumFacing[] values = EnumFacing.values();
        for (EnumFacing facing : values) {
            if (!sidesTag.hasKey(String.valueOf(facing.ordinal()))) continue;
            newSides.add(facing);
        }
        proxy.setValidSides(newSides);
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(final NetworkManager networkManager, final SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    public boolean isChunkLoadingEnabled() {
        return chunkLoadingEnabled;
    }

    public int getChunkLoadingRadius() {
        return chunkLoadingRadius;
    }

    public int getChunkLoadingExpected() {
        return chunkLoadingExpected;
    }

    public int getChunkLoadingHeld() {
        return chunkLoadingHeld;
    }

    public int getChunkLoadingShared() {
        return chunkLoadingShared;
    }

    public int getChunkLoadingLimit() {
        return chunkLoadingLimit;
    }

    public void setChunkLoadingInfo(final boolean enabled, final int radius, final int expected,
                                    final int held, final int shared, final int limit) {
        if (chunkLoadingEnabled == enabled && chunkLoadingRadius == radius
                && chunkLoadingExpected == expected && chunkLoadingHeld == held
                && chunkLoadingShared == shared && chunkLoadingLimit == limit) return;

        chunkLoadingEnabled = enabled;
        chunkLoadingRadius = radius;
        chunkLoadingExpected = expected;
        chunkLoadingHeld = held;
        chunkLoadingShared = shared;
        chunkLoadingLimit = limit;
        markDirty();
        if (world != null && !world.isRemote) {
            final IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState, final IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void update() {
        if (world == null || pos == null || world.isRemote) return;

        final IBlockState currentBlockState = world.getBlockState(pos);
        if (currentBlockState.getBlock().isAir(currentBlockState, world, pos)) return;
        if (!currentBlockState.getProperties().containsKey(BlockBaseWirelessBus.POWERED)) return;

        updateChunkLoading();
        if (!proxyReady) {
            proxy.onReady();
            proxyReady = true;
        }
        final boolean active = proxy.isActive();
        final Boolean value = currentBlockState.getValue(BlockBaseWirelessBus.POWERED);

        if (active != value) {
            final IBlockState newBlockState = currentBlockState.withProperty(BlockBaseWirelessBus.POWERED, active);
            world.setBlockState(pos, newBlockState, 3);
        }
    }

    private void updateChunkLoading() {
        if (!AEWirelessCableConfig.GENERAL_CONFIG.forceChunk) {
            NetworkChunkLoading.unregister(world, pos);
            setChunkLoadingInfo(false, -1, -1, -1, -1, -1);
            return;
        }

        NetworkChunkLoading.register(world, pos);
        if (!chunkLoadingEnabled || chunkLoadingRadius != AEWirelessCableConfig.GENERAL_CONFIG.loadRadius
                || world.getTotalWorldTime() % 20L == 0L) {
            NetworkChunkLoading.syncMember(world, pos);
        }
    }
}
