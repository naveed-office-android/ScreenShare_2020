package com.screen.share.newone.webServer;

import java.util.List;

public interface WebServerListener {
    void onWebServerStatusChanged(boolean isRunning);

    void onWebServerError(int errorType);

    void onWebServerConnectionChanged(List<String> connList);
}
