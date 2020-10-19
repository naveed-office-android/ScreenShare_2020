package com.screen.share.newone.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.screen.share.newone.R;
import com.screen.share.newone.customviews.MyCardView;
import com.screen.share.newone.utils.Constant;

import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends AppCompatActivity {

    private AdView adView;
    private InterstitialAd interstitialAd;
    MyCardView host,client;
    public static String SHOWCASE_ID="host";
    public static String SHOWCASE_ID_CLIENT="client";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_custom_button);

//        batteryOptimizeMethod();
        MobileAds.initialize(this, getResources().getString(R.string.ad_mob_app_id));
        bannerAd();
        interstitialAd();

        SharedPreferences preferences = (SharedPreferences) getSharedPreferences(Constant.PREFS, MODE_PRIVATE);
        Constant.IS_PIN_ENABLED = preferences.getBoolean(Constant.PIN_ENABLED, false);
        Constant.PIN_STRING = preferences.getString(Constant.PIN_STRING, "0000");


        host=findViewById(R.id.btnHost);
        client=findViewById(R.id.btnClient);
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this,SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(host,
                getResources().getString(R.string.host_discription), "GOT IT");

        sequence.addSequenceItem(client,
                getResources().getString(R.string.client_discription), "GOT IT");

        sequence.start();

    }

    private void batteryOptimizeMethod() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
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

    public void onHostClick(View view) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            startActivity(new Intent(MainActivity.this, HostActivity.class));
            reqNewInterstitial();
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                startActivity(new Intent(MainActivity.this, HostActivity.class));
                reqNewInterstitial();
            }
        });
    }

    public void onClientClick(View view) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            startActivity(new Intent(MainActivity.this, ClientActivity.class));
            reqNewInterstitial();
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                startActivity(new Intent(MainActivity.this, ClientActivity.class));
                reqNewInterstitial();
            }
        });
    }

    public void onRateUsClick(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
    }

    public void onShareClick(View view) {

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out\n" + getResources().getString(R.string.app_name));
            String shareMessage = "\nLet me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName() + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {

        anotherDialog();
    }
    void anotherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Alert");
        builder.setMessage("Want to close application?");
        builder.setIcon(R.drawable.alert);
        builder.setPositiveButton("RATE US", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                like();
            }
        });
        builder.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
        builder.setNeutralButton("FEEDBACK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dislike();
            }
        });
        builder.show();
    }
    void like() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));

    }

    public void dislike() {
        Intent myIntent = new Intent(Intent.ACTION_SEND);
        PackageManager pm = getPackageManager();
        Intent tempp = new Intent(Intent.ACTION_SEND);
        tempp.setType("*/*");
        List<ResolveInfo> resInfo = pm.queryIntentActivities(tempp, 0);
        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            if (ri.activityInfo.packageName.contains("android.gm")) {
                myIntent.setComponent(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
                myIntent.setAction(Intent.ACTION_SEND);
                myIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"info@innovagictechnologies.com"});
                myIntent.setType("text/html");
                myIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + getResources().getString(R.string.app_name));
            }
        }
        try {
            startActivity(myIntent);
        } catch (Exception ee) {
            Toast.makeText(getApplicationContext(), "Your phone don't support GMail", Toast.LENGTH_SHORT).show();
        }
    }
}
