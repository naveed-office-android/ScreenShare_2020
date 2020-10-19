package com.screen.share.newone.webServer;

import com.screen.share.newone.activity.HostActivity;
import com.yanzhenjie.andserver.RequestHandler;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;


public class WebServerInfoHandler implements RequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        StringEntity stringEntity = new StringEntity("" +
                HostActivity.tvIp.getText().toString() + ":" +
                HostActivity.socketServerPort, "utf-8");
        response.setEntity(stringEntity);
    }
}
