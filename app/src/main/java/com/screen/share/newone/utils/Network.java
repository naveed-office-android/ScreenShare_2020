package com.screen.share.newone.utils;


import android.content.Context;
import android.net.wifi.WifiManager;

import java.util.Locale;
import java.util.Random;

public class Network {

    private static final int PORT_MIN = 1024;
    private static final int PORT_MAX = 49151;

    public static int getRandomPort() {
        return new Random().nextInt(PORT_MAX - PORT_MIN) + PORT_MIN;
    }

    public static String getWifiIp(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipInt = wm.getConnectionInfo().getIpAddress();
        String ipString = String.format(Locale.getDefault(), "%d.%d.%d.%d",
                (ipInt & 0xff), (ipInt >> 8 & 0xff), (ipInt >> 16 & 0xff), (ipInt >> 24 & 0xff));
        return ipString;
    }

}
