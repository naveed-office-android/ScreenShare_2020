package com.screen.share.newone.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.screen.share.newone.R;
import com.screen.share.newone.utils.EditTextFilter;
import com.thefinestartist.finestwebview.FinestWebView;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientActivity extends AppCompatActivity {

    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText et4;
    private EditText et5;
    ImageView btnQRclient;

    private AdView adView;
    private InterstitialAd interstitialAd;
    ///Audio sharing
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    AudioGroup m_AudioGroup;
    AudioStream m_AudioStream;
    String ip;
    int localPort;
    String remoteAddress;
    String remotePort ;
    Button btnShareAudio;
    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        initViews();
        init();

        bannerAd();
        interstitialAd();

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

    private void initViews() {
        et1 = findViewById(R.id.et_1);
        et2 = findViewById(R.id.et_2);
        et3 = findViewById(R.id.et_3);
        et4 = findViewById(R.id.et_4);
        et5 = findViewById(R.id.et_5);
        btnQRclient = findViewById(R.id.btnclientQr);
        btnQRclient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ClientActivity.this,QR_main.class);
                //intent.putExtra("toOpen", "hostIp");

                startActivity(intent);
            }
        });
        init_function();

    }
public void shareAudio()
{
    Intent intent=new Intent(ClientActivity.this,QR_main.class);
    intent.putExtra("toOpen", "shareAudio");

    startActivity(intent);
}
    private void init() {
        et1.setFilters(new EditTextFilter[]{new EditTextFilter()});
        et2.setFilters(new EditTextFilter[]{new EditTextFilter()});
        et3.setFilters(new EditTextFilter[]{new EditTextFilter()});
        et4.setFilters(new EditTextFilter[]{new EditTextFilter()});
    }

    private void onGo() {



        String etString1 = setDefaultEditText(et1, "192");
        String etString2 = setDefaultEditText(et2, "168");
        String etString3 = setDefaultEditText(et3, "10");
        String etString4 = setDefaultEditText(et4, "1");
        String etString5 = setDefaultEditText(et5, "8080");

        String url = "http://"
                + etString1 + "."
                + etString2 + "."
                + etString3 + "."
                + etString4 + ":"
                + etString5;
        hideKeyboard(this);
//        Intent i = new Intent(Intent.ACTION_VIEW);
//        i.setData(Uri.parse(url));
//        startActivity(i);


//
//        Intent intent = new Intent(ClientActivity.this , WebViewScreening.class);
//        intent.putExtra("URL" , url);
//        startActivity(intent);


        new FinestWebView.Builder(this).show(url);
    }

    public void onGoClick(View view) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            onGo();
            reqNewInterstitial();
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                onGo();
                reqNewInterstitial();
            }
        });
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String setDefaultEditText(EditText et, String s) {
        String etString;
        if (et.getText().toString().isEmpty()) {
            etString = s;
        } else {
            etString = et.getText().toString();
        }

        return etString;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    public void connect(View view) {
        try {
            m_AudioStream.associate(InetAddress.getByName(remoteAddress), Integer.parseInt(remotePort));
            Toast.makeText(ClientActivity.this, "Transforming", Toast.LENGTH_SHORT).show();
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

