package com.tn.escooter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.BitmapRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.tn.escooter.bluetooth.DiscoverActivity;
import com.tn.escooter.buletooth.Discover2Activity;
import com.tn.escooter.buletooth.Settings2Activity;
import com.tn.escooter.buletooth.TwoWheelActivity;
import com.tn.escooter.utils.CacheStore;
import com.tn.escooter.utils.apiService;
import com.tn.escooter.utils.utils;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class MoreActivity extends Activity {
    @BindView(R.id.mainSpeed)
    TextView mainSpeed;
    @BindView(R.id.txt_max_speed) TextView maxSpeed;

    @BindView(R.id.img_avatar)
    ImageView img_avatar;
    @BindView(R.id.txt_username)
    TextView txt_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        ButterKnife.bind( this);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MyApplication.SCOOTER_TYPE == utils.bluetooth_type.LENZOD){
            String speed = String.valueOf(TwoWheelActivity.getInstance().trip);
            if(TwoWheelActivity.getInstance().is_km_per){
                mainSpeed.setText(speed+"KM");
            }else{
                mainSpeed.setText(speed+"MI");
            }
        }else{
            String speed = String.format("%.1f", MainActivity.getInstance().trip);
            if(MainActivity.getInstance().is_km_per){
                mainSpeed.setText(speed+"KM");
            }else{
                mainSpeed.setText(speed+"MI");
            }
        }
        maxSpeed.setText(String.format("%.1f", MyApplication.max_speed)+"Km/h");
        apiService.updateOdo();
    }

    public void initData(){
        apiService.baseApi("info", apiService.request_type.get_info, new JSONObjectRequestListener(){
            @Override
            public void onResponse(JSONObject res) {
                try{
                    if(res.getBoolean("success") == true){
                        txt_username.setText(res.getString("name"));
                        MyApplication.cur_user.name = res.getString("name");
                        String avatar = res.getString("avatar");
                        if(!avatar.isEmpty()){
                            Bitmap bitmap = CacheStore.getInstance().getCacheFile(avatar);
                            if(bitmap != null){
                                img_avatar.setImageBitmap(bitmap);
                                return;
                            }

                            apiService.getBitmap(avatar, new BitmapRequestListener(){
                                @Override
                                public void onResponse(Bitmap response) {
                                    Log.e( avatar, response.toString());
                                    img_avatar.setImageBitmap(response);
                                    CacheStore.getInstance().saveCacheFile(avatar, response);
                                }

                                @Override
                                public void onError(ANError anError) {
                                    anError.printStackTrace();
                                }
                            });
                        }
                    }
                }catch (Exception err){
                    err.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {
                anError.printStackTrace();
            }
        });
    };
    @Optional
    @OnClick({ R.id.lay_bluetooth, R.id.lay_settings, R.id.lay_more, R.id.lay_main,
    R.id.img_btn_profile, R.id.lay_about, R.id.lay_customer_service, R.id.lay_version, R.id.lay_share})
    public void onClick(View view) {
        Intent intent = null;
        boolean isLenzod = (MyApplication.SCOOTER_TYPE == utils.bluetooth_type.LENZOD);
        switch (view.getId()) {
            case R.id.lay_bluetooth:
                intent = new Intent(this, DiscoverActivity.class);
                if(isLenzod) intent = new Intent(this, Discover2Activity.class);
                break;
            case R.id.lay_settings:
                intent = new Intent(this, SettingsActivity.class);
                if(isLenzod) intent = new Intent(this, Settings2Activity.class);
                break;
            case R.id.lay_more:
                intent = new Intent(this, MoreActivity.class);
                break;
            case R.id.lay_main:
                intent = new Intent(this, MainActivity.class);
                if(isLenzod) intent = new Intent(this, TwoWheelActivity.class);
                break;
            case R.id.img_btn_profile:
                intent = new Intent(this, ProfileActivity.class);
                break;
            case R.id.lay_about:
                intent = new Intent(this, AboutActivity.class);
                break;
            case R.id.lay_customer_service:
                intent = new Intent(this, CustomerServiceActivity.class);
                break;
            case R.id.lay_version:
                intent = new Intent(this, VersionActivity.class);
                break;
            default:
                break;
        }
        if(intent != null){
            startActivity(intent);
        }
    }
}