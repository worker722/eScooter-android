package com.tn.escooter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class VersionActivity extends Activity {

    @Nullable
    @BindView(R.id.page_title)
    TextView m_page_title;
    @BindView(R.id.app_version)
    TextView app_version;
    @BindView(R.id.app_date)
    TextView app_date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);
        ButterKnife.bind( this);
        try{
            m_page_title.setText(R.string.version_information);
            app_version.setText(getString(R.string.version, MyApplication.app_version));
            app_date.setText(MyApplication.app_date);
        }catch (Exception err){

        }
    }
    @Optional
    @OnClick({ R.id.lay_back, R.id.lay_home})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
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
            finish();
        }
    }
}