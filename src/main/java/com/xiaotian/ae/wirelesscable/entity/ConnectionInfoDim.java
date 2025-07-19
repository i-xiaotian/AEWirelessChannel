package com.xiaotian.ae.wirelesscable.entity;

import net.minecraft.util.math.BlockPos;

public class ConnectionInfoDim extends ConnectionInfo {

    private int dim;

    public ConnectionInfoDim(final BlockPos pos) {
        super(pos);
        this.dim = 0;
    }

    public int getDim() {
        return dim;
    }

    public void setDim(final int dim) {
        this.dim = dim;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", ", dim=" + dim + "}");
    }
}
