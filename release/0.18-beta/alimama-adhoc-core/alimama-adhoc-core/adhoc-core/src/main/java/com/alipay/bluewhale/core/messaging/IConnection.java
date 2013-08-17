package com.alipay.bluewhale.core.messaging;

public interface IConnection {
    public byte[] recv();

    public void send(int task, byte[] message);

    public void close();
}
