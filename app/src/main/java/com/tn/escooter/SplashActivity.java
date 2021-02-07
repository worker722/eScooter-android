package com.tn.escooter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.tn.escooter.buletooth.Discover2Activity;
import com.tn.escooter.buletooth.TwoWheelActivity;
import com.tn.escooter.utils.Model.User;
import com.tn.escooter.utils.utils;

import static com.tn.escooter.utils.utils.sharedPref;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if(!checkPermission()) return;
        goToNextPage();
    }
    public void goToNextPage(){
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String token = sharedPref.getString(utils.shared_key.token, MyApplication.mine ? "token" : null);
        int userid = sharedPref.getInt(utils.shared_key.userid, 0);
        String serial_number = sharedPref.getString(utils.shared_key.serial_number, null);
        if(MyApplication.mine){
            userid = 774;
            serial_number = "TN2020P117A001";
        }
        MyApplication.cur_user = new User(token, userid, serial_number, null);
        MyApplication.max_speed = sharedPref.getFloat(utils.shared_key.max_speed, 0f);
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if(MyApplication.mine){
                            startActivity(new Intent(SplashActivity.this, TwoWheelActivity.class));
                            finish();
                            return;
                        }
                        if(token == null || token.isEmpty()) startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        else startActivity(new Intent(SplashActivity.this, Discover2Activity.class));
                        finish();
                    }
                },
                1000);
    }

    public boolean checkPermission() {
        if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission("android.permission.BLUETOOTH") != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.BLUETOOTH"}, 18);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        goToNextPage();
    }

}