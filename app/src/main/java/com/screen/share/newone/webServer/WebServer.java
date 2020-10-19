package com.screen.share.newone.webServer;

import android.content.res.AssetManager;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;
import com.yanzhenjie.andserver.website.AssetsWebsite;

public class WebServer {

    public static Server init(AssetManager assetManager, int port, Server.Listener listener) {
        AndServer andServer = new AndServer.Build()
                .port(port)
                .timeout(10 * 1000)
                .registerHandler("wsinfo",new WebServerInfoHandler())
                .website(new AssetsWebsite(assetManager, ""))
                .listener(listener)
                .build();

        return andServer.createServer();
    }

    public static final int NORMAL_ERROR = 0;
    public static final int PORT_ERROR = 1;


    public static Server initChs(AssetManager assetManager, int port, Server.Listener listener) {
        AndServer andServer = new AndServer.Build()
                .port(port)
                .timeout(10 * 1000)
                .registerHandler("wsinfo",new WebServerInfoHandler())
                .website(new AssetsWebsite(assetManager, "chs"))
                .listener(listener)
                .build();

        return andServer.createServer();
    }

}
