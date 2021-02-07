package com.tn.escooter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tn.escooter.utils.BytesUtils;
import com.tn.escooter.bluetooth.DiscoverActivity;
import com.tn.escooter.bluetooth.LFBluetootService;
import com.tn.escooter.utils.utils;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import android.app.Dialog;

public class SettingsActivity extends Activity {

    @Nullable
    @BindView(R.id.page_title)
    TextView m_page_title;
    @Nullable
    @BindView(R.id.img_criuse)
    ImageView mCriuseImage;
    @Nullable
    @BindView(R.id.img_btn_mode)
    ImageView mBTModeImage;
    private boolean is_change = false;
    private boolean is_km_per = false;
    private boolean is_walk_mode = false;
    private Handler mTimerHandler;
    private String TAG = "Setting Activity";
    @BindView(R.id.rad_zero_start)
     RadioButton rad_zero_start;
    @BindView(R.id.rad_push_start)
    RadioButton rad_push_start;
    private LFBluetootService bleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind( this);
        m_page_title.setText(R.string.settings);
        
        mTimerHandler = new Handler();
        
        bleService = LFBluetootService.getInstent();
        set_ui();

    }
    private void registerBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        filter.addAction(LFBluetootService.ACTION_GATT_CONNECTED);
        filter.addAction(LFBluetootService.ACTION_BLE_CODE_OK);
        filter.addAction(LFBluetootService.ACTION_GATT_DISCONNECTED);
        filter.addAction(LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(LFBluetootService.ACTION_DATA_AVAILABLE);
        registerReceiver(mReceiver, filter);
    }
    private void setWalkMode(boolean mode){
        is_walk_mode = mode;
        if(!is_walk_mode) mCriuseImage.setImageResource(R.drawable.ic_switch_off);
        else mCriuseImage.setImageResource(R.drawable.ic_switch_on);
    }
    private void setPerMode(boolean iskm){
        is_km_per = iskm;
        MainActivity.getInstance().is_km_per = is_km_per;
        if(is_km_per) mBTModeImage.setImageResource(R.drawable.ic_switch_kmh);
        else mBTModeImage.setImageResource(R.drawable.ic_switch_mph);
    }
    public void onResume(){
        super.onResume();
        registerBroadcast();
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LFBluetootService.ACTION_GATT_CONNECTED.equals(action) || LFBluetootService.ACTION_BLE_CODE_OK.equals(action)) {
                return;
            }
            if (LFBluetootService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, LFBluetootService.ACTION_GATT_DISCONNECTED);
            } else if (LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED);
            } else if (LFBluetootService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, LFBluetootService.ACTION_DATA_AVAILABLE);
                byte[] data = intent.getByteArrayExtra(LFBluetootService.EXTRA_DATA);
                String readMessage = BytesUtils.BytesToString(data);
                try {
                    String str = new String(data, "gbk");
                    readMessage = str;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if ("+UNIT=0".equals(readMessage)) {
                    setPerMode(true);
                }
                else if ("+UNIT=1".equals(readMessage)) {
                    setPerMode(false);
                }
                if (readMessage.equals("+ZSST=0")) {
                    rad_zero_start.setChecked(true);
                    rad_push_start.setChecked(false);
                } else if (readMessage.equals("+ZSST=1")) {
                    rad_zero_start.setChecked(false);
                    rad_push_start.setChecked(true);
                }
                if (readMessage.equals("+CRZE=0")) {
                    setWalkMode(false);
                } else if (readMessage.equals("+CRZE=1")) {
                    setWalkMode(true);
                }
                if (data.length == 10 && (data[0] & 255) == 170 && (data[9] & 255) == 187) {
                    int command = data[1] & 255;
                    int value5 = data[5] & 255;
                    String value6_str = Integer.toHexString(data[6] & 255);
                    if (value6_str.length() < 2) {
                        value6_str = "0" + value6_str;
                    }
                    String value5_str = Integer.toHexString(value5);
                    if (value5_str.length() < 2) {
                        value5_str = "0" + value5_str;
                    }
                    switch (command) {
                        case 163:
                            if (!MainActivity.getInstance().vehicle_type.equals("x8")) {
                                if (value6_str.substring(0, 1).equals("0")) {
                                    rad_zero_start.setChecked(true);
                                    rad_push_start.setChecked(false);
                                } else if (value6_str.substring(0, 1).equals("1")) {
                                    rad_zero_start.setChecked(false);
                                    rad_push_start.setChecked(true);
                                }
                                if (value6_str.substring(1, 2).equals("0")) {
                                    setWalkMode(false);
                                } else if (value6_str.substring(1, 2).equals("1")) {
                                    setWalkMode(true);
                                }
                                if (is_change) {
                                    if (!value5_str.substring(1, 2).equals("0")) {
                                        if (value5_str.substring(1, 2).equals("1")) {
                                            setPerMode(false);
                                            break;
                                        }
                                    } else {
                                        setPerMode(true);
                                        break;
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                return;
                            }
                            break;
                    }
                }
            }
        }
    };

    public void onStop() {
        super.onStop();
        try{
            unregisterReceiver(mReceiver);
        }catch (Exception err){

        }
    }
    @Optional
    @OnClick({ R.id.lay_bluetooth, R.id.lay_settings, R.id.lay_more, R.id.lay_main, R.id.lay_back, R.id.lay_home,
    R.id.lay_criuse_control, R.id.lay_switch_between, R.id.lay_change_bt_name, R.id.lay_change_bt_pwd, R.id.lay_self_diagnositics})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.lay_bluetooth:
                intent = new Intent(this, DiscoverActivity.class);
                break;
            case R.id.lay_settings:
                break;
            case R.id.lay_more:
                intent = new Intent(this, MoreActivity.class);
                break;
            case R.id.lay_main:
            case R.id.lay_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.lay_back:
                finish();
                return;
            case R.id.rad_zero_start /*2131689913*/:
                if (MainActivity.getInstance().vehicle_type.equals("x802")) {
                    bleService.sendHexString("AA120600BEBB");
                    return;
                }
                bleService.sendString("+ZSST=0");
                rad_zero_start.setChecked(true);
                rad_push_start.setChecked(false);
                mTimerHandler.postDelayed(new Runnable() {
                    public void run() {
                        bleService.sendString("+ZSST=?");
                    }
                }, 250);
                return;
            case R.id.rad_push_start /*2131689914*/:
                if (MainActivity.getInstance().vehicle_type.equals("x802")) {
                    bleService.sendHexString("AA120601BFBB");
                    return;
                }
                bleService.sendString("+ZSST=1");
                rad_zero_start.setChecked(false);
                rad_push_start.setChecked(true);
                mTimerHandler.postDelayed(new Runnable() {
                    public void run() {
                        bleService.sendString("+ZSST=?");
                    }
                }, 250);
                return;
            case R.id.lay_criuse_control /*2131689917*/:
                if (!MainActivity.getInstance().vehicle_type.equals("x802")) {
                    if (is_walk_mode) {
                        bleService.sendString("+CRZE=0");
                    } else {
                        bleService.sendString("+CRZE=1");
                    }
                    mTimerHandler.postDelayed(new Runnable() {
                        public void run() {
                            bleService.sendString("+CRZE=?");
                        }
                    }, 200);
                    return;
                } else if (is_walk_mode) {
                    bleService.sendHexString("AA050600A9BB");
                    return;
                } else {
                    bleService.sendHexString("AA050601A8BB");
                    return;
                }
            case R.id.lay_switch_between:
                is_change = false;
                if (is_km_per) {
                    if (MainActivity.getInstance().vehicle_type.equals("x802")) {
                        bleService.sendHexString("AA190601B4BB");
                    } else {
                        bleService.sendString("+UNIT=1");
                    }
                    MainActivity.getInstance().is_km_per = false;
                } else {
                    if (MainActivity.getInstance().vehicle_type.equals("x802")) {
                        bleService.sendHexString("AA190600B5BB");
                    } else {
                        bleService.sendString("+UNIT=0");
                    }
                    MainActivity.getInstance().is_km_per = true;
                }
                mTimerHandler.postDelayed(new Runnable() {
                    public void run() {
                        is_change = true;
                        bleService.sendString("+UNIT=?");
                    }
                }, 300);
                return;
            case R.id.lay_self_diagnositics:
                intent = new Intent(this, SelfcheckActivity.class);
                break;
            case R.id.lay_change_bt_name:
                set_blue_name();
                break;
            case R.id.lay_change_bt_pwd:
                showSettingDlg();
                break;
            default:
                break;
        }
        if(intent != null){
            startActivity(intent);
        }
    }
    private void set_ui() {
        Log.i("vehicle_type", "vehicle_type==" + MainActivity.getInstance().vehicle_type);
        if (MainActivity.getInstance().vehicle_type.equals("cxinwalk") || MainActivity.getInstance().vehicle_type.equals("MERCURY3") || MainActivity.getInstance().vehicle_type.equals("MERCURY1") || MainActivity.getInstance().vehicle_type.equals("Hiboy") || MainActivity.getInstance().vehicle_type.equals("JOYOR")) {
        } else if (MainActivity.getInstance().vehicle_type.equals("spark1") || MainActivity.getInstance().vehicle_type.equals("spark2")) {
        } else {
        }
        if (MainActivity.getInstance().vehicle_type.equals("CHIRREY") || MainActivity.getInstance().vehicle_type.equals("Q5") || MainActivity.getInstance().vehicle_type.equals("Aiyou") || MainActivity.getInstance().vehicle_type.equals("FS022") || MainActivity.getInstance().vehicle_type.equals("x8") || MainActivity.getInstance().vehicle_type.equals("JOYOR") || MainActivity.getInstance().vehicle_type.equals("jinling") || MainActivity.getInstance().vehicle_type.equals("x802") || MainActivity.getInstance().vehicle_type.equals("JOYOR02") || MainActivity.getInstance().vehicle_type.equals("dst01")) {
            mTimerHandler.postDelayed(new Runnable() {
                public void run() {
                    bleService.sendString("+ZSST=?");
                }
            }, 200);
            mTimerHandler.postDelayed(new Runnable() {
                public void run() {
                    bleService.sendString("+CRZE=?");
                }
            }, 400);
        } else if (MainActivity.getInstance().vehicle_type.equals("CHIRREY New01") || MainActivity.getInstance().vehicle_type.equals("jnt03") || MainActivity.getInstance().vehicle_type.equals("CHIRREY 10")) {
            mTimerHandler.postDelayed(new Runnable() {
                public void run() {
                    bleService.sendString("+CRZE=?");
                }
            }, 400);
        }
        if (MainActivity.getInstance().vehicle_type.equals("dst01")) {
        }
        if (MainActivity.getInstance().vehicle_type.equals("FS022") || MainActivity.getInstance().vehicle_type.equals("Aiyou") || MainActivity.getInstance().vehicle_type.equals("JOYOR") || MainActivity.getInstance().vehicle_type.equals("JOYOR02")) {
            mTimerHandler.postDelayed(new Runnable() {
                public void run() {
                    bleService.sendString("HLGT=?");
                }
            }, 400);
        }
        if (MainActivity.getInstance().vehicle_type.equals("CHIRREY New01")) {
        }
    }
    private void set_blue_name() {
        final Dialog dialog2 = new Dialog(this, R.style.myStyle);
        dialog2.setContentView(R.layout.blue_name_dialog);
        dialog2.setCanceledOnTouchOutside(false);
        dialog2.show();
        final EditText edit_pwd = (EditText) dialog2.findViewById(R.id.connect_pwd_edit);
        dialog2.findViewById(R.id.connect_cancle_btn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog2.dismiss();
            }
        });
        dialog2.findViewById(R.id.connect_ok_btn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String blue_pwd = edit_pwd.getText().toString().trim();
                if (TextUtils.isEmpty(blue_pwd)) {
                    utils.SuperActivityToast(SettingsActivity.this, getString(R.string.blue_pwd_empty_toast), utils.toast.TOAST_ERROR);
                    return;
                }
                bleService.sendString("SN+" + blue_pwd);
                dialog2.dismiss();
                utils.SuperActivityToast(SettingsActivity.this, getString(R.string.modity_success_hint), utils.toast.TOAST_SUCCESS);
            }
        });
    }
    private void showSettingDlg() {
        final Dialog dialog2 = new Dialog(this);
        dialog2.setTitle(R.string.blt_set_psw);
        dialog2.setContentView(R.layout.dialog_renp2);
        ((EditText) dialog2.findViewById(R.id.tv_content_p0)).setKeyListener(new NumberKeyListener() {
            public int getInputType() {
                return 8194;
            }

            /* access modifiers changed from: protected */
            public char[] getAcceptedChars() {
                String acceptedString = "0123456789";
                char[] acceptedChars = new char[acceptedString.length()];
                acceptedString.getChars(0, acceptedString.length(), acceptedChars, 0);
                return acceptedChars;
            }
        });
        ((EditText) dialog2.findViewById(R.id.tv_content_p1)).setKeyListener(new NumberKeyListener() {
            public int getInputType() {
                return 8194;
            }

            /* access modifiers changed from: protected */
            public char[] getAcceptedChars() {
                String acceptedString = "0123456789";
                char[] acceptedChars = new char[acceptedString.length()];
                acceptedString.getChars(0, acceptedString.length(), acceptedChars, 0);
                return acceptedChars;
            }
        });
        ((EditText) dialog2.findViewById(R.id.tv_content_p2)).setKeyListener(new NumberKeyListener() {
            public int getInputType() {
                return 8194;
            }

            /* access modifiers changed from: protected */
            public char[] getAcceptedChars() {
                String acceptedString = "0123456789";
                char[] acceptedChars = new char[acceptedString.length()];
                acceptedString.getChars(0, acceptedString.length(), acceptedChars, 0);
                return acceptedChars;
            }
        });
        dialog2.findViewById(R.id.ble_set_ok).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (!((EditText) dialog2.findViewById(R.id.tv_content_p0)).getText().toString().equals(bleService.password)) {
                    utils.SuperActivityToast(SettingsActivity.this, getString(R.string.validation_error_password_not_pare), utils.toast.TOAST_WARNING);
                    dialog2.dismiss();
                } else if (changePassword(((EditText) dialog2.findViewById(R.id.tv_content_p1)).getText().toString(), ((EditText) dialog2.findViewById(R.id.tv_content_p2)).getText().toString())) {
                    bleService.sendString("SC+" + ((EditText) dialog2.findViewById(R.id.tv_content_p1)).getText().toString());
                    utils.SuperActivityToast(SettingsActivity.this, getString(R.string.modity_success_hint), utils.toast.TOAST_SUCCESS);
                    dialog2.dismiss();
                }
            }
        });
        dialog2.findViewById(R.id.blt_set_cancle).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                dialog2.dismiss();
            }
        });
        dialog2.setCanceledOnTouchOutside(false);
        dialog2.show();
    }

    /* access modifiers changed from: private */
    public boolean changePassword(String password, String confirmPassword) {
        if (password.isEmpty()) {
            utils.SuperActivityToast(this, getString(R.string.empty_pwd), utils.toast.TOAST_ERROR);
            return false;
        } else if (password.length() < 6) {
            utils.SuperActivityToast(this, getString(R.string.pwd_too_short), utils.toast.TOAST_ERROR);
            return false;
        } else if (password.length() > 6) {
            utils.SuperActivityToast(this, getString(R.string.pwd_too_long), utils.toast.TOAST_ERROR);
            return false;
        } else if (password.equals(confirmPassword)) {
            return true;
        } else {
            utils.SuperActivityToast(this, getString(R.string.pwd_not_match), utils.toast.TOAST_ERROR);
            return false;
        }
    }
}