package com.screen.share.newone.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.screen.share.newone.R;
import com.screen.share.newone.utils.Constant;

public class SettingsActivity extends AppCompatActivity {

    private ConstraintLayout clEnablPin;
    private CheckBox cbREnablePin;

    private ConstraintLayout clSetPin;
    private TextView tvSetPin;
    private TextView tvSetPinSummary;
    private TextView tvSetPinValue;

    private SharedPreferences preferences;
    private boolean isPinEnable;

    private AdView adView;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        init();

        bannerAd();
        interstitialAd();
    }

    private void initViews() {
        clEnablPin = (ConstraintLayout) findViewById(R.id.cl_enable_pin);
        cbREnablePin = (CheckBox) findViewById(R.id.cb_enable_pin);

        clSetPin = (ConstraintLayout) findViewById(R.id.cl_set_pin);
        tvSetPin = (TextView) findViewById(R.id.tv_set_pin);
        tvSetPinSummary = (TextView) findViewById(R.id.tv_set_pin_summary);
        tvSetPinValue = (TextView) findViewById(R.id.tv_set_pin_value);
    }

    private void init() {
        preferences = (SharedPreferences) getSharedPreferences(Constant.PREFS, MODE_PRIVATE);
        isPinEnable = preferences.getBoolean(Constant.PIN_ENABLED, false);
        if (!isPinEnable) {
            defaultView();
        } else {
            pinEnabledView();
        }
    }

    private void defaultView() {
        cbREnablePin.setChecked(false);
        isPinEnable = false;
        preferences.edit().putBoolean(Constant.PIN_ENABLED, false).apply();

        clSetPin.setEnabled(false);
        tvSetPin.setTextColor(getResources().getColor(R.color.colorHeaderTextDisabled));
        tvSetPinSummary.setTextColor(getResources().getColor(R.color.colorSecondaryTextDisabled));
        tvSetPinValue.setTextColor(getResources().getColor(R.color.colorHeaderTextDisabled));
        tvSetPinValue.setText("0000");
        preferences.edit().putString(Constant.PIN, "0000").apply();

        Constant.IS_PIN_ENABLED = preferences.getBoolean(Constant.PIN_ENABLED, false);
        Constant.PIN_STRING = preferences.getString(Constant.PIN, "0000");
    }

    private void pinEnabledView() {
        cbREnablePin.setChecked(true);
        isPinEnable = true;
        preferences.edit().putBoolean(Constant.PIN_ENABLED, true).apply();

        clSetPin.setEnabled(true);
        tvSetPin.setTextColor(getResources().getColor(R.color.colorHeaderText));
        tvSetPinSummary.setTextColor(getResources().getColor(R.color.colorSecondaryText));
        tvSetPinValue.setTextColor(getResources().getColor(R.color.colorAccent));
        tvSetPinValue.setText(preferences.getString(Constant.PIN, "0000"));
        preferences.edit().putString(Constant.PIN, tvSetPinValue.getText().toString()).apply();

        Constant.IS_PIN_ENABLED = preferences.getBoolean(Constant.PIN_ENABLED, false);
        Constant.PIN_STRING = preferences.getString(Constant.PIN, "0000");
    }

    private void pinDisabledView() {
        cbREnablePin.setChecked(false);
        isPinEnable = false;
        preferences.edit().putBoolean(Constant.PIN_ENABLED, false).apply();

        clSetPin.setEnabled(false);
        tvSetPin.setTextColor(getResources().getColor(R.color.colorHeaderTextDisabled));
        tvSetPinSummary.setTextColor(getResources().getColor(R.color.colorSecondaryTextDisabled));
        tvSetPinValue.setTextColor(getResources().getColor(R.color.colorHeaderTextDisabled));
        tvSetPinValue.setText(preferences.getString(Constant.PIN, "0000"));
        preferences.edit().putString(Constant.PIN, tvSetPinValue.getText().toString()).apply();

        Constant.IS_PIN_ENABLED = preferences.getBoolean(Constant.PIN_ENABLED, false);
        Constant.PIN_STRING = preferences.getString(Constant.PIN, "0000");
    }

    public void onEnablePinClick(View view) {
        if (!isPinEnable) {
            pinEnabledView();
        } else {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            } else {
                pinDisabledView();
                reqNewInterstitial();
            }

            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    pinDisabledView();
                    reqNewInterstitial();
                }
            });
        }
    }

    public void onSetPinClick(View view) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title("Set Pin");
        builder.content("Enter pin.\nDefault:0000");
        builder.inputRange(4, 4);
        builder.inputType(InputType.TYPE_CLASS_NUMBER);
        builder.cancelable(false);

        builder.input(Constant.PIN_STRING, null, false, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                preferences.edit().putString(Constant.PIN, input.toString()).apply();
                tvSetPinValue.setText(input.toString());

                Constant.PIN_STRING = tvSetPinValue.getText().toString();
            }
        });
        builder.positiveText("OK");
        builder.negativeText("CANCEL");
        builder.show();
    }

    public void onDefaultSettingsClick(View view) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            defaultView();
            reqNewInterstitial();
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                defaultView();
                reqNewInterstitial();
            }
        });
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}
