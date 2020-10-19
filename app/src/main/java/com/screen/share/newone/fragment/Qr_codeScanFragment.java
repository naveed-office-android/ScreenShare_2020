package com.screen.share.newone.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.screen.share.newone.R;
import com.thefinestartist.finestwebview.FinestWebView;

import java.util.Collection;

public class Qr_codeScanFragment extends Fragment implements View.OnClickListener {
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT = 1;

    TextView tvQRresult;
    WebView webView;
    String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    Bundle bundle;
    String toOpen;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_qrcode_reader, container, false);
        // Initialize variables
        /*bundle=getArguments();
        toOpen=bundle.getString("toOpen");*/
        tvQRresult = rootview.findViewById(R.id.QRResult);
        webView = rootview.findViewById(R.id.webView);
        getRuntimePermissions();
        openScanner(IntentIntegrator.QR_CODE_TYPES, R.string.scan_qrcode);

        return rootview;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                //Snackbar.make(getActivity(), "Go");
                 getActivity().finish();
            } else {
                /*if(toOpen.equals("hostIp"))*/ {

                    Toast.makeText(getActivity(), "Scan Result: " + result.getContents(), Toast.LENGTH_SHORT).show();
                    String contents = data.getStringExtra("SCAN_RESULT");
                    tvQRresult.setText(contents);

                    String url = "http://" + result.getContents();
                    new FinestWebView.Builder(getActivity()).show(url);
                }


                    //webView.setVisibility(View.GONE);

            }
        }
    }

    /**
     * Open scanner
     *
     * @param scannerType - type (qr code/bar code)
     * @param promptId    - string resource id for prompt
     */
    public void openScanner(Collection<String> scannerType, int promptId) {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        // use forSupportFragment or forFragment method to use fragments instead of activity
        integrator.setDesiredBarcodeFormats(scannerType);
        integrator.setPrompt(getActivity().getString(promptId));
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.initiateScan();
    }

    private void getRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    @Override
    public void onClick(View v) {

    }
}
