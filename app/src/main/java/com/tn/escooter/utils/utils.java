package com.tn.escooter.utils;

//https://github.com/JohnPersano/SuperToasts

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;

import java.io.IOException;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class utils {
    public static SharedPreferences sharedPref = null;

    public interface shared_key {
        public static final String userid = "userid";
        public static final String token = "token";
        public static final String serial_number = "serial_number";
        public static final String max_speed = "max_speed";
        public static final String username = "username";
    }

    public interface toast {
        public static final int TOAST_INFO = 0;
        public static final int TOAST_SUCCESS = 1;
        public static final int TOAST_WARNING = 2;
        public static final int TOAST_ERROR = 3;
    }

    public interface bluetooth_type {
        public static final String MACWHEEL = "macwheel";
        public static final String LENZOD = "lenzod";
    }

    public interface Key {
        public static final String ACTION = "key_action";
        public static final String RESULT = "key_result";
    }

    public interface Command {
        public static final String CONSTANT_SPEED_OFF = "FF551D010274";
        public static final String CONSTANT_SPEED_ON = "FF551D010173";
        public static final String ELECT_24V = "FF551B010171";
        public static final String ELECT_36V = "FF551B010272";
        public static final String ELECT_48V = "FF551B010373";
        public static final String ELECT_60V = "FF551B010474";
        public static final String GEAR_D1 = "FF551F010276";
        public static final String GEAR_D2 = "FF551F010377";
        public static final String GEAR_D3 = "FF551F010478";
        public static final String GEAR_P = "FF551F010175";
        public static final String LOCK = "FF551701026E";
        public static final String SPEED_KM = "FF551801016E";
        public static final String SPEED_MODE_ELECT = "FF5510010166";
        public static final String SPEED_MODE_HELP = "FF5510010267";
        public static final String SPEED_MODE_RIDE = "FF5510010368";
        public static final String SPEED_MP = "FF551801026F";
        public static final String START_MODE_NOT_ZERO = "FF551A010271";
        public static final String START_MODE_ZERO = "FF551A010170";
        public static final String UNLOCK = "FF551701016D";
        public static final String WHEEL_10C = "FF551C010677";
        public static final String WHEEL_12C = "FF551C010778";
        public static final String WHEEL_14C = "FF551C010879";
        public static final String WHEEL_16C = "FF551C01097A";
        public static final String WHEEL_18C = "FF551C010A7B";
        public static final String WHEEL_20C = "FF551C010B7C";
        public static final String WHEEL_5C = "FF551C010172";
        public static final String WHEEL_5_5C = "FF551C010273";
        public static final String WHEEL_6C = "FF551C010374";
        public static final String WHEEL_6_5C = "FF551C010475";
        public static final String WHEEL_8C = "FF551C010576";
    }

    public interface Lenzod_Action {
        public static final String BASE = "cn.sccss.speed.action.";
        public static final String CONSTANT_MODE = "cn.sccss.speed.action.CONSTANT_MODE";
        public static final String ELECT_MODE = "cn.sccss.speed.action.ELECT_MODE";
        public static final String RIDE_MODE = "cn.sccss.speed.action.SPEED_MODE";
        public static final String SETTING_CHANGE = "SETTING_CHANGE";
        public static final String SPEED_MODE = "cn.sccss.speed.action.SPEED_MODE";
        public static final String START_MODE = "cn.sccss.speed.action.START_MODE";
        public static final String WHEEL_MODE = "cn.sccss.speed.action.WHEEL_MODE";
    }

    public static final String CAR_TYPE_HUABAN = "FF55020839353237373839300D";
    public static final String CAR_TYPE_NIUNIU = "FF5502083935323730313233FB";
    public static final String KEY_CONTENT_DEEP_LINK_METADATA_DESCRIPTION = "description";

    public static String UuidFilter(String str) {
        String sb = "";
        String str_uuid = str.replace("-", "");
        for (int i = 0; i < str_uuid.length() / 2; i++) {
            sb = sb + str_uuid.substring((str_uuid.length() - (i * 2)) - 2, str_uuid.length() - (i * 2));
        }
        return sb;
    }
    public static void killProcess(Context context) {
        String readLine;
        String packageName = context.getPackageName();
        try {
            Runtime runtime = Runtime.getRuntime();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runtime.exec("ps").getInputStream()));
            do {
                readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
            } while (!readLine.contains(packageName));
            bufferedReader.close();
            StringTokenizer stringTokenizer = new StringTokenizer(readLine);
            int i = 0;
            String str = "";
            do {
                if (!stringTokenizer.hasMoreTokens()) {
                    break;
                }
                i++;
                str = stringTokenizer.nextToken();
            } while (i != 2);
            StringBuilder sb = new StringBuilder();
            sb.append("kill -15 ");
            sb.append(str);
            runtime.exec(sb.toString());
        } catch (IOException unused) {
        }
    }
    public static String randomString(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
    public static void SuperActivityToast(Context context, String message, int type) {
        String toast_type = PaletteUtils.DARK_GREY;
        switch (type){
            case toast.TOAST_INFO:     //info
                toast_type = PaletteUtils.MATERIAL_BLUE;
                break;
            case toast.TOAST_SUCCESS:     //success
                toast_type = PaletteUtils.MATERIAL_CYAN;
                break;
            case toast.TOAST_WARNING:     //warning
                toast_type = PaletteUtils.MATERIAL_AMBER;
                break;
            case toast.TOAST_ERROR:     //error
                toast_type = PaletteUtils.MATERIAL_RED;
                break;
            default:
                break;
        }
        SuperActivityToast(context, message, toast_type, Gravity.TOP);
    }

    public static void SuperActivityToast(Context context, String message, String type, int gravity){
        SuperActivityToast.create(context, new Style(), Style.TYPE_BUTTON)
                .setText(message)
                .setDuration(Style.DURATION_LONG)
                .setFrame(Style.FRAME_LOLLIPOP)
                .setColor(PaletteUtils.getSolidColor(type))
                .setGravity(gravity)
                .setAnimations(Style.ANIMATIONS_POP).show();
    }


}

