package com.screen.share.newone.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.screen.share.newone.R;
import com.screen.share.newone.utils.AllPreferences;

public class Privacy_policy extends AppCompatActivity {
    CheckBox checkBox;
    LottieAnimationView animationView;
    AllPreferences preferences;

    @Override
    protected void onResume() {
        preferences=new AllPreferences(this);
        if(preferences.getbol("firstTime"))
        {
            startActivity(new Intent(Privacy_policy.this,MainActivity.class));
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        checkBox=findViewById(R.id.checkbox_read);
        animationView=findViewById(R.id.lottie_progress);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    animationView.setVisibility(View.VISIBLE);
                    animationView.playAnimation();
                }
                else
                {
                    animationView.setVisibility(View.GONE);
                }
            }
        });

        animationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked())
                {
                    preferences.set("firstTime", true);
                    startActivity(new Intent(Privacy_policy.this,MainActivity.class));
                    finish();
                }
                else
                {
                    Toast.makeText(Privacy_policy.this, "Please Accept Terms and Conditions", Toast.LENGTH_LONG).show();
                    //StringUtils.showSnackbar(PrivacyPolicy.this,"Please Accept Terms and Conditions");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        //
    }
}
