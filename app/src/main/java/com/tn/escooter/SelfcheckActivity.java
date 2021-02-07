package com.tn.escooter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler.Callback;

import androidx.annotation.Nullable;

import com.tn.escooter.utils.BytesUtils;
import com.tn.escooter.bluetooth.LFBluetootService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class SelfcheckActivity extends Activity {

    @Nullable
    @BindView(R.id.page_title)
    TextView m_page_title;
    private static final int SET_NOTIFICATION_TIME_INTERVAL = 1500;
    /* access modifiers changed from: private */
    public String TAG;
    /* access modifiers changed from: private */
    public ArrayList<ImageView> arrayList;
    private LFBluetootService bleService;

    @BindView(R.id.check_percent_text)
    TextView checkPercentText;
    @BindView(R.id.check_result1)
    ImageView checkResult1;
    @BindView(R.id.check_result2)
    ImageView checkResult2;
    @BindView(R.id.check_result3)
    ImageView checkResult3;
    @BindView(R.id.check_result4)
    ImageView checkResult4;
    @BindView(R.id.check_result5)
    ImageView checkResult5;
    @BindView(R.id.check_start_btn)
    Button checkStartBtn;
    @BindView(R.id.escooter_check_result1)
    ImageView escooterCheckResult1;
    @BindView(R.id.escooter_check_result2)
    ImageView escooterCheckResult2;
    @BindView(R.id.escooter_check_result3)
    ImageView escooterCheckResult3;
    @BindView(R.id.escooter_check_result4)
    ImageView escooterCheckResult4;
    @BindView(R.id.escooter_check_result5)
    ImageView escooterCheckResult5;
    @BindView(R.id.escooter_check_result6)
    ImageView escooterCheckResult6;
    @BindView(R.id.escooter_check_result7)
    ImageView escooterCheckResult7;
    private boolean isConnect = false;
    /* access modifiers changed from: private */
    public boolean is_recive = false;
    /* access modifiers changed from: private */
    public boolean is_start = false;
    @BindView(R.id.liner_check_escooter)
    LinearLayout linerCheckEscooter;
    @BindView(R.id.liner_check_chirrey_q5)
    LinearLayout liner_check_chirrey_q5;
    @BindView(R.id.liner_check_other)
    LinearLayout liner_check_other;
    @BindView(R.id.liner_check_spark2)
    LinearLayout liner_check_spark2;

    @BindView(R.id.mcx03_check_result1)
    ImageView mcx03_check_result1;
    @BindView(R.id.mcx03_check_result2)
    ImageView mcx03_check_result2;
    @BindView(R.id.mcx03_check_result3)
    ImageView mcx03_check_result3;
    @BindView(R.id.mcx03_check_result4)
    ImageView mcx03_check_result4;
    @BindView(R.id.mcx03_check_result5)
    ImageView mcx03_check_result5;
    @BindView(R.id.other_check_result1)
    ImageView other_check_result1;
    @BindView(R.id.other_check_result2)
    ImageView other_check_result2;
    @BindView(R.id.other_check_result3)
    ImageView other_check_result3;
    @BindView(R.id.other_check_result4)
    ImageView other_check_result4;
    /* access modifiers changed from: private */
    public int progress;



    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(new Callback() {
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (progress > 360) {
                        is_start = false;
                        is_recive = true;
                        break;
                    } else {
//                        progressBar.setProgress(progress);
                        progress = progress + 1;
                        checkPercentText.setText(Integer.valueOf((int) (((double) progress) / 3.6d)) + "%");
                        mHandler.sendEmptyMessageDelayed(1, 20);
                        break;
                    }
            }
            return false;
        }
    });
    @SuppressLint({"NewApi"})
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LFBluetootService.ACTION_GATT_CONNECTED.equals(action) || LFBluetootService.ACTION_BLE_CODE_OK.equals(action)) {
                return;
            }
            if (LFBluetootService.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (LFBluetootService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(LFBluetootService.EXTRA_DATA);
                String BytesToString = BytesUtils.BytesToString(data);
                new String(data);
                if ((data[0] & 255) == 58 && (data[1] & 255) == 26 && (data[data.length - 2] & 255) == 13 && (data[data.length - 1] & 255) == 10) {
                    int check = data[8] & 255;
                    switch (data[2] & 255) {
                        case 82:
                            if (is_recive) {
                                int check_result = check - 33;
                                if (check_result >= 0 && check_result <= 4) {
                                    for (int i = 0; i < arrayList.size(); i++) {
                                        if (check_result == i) {
                                            ((ImageView) arrayList.get(i)).setImageResource(R.drawable.check_error_icon);
                                        } else {
                                            ((ImageView) arrayList.get(i)).setImageResource(R.drawable.check_noraml_icon);
                                        }
                                    }
                                    break;
                                } else {
                                    for (int i2 = 0; i2 < arrayList.size(); i2++) {
                                        ((ImageView) arrayList.get(i2)).setImageResource(R.drawable.check_noraml_icon);
                                    }
                                    break;
                                }
                            } else {
                                return;
                            }
                    }
                }
                if (data.length == 10 && (data[0] & 255) == 170 && (data[9] & 255) == 187) {
                    int command = data[1] & 255;
                    int value3 = data[3] & 255;
                    String dyu_hex = Integer.toHexString(data[5] & 255);
                    if (dyu_hex.length() < 2) {
                        String dyu_hex2 = "0" + dyu_hex;
                    }
                    String value_str = Integer.toHexString(value3);
                    if (value_str.length() < 2) {
                        value_str = "0" + value_str;
                    }
                    switch (command) {
                        case 163:
                            String binaryString = BytesUtils.hexString2binaryString(value_str);
                            if (!is_recive) {
                                return;
                            }
                            if (MainActivity.getInstance().vehicle_type.equals("CHIRREY") || MainActivity.getInstance().vehicle_type.equals("Q5") || MainActivity.getInstance().vehicle_type.equals("jinling")) {
                                for (int i3 = 3; i3 < binaryString.length(); i3++) {
                                    if (binaryString.substring(i3, i3 + 1).equals("0")) {
                                        ((ImageView) arrayList.get(i3 - 3)).setImageResource(R.drawable.check_noraml_icon);
                                    } else {
                                        ((ImageView) arrayList.get(i3 - 3)).setImageResource(R.drawable.check_error_icon);
                                    }
                                }
                                return;
                            } else if (MainActivity.getInstance().vehicle_type.equals("x8") || MainActivity.getInstance().vehicle_type.equals("yy")) {
                                for (int i4 = 3; i4 < binaryString.length(); i4++) {
                                    if (binaryString.substring(i4, i4 + 1).equals("0")) {
                                        ((ImageView) arrayList.get(7 - i4)).setImageResource(R.drawable.check_noraml_icon);
                                    } else {
                                        ((ImageView) arrayList.get(7 - i4)).setImageResource(R.drawable.check_error_icon);
                                    }
                                }
                                return;
                            } else if (MainActivity.getInstance().vehicle_type.equals("jnt03") || MainActivity.getInstance().vehicle_type.equals("x802")) {
                                for (int i5 = 1; i5 < binaryString.length(); i5++) {
                                    if (binaryString.substring(i5, i5 + 1).equals("0")) {
                                        ((ImageView) arrayList.get(i5 - 1)).setImageResource(R.drawable.check_noraml_icon);
                                    } else {
                                        ((ImageView) arrayList.get(i5 - 1)).setImageResource(R.drawable.check_error_icon);
                                    }
                                }
                                return;
                            } else {
                                for (int i6 = 4; i6 < binaryString.length(); i6++) {
                                    if (binaryString.substring(i6, i6 + 1).equals("0")) {
                                        ((ImageView) arrayList.get(i6 - 4)).setImageResource(R.drawable.check_noraml_icon);
                                    } else {
                                        ((ImageView) arrayList.get(i6 - 4)).setImageResource(R.drawable.check_error_icon);
                                    }
                                }
                                return;
                            }
                        default:
                            return;
                    }
                }
            }
        }
    };

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfcheck);
        ButterKnife.bind( this);
        m_page_title.setText(R.string.self_diagnostics);
        initEven();
        registerBroadcast();
    }

    private void initEven() {
        arrayList = new ArrayList<>();
        if (MainActivity.getInstance().vehicle_type.equals("spark2")) {
            liner_check_spark2.setVisibility(View.VISIBLE);
            liner_check_chirrey_q5.setVisibility(View.GONE);
            liner_check_other.setVisibility(View.GONE);
            linerCheckEscooter.setVisibility(View.GONE);
            arrayList.add(checkResult1);
            arrayList.add(checkResult2);
            arrayList.add(checkResult3);
            arrayList.add(checkResult4);
            arrayList.add(checkResult5);
        } else if (MainActivity.getInstance().vehicle_type.equals("x8") || MainActivity.getInstance().vehicle_type.equals("yy") || MainActivity.getInstance().vehicle_type.equals("CHIRREY") || MainActivity.getInstance().vehicle_type.equals("Q5") || MainActivity.getInstance().vehicle_type.equals("jinling")) {
            liner_check_spark2.setVisibility(View.GONE);
            liner_check_chirrey_q5.setVisibility(View.VISIBLE);
            liner_check_other.setVisibility(View.GONE);
            linerCheckEscooter.setVisibility(View.GONE);
            arrayList.add(mcx03_check_result1);
            arrayList.add(mcx03_check_result2);
            arrayList.add(mcx03_check_result3);
            arrayList.add(mcx03_check_result4);
            arrayList.add(mcx03_check_result5);
        } else if (MainActivity.getInstance().vehicle_type.equals("jnt03") || MainActivity.getInstance().vehicle_type.equals("x802")) {
            liner_check_spark2.setVisibility(View.GONE);
            liner_check_chirrey_q5.setVisibility(View.GONE);
            liner_check_other.setVisibility(View.GONE);
            linerCheckEscooter.setVisibility(View.VISIBLE);
            arrayList.add(escooterCheckResult1);
            arrayList.add(escooterCheckResult2);
            arrayList.add(escooterCheckResult3);
            arrayList.add(escooterCheckResult4);
            arrayList.add(escooterCheckResult5);
            arrayList.add(escooterCheckResult6);
            arrayList.add(escooterCheckResult7);
        } else {
            liner_check_spark2.setVisibility(View.GONE);
            liner_check_chirrey_q5.setVisibility(View.GONE);
            liner_check_other.setVisibility(View.VISIBLE);
            linerCheckEscooter.setVisibility(View.GONE);
            arrayList.add(other_check_result1);
            arrayList.add(other_check_result2);
            arrayList.add(other_check_result3);
            arrayList.add(other_check_result4);
        }
    }

    private void registerBroadcast() {
        bleService = LFBluetootService.getInstent();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        filter.addAction(LFBluetootService.ACTION_GATT_CONNECTED);
        filter.addAction(LFBluetootService.ACTION_BLE_CODE_OK);
        filter.addAction(LFBluetootService.ACTION_GATT_DISCONNECTED);
        filter.addAction(LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(LFBluetootService.ACTION_DATA_AVAILABLE);
        registerReceiver(mReceiver, filter);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        try{
            unregisterReceiver(mReceiver);
        }catch (Exception err){

        }
    }
    public void onResume(){
        super.onResume();
        registerBroadcast();
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
    }


    @Optional
    @OnClick({ R.id.check_start_btn, R.id.lay_back, R.id.lay_home})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.check_start_btn :
                if (!is_start) {
                    progress = 0;
                    is_start = true;
                    is_recive = false;
                    for (int i = 0; i < arrayList.size(); i++) {
                        ((ImageView) arrayList.get(i)).setImageResource(R.color.transparent);
                    }
                    mHandler.sendEmptyMessage(1);
                    return;
                }
                return;
            case R.id.lay_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.lay_back:
                finish();
                return;
            default:
                break;
        }
        if(intent != null){
            startActivity(intent);
        }
    }
}