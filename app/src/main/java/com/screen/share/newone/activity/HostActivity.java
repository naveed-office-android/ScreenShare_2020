package com.screen.share.newone.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.screen.share.newone.R;
import com.screen.share.newone.hosting.HostingListener;
import com.screen.share.newone.hosting.HostingService;
import com.screen.share.newone.record.RecordingListener;
import com.screen.share.newone.record.RecordingService;
import com.screen.share.newone.utils.Constant;
import com.screen.share.newone.utils.Network;
import com.screen.share.newone.utils.PermissionUtil;
import com.screen.share.newone.webServer.WebServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class HostActivity extends AppCompatActivity {

    private ImageView ivWifi;
    private ImageView ivHotspot;
    private Button ivHosting, ivRecording;
    //private ImageView ivRecording;
    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;
    public final static int WIDTH = 400;
    public final static int HEIGHT = 400;
    WifiManager wifiManager;
    public static TextView tvIp;
    private TextView tvPort;
    private TextView tvPin;

    //ImageView qrscanner;
    public static int webServerPort;
    public static int socketServerPort = 8012;

    private LinearLayout llPin;

    private LinearLayout llHost;

    private SharedPreferences preferences;

    private boolean isHosting;
    private boolean isRecording;
    private boolean isPinEnabled;

    private BroadcastReceiver broadcastReceiverNetworkState;

    private int networkStateMode = 0;

    public static final int RECORD_REQUEST_CODE = 101;
    private MediaProjectionManager projectionManager;
    private RecordingService recordingService;
    private HostingService hostingService;
    private AdView adView;
    private InterstitialAd interstitialAd;
    boolean onceHost;
    Menu menu;


    ///Audio sharing
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    AudioGroup m_AudioGroup;
    AudioStream m_AudioStream;
    String ip;
    int localPort;
    String remoteAddress;
    String remotePort ;
    Button btnShareAudio;
    EditText etip,etport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        initViews();
        init();
        initPrefs();
        initViewsControl();

        refreshUi();
        refreshIp();

        bannerAd();
        interstitialAd();

        onceHost = preferences.getBoolean("onceHost", false);
        if (!onceHost) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("onceHost", true);
            editor.commit();
            editor.apply();

            new MaterialShowcaseView.Builder(this)
                    .setTarget(ivHosting)
                    .setDismissText("GOT IT")
                    .setContentText(getResources().getString(R.string.start_host_discrp))
                    .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                    // .singleUse(SHOWCASE_ID_HOST) // provide a unique ID used to ensure it is only shown once
                    .show();

        }
        init_function();

    }

    private void initViews() {
        ivWifi = findViewById(R.id.iv_wifi);
        ivHotspot = findViewById(R.id.iv_hotspot);
        ivHosting = findViewById(R.id.btnstartHosting);
        ivRecording = findViewById(R.id.btnstartRecording);

        tvIp = findViewById(R.id.tv_ip);
        tvPort = findViewById(R.id.tv_port);
        tvPin = findViewById(R.id.tv_pin);

        llHost = findViewById(R.id.ll_host);
        llPin = findViewById(R.id.ll_pin);
        btnShareAudio=findViewById(R.id.btnShareAudio);
        etip=findViewById(R.id.etIP);
        btnShareAudio=findViewById(R.id.btnShareAudio);

    }

    private void init() {
        tvPort.setText("8080");
        webServerPort = Integer.parseInt(tvPort.getText().toString());
        preferences = (SharedPreferences) getSharedPreferences(Constant.PREFS, MODE_PRIVATE);

        initBroadcastReceiverNetworkStateChanged();
        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        Intent hostIntent = new Intent(this, HostingService.class);
        bindService(hostIntent, hostingConnection, BIND_AUTO_CREATE);

        Intent recordIntent = new Intent(this, RecordingService.class);
        bindService(recordIntent, recordingConnection, BIND_AUTO_CREATE);
        btnShareAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQRAudio();
            }
        });
    }

    private void initPrefs() {
        isHosting = preferences.getBoolean(Constant.IS_HOSTING, false);
        isRecording = preferences.getBoolean(Constant.IS_RECORDING, false);
        isPinEnabled = preferences.getBoolean(Constant.PIN_ENABLED, false);
    }

    private void initViewsControl() {
        if (!isHosting) {
            /*ivHosting.setImageResource(getResources().
                    getDrawable(R.drawable.start_hosting));*///start hosting
            ivHosting.setText(R.string.start_hosting);
            ivRecording.setVisibility(View.GONE);
        } else {
            ivHosting.setText(R.string.stop_hosting);

//            ivHosting.setImageResource(getResources().
//                    getDrawable(R.drawable.stop_hosting));//stop hosting
            ivRecording.setVisibility(View.VISIBLE);
        }

        if (!isRecording) {
            ivRecording.setText(R.string.start_recording);

//            ivRecording.setImageResource(getResources().
//                    getDrawable(R.drawable.start_recording));//start recording
        } else {
            ivRecording.setText(R.string.stop_recording);

//            ivRecording.setImageResource(getResources().
//                    getDrawable(R.drawable.stop_recording));//stop recording
        }

        if (!isPinEnabled) {
            llPin.setVisibility(View.GONE);
        } else {
            llPin.setVisibility(View.VISIBLE);
        }

        tvPin.setText(preferences.getString(Constant.PIN, "0000"));
    }

    private void initBroadcastReceiverNetworkStateChanged() {
        final IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");
        filters.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        broadcastReceiverNetworkState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra("wifi_state", -66);
                if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())
                        && state == 13) {
                    networkStateMode = 1;
                } else {
                    networkStateMode = 0;
                }
                refreshUi();
                refreshIp();
            }
        };
        registerReceiver(broadcastReceiverNetworkState, filters);
    }

    private void refreshUi() {
        if (networkStateMode == 0) {
            /*ivWifi.setImageResource(R.drawable.wifi_on);
            ivHotspot.setImageResource(R.drawable.hotspot_off);*/
        } else if (networkStateMode == 1) {
            /*ivWifi.setImageResource(R.drawable.wifi_off);
            ivHotspot.setImageResource(R.drawable.hotspot_on);*/
        }
    }

    private void refreshIp() {
        if (networkStateMode == 0) {
            tvIp.setText(Network.getWifiIp(this));
        } else {
            tvIp.setText("192.168.43.1");
        }
    }

    private ServiceConnection hostingConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HostingService.HostingServiceBinder binder = (HostingService.HostingServiceBinder) service;
            hostingService = binder.getHostingService();
            isHosting = hostingService.isRunning();

            hostingService.setListener(new HostingListener() {
                @Override
                public void onServerStatusChanged(boolean isRunning) {
                    isHosting = isRunning;
                }

                @Override
                public void onWebServerError(final int errorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (errorType == WebServer.PORT_ERROR)
                                Toast.makeText(hostingService, "\n" +
                                                "Due to the occupation of the port, has been changed to an available port",
                                        Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onSocketServerError(final int errorType) {
                }

                @Override
                public void onSocketServerConnectionChanged(final List<String> connList) {
//                    Snackbar.make(llHost, "" + connList, Snackbar.LENGTH_INDEFINITE).show();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private ServiceConnection recordingConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordingService.RecordBinder binder = (RecordingService.RecordBinder) service;
            recordingService = binder.getRecordService();
            recordingService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            isRecording = recordingService.isRunning();

            recordingService.setListener(new RecordingListener() {
                @Override
                public void onRecordStatusChanged(boolean isRunning) {
                    isRecording = isRunning;
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    public void onHostingClick(View view) {
        if (!wifiManager.isWifiEnabled()) {

            Snackbar snackbar = Snackbar.make(llHost, "Please Enable Wifi", Snackbar.LENGTH_SHORT)
                    .setAction("ENABLE", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            enableWifi();
                        }
                    });
            snackbar.show();
            return;
        }
        if (!isHosting && !hostingService.isRunning()) {
            preferences.edit().putBoolean(Constant.IS_HOSTING, true).apply();
            Snackbar.make(llHost, "Hosting Started", Snackbar.LENGTH_SHORT).show();

            hostingService.startHosting();

        } else {
            preferences.edit().putBoolean(Constant.IS_HOSTING, false).apply();
            Snackbar.make(llHost, "Hosting Stopped", Snackbar.LENGTH_SHORT).show();

            hostingService.stopHosting();
            recordingService.stopRecord();
        }
        initPrefs();
        initViewsControl();
        boolean onceRecord = preferences.getBoolean("onceRecord", false);
        if (!onceRecord) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("onceRecord", true);
            editor.commit();
            editor.apply();
            new MaterialShowcaseView.Builder(this)
                    .setTarget(ivRecording)
                    .setDismissText("GOT IT")
                    .setContentText("This is some amazing feature you should know about")
                    .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                    // .singleUse(SHOWCASE_ID_HOST) // provide a unique ID used to ensure it is only shown once
                    .show();

        }

    }

    private void onRecord() {
        PermissionUtil.requestPermission(
                HostActivity.this,
                new PermissionUtil.PermissionsListener() {
                    @Override
                    public void onGranted() {
                        if (isRecording) {
                            recordingService.stopRecord();

                            preferences.edit().putBoolean(Constant.IS_RECORDING, false).apply();
                            Snackbar.make(llHost, "Recording Stopped", Snackbar.LENGTH_SHORT).show();

                        } else {
                            ///Screen Capture
                            Intent captureIntent = projectionManager.createScreenCaptureIntent();
                            startActivityForResult(captureIntent, RECORD_REQUEST_CODE);

                            preferences.edit().putBoolean(Constant.IS_RECORDING, true).apply();
                            Snackbar.make(llHost, "Recording Started", Snackbar.LENGTH_SHORT).show();

                        }

                        initPrefs();
                        initViewsControl();

                    }
                },
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
    }


    public void onRecordingClick(View view) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            onRecord();
            reqNewInterstitial();
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                onRecord();
                reqNewInterstitial();
            }
        });
    }

    public void onSettingsClick(View view) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            startActivity(new Intent(HostActivity.this, SettingsActivity.class));
            reqNewInterstitial();
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                startActivity(new Intent(HostActivity.this, SettingsActivity.class));
                reqNewInterstitial();
            }
        });

    }

    public void onRefreshClick(View view) {
        refreshIp();
        refreshUi();
        Snackbar.make(llHost, "IP Refreshed", Snackbar.LENGTH_SHORT).show();
    }

    private void bannerAd() {
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int error) {
                adView.setVisibility(View.GONE);
            }

        });
    }

    private void interstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial));
        reqNewInterstitial();
    }

    public void reqNewInterstitial() {
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            MediaProjection mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordingService.setMediaProject(mediaProjection);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    recordingService.startRecord();
                    HostActivity.this.moveTaskToBack(true);
                }
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
        if (isHosting) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            builder.title("Warning");
            builder.content("Hosting is currently on. If you exit it will stop." +
                    " Are sure you want to exit the application?");
            builder.positiveText("OK");
            builder.negativeText("Cancel");
            builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    preferences.edit().putBoolean(Constant.IS_HOSTING, false).apply();
                    finish();
                }
            });
            builder.show();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPrefs();
        initViewsControl();
    }

    @Override
    public void onDestroy() {
        //releasing resources

        hostingService.removeListener();
        recordingService.removeListener();
        unbindService(hostingConnection);
        unbindService(recordingConnection);
        if (broadcastReceiverNetworkState != null) {
            unregisterReceiver(broadcastReceiverNetworkState);
        }

        preferences.edit().putBoolean(Constant.IS_HOSTING, false).apply();
        preferences.edit().putBoolean(Constant.IS_RECORDING, false).apply();
//killing service
        hostingService.stopHosting();
        recordingService.stopRecord();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    void showQR() {
        if (isHosting && isRecording) {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.qr_dialog);
            ImageView qrcode = dialog.findViewById(R.id.ivqrCode);
            // this is a small sample use of the QRCodeEncoder class from zxing
            try {
                // generate a 150x150 QR code
                Bitmap bm = encodeAsBitmap(tvIp.getText().toString() + ":" + tvPort.getText().toString());

                if (bm != null) {
                    qrcode.setImageBitmap(bm);
                }
                dialog.show();
            } catch (WriterException e) {
                //eek }
                Toast.makeText(HostActivity.this, "Error Occurred", Toast.LENGTH_LONG).show();

            }

        } else {
            Toast.makeText(HostActivity.this, "Please Start hosting and Recording", Toast.LENGTH_LONG).show();
        }
        // Snackbar.make(HostActivity.this, "Start hosting and Recording", Snackbar.LENGTH_SHORT);
    }
    void showQRAudio() {
        if (isHosting && isRecording) {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.qr_dialog);
            ImageView qrcode = dialog.findViewById(R.id.ivqrCode);
            // this is a small sample use of the QRCodeEncoder class from zxing
            try {
                // generate a 150x150 QR code
                Bitmap bm = encodeAsBitmap(ip+ ":" + localPort);

                if (bm != null) {
                    qrcode.setImageBitmap(bm);
                }
                dialog.show();
            } catch (WriterException e) {
                //eek }
                Toast.makeText(HostActivity.this, "Error Occurred", Toast.LENGTH_LONG).show();

            }

        } else {
            Toast.makeText(HostActivity.this, "Please Start hosting and Recording", Toast.LENGTH_LONG).show();
        }
        // Snackbar.make(HostActivity.this, "Start hosting and Recording", Snackbar.LENGTH_SHORT);
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, HEIGHT, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }

        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.host_menu, menu);
        this.menu = menu;


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_setting) {
            onSettingsClick(item.getActionView());
        }
        if (id == R.id.menu_scanner) {
            showQR();
        }
        return super.onOptionsItemSelected(item);
    }

    void enableWifi() {
        wifiManager.setWifiEnabled(true);
    }

    void disableWifi() {
        wifiManager.setWifiEnabled(false);
    }
    public void connect(View view) {
        try {
            //remoteAddress
            m_AudioStream.associate(InetAddress.getByName(remoteAddress), Integer.parseInt(remotePort));
            Toast.makeText(HostActivity.this, "Transforming", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        m_AudioStream.join(m_AudioGroup);
    }

    void disconnect() {
        m_AudioStream.release();
    }

    void init_function() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
            m_AudioGroup = new AudioGroup();
            m_AudioGroup.setMode(AudioGroup.MODE_NORMAL);
            //port number
            m_AudioStream = new AudioStream(InetAddress.getByAddress(getLocalIPAddress2()));
            localPort = m_AudioStream.getLocalPort();
            m_AudioStream.setCodec(AudioCodec.PCMU);
            m_AudioStream.setMode(RtpStream.MODE_NORMAL);


        } catch (Exception e) {
            Log.e("----------------------", e.toString());
            e.printStackTrace();
        }
    }
    private byte[] getLocalIPAddress2() {
        byte[] bytes = null;

        try {
            // get the string ip

            //wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

            // convert to bytes
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            bytes = new byte[0];
            if (inetAddress != null) {
                bytes = inetAddress.getAddress();
            }

        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(this, R.string.phone_voip_incompatible, Toast.LENGTH_SHORT).show();
        }

        return bytes;
    }
}
