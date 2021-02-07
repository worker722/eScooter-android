package com.tn.escooter.utils;

import android.graphics.Bitmap;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.BitmapRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.tn.escooter.MyApplication;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public final class apiService {

    public interface request_type {
        public static final int get_info = 1;
        public static final int save_odo = 2;
        public static final int check_sn = 3;
        public static final int upload_avatar = 4;
        public static final int custom_service = 5;
    }

    public static void login(String username, String password, JSONObjectRequestListener response){
        AndroidNetworking.post("https://www.terasys-network.com/index.php?option=com_api&format=raw&app=users&resource=login")
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(response);
    }
    private static String url = "https://www.terasys-network.com/plugins/api/users/users/base.php";
    @NotNull
    @Contract(pure = true)
    private static String getKey(int type){
        if(type == request_type.check_sn)
            return "serial_number";
        else if(type == request_type.save_odo)
            return "odo";
        return "info";
    }
    public static void baseApi(String value, int type, JSONObjectRequestListener response){
        AndroidNetworking.post(url)
                .addBodyParameter("userid", String.valueOf(MyApplication.cur_user.id))
                .addBodyParameter("serial_number", String.valueOf(MyApplication.cur_user.serial_number))
                .addBodyParameter(getKey(type), value)
                .addBodyParameter("type", String.valueOf(type))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(response);
    }
    public static void updateOdo(){
        try{
            if(MyApplication.cur_odo > 0f && MyApplication.cur_user.id > 0){
                baseApi(String.format("%.1f", MyApplication.cur_odo), request_type.save_odo, new JSONObjectRequestListener(){
                    @Override
                    public void onResponse(JSONObject response) {
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                    }
                });
            }
        }catch (Exception err){
            err.printStackTrace();
        }
    }
    public static void uploadAvatar(String base64, UploadProgressListener uploadProgressListener, JSONObjectRequestListener jsonObjectRequestListener){
        AndroidNetworking.post(url)
                .addBodyParameter("image", base64)
                .addBodyParameter("userid", String.valueOf(MyApplication.cur_user.id))
                .addBodyParameter("serial_number", String.valueOf(MyApplication.cur_user.serial_number))
                .addBodyParameter("type",String.valueOf(request_type.upload_avatar))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(jsonObjectRequestListener);
    }
    public static void sendCustomService(String username, String subject, String message, JSONObjectRequestListener jsonObjectRequestListener){
        AndroidNetworking.post(url)
                .addBodyParameter("userid", String.valueOf(MyApplication.cur_user.id))
                .addBodyParameter("serial_number", String.valueOf(MyApplication.cur_user.serial_number))
                .addBodyParameter("type",String.valueOf(request_type.custom_service))
                .addBodyParameter("username",username)
                .addBodyParameter("subject",subject)
                .addBodyParameter("message",message)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(jsonObjectRequestListener);
    }
    public static void getBitmap(String imageUrl, BitmapRequestListener bitmapRequestListener){
        AndroidNetworking.get(imageUrl)
                .setTag("imageRequestTag")
                .setPriority(Priority.MEDIUM)
                .setBitmapMaxHeight(100)
                .setBitmapMaxWidth(100)
                .setBitmapConfig(Bitmap.Config.ARGB_8888)
                .build()
                .getAsBitmap(bitmapRequestListener);
    }
}
