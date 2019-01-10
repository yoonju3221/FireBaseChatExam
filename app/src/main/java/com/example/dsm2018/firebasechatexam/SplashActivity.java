package com.example.dsm2018.firebasechatexam;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

public class SplashActivity extends AppCompatActivity {
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    int versionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        PackageInfo plnfo = null;
        try {
            plnfo = getPackageManager()
                    .getPackageInfo(SplashActivity.this.getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        versionCode = plnfo.versionCode;

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build();

        HashMap<String, Object> defalutConfigMap = new HashMap<>();
        defalutConfigMap.put("versionCode", versionCode);

        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(defalutConfigMap);

        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mFirebaseRemoteConfig.activateFetched();
                    displayMessage();
                }else{
                    Log.w("SplashActivity", "Fetch failed");
                }
            }
        });


    }

    private void displayMessage() {
        int newVersionCode =Integer.parseInt(mFirebaseRemoteConfig.getString("versionCode"));

        Log.d("SplashActivity", versionCode + ", " + newVersionCode);

        if(versionCode < newVersionCode){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("최신 버전이 아닙니다.").setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            builder.create().show();
        }else{
            Intent intent = new Intent(this,  SigninActivity.class);
            startActivity(intent);
            finish();
        }

    }

}
