package com.tn.escooter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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
import com.tn.escooter.buletooth.Discover2Activity;
import com.tn.escooter.buletooth.TwoWheelActivity;
import com.tn.escooter.utils.BytesUtils;
import com.tn.escooter.bluetooth.LFBluetootService;
import com.tn.escooter.utils.YiHuoUtils;
import com.tn.escooter.utils.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static com.tn.escooter.utils.utils.sharedPref;

public class MainActivity extends FragmentActivity implements OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public LFBluetootService bleService = null;
    public BluetoothAdapter bluetoothAdapter;
    public LFBluetootService lfBluetootService;
    public boolean bIsInit = false;
    public boolean is_km_per = false;
    public float per_num = 1.0f;
    public boolean is_lock = false;
    public Integer gear_num = Integer.valueOf(0);
    public String vehicle_type = "";
    public boolean is_headlights = false;
    public boolean is_click_blue_connect = false;
    public static MainActivity instance = null;
    public float trip = 0.f;
    public BluetoothAdapter mBluetoothAdapter;
    public static MainActivity getInstance(){
        if(instance == null) instance = new MainActivity();
        return instance;
    }
    @BindView(R.id.map_framelayout) FrameLayout mapFramelayout;
    @BindView(R.id.battery_text) TextView battery_text;
    @BindView(R.id.img_battery) ImageView img_battery;
    @BindView(R.id.lay_speedmetor) FrameLayout lay_speedmetor;
    @BindView(R.id.lay_map_speedmetor) FrameLayout lay_map_speedmetor;
    @BindView(R.id.mainModeText) TextView mainModeText;

    @BindView(R.id.mainSpeed) TextView mainSpeed;
    @BindView(R.id.mainTrip) TextView mainTrip;
    @BindView(R.id.img_pointer) ImageView img_pointer;
    @BindView(R.id.mainOdo) TextView mainOdo;
    @BindView(R.id.mainRemaining) TextView mainRemaining;
    @BindView(R.id.mainRemainingPer) TextView mainRemainingPer;

    @BindView(R.id.img_map_pointer) ImageView img_map_pointer;
    @BindView(R.id.mapmainSpeed) TextView mapMainSpeed;
    @BindView(R.id.mapmainOdo) TextView mapOdo;
    @BindView(R.id.img_map_battery) ImageView img_map_battery;
    @BindView(R.id.mapmainTrip) TextView mapTrip;

    @BindView(R.id.img_btn_headlight) ImageView img_btn_headlight;
    @BindView(R.id.img_mode_view) ImageView img_mode_view;
    @BindView(R.id.img_btn_lock) ImageView img_btn_lock;
    @BindView(R.id.img_btn_mode) ImageView img_btn_mode;
    @BindView(R.id.lay_speed_metro) LinearLayout lay_speed_metro;
    
    @BindView(R.id.txt_autocomplete) TextView searchBtn;
    @BindView(R.id.img_trash) ImageView speakSearchBtn;

    public final ServiceConnection serviceConnectionBle = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            lfBluetootService = ((LFBluetootService.LocalBinder) service).getService();
            if (!lfBluetootService.initialize()) finish();

            initAll();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            lfBluetootService = null;
        }
    };

    private void checkAuth(){
        if(MyApplication.cur_user.token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MyApplication.SCOOTER_TYPE == utils.bluetooth_type.LENZOD){
            startActivity(new Intent(this, TwoWheelActivity.class));
            finish();
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind((Activity) this);

        if(instance == null) instance = this;

        checkAuth();
        checkPermission();
        initMyLocation();
        set_Default_Ui();
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
                return data;
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

        /* access modifiers changed from: protected */
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(new String[]{result});
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        private ParserTask() {
        }

        /* access modifiers changed from: protected */
        public List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            List<List<HashMap<String, String>>> routes = null;
            try {
                return new DirectionsJSONParser().parse(new JSONObject(jsonData[0]));
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
                lineOptions.color(-16711936);
            }
            if (lineOptions != null) {
                Polyline polyline = mMap.addPolyline(lineOptions);
                polyline.setWidth(12.0f);
                polyline.setColor(-16711936);
                polyline.setGeodesic(true);
            }
        }
    }
    /* access modifiers changed from: private */
    public void drawMarkerJ(LatLng gps, int pic) {
        if (mMap != null) {
            Marker addMarker = mMap.addMarker(new MarkerOptions().position(gps).icon(BitmapDescriptorFactory.fromResource(pic)).title("Current Position"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 13.0f));
        }
    }
    private void initMyLocation() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mGoogleApiClient_dash = new Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);
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

    /* access modifiers changed from: private */
    public Location mLocation;
    private JSONArray mLocationArray;
    public LatLng lalng;
    public Double latitude = Double.valueOf(0.0);
    public Double longitude = Double.valueOf(0.0);
    public LatLng end_lat;
    public String place_id;
    private Handler mHandler = new Handler();
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
                    place_id = null;
//                    if (distance_des < 100) {
//                    }
                }
            }
        }

        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        public void onProviderEnabled(String s) {
        }

        public void onProviderDisabled(String s) {
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(1);
        checkPermission();
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
    public void checkPermission() {
        if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission("android.permission.BLUETOOTH") != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.BLUETOOTH"}, 18);
        }
    }

    private void getCurrentLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled("gps");
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled("network");
        checkPermission();
        if (isGPSEnabled || isNetworkEnabled) {
            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates("gps", 6000, 1.0f, mLocationListener);
                mLocation = mLocationManager.getLastKnownLocation("gps");
                if (mLocation != null) {
                    longitude = Double.valueOf(mLocation.getLongitude());
                    latitude = Double.valueOf(mLocation.getLatitude());
                }
            }
            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates("network", 6000, 1.0f, mLocationListener);
                mLocation = mLocationManager.getLastKnownLocation("network");
                if (mLocation != null) {
                    longitude = Double.valueOf(mLocation.getLongitude());
                    latitude = Double.valueOf(mLocation.getLatitude());
                }
            }
        }
    }

    public String directionsUrl;
    public String search_result_address;
    public String search_result_id;

    public void onResume() {
        super.onResume();
        try {
            mGoogleApiClient_dash.connect();

            if(mBluetoothAdapter == null){
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 2);
            }
            if (lfBluetootService == null) {
                Intent intent = new Intent(this, LFBluetootService.class);
                bindService(intent, serviceConnectionBle, Context.BIND_AUTO_CREATE);
            }

            if (!TextUtils.isEmpty(search_result_address)) {
                searchBtn.setText(search_result_address);
            }
            if (mLocation != null && !TextUtils.isEmpty(search_result_id)) {
                speakSearchBtn.setImageResource(R.drawable.map_search_finish_icon);
                place_id = search_result_id;
                search_result_id = null;
                directionsUrl = getDirectionsUrl(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), null, place_id, null);
                new DownloadTask().execute(new String[]{directionsUrl});
            }
            set_ui();
            registerBroadCast();
        }catch (Exception err) {
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
        return "https://maps.googleapis.com/maps/api/directions/" + "json" + "?" + (str_origin + "&" + str_dest + "&" + "sensor=false" + "&" + "mode=driving") + "&key=AIzaSyBQTyKTsKrZ4AamGOtID4qHsmdgOohMbas";
    }

    public void onPause() {
        super.onPause();
        mGoogleApiClient_dash.disconnect();
    }
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
                int swidth  = lay_speed_metro.getMeasuredWidth();
                int height = lay_speed_metro.getMeasuredHeight();

                if(height < swidth) swidth = height;
                ViewGroup.LayoutParams params = lay_speedmetor.getLayoutParams();
                params.width = swidth;
                params.height = swidth;
                lay_speedmetor.setLayoutParams(params);
            }
        });



        Display display = getWindowManager().getDefaultDisplay();
        int swidth = display.getWidth()/2;
        int height = display.getHeight()/3;
        if(height < swidth) swidth = height;
        ViewGroup.LayoutParams params = lay_map_speedmetor.getLayoutParams();
        params.width = swidth;
        params.height = swidth;
        lay_map_speedmetor.setLayoutParams(params);

        mainSpeed.setText("0.0");
        mapMainSpeed.setText("0.0");
        mainTrip.setText("0000");
        mainOdo.setText("0000");
        battery_text.setText("80%");
        mainModeText.setText("B");
        mapOdo.setText("0.0");
        mapTrip.setText("0.0");
        mainRemaining.setText("0000");
        set_per_ui(is_km_per);
    }
    public int setBattery(int battery){
        int batter_percentage = R.drawable.ic_battery_0;
        if (battery < 0) battery = 0;
        else if (battery > 100) battery = 100;

        if(battery == 0) batter_percentage = R.drawable.ic_battery_0;
        else if (battery <= 20) batter_percentage = R.drawable.ic_battery_20;
        else if (battery <= 40) batter_percentage = R.drawable.ic_battery_40;
        else if (battery <= 60) batter_percentage = R.drawable.ic_battery_60;
        else if (battery <= 80) batter_percentage = R.drawable.ic_battery_80;
        else if (battery <= 100) batter_percentage = R.drawable.ic_battery_100;

        img_battery.setImageResource(batter_percentage);
        img_map_battery.setImageResource(batter_percentage);

        battery_text.setText( battery + "%");
//        remain_text.setText("Remain: " + String.format("%.1f", new Object[]{Float.valueOf((((float) (battery * 20)) * per_num) / 100.0f)}));
        if (is_km_per) {
            mainRemaining.setText(String.format("%.1f", new Object[]{Float.valueOf((((float) (battery * 20)) * per_num) / 100.0f)}));
        } else {
            mainRemaining.setText(String.format("%.1f", new Object[]{Float.valueOf((((float) (battery * 20)) * per_num) / 100.0f)}));
        }
        return battery;
    }
    public void setSpeed(int i_speed){
        float max = 286.0f;

        if (i_speed > 300) {
            i_speed = 300;
        }
        float speed = Float.valueOf((((float) i_speed) / 10.0f) * per_num);

        if(MyApplication.max_speed < speed){
            MyApplication.max_speed = speed;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(utils.shared_key.max_speed, speed);
            editor.apply();
        }

        mainSpeed.setText(String.format("%.1f", new Object[]{speed}));
        mapMainSpeed.setText(String.format("%.1f", new Object[]{speed}));
        float angle = max/300 * (float)(i_speed);
        img_pointer.setRotation(angle);
        img_map_pointer.setRotation(angle);
    }
    public void setMode(int mode){
        if(mode == 1)img_mode_view.setImageResource(R.drawable.ic_mode1);
        else if(mode == 2)img_mode_view.setImageResource(R.drawable.ic_mode2);
        else if(mode == 3)img_mode_view.setImageResource(R.drawable.ic_mode3);
        else return;

        gear_num = mode;
    }
    public void setHeadlightLock(int type, boolean state) { //type: 0 headlight, 1: lock
        if(type == 0){
            is_headlights = state;
            if(state) img_btn_headlight.setImageResource(R.drawable.ic_headlight_on);
            else img_btn_headlight.setImageResource(R.drawable.ic_headlight_off);
        }else{
            is_lock = state;
            if(state) img_btn_lock.setImageResource(R.drawable.ic_lock);
            else  img_btn_lock.setImageResource(R.drawable.ic_unlock);
        }
    }

    @SuppressLint({"NewApi"})
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LFBluetootService.ACTION_GATT_CONNECTING.equals(action)) {
            } else if (LFBluetootService.ACTION_BLE_CODE_OK.equals(action)) {
                utils.SuperActivityToast(MainActivity.this, getString(R.string.device_connected), utils.toast.TOAST_INFO);
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        LFBluetootService.getInstent().sendString("+UNIT=?");
                    }
                }, 500);
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        LFBluetootService.getInstent().sendString("HLGT=?");
                    }
                }, 1000);
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        LFBluetootService.getInstent().sendString("+MODE=?");
                    }
                }, 1500);
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        LFBluetootService.getInstent().sendString("+LOCK=?");
                    }
                }, 2000);
            } else if (LFBluetootService.ACTION_GATT_DISCONNECTED.equals(action) || LFBluetootService.ACTION_DISCONNECTED.equals(action)) {
//                utils.SuperActivityToast(MainActivity.this, "bluetooth disconnected", utils.toast.TOAST_ERROR);
            } else if (LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                utils.SuperActivityToast(MainActivity.this, LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED, utils.toast.TOAST_INFO);
            } else if (LFBluetootService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(LFBluetootService.EXTRA_DATA);
                String readMessage = BytesUtils.BytesToString(data);
                try {
                    String str = new String(data, "gbk");
                    readMessage = str;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if ("+UNIT=0".equals(readMessage)) set_per_ui(true);
                else if ("+UNIT=1".equals(readMessage))  set_per_ui(false);
                else if (readMessage.equals("+LOCK=1")) setHeadlightLock(1, true);
                else if (readMessage.equals("+LOCK=0")) setHeadlightLock(1, false);
                else if (readMessage.contains("+MODE=")) setMode(Integer.valueOf(readMessage.substring(6, 7)));
                else if (readMessage.equals("HLGT=0")) setHeadlightLock(0, false);
                else if (readMessage.equals("HLGT=1")) setHeadlightLock(0, true);
                else if (readMessage.equals("imoog00-rdn09")) vehicle_type = "spark2";
                else if (readMessage.equals("imoogoo-rdn03")) vehicle_type = "CHIRREY";
                else if (readMessage.equals("imoogoo-chr03") || readMessage.equals("imoogoo-chr06")) vehicle_type = "CHIRREY New01";
                else if (readMessage.equals("imoogoo-rdn10")) vehicle_type = "CHIRREY 10";
                else if (readMessage.equals("imoogoo-jnt03")) vehicle_type = "jnt03";
                else if (readMessage.equals("imoogoo-dst01")) vehicle_type = "dst01";
                else if (readMessage.equals("imoogoo-ctd02") || readMessage.equals("imoogoo-ctd03") || readMessage.equals("imoogoo-jlst01")) vehicle_type = "Q5";
                else if (readMessage.equals("imoogoo-rdn08")) vehicle_type = "spark1";
                else if (readMessage.equals("imoogoo-rdn07")) vehicle_type = "Aiyou";
                else if (readMessage.equals("moogoo-mlq02")) vehicle_type = "MLQ02";
                else if (readMessage.equals("moogoo-mlq03")) vehicle_type = "MLQ03";
                else if (readMessage.equals("moogoo-mlq04")) vehicle_type = "MLQ04";
                else if (readMessage.equals("moogoo-mlq05")) vehicle_type = "MLQ05";
                else if (readMessage.equals("imoogoo-ylk03")) vehicle_type = "FS022";
                else if (readMessage.equals("imoogoo-jor01")) vehicle_type = "JOYOR";
                else if (readMessage.equals("imoogoo-cxw01")) vehicle_type = "cxinwalk";
                else if (readMessage.equals("imoogoo-rdn02")) vehicle_type = "MERCURY3";
                else if (readMessage.equals("imoogoo-rdn04")) vehicle_type = "MERCURY1";
                else if (readMessage.equals("imoogoo-rdn05")) vehicle_type = "Hiboy";
                else if (readMessage.equals("imogoo-hover15")) vehicle_type = "MCX05";
                else if (readMessage.equals("imoogoo-yk12")) vehicle_type = "FS02";
                else if (readMessage.equals("imoogoo-jnl01")) vehicle_type = "jinling";
                else if (readMessage.equals("imoogoo-rdn13")) vehicle_type = "JOYOR02";
                else if (readMessage.equals("imoogoo-yyst01")) vehicle_type = "yy";
                else if (readMessage.equals("imoogoo-mcw01")) vehicle_type = "x8";
                else if (readMessage.equals("imoogoo-mcw04") || readMessage.equals("imoogoo-mcw02") || readMessage.equals("imoogoo-mcw03")) vehicle_type = "x802";

                if (readMessage.contains("imoog") || readMessage.contains("imog") || readMessage.contains("moog")) { set_ui(); }

                if (data.length == 10 && (data[0] & 255) == 170 && (data[9] & 255) == 187) {
                    int command = data[1] & 255;
                    int b = data[3] & 255;
                    int value4 = data[4] & 255;
                    int value5 = data[5] & 255;
                    int[] a = {data[4], data[5]};
                    int[] b2 = {data[3], data[4]};
                    int[] c = {data[5], data[6], data[7]};
                    int b3 = ((a[0] & 255) << 8) | (a[1] & 255);
                    int b4 = ((b2[0] & 255) << 8) | (b2[1] & 255);
                    int b5 = ((c[0] & 255) << 16) | ((c[1] & 255) << 8) | (c[2] & 255);
                    String value7_str = Integer.toHexString(data[7] & 255);
                    if (value7_str.length() < 2) value7_str = "0" + value7_str;

                    String value5_str = Integer.toHexString(value5);
                    if (value5_str.length() < 2) value5_str = "0" + value5_str;

                    String value4_str = Integer.toHexString(value4);
                    if (value4_str.length() < 2) value4_str = "0" + value4_str;

                    switch (command) {
                        case 161:
                            setBattery(b);
                            setSpeed(b3);
                            if (vehicle_type.equals("yy")) setMode(Integer.valueOf(value4_str.charAt(value4_str.length()-1)));
                            break;
                        case 162:
                            trip = Float.valueOf((((float) b4) * per_num) / 10.0f);
                            mainTrip.setText(String.format("%.1f", new Object[]{trip}));
                            mainOdo.setText(String.format("%.1f", new Object[]{Float.valueOf((((float) b5) * per_num) / 10.0f)}));
                            if (is_km_per) {
                                mapOdo.setText("Map Odo: " + String.format("%.1f", new Object[]{Float.valueOf((((float) b5) * per_num) / 10.0f)}) + " Km");
                                mapTrip.setText("Map Trip: " + String.format("%.1f", new Object[]{Float.valueOf((((float) b4) * per_num) / 10.0f)}) + " Km");
                            } else {
                                mapTrip.setText("Map Trip: " + String.format("%.1f", new Object[]{Float.valueOf((((float) b4) * per_num) / 10.0f)}) + " mile");
                                mapOdo.setText("Map Odo: " + String.format("%.1f", new Object[]{Float.valueOf((((float) b5) * per_num) / 10.0f)}) + " mile");
                            }
                            break;
                        case 163:
                            if (vehicle_type.equals("x802")) {
                                setMode(Integer.valueOf(Integer.parseInt(value5_str.substring(0, 1))));
                                if (value4_str.substring(0, 1).equals("0")) setHeadlightLock(0, false);
                                else if (value4_str.substring(0, 1).equals("1")) setHeadlightLock(0, true);

                                if (value4_str.substring(1, 2).equals("0")) setHeadlightLock(1, false);
                                else if (value4_str.substring(1, 2).equals("1")) setHeadlightLock(1, true);
                            }
                            break;
                    }
                }
                if ((data[0] & 255) == 58 && (data[1] & 255) == 26 && (data[data.length - 2] & 255) == 13 && (data[data.length - 1] & 255) == 10) {
                    int battery = data[4] & 255;
                    int b6 = data[5] & 255;
                    int[] b7 = {data[5], data[6]};
                    int b8 = ((b7[0] & 255) << 8) | (b7[1] & 255);
                    int[] a2 = {data[7], data[8]};
                    int b9 = ((a2[0] & 255) << 8) | (a2[1] & 255);
                    int b10 = ((data[9] & 255) << 16) | ((data[10] & 255) << 8) | (data[11] & 255);
                    switch (data[2] & 255) {
                        case 32:
                            setBattery(battery);
                            setSpeed(b8);
                            trip = Float.valueOf((((float) b9) * per_num) / 10.0f);
                            mainTrip.setText(String.format("%.1f", new Object[]{trip}));
                            mainOdo.setText(String.format("%.1f", new Object[]{Float.valueOf((((float) b10) * per_num) / 10.0f)}));
                            if (!is_km_per) {
                                mapTrip.setText("Map Trip: " + String.format("%.1f", new Object[]{Float.valueOf((((float) b9) * per_num) / 10.0f)}) + " mile");
                                mapOdo.setText("Map Odo: " + String.format("%.1f", new Object[]{Float.valueOf((((float) b10) * per_num) / 10.0f)}) + " mile");
                            } else {
                                mapOdo.setText("Map Odo: " + String.format("%.1f", new Object[]{Float.valueOf((((float) b10) * per_num) / 10.0f)}) + " Km");
                                mapTrip.setText("Map Trip: " + String.format("%.1f", new Object[]{Float.valueOf((((float) b9) * per_num) / 10.0f)}) + " Km");
                            }
                            break;
                    }
                }
                if (data.length == 6 && (data[0] & 255) == 170 && (data[5] & 255) == 187) {
                    int command2 = data[1] & 255;
                    int b11 = data[2] & 255;
                    int b12 = data[3] & 255;
                    int[] b13 = {data[2], data[3]};
                    int b14 = ((b13[0] & 255) << 8) | (b13[1] & 255);
                    String valueHi = Integer.toHexString(b11);
                    if (valueHi.length() < 2) valueHi = "0" + valueHi;

                    String valueLi = Integer.toHexString(b12);
                    if (valueLi.length() < 2) valueLi = "0" + valueLi;

                    switch (command2) {
                        case 241:
                            setBattery(b11);
                            setSpeed(b12);
                            break;
                        case 242:
                            trip = Float.valueOf((((float) b14) * per_num) / 10.0f);
                            mainTrip.setText(String.format("%.1f", new Object[]{trip}));
                            mapTrip.setText("Map Trip: " + String.format("%.1f", new Object[]{Float.valueOf(((float) b14) / 10.0f)}) + " Km");
                            break;
                        case 243:
                            mainOdo.setText(String.format("%.1f", new Object[]{Float.valueOf(((float) b14) / 10.0f)}));
                            mapOdo.setText("Map Odo: " + String.format("%.1f", new Object[]{Float.valueOf(((float) b14) / 10.0f)}) + " Km");
                            break;
                        case 245:
                            if (b11 > 150) { }
                            if (b12 >= 0) {
                                if (b12 <= 100) { return; }
                                return;
                            }
                            break;
                        case 246:
                            String substring = valueHi.substring(0, 1);
                            String aa = valueHi.substring(0, 1);
                            String bb = valueHi.substring(1, 2);
                            String cc = valueLi.substring(0, 1);
                            String dd = valueLi.substring(1, 2);
                            int ib = Integer.parseInt(bb, 16);
                            int ic = Integer.parseInt(cc, 16);
                            if (ic > 10) ic = 10;
                            else if (ic < 1) ic = 1;

                            int id = Integer.parseInt(dd, 16);

                            if (ib > 4 || ic > 6) mainModeText.setText("Main Mode: " + "H");
                            else if (ib > 2 || ic > 3 || id > 10) mainModeText.setText("Main Mode: " + "N");
                            else mainModeText.setText("Main Mode: " + "B");
                            break;
                        case ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION /*250*/:
                            if (b12 == 1 || b12 == 0) { }
                            return;
                        default:
                            return;
                    }
                }
            }
    };
};
    /* access modifiers changed from: private */
    public void set_ui() {
        if (vehicle_type.equals("CHIRREY") || vehicle_type.equals("Aiyou") || vehicle_type.equals("FS022") || vehicle_type.equals("JOYOR") || vehicle_type.equals("JOYOR02")) {
            img_btn_lock.setVisibility(View.VISIBLE);
            img_btn_headlight.setVisibility(View.GONE);
            img_btn_mode.setVisibility(View.VISIBLE);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    LFBluetootService.getInstent().sendString("+LOCK=?");
                }
            }, 300);
        } else if (vehicle_type.equals("spark1") || vehicle_type.equals("yy")) {
            img_btn_lock.setVisibility(View.GONE);
            img_btn_headlight.setVisibility(View.GONE);
            img_btn_mode.setVisibility(View.VISIBLE);
        } else if (vehicle_type.equals("jinling")) {
            img_btn_lock.setVisibility(View.GONE);
            img_btn_headlight.setVisibility(View.VISIBLE);
            img_btn_mode.setVisibility(View.GONE);
        } else if (vehicle_type.equals("x8") || vehicle_type.equals("x802")) {
            img_btn_lock.setVisibility(View.VISIBLE);
            img_btn_headlight.setVisibility(View.VISIBLE);
            img_btn_mode.setVisibility(View.VISIBLE);
        } else {
            img_btn_lock.setVisibility(View.GONE);
            img_btn_headlight.setVisibility(View.VISIBLE);
            img_btn_mode.setVisibility(View.VISIBLE);
        }
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

    public void initAll(){
        if(bIsInit) return;
        bIsInit = true;
        initGPS();
        initEvent();
        registerBroadCast();
        set_ui();
    }

    public void initEvent() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleService = LFBluetootService.getInstent();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 18:
                if (grantResults[0] == 0) {
                }
                return;
            default:
                return;
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

    public void registerBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        filter.addAction(LFBluetootService.ACTION_GATT_CONNECTED);
        filter.addAction(LFBluetootService.ACTION_GATT_DISCONNECTED);
        filter.addAction(LFBluetootService.ACTION_START_SCAN);
        filter.addAction(LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(LFBluetootService.ACTION_BLE_REQUEST_PASSWORD);
        filter.addAction(LFBluetootService.ACTION_DATA_AVAILABLE);
        filter.addAction(LFBluetootService.ACTION_BLE_REQUEST_PASSWORD_AGAIN);
        filter.addAction(LFBluetootService.ACTION_BLE_CODE_OK);
        filter.addAction(LFBluetootService.ACTION_GATT_CONNECTING);
        filter.addAction(LFBluetootService.ACTION_DISCONNECTED);
        
        filter.addAction(LFBluetootService.ACTION_GATT_CONNECTED);
        filter.addAction(LFBluetootService.ACTION_GATT_CONNECTING);
        filter.addAction(LFBluetootService.ACTION_DISCONNECTED);
        filter.addAction(LFBluetootService.ACTION_BLE_CODE_OK);
        filter.addAction(LFBluetootService.ACTION_GATT_DISCONNECTED);
        filter.addAction(LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(LFBluetootService.ACTION_DATA_AVAILABLE);
        registerReceiver(mReceiver, filter);
    }
    @Override
    protected void onStop() {
        super.onStop();
        try{
            unregisterReceiver(mReceiver);
        }catch (Exception err){
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void joyor02_lock(String pws) {
        if (pws.length() == 12) {
            String xor = YiHuoUtils.xor(new String[]{"AA", "16", "0B", pws.substring(0, 2), pws.substring(2, 4), pws.substring(4, 6), pws.substring(6, 8), pws.substring(8, 10), pws.substring(10, 12)});
            LFBluetootService.getInstent().sendHexString("AA160B" + pws + xor + "BB");
        }
    }
    @Optional
    @OnClick({ R.id.main_arrow_down_btn, R.id.main_arrow_up_btn, R.id.img_btn_headlight,  R.id.img_btn_mode,  R.id.img_btn_lock, R.id.lay_bluetooth, R.id.lay_settings, R.id.lay_more, R.id.lay_main})
    public void onClick(View view) {
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
            case R.id.img_btn_headlight:
                if (!vehicle_type.equals("x802")) {
                    if (is_headlights) LFBluetootService.getInstent().sendString("HLGT=0");
                    else LFBluetootService.getInstent().sendString("HLGT=1");
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            LFBluetootService.getInstent().sendString("HLGT=?");
                        }
                    }, 200);
                } else if (is_headlights) LFBluetootService.getInstent().sendHexString("AA070600ABBB");
                else LFBluetootService.getInstent().sendHexString("AA070601AABB");

                return;
            case R.id.img_btn_mode:
                if (!vehicle_type.equals("x802")) set_mode_select();
                else if (gear_num.intValue() == 1) LFBluetootService.getInstent().sendHexString("AA180602B6BB");
                else if (gear_num.intValue() == 2) LFBluetootService.getInstent().sendHexString("AA180603B7BB");
                else if (gear_num.intValue() == 3) LFBluetootService.getInstent().sendHexString("AA180601B5BB");
                return;

            case R.id.img_btn_lock:
                if (vehicle_type.equals("JOYOR02")) joyor02_lock("123456");
                else if (!vehicle_type.equals("x802")) {
                    if (is_lock) LFBluetootService.getInstent().sendString("+LOCK=0");
                    else LFBluetootService.getInstent().sendString("+LOCK=1");
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            LFBluetootService.getInstent().sendString("+LOCK=?");
                        }
                    }, 300);
                    return;
                } else if (is_lock) LFBluetootService.getInstent().sendHexString("AA160600BABB");
                else LFBluetootService.getInstent().sendHexString("AA160601BBBB");
                return;
            case R.id.lay_bluetooth:
                intent = new Intent(this, Discover2Activity.class);
                break;
            case R.id.lay_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.lay_more:
                intent = new Intent(this, MoreActivity.class);
                break;
            default:
                break;
        }
        if(intent != null) startActivity(intent);
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
    public void set_mode_select() {
        if (vehicle_type.equals("spark2") && gear_num.intValue() < 9)
            LFBluetootService.getInstent().sendString("+MODE=" + (gear_num.intValue() + 1));
        else if ((vehicle_type.equals("cxinwalk") || vehicle_type.equals("MERCURY1") || vehicle_type.equals("FS02") || vehicle_type.equals("Q5") || vehicle_type.equals("CHIRREY New01") || vehicle_type.equals("spark1") || vehicle_type.equals("Aiyou") || vehicle_type.equals("x8") || vehicle_type.contains("MLQ0") || vehicle_type.equals("FS022") || vehicle_type.equals("JOYOR") || vehicle_type.equals("JOYOR02") || vehicle_type.equals("jnt03") || vehicle_type.equals("CHIRREY 10")) && gear_num.intValue() < 3)
            LFBluetootService.getInstent().sendString("+MODE=" + (gear_num.intValue() + 1));
        else if ((vehicle_type.equals("MERCURY3") || vehicle_type.equals("Hiboy") || vehicle_type.equals("MCX05") || vehicle_type.equals("CHIRREY") || vehicle_type.equals("dst01")) && gear_num.intValue() < 2)
            LFBluetootService.getInstent().sendString("+MODE=" + (gear_num.intValue() + 1));
        else
            LFBluetootService.getInstent().sendString("+MODE=1");

        if (!vehicle_type.equals("spark2")){
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    LFBluetootService.getInstent().sendString("+MODE=?");
                }
            }, 300);
        }

    }

//    public void showBlueMusicSetting(Context context) {
//        if (!isShow) {
//            isShow = D;
//            final Dialog dialog = new Dialog(this, R.style.myStyle);
//            dialog.setContentView(R.layout.blue_music_dialog);
//            dialog.findViewById(R.id.setting_cancel).setOnClickListener(new OnClickListener() {
//                public void onClick(View arg0) {
//                    isShow = false;
//                    dialog.dismiss();
//                    startActivity(new Intent(MainActivity.this, class));
//                    finish();
//                }
//            });
//            dialog.findViewById(R.id.setting_btn).setOnClickListener(new OnClickListener() {
//                public void onClick(View arg0) {
//                    isShow = false;
//                    dialog.dismiss();
//                    startActivity(new Intent("android.settings.BLUETOOTH_SETTINGS"));
//                }
//            });
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.show();
//        }
//    }


    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        try {
            unbindService(serviceConnectionBle);
        }catch (Exception err){

        }
    }
}
