package com.alipay.bluewhale.core.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

/**
 * 与网络相关的操作
 * @author yannian
 *
 */
public class NetWorkUtils {
    private static Logger LOG = Logger.getLogger(NetWorkUtils.class);

    public static String local_hostname() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            LOG.error("local_hostname", e);
        }
        return hostname;
    }

    /**
     * 测试代码所用，判断是否可以绑定相关端口
     * 
     * @param port
     * @return
     * @throws IOException
     */
    public static int try_port(int port) throws IOException {
        ServerSocket socket = new ServerSocket(port);
        int rtn = socket.getLocalPort();
        socket.close();
        return rtn;
    }

    public static int available_port() {
        return available_port(0);
    }

    /**
     * 测试所用，尝试绑定prfered端口，如果已经被占用，则由系统随机分配
     * 
     * @param prefered
     * @return
     */
    public static int available_port(int prefered) {
        int rtn = -1;
        try {
            rtn = try_port(prefered);
        } catch (IOException e) {
            rtn = available_port();
        }
        return rtn;
    }
}
