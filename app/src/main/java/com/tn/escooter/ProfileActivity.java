package com.tn.escooter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.BitmapRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.tn.escooter.utils.CacheStore;
import com.tn.escooter.utils.apiService;
import com.tn.escooter.utils.utils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static com.tn.escooter.utils.utils.sharedPref;

public class ProfileActivity extends Activity {

    @BindView(R.id.img_avatar)
    ImageView img_avatar;
    @BindView(R.id.edt_name) EditText edt_name;
    @BindView(R.id.edt_street) EditText edt_street;
    @BindView(R.id.edt_zip) EditText edt_zip;
    @BindView(R.id.edt_town) EditText edt_town;
    @BindView(R.id.edt_sn) EditText edt_sn;
    @BindView(R.id.edt_odo) EditText edt_odo;
    public Uri avatar_url = null;
    ProgressDialog progDailog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind( this);

        progDailog =  ProgressDialog.show(this, getString(R.string.loading), getString(R.string.loading));
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        apiService.updateOdo();
    }

    public void initData(){
        apiService.baseApi("info", apiService.request_type.get_info, new JSONObjectRequestListener(){
            @Override
            public void onResponse(JSONObject res) {
                progDailog.dismiss();
                try{
                    if(res.getBoolean("success") == true){
                        MyApplication.cur_user.name = res.getString("name");
                        edt_name.setText(res.getString("name"));
                        edt_street.setText(res.getString("street"));
                        edt_zip.setText(res.getString("zip"));
                        edt_town.setText(res.getString("town"));
                        String avatar = res.getString("avatar");
                        edt_sn.setText(MyApplication.cur_user.serial_number);
                        edt_odo.setText(String.format("%.1f", MyApplication.cur_odo));
                        if(!avatar.isEmpty()){
                            Bitmap bitmap = CacheStore.getInstance().getCacheFile(avatar);
                            if(bitmap != null){
                                img_avatar.setImageBitmap(bitmap);
                                return;
                            }
                            apiService.getBitmap(avatar, new BitmapRequestListener(){
                                @Override
                                public void onResponse(Bitmap response) {
                                    img_avatar.setImageBitmap(response);
                                    Log.e( avatar, response.toString());
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
                progDailog.dismiss();
            }
        });
    };
    @Optional
    @OnClick({ R.id.lay_back, R.id.lay_home, R.id.btn_logoff, R.id.img_avatar})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.lay_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.lay_back:
                finish();
                return;
            case R.id.btn_logoff:
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();
                intent = new Intent(this, LoginActivity.class);
                break;
            case R.id.img_avatar:
                selectImage();
                break;
            default:
                break;
        }
        if(intent != null){
            startActivity(intent);
        }
    }
    public void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 1);
    }
    public void uploadAvatar(String base64) {
        apiService.uploadAvatar(base64, new UploadProgressListener(){
            @Override
            public void onProgress(long bytesUploaded, long totalBytes) {
//                utils.SuperActivityToast(ProfileActivity.this, getString(R.string.txt_uploading, String.valueOf(bytesUploaded), String.valueOf(totalBytes)), utils.toast.TOAST_INFO);
            }
        }, new JSONObjectRequestListener(){
            @Override
            public void onResponse(JSONObject response) {
                progDailog.dismiss();
                utils.SuperActivityToast(ProfileActivity.this, getString(R.string.upload_success), utils.toast.TOAST_INFO);
            }

            @Override
            public void onError(ANError anError) {
                progDailog.dismiss();
                utils.SuperActivityToast(ProfileActivity.this, getString(R.string.upload_error), utils.toast.TOAST_WARNING);
                anError.printStackTrace();
            }
        });
    }
    @Override
    protected void onActivityResult(int RC, int RQC, Intent I) {
        super.onActivityResult(RC, RQC, I);
        if (RC == 1 && RQC == RESULT_OK && I != null && I.getData() != null) {
            progDailog =  ProgressDialog.show(ProfileActivity.this, getString(R.string.txt_uploading), getString(R.string.txt_uploading));
            avatar_url = I.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), avatar_url);
                img_avatar.setImageBitmap(bitmap);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] byteArray = outputStream.toByteArray();

                String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                uploadAvatar(encodedString);
            } catch (Exception e) {
                e.printStackTrace();
                progDailog.dismiss();
            }
        }
    }
}