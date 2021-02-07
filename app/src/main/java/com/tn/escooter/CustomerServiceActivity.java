package com.tn.escooter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.tn.escooter.utils.apiService;
import com.tn.escooter.utils.utils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class CustomerServiceActivity extends Activity {

    @Nullable
    @BindView(R.id.page_title)
    TextView m_page_title;
    @BindView(R.id.input_email_to)
    EditText input_email_to;
    @BindView(R.id.input_username)
    EditText input_username;
    @BindView(R.id.input_subject)
    EditText input_subject;
    @BindView(R.id.input_message)
    EditText input_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_service);
        ButterKnife.bind( this);
        m_page_title.setText(R.string.customer_services);
        input_email_to.setText(getString(R.string.def_email));
        input_username.setText(MyApplication.cur_user.name);
    }
    public void sendCustomService(){
        String username = input_username.getText().toString();
        String subject = input_subject.getText().toString();
        String message = input_message.getText().toString();
        if(username.isEmpty() || subject.isEmpty() || message.isEmpty()){
            utils.SuperActivityToast(this, getString(R.string.invalid_field), utils.toast.TOAST_WARNING);
            return;
        }
        apiService.sendCustomService(username, subject, message, new JSONObjectRequestListener(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")){
                        input_subject.setText("");
                        input_message.setText("");
                        utils.SuperActivityToast(CustomerServiceActivity.this, getString(R.string.send_success), utils.toast.TOAST_INFO);
                        return;
                    }
                    utils.SuperActivityToast(CustomerServiceActivity.this, getString(R.string.request_error), utils.toast.TOAST_INFO);

                } catch (JSONException e) {
                    e.printStackTrace();
                    utils.SuperActivityToast(CustomerServiceActivity.this, e.getMessage(), utils.toast.TOAST_INFO);
                }
            }

            @Override
            public void onError(ANError anError) {

            }
        });
    }
    @Optional
    @OnClick({ R.id.lay_back, R.id.lay_home, R.id.btn_send})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.lay_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.lay_back:
                finish();
                return;
            case R.id.btn_send:
                sendCustomService();
                break;
            default:
                break;
        }
        if(intent != null){
            startActivity(intent);
            finish();
        }
    }
}