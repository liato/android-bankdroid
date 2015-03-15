package com.liato.bankdroid.utils;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkUtils {

    private NetworkUtils() {
    }

    public static boolean isInternetAvailable() {
        return ping(new byte[]{8, 8, 8, 8}, 500);
    }


    public static boolean ping(byte[] ipAddress, int timeout) {
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(timeout);
            datagramSocket.connect(InetAddress.getByAddress(ipAddress), 7);
            if (datagramSocket.isConnected()) {
                return true;
            }
        } catch (SocketException | UnknownHostException e) {
            return false;
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }
        return false;
    }
}
