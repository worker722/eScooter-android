package com.tn.escooter.buletooth;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tn.escooter.MainActivity;
import com.tn.escooter.MoreActivity;
import com.tn.escooter.R;
import com.tn.escooter.bluetooth.LFBluetootService;
import com.tn.escooter.utils.apiService;
import com.tn.escooter.utils.utils;

import org.jetbrains.annotations.NotNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class Settings2Activity extends Activity {

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
    @BindView(R.id.lay_change_bt_name)
    LinearLayout lay_change_bt_name;
    @BindView(R.id.lay_change_bt_pwd)
    LinearLayout lay_change_bt_pwd;

    private LFBluetootService bleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind( this);
        m_page_title.setText(R.string.settings);
        lay_change_bt_name.setVisibility(View.GONE);
        lay_change_bt_pwd.setVisibility(View.GONE);
        mTimerHandler = new Handler();
        bleService = LFBluetootService.getInstent();
    }
    private void registerBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
//        filter.addAction(LFBluetootService.ACTION_GATT_CONNECTED);
//        filter.addAction(LFBluetootService.ACTION_BLE_CODE_OK);
//        filter.addAction(LFBluetootService.ACTION_GATT_DISCONNECTED);
//        filter.addAction(LFBluetootService.ACTION_GATT_SERVICES_DISCOVERED);
//        filter.addAction(LFBluetootService.ACTION_DATA_AVAILABLE);
//

        filter.addAction(utils.Lenzod_Action.START_MODE);
        filter.addAction(utils.Lenzod_Action.CONSTANT_MODE);
        filter.addAction(utils.Lenzod_Action.SPEED_MODE);
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
        apiService.updateOdo();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(utils.Lenzod_Action.START_MODE.equals(action)){
                rad_zero_start.setChecked(TwoWheelActivity.getInstance().zero_start_mode);
                rad_push_start.setChecked(!TwoWheelActivity.getInstance().zero_start_mode);
            }else if(utils.Lenzod_Action.CONSTANT_MODE.equals(action)){
                setWalkMode(TwoWheelActivity.getInstance().constant_speed);
            }else if(utils.Lenzod_Action.SPEED_MODE.equals(action)){
                setPerMode(TwoWheelActivity.getInstance().is_km_per);
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
            R.id.lay_criuse_control, R.id.lay_switch_between, R.id.lay_change_bt_name, R.id.lay_change_bt_pwd,
            R.id.lay_self_diagnositics, R.id.rad_push_start, R.id.rad_zero_start})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.lay_bluetooth:
                intent = new Intent(this, Discover2Activity.class);
                break;
            case R.id.lay_settings:
                break;
            case R.id.lay_more:
                intent = new Intent(this, MoreActivity.class);
                break;
            case R.id.lay_main:
            case R.id.lay_home:
                intent = new Intent(this, TwoWheelActivity.class);
                break;
            case R.id.lay_back:
                finish();
                return;
            case R.id.rad_zero_start /*2131689913*/:
                sendCommandBroadcast(utils.Command.START_MODE_ZERO);
                return;
            case R.id.rad_push_start /*2131689914*/:
                sendCommandBroadcast(utils.Command.START_MODE_NOT_ZERO);
                return;
            case R.id.lay_criuse_control /*2131689917*/:
                sendCommandBroadcast(is_walk_mode ? utils.Command.CONSTANT_SPEED_OFF : utils.Command.CONSTANT_SPEED_ON);
               return;
            case R.id.lay_switch_between:
                sendCommandBroadcast(is_km_per ? utils.Command.SPEED_MP : utils.Command.SPEED_KM);
                return;
            case R.id.lay_self_diagnositics:
                intent = new Intent(this, CheckActivity.class);
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
//            finish();
        }
    }
    private void sendCommandBroadcast(String str) {
        Intent intent = new Intent(utils.Lenzod_Action.SETTING_CHANGE);
        intent.putExtra(utils.Key.ACTION, str);
        sendBroadcast(intent);
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
                    utils.SuperActivityToast(Settings2Activity.this, getString(R.string.blue_pwd_empty_toast), utils.toast.TOAST_ERROR);
                    return;
                }
                bleService.sendString("SN+" + blue_pwd);
                dialog2.dismiss();
                utils.SuperActivityToast(Settings2Activity.this, getString(R.string.modity_success_hint), utils.toast.TOAST_SUCCESS);
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
                    utils.SuperActivityToast(Settings2Activity.this, getString(R.string.validation_error_password_not_pare), utils.toast.TOAST_WARNING);
                    dialog2.dismiss();
                } else if (changePassword(((EditText) dialog2.findViewById(R.id.tv_content_p1)).getText().toString(), ((EditText) dialog2.findViewById(R.id.tv_content_p2)).getText().toString())) {
                    bleService.sendString("SC+" + ((EditText) dialog2.findViewById(R.id.tv_content_p1)).getText().toString());
                    utils.SuperActivityToast(Settings2Activity.this, getString(R.string.modity_success_hint), utils.toast.TOAST_SUCCESS);
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
    public boolean changePassword(@NotNull String password, String confirmPassword) {
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