package com.screen.share.newone.hosting;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.screen.share.newone.R;
import com.screen.share.newone.activity.HostActivity;
import com.screen.share.newone.notification.MyNotification;
import com.screen.share.newone.record.RxBusRecord;
import com.screen.share.newone.utils.Network;
import com.screen.share.newone.webServer.SocketServer;
import com.screen.share.newone.webServer.WebServer;
import com.screen.share.newone.webServer.WebServerListener;
import com.yanzhenjie.andserver.Server;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HostingService extends Service {

    private SocketServer socketServer;
    private Server server;
    private AssetManager mAssetManager;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private HostingListener listener;

    public void setListener(HostingListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        listener = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Disposable disposable =
                RxBusRecord.getDefault()
                        .toObservable(String.class)
                        .subscribeOn(Schedulers.io())
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                socketServer.broadcast(s);
                            }
                        })
                        .subscribe();
        compositeDisposable.add(disposable);
        mAssetManager = getAssets();
    }

    public void makeForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(
                    1,
                    MyNotification.from(this)
                            .setTitle("Share Screen")
                            .setActivityClass(HostActivity.class)
                            .build()
            );
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.screen.share.newone";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        Intent activityIntent = new Intent(this, HostActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Share Screen")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    private void createWebServer() {
        Server.Listener listener = new Server.Listener() {
            @Override
            public void onStarted() {
                if (HostingService.this.listener != null) {
                    HostingService.this.listener.onServerStatusChanged(true);
                    makeForeground();
                }
            }

            @Override
            public void onError(Exception e) {
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("Address already in use")) {
                        int randomPort = Network.getRandomPort();
                        HostActivity.webServerPort = randomPort;
                        createWebServer();
                        server.start();
                        if (HostingService.this.listener != null) {
                            HostingService.this.listener.onWebServerError(WebServer.PORT_ERROR);
                        }
                        return;
                    }
                }
                if (HostingService.this.listener != null) {
                    HostingService.this.listener.onWebServerError(WebServer.NORMAL_ERROR);
                }
            }

            @Override
            public void onStopped() {
                if (HostingService.this.listener != null) {
                    HostingService.this.listener.onServerStatusChanged(false);
                }
            }
        };


        if (getResources().getConfiguration().locale.getCountry().equals("CN")) {

            server = WebServer.initChs(mAssetManager, HostActivity.webServerPort, listener);
        } else {
            server = WebServer.init(mAssetManager, HostActivity.webServerPort, listener);
        }

    }

    private void createSocketServer() {

        socketServer = SocketServer.init("0.0.0.0", HostActivity.socketServerPort);

        socketServer.setListener(new WebServerListener() {
            @Override
            public void onWebServerStatusChanged(boolean isRunning) {
            }

            @Override
            public void onWebServerError(int errorType) {
                if (errorType == SocketServer.PORT_ERROR) {
                    int randomPort = Network.getRandomPort();
                    HostActivity.socketServerPort = randomPort;
                    createSocketServer();
                    socketServer.start();
                    return;
                }
                if (listener != null) {
                    listener.onSocketServerError(errorType);
                }
            }

            @Override
            public void onWebServerConnectionChanged(List<String> connList) {
                if (listener != null) {
                    listener.onSocketServerConnectionChanged(connList);
                }
            }
        });
    }

    public boolean isRunning() {
        return server != null && server.isRunning();
    }

    public void startHosting() {

        createWebServer();
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        createSocketServer();
        socketServer.start();
    }


    @Override
    public void onDestroy() {
        stopHosting();
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new HostingServiceBinder();
    }

    public class HostingServiceBinder extends Binder {
        public HostingService getHostingService() {
            return HostingService.this;
        }
    }

    public void stopHosting() {
        if (server != null) {
            server.stop();
        }
        if (socketServer != null) {
            socketServer.stopWithException();
        }
        stopForeground(true);
    }

}
