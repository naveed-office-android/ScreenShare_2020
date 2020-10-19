package com.screen.share.newone.webServer;

//import android.util.Log;

import com.screen.share.newone.utils.Constant;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SocketServer extends WebSocketServer {

    public static final int PORT_ERROR = 1;
    public static final int NORMAL_ERROR = 0;
    public static final int SERVER_CLOSE_ERROR = 2;

    private WebServerListener webServerListener;

    public void setListener(WebServerListener listener) {
        webServerListener = listener;
    }

    public SocketServer(InetSocketAddress address) {
        super(address);
    }

    public static SocketServer init(String host, int port) {
        return new SocketServer(new InetSocketAddress(host, port));
    }


    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (Constant.IS_PIN_ENABLED){
            conn.send(Constant.PIN_STRING);
        }
        String connIp = conn.getRemoteSocketAddress().getAddress().toString().replace("/", "");
        connList.add(connIp);
        webServerListener.onWebServerConnectionChanged(connList);
    }

    public void stopWithException() {
        try {
            this.stop();
            running = false;
            webServerListener.onWebServerStatusChanged(running);
        } catch (IOException e) {
            e.printStackTrace();
            webServerListener.onWebServerError(SERVER_CLOSE_ERROR);
        } catch (InterruptedException e) {
            e.printStackTrace();
            webServerListener.onWebServerError(SERVER_CLOSE_ERROR);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

//        Log.d("Message", message);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    private boolean running = false;

    public boolean isRunning() {
        return running;
    }

    private List<String> connList = new ArrayList<>();


    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {

    }

    @Override
    public void onStart() {
        running = true;
        webServerListener.onWebServerStatusChanged(running);
    }

    @Override
    public void onClosing(WebSocket conn, int code, String reason, boolean remote) {
        super.onClosing(conn, code, reason, remote);
        String connIp = conn.getRemoteSocketAddress().getAddress().toString().replace("/", "");
        for (String ip : connList) {
            if (ip.equals(connIp)) {
                connList.remove(ip);
                break;
            }
        }
        webServerListener.onWebServerConnectionChanged(connList);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Address already in use")) {
                webServerListener.onWebServerError(PORT_ERROR);
                return;
            }
        }
        webServerListener.onWebServerError(NORMAL_ERROR);
    }

}
