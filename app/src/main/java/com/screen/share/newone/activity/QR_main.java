package com.screen.share.newone.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.screen.share.newone.R;
import com.screen.share.newone.fragment.Qr_codeScanFragment;

public class QR_main extends AppCompatActivity {
    Fragment fragment;
    FragmentManager fragmentManager;
    String toOpen;
    Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_main);
        toOpen = getIntent().getStringExtra("toOpen");
        /*bundle = new Bundle();
        if (toOpen.equals("shareAudio")) {
            bundle.putString("toOpen", "hostIP");
        }
        else
        {
            bundle.putString("toOpen", "shareAudio");
        }*/
        fragment = new Qr_codeScanFragment();
//        fragment.setArguments(bundle);
        fragmentManager = getSupportFragmentManager();
        try {
            if (fragment != null && fragmentManager != null)
                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
