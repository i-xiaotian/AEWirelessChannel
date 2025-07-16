package com.xiaotian.ae.wirelesscable.common.entity;

import java.util.UUID;

public class ConnectionInfo {

    private String connectionKey;
    private int targetX;
    private int targetY;
    private int targetZ;
    private boolean isConnect;
    private boolean needUpdateGridNode;

    public ConnectionInfo() {
        this.connectionKey = UUID.randomUUID().toString();
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
        this.isConnect = false;
        this.needUpdateGridNode = true;
    }

    public int getTargetX() {
        return targetX;
    }

    public void setTargetX(final int targetX) {
        this.targetX = targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public void setTargetY(final int targetY) {
        this.targetY = targetY;
    }

    public int getTargetZ() {
        return targetZ;
    }

    public void setTargetZ(final int targetZ) {
        this.targetZ = targetZ;
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

    @Override
    public String toString() {
        return "ConnectionInfo{" +
                "connectionKey='" + connectionKey + '\'' +
                ", targetX=" + targetX +
                ", targetY=" + targetY +
                ", targetZ=" + targetZ +
                ", isConnect=" + isConnect +
                ", needUpdateGridNod=" + needUpdateGridNode +
                '}';
    }
}
