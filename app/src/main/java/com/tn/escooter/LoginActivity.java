package com.tn.escooter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.github.ybq.android.spinkit.SpinKitView;
import com.tn.escooter.buletooth.Discover2Activity;
import com.tn.escooter.utils.Model.User;
import com.tn.escooter.utils.apiService;
import com.tn.escooter.utils.utils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static com.tn.escooter.utils.utils.sharedPref;

public class LoginActivity extends Activity {

    @BindView(R.id.btn_login)
    LinearLayout btn_login;
    @BindView(R.id.spin_kit)
    SpinKitView progressBar;
    @BindView(R.id.input_username)
    EditText input_username;
    @BindView(R.id.input_password)
    EditText input_password;
    @BindView(R.id.input_SN)
    EditText input_SN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind( this);
        progressLogin(false);
        if(MyApplication.mine){
            debugging();
        }
    }
    private void debugging(){
        input_username.setText("test");
        input_password.setText("test");
        input_SN.setText("TN2020P117A001");
    }
    private void progressLogin(boolean state){
        if(state){
            btn_login.setEnabled(false);
            btn_login.setAlpha(0.8f);
            progressBar.setVisibility(View.VISIBLE);
        }else{
            btn_login.setEnabled(true);
            btn_login.setAlpha(1f);
            progressBar.setVisibility(View.INVISIBLE);
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(btn_login.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
    private void login(){
        progressLogin(true);
        String username = input_username.getText().toString();
        String pwd = input_password.getText().toString();
        String serial_number = input_SN.getText().toString();

        if(username.isEmpty()){
            utils.SuperActivityToast(this, getString(R.string.login_empty_email), utils.toast.TOAST_ERROR);
            progressLogin(false);
            return;
        }
        if(pwd.isEmpty()){
            utils.SuperActivityToast(this, getString(R.string.login_empty_pwd), utils.toast.TOAST_ERROR);
            progressLogin(false);
            return;
        }
        if(serial_number.isEmpty()){
            utils.SuperActivityToast(this, getString(R.string.login_empty_sn), utils.toast.TOAST_ERROR);
            progressLogin(false);
            return;
        }
        apiService.login(username, pwd, new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int userid = response.getInt("id");
                    String token = response.getString("auth");
                    int code = response.getInt("code");
                    if(code != 200){
                        utils.SuperActivityToast(LoginActivity.this, getString(R.string.request_error), utils.toast.TOAST_ERROR);
                        progressLogin(false);
                        return;
                    }
                    MyApplication.cur_user.token = token;
                    MyApplication.cur_user.id = userid;
                    MyApplication.cur_user.name = username;
                    apiService.baseApi(serial_number, apiService.request_type.check_sn, new JSONObjectRequestListener(){
                        @Override
                        public void onResponse(JSONObject response) {
                            progressLogin(false);
                            boolean success = false;
                            try{
                                success = response.getBoolean("success");
                            }catch (Exception err){
                                err.printStackTrace();
                            }
                            if(!success){
                                MyApplication.cur_user = new User(null, 0, null, null);
                                utils.SuperActivityToast(LoginActivity.this, getString(R.string.wrong_serial_number), utils.toast.TOAST_ERROR);
                                return;
                            }

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(utils.shared_key.token, token);
                            editor.putInt(utils.shared_key.userid, userid);
                            editor.putString(utils.shared_key.serial_number, serial_number);
                            editor.apply();
                            MyApplication.cur_user.serial_number = serial_number;
                            startActivity(new Intent(LoginActivity.this, Discover2Activity.class));
                            finish();
                        }

                        @Override
                        public void onError(ANError anError) {
                            utils.SuperActivityToast(LoginActivity.this, getString(R.string.wrong_serial_number), utils.toast.TOAST_ERROR);
                            progressLogin(false);
                            anError.printStackTrace();
                        }
                    });
                } catch (JSONException e) {
                    utils.SuperActivityToast(LoginActivity.this, getString(R.string.request_error), utils.toast.TOAST_ERROR);
                    progressLogin(false);
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(ANError error) {
                utils.SuperActivityToast(LoginActivity.this, getString(R.string.login_failed), utils.toast.TOAST_ERROR);
                progressLogin(false);
            }
        });
    }
    @Optional
    @OnClick({ R.id.btn_login, R.id.link_signup})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.link_signup:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.terasys-network.com/index.php/shop/user"));
                break;
            default:
                break;
        }
        if(intent != null){
            startActivity(intent);
        }
    }
}