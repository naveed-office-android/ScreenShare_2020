package com.screen.share.newone.hosting;

import java.util.List;

public interface HostingListener {
    void onServerStatusChanged(boolean isRunning);

    void onWebServerError(int errorType);

    void onSocketServerError(int errorType);

    void onSocketServerConnectionChanged(List<String> connList);
}
