package com.xiaotian.ae.wirelesscable.entity;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class ConnectionInfo {

    private String connectionKey;
    private int inputBusX;
    private int inputBusY;
    private int inputBusZ;
    private int outputBusX;
    private int outputBusY;
    private int outputBusZ;
    private boolean isConnect;
    private boolean needUpdateGridNode;

    public ConnectionInfo(final BlockPos pos) {
        this.connectionKey = UUID.randomUUID().toString();
        this.inputBusX = pos.getX();
        this.inputBusY = pos.getY();
        this.inputBusZ = pos.getZ();
        this.outputBusX = 0;
        this.outputBusY = 0;
        this.outputBusZ = 0;
        this.isConnect = false;
        this.needUpdateGridNode = true;
    }

    public int getOutputBusX() {
        return outputBusX;
    }

    public void setOutputBusX(final int outputBusX) {
        this.outputBusX = outputBusX;
    }

    public int getOutputBusY() {
        return outputBusY;
    }

    public void setOutputBusY(final int outputBusY) {
        this.outputBusY = outputBusY;
    }

    public int getOutputBusZ() {
        return outputBusZ;
    }

    public void setOutputBusZ(final int outputBusZ) {
        this.outputBusZ = outputBusZ;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(final boolean connect) {
        isConnect = connect;
    }

    public boolean isNeedUpdateGridNode() {
        return needUpdateGridNode;
    }

    public void setNeedUpdateGridNode(final boolean needUpdateGridNode) {
        this.needUpdateGridNode = needUpdateGridNode;
    }

    public String getConnectionKey() {
        return connectionKey;
    }

    public void setConnectionKey(final String connectionKey) {
        this.connectionKey = connectionKey;
    }

    public int getInputBusX() {
        return inputBusX;
    }

    public void setInputBusX(final int inputBusX) {
        this.inputBusX = inputBusX;
    }

    public int getInputBusY() {
        return inputBusY;
    }

    public void setInputBusY(final int inputBusY) {
        this.inputBusY = inputBusY;
    }

    public int getInputBusZ() {
        return inputBusZ;
    }

    public void setInputBusZ(final int inputBusZ) {
        this.inputBusZ = inputBusZ;
    }

    @Override
    public String toString() {
        return "ConnectionInfo{" +
                "connectionKey='" + connectionKey + '\'' +
                ", inputBusX=" + inputBusX +
                ", inputBusY=" + inputBusY +
                ", inputBusZ=" + inputBusZ +
                ", outputBusX=" + outputBusX +
                ", outputBusY=" + outputBusY +
                ", outputBusZ=" + outputBusZ +
                ", isConnect=" + isConnect +
                ", needUpdateGridNode=" + needUpdateGridNode +
                '}';
    }
}
