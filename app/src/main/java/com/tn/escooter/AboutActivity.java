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

public class AboutActivity extends Activity {

    @Nullable
    @BindView(R.id.page_title)
    TextView m_page_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind( this);
        m_page_title.setText(R.string.about_us_title);
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