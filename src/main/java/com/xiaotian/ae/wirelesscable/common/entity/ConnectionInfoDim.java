package com.xiaotian.ae.wirelesscable.common.entity;

public class ConnectionInfoDim extends ConnectionInfo {

    private int dim;

    public ConnectionInfoDim() {
        super();
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
