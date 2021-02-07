package com.tn.escooter.buletooth;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tn.escooter.MoreActivity;
import com.tn.escooter.MyApplication;
import com.tn.escooter.R;
import com.tn.escooter.utils.BaseOnBluetoothListener;
import com.tn.escooter.utils.BytesUtils;
import com.tn.escooter.utils.MapSearchResultItem;
import com.tn.escooter.utils.apiService;
import com.tn.escooter.utils.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tn.escooter.MyApplication.odo_save_time;
import static com.tn.escooter.utils.utils.killProcess;
import static com.tn.escooter.utils.utils.sharedPref;

public class TwoWheelActivity extends FragmentActivity implements OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String PARAM_BT_DEVICE = "param_bt_device";
    boolean isExit = false;
    private BaseOnBluetoothListener listener;
    public Handler mHandler;

    public boolean bIsInit = false;
    public boolean is_km_per = false;
    public float per_num = 1.0f;
    public float trip = 0.f;
    public boolean is_lock = false;
    public Integer gear_num = Integer.valueOf(0);
    public boolean is_headlights = false;
    public static TwoWheelActivity instance = null;

    //map
    public String directionsUrl;
    public String search_result_address;
    public String search_result_id;
    public Location mLocation;
    public LatLng lalng;
    public Double latitude = Double.valueOf(0.0);
    public Double longitude = Double.valueOf(0.0);
    public LatLng end_lat;
    public String place_id;
    public boolean isTraking = false;

    //----------------------------------


    public static TwoWheelActivity getInstance() {
        if (instance == null) {
            instance = new TwoWheelActivity();
        }
        return instance;
    }

    @BindView(R.id.map_framelayout)
    FrameLayout mapFramelayout;
    @BindView(R.id.battery_text)
    TextView battery_text;
    @BindView(R.id.img_battery)
    ImageView img_battery;
    @BindView(R.id.lay_speedmetor)
    FrameLayout lay_speedmetor;
    @BindView(R.id.lay_map_speedmetor)
    FrameLayout lay_map_speedmetor;
    @BindView(R.id.mainModeText)
    TextView mainModeText;

    @BindView(R.id.mainSpeed)
    TextView mainSpeed;
    @BindView(R.id.mainTrip)
    TextView mainTrip;
    @BindView(R.id.img_pointer)
    ImageView img_pointer;
    @BindView(R.id.mainOdo)
    TextView mainOdo;
    @BindView(R.id.mainRemaining)
    TextView mainRemaining;
    @BindView(R.id.mainRemainingPer)
    TextView mainRemainingPer;

    @BindView(R.id.img_map_pointer)
    ImageView img_map_pointer;
    @BindView(R.id.mapmainSpeed)
    TextView mapMainSpeed;
    @BindView(R.id.mapmainOdo)
    TextView mapOdo;
    @BindView(R.id.img_map_battery)
    ImageView img_map_battery;
    @BindView(R.id.img_map_mode_view)
    ImageView img_map_mode_view;
    @BindView(R.id.mapmainTrip)
    TextView mapTrip;

    @BindView(R.id.img_btn_headlight)
    ImageView img_btn_headlight;
    @BindView(R.id.img_mode_view)
    ImageView img_mode_view;
    @BindView(R.id.img_btn_lock)
    ImageView img_btn_lock;
    @BindView(R.id.img_btn_mode)
    ImageView img_btn_mode;
    @BindView(R.id.lay_speed_metro)
    LinearLayout lay_speed_metro;

    @BindView(R.id.txt_autocomplete)
    AutoCompleteTextView txt_autocomplete;


    public void set_Default_Ui() {
        ViewTreeObserver vto = lay_speed_metro.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    lay_speed_metro.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    lay_speed_metro.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int swidth = lay_speed_metro.getMeasuredWidth();
                int height = lay_speed_metro.getMeasuredHeight();

                if (height < swidth) swidth = height;
                ViewGroup.LayoutParams params = lay_speedmetor.getLayoutParams();
                params.width = swidth;
                params.height = swidth;
                lay_speedmetor.setLayoutParams(params);
            }
        });

        img_btn_headlight.setVisibility(View.GONE);
        Display display = getWindowManager().getDefaultDisplay();
        int swidth = display.getWidth();
        int height = display.getHeight()*3/8;
        if (height < swidth) swidth = height;
        ViewGroup.LayoutParams params = lay_map_speedmetor.getLayoutParams();
        params.width = swidth;
        params.height = swidth;
        lay_map_speedmetor.setLayoutParams(params);

        mainSpeed.setText("0.0");
        mapMainSpeed.setText("0.0");
        mainTrip.setText("0000");
        mainOdo.setText("0000");
        mainModeText.setText("B");
        mapOdo.setText("0.0");
        mapTrip.setText("0.0");
        mainRemaining.setText("0000");
        set_per_ui(is_km_per);
        setBattery(0);
    }

    /* access modifiers changed from: public */
    public void set_per_ui(boolean b) {
        is_km_per = b;
        if (b) {
            mainRemainingPer.setText(R.string.km);
            per_num = 1.0f;
            return;
        }
        mainRemainingPer.setText(R.string.mile);
        per_num = 0.62f;
    }

    public int setBattery(int battery) {
        if (!(battery <= 100 && battery >= 0)) {
            return 0;
        }
        battery = Integer.valueOf(battery);
        int batter_percentage = R.drawable.ic_battery_0;

        if (battery == 0) batter_percentage = R.drawable.ic_battery_0;
        else if (battery <= 20) batter_percentage = R.drawable.ic_battery_20;
        else if (battery <= 40) batter_percentage = R.drawable.ic_battery_40;
        else if (battery <= 60) batter_percentage = R.drawable.ic_battery_60;
        else if (battery <= 80) batter_percentage = R.drawable.ic_battery_80;
        else if (battery <= 100) batter_percentage = R.drawable.ic_battery_100;

        img_battery.setImageResource(batter_percentage);
        img_map_battery.setImageResource(batter_percentage);

        battery_text.setText(battery + "%");
//        remain_text.setText("Remain: " + String.format("%.1f", new Object[]{Float.valueOf((((float) (battery * 20)) * per_num) / 100.0f)}));
        if (is_km_per) {
            mainRemaining.setText(String.format("%.1f", new Object[]{Float.valueOf((((float) (battery * 20)) * per_num) / 100.0f)}));
        }
        return battery;
    }

    public void setSpeed(String str_speed) {

        float max = 286.0f;
        float speed = ((float) Integer.parseInt(str_speed, 16)) / 1000.0f;

        if (MyApplication.max_speed < speed) {
            MyApplication.max_speed = speed;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(utils.shared_key.max_speed, speed);
            editor.apply();
        }

        mainSpeed.setText(String.format("%.1f", new Object[]{speed}));
        mapMainSpeed.setText(String.format("%.1f", new Object[]{speed}));
        float angle = max / 30 * speed;
        img_pointer.setRotation(angle);
        img_map_pointer.setRotation(angle);
    }

    public void setMode(int mode) {
        gear_num = mode;
        if (mode == 1) {
            img_mode_view.setImageResource(R.drawable.ic_mode1);
            img_map_mode_view.setImageResource(R.drawable.ic_mode1);
        } else if (mode == 2) {
            img_mode_view.setImageResource(R.drawable.ic_mode2);
            img_map_mode_view.setImageResource(R.drawable.ic_mode2);
        } else if (mode == 3) {
            img_mode_view.setImageResource(R.drawable.ic_mode3);
            img_map_mode_view.setImageResource(R.drawable.ic_mode3);
        }
    }

    public void setHeadlightLock(int type, boolean state) { //type: 0 headlight, 1: lock
        if (type == 0) {
            is_headlights = state;
            if (state) img_btn_headlight.setImageResource(R.drawable.ic_headlight_on);
            else img_btn_headlight.setImageResource(R.drawable.ic_headlight_off);
        } else {
            is_lock = state;
            if (state) img_btn_lock.setImageResource(R.drawable.ic_lock);
            else img_btn_lock.setImageResource(R.drawable.ic_unlock);
        }
    }

    private BroadcastReceiver mSettingActionBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String stringExtra = intent.getStringExtra(utils.Key.ACTION);
            StringBuilder sb = new StringBuilder();
            sb.append("Received order：");
            sb.append(stringExtra);
            Log.d("twowheel", sb.toString());
            if (!TextUtils.isEmpty(stringExtra)) {
                command(stringExtra);
            }
        }
    };
    private boolean on_text_change = false;

    @Override
    protected void onResume() {
        super.onResume();
        apiService.updateOdo();
    }

    public void selectLocation() {
        try {
            mGoogleApiClient_dash.connect();

            if (!TextUtils.isEmpty(search_result_address)) {
                txt_autocomplete.setText(search_result_address);
            }
            if (mLocation != null && !TextUtils.isEmpty(search_result_id)) {
                place_id = search_result_id;
                search_result_id = null;
                directionsUrl = getDirectionsUrl(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), null, place_id, null);
                on_text_change = false;
                new TwoWheelActivity.DownloadTask().execute(new String[]{directionsUrl});
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 16.0f));
                    }
                }, 5000);

            }
        } catch (Exception err) {
        }
    }

    /* access modifiers changed from: private */
    public String getDirectionsUrl(LatLng origin, LatLng dest, String place_id2, String type) {
        String str_dest;
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        if ("default".equals(type)) {
            str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        } else {
            str_dest = "destination=place_id:" + place_id2;
        }
        return "https://maps.googleapis.com/maps/api/directions/" + "json" + "?" + (str_origin + "&" + str_dest + "&" + "sensor=false" + "&" + "mode=bicycling") + "&key=AIzaSyBQTyKTsKrZ4AamGOtID4qHsmdgOohMbas";
    }

    public void onPause() {
        super.onPause();
        mGoogleApiClient_dash.disconnect();
    }

    public static void start(Activity activity, BluetoothDevice bluetoothDevice, Bundle bundle) {
        Intent intent = new Intent(activity, TwoWheelActivity.class);
        intent.putExtra(PARAM_BT_DEVICE, bluetoothDevice);
        ActivityCompat.startActivity(activity, intent, bundle);
    }

    public void initData() {
        this.listener = new BaseOnBluetoothListener() {
            public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                if (bluetoothGattCharacteristic != null) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            onRead(bluetoothGattCharacteristic.getValue());
                        }
                    });
                }
            }

            public void onBlueToothConneted() {
                loopRequestStatus();
            }

            public void onBlueToothDisconneted() {
                super.onBlueToothDisconneted();
                mHandler.removeMessages(1);
                mHandler.postDelayed(new Runnable() {
                    public final void run() {
                        Intent intent = new Intent(TwoWheelActivity.this, Discover2Activity.class);
                        intent.setFlags((int) 71303168);
                        startActivity(intent);
                        finish();
                    }
                }, 3000);
            }
        };
        QBlueToothManager.getInstance().addBluetoothListener(this.listener);
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.mHandler = new Handler() {
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    command("FF55010055");
                    sendEmptyMessageDelayed(1, 2000);
                }
            }
        };
        registerReceiver(this.mSettingActionBroadcastReceiver, new IntentFilter(utils.Lenzod_Action.SETTING_CHANGE));
    }

    public MyListViewAdapter myListViewAdapter;
    private ArrayList<MapSearchResultItem> mapSearchResultItems;

    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        instance = this;
        setContentView(R.layout.activity_main);
        ButterKnife.bind((Activity) this);
        set_Default_Ui();
        if(!MyApplication.mine){
            init();
            initData();
        }
        checkPermission();
        initGPS();
        initMyLocation();
//        try{
//            this.thread.start();
//        }catch (Exception err){
//
//        }
    }

    public String address;
    public boolean is_search = false;

    public void initMap() {
        try {
            this.mapSearchResultItems = new ArrayList<>();
            myListViewAdapter = new MyListViewAdapter(this, R.layout.search_item, mapSearchResultItems);
            txt_autocomplete.setThreshold(1);
            txt_autocomplete.setAdapter(myListViewAdapter);

            View locationButton = ((View) mapFragment.getView().findViewById((int) 1).getParent()).findViewById((int) 2);
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.width = 150;
            rlp.height = 150;
//            rlp.setMargins(0, 200, 40, 0);
            locationButton.setLayoutParams(rlp);
            txt_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    MapSearchResultItem mapSearchResultItem = (MapSearchResultItem) mapSearchResultItems.get(i);
                    search_result_address = mapSearchResultItem.getAddress_title();
                    search_result_id = mapSearchResultItem.getPlace_id();
                    selectLocation();
                }
            });

            txt_autocomplete.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    on_text_change = true;
                    String locale_language = Locale.getDefault().getLanguage();
                    Log.e("adf", locale_language);
                    address = txt_autocomplete.getText().toString();
                    if (!TextUtils.isEmpty(address)) {
                        is_search = true;
                        if (!"zh".equals(locale_language)) {
                            String directionsUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + address + "&key=AIzaSyBQTyKTsKrZ4AamGOtID4qHsmdgOohMbas";
                            new TwoWheelActivity.DownloadTask().execute(new String[]{directionsUrl});
                            return;
                        }
                        return;
                    }
                    is_search = false;
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } catch (Exception err) {

        }
    }

    @OnClick({R.id.lay_bluetooth, R.id.lay_settings, R.id.lay_more, R.id.img_btn_headlight, R.id.img_btn_mode, R.id.img_btn_lock, R.id.main_arrow_down_btn,
            R.id.main_arrow_up_btn, R.id.img_trash, R.id.img_voice})
    public void onClick(View view) {
        String str = null;
        Intent intent = null;
        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        switch (view.getId()) {
            case R.id.main_arrow_down_btn:
                ObjectAnimator animator = ObjectAnimator.ofFloat(mapFramelayout, "translationY", new float[]{-height, 0});
                animator.setDuration(1500);
                animator.start();
                mapFramelayout.setVisibility(View.VISIBLE);
                return;
            case R.id.main_arrow_up_btn:
                is_navigation = false;
                ObjectAnimator animatorup = ObjectAnimator.ofFloat(mapFramelayout, "translationY", new float[]{0, -height});
                animatorup.setDuration(1500);
                animatorup.start();
                animatorup.addListener(new Animator.AnimatorListener() {
                    public void onAnimationStart(Animator animator) {
                    }

                    public void onAnimationEnd(Animator animator) {
                        mapFramelayout.setVisibility(View.GONE);
                    }

                    public void onAnimationCancel(Animator animator) {
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }
                });
                return;
            case R.id.lay_bluetooth:
                intent = new Intent(this, Discover2Activity.class);
                break;
            case R.id.lay_settings:
                intent = new Intent(this, Settings2Activity.class);
                break;
            case R.id.lay_more:
                intent = new Intent(this, MoreActivity.class);
                break;
//            case R.id.img_btn_headlight:
//                intent = new Intent(this, CarLampSettingsActivity.class);
            case R.id.img_btn_lock /*2131296358*/:
                command(this.is_lock ? utils.Command.UNLOCK : utils.Command.LOCK);
                break;
            case R.id.img_btn_mode /*2131296363*/:
                if (gear_num == 1) {
                    str = utils.Command.GEAR_D1;
                } else if (gear_num == 2) {
                    str = utils.Command.GEAR_D2;
                } else if (gear_num == 3) {
                    str = utils.Command.GEAR_D3;
                }
                if (!TextUtils.isEmpty(str)) {
                    command(str);
                    break;
                }
                break;
            case R.id.img_trash:
                search_result_address = "";
                search_result_id = "";
                txt_autocomplete.setText("");
                mMap.clear();
                break;
            case R.id.img_voice /*2131690214*/:
                if (ContextCompat.checkSelfPermission(this, "android.permission.CALL_PHONE") != 0) {
                    ActivityCompat.requestPermissions(this, new String[]{"android.permission.CALL_PHONE"}, 3);
                    return;
                }
                startVoiceRecognitionActivity();
//                if (this.is_navigation) {
//                    this.mMap.clear();
//                    this.is_navigation = false;
//                    txt_autocomplete.setText("");
//                    search_result_id = null;
//                    this.place_id = null;
//                    return;
//                }
//                intent = new Intent(this, MapSearchActivity.class);
//                break;
        }
        if (intent != null) startActivity(intent);
    }

    private boolean command(byte[] bArr) {
        boolean write = QBlueToothManager.getInstance().write(bArr);
        if (!write) {
            this.mHandler.removeMessages(1);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("send command：");
            sb.append(new String(bArr));
            Log.d("TwoWheel", sb.toString());
        }
        return write;
    }

    public boolean command(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("send command：");
        sb.append(str);
        Log.d("TwoWheel", sb.toString());
        return command(BytesUtils.hexStringToBytes(str));
    }

    public boolean zero_start_mode = false;
    public boolean constant_speed = false;

    public void checkUpdateOdo() {
        Date cur_date = new Date();
        if (odo_save_time == null) {
            odo_save_time = cur_date;
            apiService.updateOdo();
        } else {
            long diff = odo_save_time.getTime() - cur_date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            if (minutes >= 20) {
                odo_save_time = cur_date;
                apiService.updateOdo();
            }
        }
    }

    public void onRead(byte[] bArr) {
        String bytesToHexString = BytesUtils.bytesToHexString(bArr);
        StringBuilder sb = new StringBuilder();
        String r2 = null;
        sb.append("Read data：");
        sb.append(bytesToHexString);
        Log.d("TwoWheel", sb.toString());
        if (!TextUtils.isEmpty(bytesToHexString)) {
            char c = 15;
            if (bytesToHexString.length() >= 6) {
                String substring = bytesToHexString.substring(0, 6);
                if (bytesToHexString.length() >= 8) {
                    bytesToHexString.substring(6, 8);
                }
                String substring2 = bytesToHexString.length() >= 10 ? bytesToHexString.substring(8, bytesToHexString.length() - 2) : "";
                int hashCode = substring.hashCode();
                if (hashCode != 2070318496) {
                    switch (hashCode) {
                        case 2070318449:
                            if (substring.equals("FF550A")) c = 0;
                            break;
                        case 2070318450:
                            if (substring.equals("FF550B")) c = 1;
                            break;
                        case 2070318451:
                            if (substring.equals("FF550C")) c = 2;
                            break;
                        case 2070318464:
                            if (substring.equals("FF5511")) c = 3;
                            break;
                        case 2070318470:
                            if (substring.equals("FF5517")) c = 4;
                            break;
                        case 2070318463:
                            if (substring.equals("FF5510")) c = 5;
                            break;
                        case 2070318452:
                            if (substring.equals("FF550D")) c = 6;
                            break;
                        case 2070318471:
                            if (substring.equals("FF5518")) c = 7;
                            break;
                        case 2070318480:
                            if (substring.equals("FF551A")) c = 8;
                            break;
                        case 2070318481:
                            if (substring.equals("FF551B")) c = 9;
                            break;
                        case 2070318482:
                            if (substring.equals("FF551C")) c = 10;
                            break;
                        case 2070318483:
                            if (substring.equals("FF551D")) c = 11;
                            break;
                        case 2070318484:
                            if (substring.equals("FF551E")) c = 12;
                            break;
                        case 2070318485:
                            if (substring.equals("FF551F")) c = 13;
                            break;
                    }
                } else if (substring.equals("FF5522")) {
                    c = 14;
                }
                String str = "03";
                String str2 = "FF55";
                String str3 = "01";
                String str4 = "02";
                switch (c) {
                    case 0:
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Speed：");
                        sb2.append(substring2);
                        Log.d("TwoWheel", sb2.toString());
                        if (!TextUtils.isEmpty(substring2) && !substring2.contains(str2)) {
                            try {
                                setSpeed(substring2);
                            } catch (NumberFormatException e) {
                                Log.d("TwoWheel", "Error converting vehicle speed！");
                                e.printStackTrace();
                            }
                        }
                        return;
                    case 1:
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append("This range：");
                        sb3.append(substring2);
                        Log.d("TwoWheel", sb3.toString());
                        if (!TextUtils.isEmpty(substring2) && !substring2.contains(str2)) {
                            try {
                                trip = (float) Long.valueOf(substring2, 16).longValue() / 1000.0f;
                                mainTrip.setText(String.format("%.1f", trip));
                                mapTrip.setText(String.format("%.1f", trip));
                                // this.mMainFragment.setThisTimeLength(((float) Long.valueOf(substring2, 16).longValue()) / 1000.0f);
                            } catch (NumberFormatException e2) {
                                Log.d("TwoWheel", "Error converting this mileage！");
                                e2.printStackTrace();
                            }
                        } else {
                        }
                        return;
                    case 2:
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append("total mileage：");
                        sb4.append(substring2);
                        Log.d("TwoWheel", sb4.toString());
                        if (!TextUtils.isEmpty(substring2) && !substring2.contains(str2)) {
                            try {
                                float odo = ((float) Long.valueOf(substring2, 16).longValue()) / 1000.0f;
                                MyApplication.cur_odo = odo * per_num;
                                checkUpdateOdo();
                                mainOdo.setText(String.format("%.1f", odo));
                                mapOdo.setText(String.format("%.1f", odo));
                                // this.mMainFragment.setAmountLength();
                            } catch (NumberFormatException e3) {
                                Log.d("TwoWheel", "Error converting total mileage！");
                                e3.printStackTrace();
                            }
                        }
                        return;
                    case 3:
                        StringBuilder sb5 = new StringBuilder();
                        sb5.append("Body temperature：");
                        sb5.append(substring2);
                        Log.d("TwoWheel", sb5.toString());
                        if (!TextUtils.isEmpty(substring2) && !substring2.contains(str2)) {
                            try {
                                // this.mMainFragment.setTemperature(Integer.parseInt(substring2, 16));
                            } catch (NumberFormatException e4) {
                                Log.d("TwoWheel", "Error converting car body temperature！");
                                e4.printStackTrace();
                            }
                        }
                        return;
                    case 4:
                        StringBuilder sb6 = new StringBuilder();
                        sb6.append("Locked state：");
                        sb6.append(substring2);
                        Log.d("TwoWheel", sb6.toString());
                        if (str4.equals(substring2)) {
                            setHeadlightLock(1, true);
                        } else if (str3.equals(substring2)) {
                            setHeadlightLock(1, false);
                        }
                        return;
                    case 5:
                        //                        StringBuilder sb7 = new StringBuilder();
                        //                        sb7.append("Operating mode：");
                        //                        sb7.append(substring2);
                        //                        Log.d("TwoWheel", sb7.toString());
                        //                        if (str3.equals(substring2)) {
                        //                            setMode(1);
                        //                            r2 = utils.Command.SPEED_MODE_ELECT;
                        //                        } else if (str4.equals(substring2)) {
                        //                            setMode(2);
                        //                            r2 = utils.Command.SPEED_MODE_HELP;
                        //                        } else if (str.equals(substring2)) {
                        //                            setMode(3);
                        //                            r2 = utils.Command.SPEED_MODE_RIDE;
                        //                        }
                        //                        sendSettingResult("cn.sccss.speed.action.SPEED_MODE", r2);
                        return;
                    case 6:
                        StringBuilder sb8 = new StringBuilder();
                        sb8.append("battery power：");
                        sb8.append(substring2);
                        Log.d("TwoWheel", sb8.toString());
                        if (!TextUtils.isEmpty(substring2) && !substring2.contains(str2)) {
                            try {
                                setBattery(Integer.parseInt(substring2, 16));
                            } catch (NumberFormatException e5) {
                                Log.d("TwoWheel", "Error converting battery level！");
                                e5.printStackTrace();
                            }
                        }
                        return;
                    case 7:
                        StringBuilder sb9 = new StringBuilder();
                        sb9.append("Speed mode：");
                        sb9.append(substring2);
                        Log.d("TwoWheel", sb9.toString());
                        if (str3.equals(substring2)) {
                            set_per_ui(true);
                            r2 = utils.Command.SPEED_KM;
                        } else if (str4.equals(substring2)) {
                            set_per_ui(false);
                            r2 = utils.Command.SPEED_MP;
                        }
                        sendSettingResult(utils.Lenzod_Action.SPEED_MODE, r2);
                        return;
                    case 8:
                        StringBuilder sb10 = new StringBuilder();
                        sb10.append("Start mode：");
                        sb10.append(substring2);
                        Log.d("TwoWheel", sb10.toString());
                        if (str3.equals(substring2)) {

                            // this.mSettingUtil.setStartMode(1);
                            zero_start_mode = true;
                            r2 = utils.Command.START_MODE_ZERO;
                        } else if (str4.equals(substring2)) {
                            // this.mSettingUtil.setStartMode(2);
                            zero_start_mode = false;
                            r2 = utils.Command.START_MODE_NOT_ZERO;
                        }
                        sendSettingResult(utils.Lenzod_Action.START_MODE, r2);
                        return;
                    case 9:
                        //                            StringBuilder sb11 = new StringBuilder();
                        //                            sb11.append("Voltage mode：");
                        //                            sb11.append(substring2);
                        //                            Log.d("TwoWheel", sb11.toString());
                        //                            if (str3.equals(substring2)) {
                        //                                this.mSettingUtil.setElect(1);
                        //                                r2 = utils.Command.ELECT_24V;
                        //                            } else if (str4.equals(substring2)) {
                        //                                this.mSettingUtil.setElect(2);
                        //                                r2 = utils.Command.ELECT_36V;
                        //                            } else if (str.equals(substring2)) {
                        //                                this.mSettingUtil.setElect(3);
                        //                                r2 = utils.Command.ELECT_48V;
                        //                            } else if ("04".equals(substring2)) {
                        //                                this.mSettingUtil.setElect(4);
                        //                                r2 = utils.Command.ELECT_60V;
                        //                            }
                        //                        sendSettingResult(utils.Lenzod_Action.ELECT_MODE, r2);
                        return;
                    case 10:
                        StringBuilder sb12 = new StringBuilder();
                        sb12.append("Wheel diameter setting：");
                        sb12.append(substring2);
                        Log.d("TwoWheel", sb12.toString());
                        try {
                            int parseInt = Integer.parseInt(substring2, 16);
                            // this.mSettingUtil.setWheel(parseInt);
                            r2 = String.valueOf(parseInt);
                        } catch (Exception unused) {
                        }
                        sendSettingResult(utils.Lenzod_Action.WHEEL_MODE, r2);
                        return;
                    case 11:
                        StringBuilder sb13 = new StringBuilder();
                        sb13.append("Cruise control：");
                        sb13.append(substring2);
                        Log.d("TwoWheel", sb13.toString());
                        if (str3.equals(substring2)) {
                            constant_speed = true;
                            // this.mMainFragment.setConstantSpeed(true);
                            r2 = utils.Command.CONSTANT_SPEED_ON;
                        } else if (str4.equals(substring2)) {
                            constant_speed = false;
                            // this.mMainFragment.setConstantSpeed(false);
                            r2 = utils.Command.CONSTANT_SPEED_OFF;
                        }
                        sendSettingResult(utils.Lenzod_Action.CONSTANT_MODE, r2);
                        return;
                    case 13:
                        StringBuilder sb14 = new StringBuilder();
                        sb14.append("Gear setting：");
                        sb14.append(substring2);
                        Log.d("TwoWheel", sb14.toString());
                        if (str3.equals(substring2)) {
                        //setMode(1);
                        } else if (str4.equals(substring2)) {
                            setMode(1);
                        } else if (str.equals(substring2)) {
                            setMode(2);
                        } else if ("04".equals(substring2)) {
                            setMode(3);
                        }
                        return;

                    case 14:
                        StringBuilder sb15 = new StringBuilder();
                        sb15.append("Driving time：");
                        sb15.append(substring2);
                        Log.d("TwoWheel", sb15.toString());
                        try {
                            // this.mMainFragment.setRunTime(((float) Integer.parseInt(substring2, 16)) / 60.0f);
                            return;
                        } catch (Exception unused2) {
                            return;
                        }
                    default:
                        return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void loopRequestStatus() {
        command("FF55010055");
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 3000);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        if(MyApplication.mine)
            return;
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 2000);
    }

    private Thread thread = new Thread(new Runnable() {
        public void run() {
            while (true) {
                if (is_navigation) {
                    try {
                        Log.e("thread", "running");
                        LatLng position_startLat = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                        if (!TextUtils.isEmpty(place_id)) {
                            directionsUrl = getDirectionsUrl(position_startLat, null, place_id, null);
                        } else {
                            directionsUrl = getDirectionsUrl(position_startLat, des_latlng, null, "default");
                        }
                        Log.v("lng=lat", "directionsUrl===" + directionsUrl);
                        new DownloadTask().execute(new String[]{directionsUrl});
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    private void sendSettingResult(String str, String str2) {
        Intent intent = new Intent(str);
        intent.putExtra(utils.Key.RESULT, str2);
        sendBroadcast(intent);
    }

    public void onConnectError(Exception exc) {
        utils.SuperActivityToast(this, getString(R.string.connect_err), utils.toast.TOAST_WARNING);
        this.mHandler.postDelayed(new Runnable() {
            public final void run() {
                startActivity(new Intent(TwoWheelActivity.this, Discover2Activity.class));
                finish();
            }
        }, 1000);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        QBlueToothManager.getInstance().removeBluetoothListener(this.listener);
        this.mHandler.removeMessages(1);
        unregisterReceiver(this.mSettingActionBroadcastReceiver);
    }

    public void onBackPressed() {
        exitBy2Click();
    }

    private void exitBy2Click() {
        if (!this.isExit) {
            this.isExit = true;
            Toast.makeText(this, "Quit", Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                public void run() {
                    isExit = false;
                }
            }, 2000);
            return;
        }
        QBlueToothManager.getInstance().disconnect();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                finish();
                Process.killProcess(Process.myPid());
                try {
                    killProcess(TwoWheelActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }


    private void startVoiceRecognitionActivity() {
        try {
            Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
            intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
            intent.putExtra("android.speech.extra.PROMPT", "Try saying...");
            intent.putExtra("android.speech.extra.LANGUAGE", Locale.getDefault());
            startActivityForResult(intent, 0);
        } catch (Exception e) {
            e.printStackTrace();
            search_voice_hint_dialog();
        }
    }

    public boolean is_navigation = false;
    private Geocoder geocoder;
    public LatLng des_latlng;

    private LatLng get_des_latlng(String des) {
        List<Address> fromLocationName = null;
        try {
            fromLocationName = this.geocoder.getFromLocationName(des, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fromLocationName == null || fromLocationName.size() <= 0) {
            return null;
        }
        double lat = ((Address) fromLocationName.get(0)).getLatitude();
        double lng = ((Address) fromLocationName.get(0)).getLongitude();
        Log.v("lng=lat", "get_des_latlng" + lat + "  lng=" + lng);
        LatLng desLng = new LatLng(lat, lng);
        this.is_navigation = true;
        return desLng;
    }

    private void search_voice_hint_dialog() {
        final Dialog dialog = new Dialog(this, R.style.myStyle);
        dialog.setContentView(R.layout.search_voice_hint_dialog);
        dialog.findViewById(R.id.hint_ok_btn).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public boolean checkPermission() {
        if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission("android.permission.BLUETOOTH") != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.BLUETOOTH"}, 18);
            return false;
        } else {
            initMap();
        }
        return true;
    }

    public double getDistance(LatLng start, LatLng end) {
        double lat1 = 0.017453292519943295d * start.latitude;
        double lat2 = 0.017453292519943295d * end.latitude;
        return 1000.0d * Math.acos((Math.sin(lat1) * Math.sin(lat2)) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos((0.017453292519943295d * end.longitude) - (0.017453292519943295d * start.longitude)))) * 6371.0d;
    }

    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (location != null) {
                new LatLng(latitude.doubleValue(), longitude.doubleValue());
                mLocation = location;
                latitude = Double.valueOf(mLocation.getLatitude());
                longitude = Double.valueOf(mLocation.getLongitude());
                lalng = new LatLng(latitude.doubleValue(), longitude.doubleValue());
                if (end_lat != null) {
                    double distance_des = getDistance(lalng, end_lat);
                    if (distance_des < 100.0d && is_navigation) {
                        is_navigation = false;
                        place_id = null;
                        end_Navigation();
                    }
                }
                zoomIn(latitude, longitude);
            }
        }

        public void zoomIn(double Lat, double Long) {
            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(Lat, Long));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);
            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }
    };

    /* access modifiers changed from: private */
    public void end_Navigation() {
        final Dialog dialog = new Dialog(this, R.style.myStyle);
        dialog.setContentView(R.layout.map_stop_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.findViewById(R.id.blt_set_cancle_fix).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.blt_set_ok_fix).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private LocationManager mLocationManager;
    public GoogleApiClient mGoogleApiClient_dash;
    private SupportMapFragment mapFragment;
    public GoogleMap mMap;

    public class DirectionsJSONParser {
        public DirectionsJSONParser() {
        }

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<>();
            try {
                JSONArray jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    JSONArray jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList();
                    for (int j = 0; j < jLegs.length(); j++) {
                        JSONArray jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String str = "";
                            List<LatLng> list = decodePoly((String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points"));
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }
                JSONArray jLegs2 = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e2) {
            }
            return routes;
        }

        private List<LatLng> decodePoly(String encoded) {
            int index;
            List<LatLng> poly = new ArrayList<>();
            int index2 = 0;
            int len = encoded.length();
            int lat = 0;
            int lng = 0;
            while (index2 < len) {
                int shift = 0;
                int result = 0;
                while (true) {
                    index = index2 + 1;
                    int b = encoded.charAt(index2) - '?';
                    result |= (b & 31) << shift;
                    shift += 5;
                    if (b < 32) {
                        break;
                    }
                    index2 = index;
                }
                lat += (result & 1) != 0 ? (result >> 1) ^ -1 : result >> 1;
                int shift2 = 0;
                int result2 = 0;
                while (true) {
                    int index3 = index;
                    index = index3 + 1;
                    int b2 = encoded.charAt(index3) - '?';
                    result2 |= (b2 & 31) << shift2;
                    shift2 += 5;
                    if (b2 < 32) {
                        break;
                    }
                }
                lng += (result2 & 1) != 0 ? (result2 >> 1) ^ -1 : result2 >> 1;
                poly.add(new LatLng(((double) lat) / 100000.0d, ((double) lng) / 100000.0d));
                index2 = index;
            }
            return poly;
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        private DownloadTask() {
        }

        /* access modifiers changed from: protected */
        public String doInBackground(String... url) {
            String data = "";
            try {
                return downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
                return data;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.v("lng=lat", "directionsUrl=" + result.toString());
            if (on_text_change) {
                json_Analysis(result);
            } else {
                new ParserTask().execute(new String[]{result});
            }
        }
    }

    /* access modifiers changed from: private */
    public void json_Analysis(String result) {
        this.mapSearchResultItems.clear();
        this.myListViewAdapter.notifyDataSetChanged();
        try {
            JSONArray predictions = new JSONObject(result).getJSONArray("predictions");
            for (int i = 0; i < predictions.length(); i++) {
                JSONObject jsonObject = predictions.getJSONObject(i);
                String address_detail = jsonObject.getString(utils.KEY_CONTENT_DEEP_LINK_METADATA_DESCRIPTION);
                String place_id = jsonObject.getString("place_id");
                this.mapSearchResultItems.add(new MapSearchResultItem(jsonObject.getJSONObject("structured_formatting").getString("main_text"), address_detail, place_id, this.address));
            }
            this.myListViewAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(strUrl).openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String str = "";
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception  url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        System.out.println("url:" + strUrl + "---->   downloadurl:" + data);
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        private ParserTask() {
        }

        /* access modifiers changed from: protected */
        public List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            List<List<HashMap<String, String>>> routes = null;
            try {
                return new TwoWheelActivity.DirectionsJSONParser().parse(new JSONObject(jsonData[0]));
            } catch (Exception e) {
                e.printStackTrace();
                return routes;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(List<List<HashMap<String, String>>> result) {
            PolylineOptions lineOptions = null;
            new MarkerOptions();
            for (int i = 0; i < result.size(); i++) {
                ArrayList<LatLng> points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                mMap.clear();
                List<HashMap<String, String>> path = (List) result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = (HashMap) path.get(j);
                    double lat = Double.parseDouble((String) point.get("lat"));
                    double lng = Double.parseDouble((String) point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                    if (j == 0) {
                    }
                    if (j == path.size() - 1) {
                        LatLng latLng = new LatLng(lat, lng);
                        end_lat = latLng;
                        drawMarkerJ(position, R.drawable.marker);
                    }
                }
                lineOptions.addAll(points);
                lineOptions.width(12.0f);
                lineOptions.color(getColor(R.color.polylinecolor));
            }
            if (lineOptions != null) {
                Polyline polyline = mMap.addPolyline(lineOptions);
                polyline.setWidth(12.0f);
                polyline.setColor(getColor(R.color.polylinecolor));
                polyline.setGeodesic(true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: private */
    public void drawMarkerJ(LatLng gps, int pic) {
        if (mMap != null) {
            Marker addMarker = mMap.addMarker(new MarkerOptions().position(gps).icon(BitmapDescriptorFactory.fromResource(pic)).title(getString(R.string.target_position)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 13.0f));
        }
    }
    private void initMyLocation() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mGoogleApiClient_dash = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(1);
        if(!checkPermission()) return;
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.setMaxZoomPreference(35.0f);
            getCurrentLocation();
            if (mLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 16.0f));
            }
        }
    }

    private void getCurrentLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled("gps");
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled("network");
        checkPermission();
        if (isGPSEnabled) {
            mLocationManager.requestLocationUpdates("gps", 1000, 1.0f, mLocationListener);
            mLocation = mLocationManager.getLastKnownLocation("gps");
            if (mLocation != null) {
                longitude = Double.valueOf(mLocation.getLongitude());
                latitude = Double.valueOf(mLocation.getLatitude());
            }
        }
        else if (isNetworkEnabled) {
            mLocationManager.requestLocationUpdates("network", 1000, 1.0f, mLocationListener);
            mLocation = mLocationManager.getLastKnownLocation("network");
            if (mLocation != null) {
                longitude = Double.valueOf(mLocation.getLongitude());
                latitude = Double.valueOf(mLocation.getLatitude());
            }
        }
    }

    public void initGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled("gps");
        boolean isProviderEnabled = locationManager.isProviderEnabled("network");
        if (!gps) {
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);
            dialog.setMessage("open_gps");
            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    startActivityForResult(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"), 0);
                }
            });
            dialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
