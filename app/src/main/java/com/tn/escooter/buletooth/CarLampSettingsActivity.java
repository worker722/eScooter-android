package com.tn.escooter.buletooth;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tn.escooter.R;
import com.tn.escooter.utils.BytesUtils;
import static com.tn.escooter.utils.BytesUtils.getLength;

public class CarLampSettingsActivity extends Activity implements View.OnClickListener {
    private final int DATA_TOAST = 12;
    private final int SCAN_RESULT_TOAST = 10;
    private final String TAG = "CarLampSettingsActivity";
    /* access modifiers changed from: private */
    public int Width;
    /* access modifiers changed from: private */
    public int X;
    /* access modifiers changed from: private */
    public int Y;
    private int a = 0;
    /* access modifiers changed from: private */
    public Bitmap bitmapBack;
    private Button cancel;
    private Button car_lamp_settings_rollback;
    private ColorPickView chromatic_circle;
    /* access modifiers changed from: private */
    public LinearLayout colour_imi;
    /* access modifiers changed from: private */
    public int conX;
    /* access modifiers changed from: private */
    public int conY;
    /* access modifiers changed from: private */
    public int controlwidth;
    /* access modifiers changed from: private */
    public int count = 0;
    /* access modifiers changed from: private */
    public boolean defaul = false;
    private int defaultColor;
    private String faileds;
    /* access modifiers changed from: private */
    public int fillColors;
    /* access modifiers changed from: private */
    public final Handler handlers = new Handler() {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            CarLampSettingsActivity.access$004(CarLampSettingsActivity.this);
            CarLampSettingsActivity.this.setChange();
            if (CarLampSettingsActivity.this.count == 3) {
                CarLampSettingsActivity.this.isSuccess = false;
                CarLampSettingsActivity.this.count = 0;
            }
            if (CarLampSettingsActivity.this.isSuccess) {
                CarLampSettingsActivity.this.handlers.sendEmptyMessageDelayed(0, 500);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean isSuccess = false;
    private boolean lights = false;
    private LinearLayout linear_rollback;
    private String losts;
    private Context mContext;
    /* access modifiers changed from: private */
    public ImageView mImageViewColourBackground;
    /* access modifiers changed from: private */
    public ImageView mImageViewRotundity;
    private String names;
    private String newCode = "";
    private String newCode2;
    private Button right_confirm;
    private byte[] srtbyte = null;
    private String states;
    private TextView tv_pure_color;
    private TextView tv_rgb;
    /* access modifiers changed from: private */
    public int wmheight;
    /* access modifiers changed from: private */
    public int wmwidth;

    private final class ImageViewOnTouchListener implements View.OnTouchListener {
        private int mX;
        private int mY;
        private int x;
        private int y;

        private ImageViewOnTouchListener() {
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == 0) {
                this.x = (int) motionEvent.getX();
                this.y = (int) motionEvent.getY();
            }
            if (motionEvent.getAction() == 2) {
                int[] iArr = new int[2];
                CarLampSettingsActivity.this.mImageViewRotundity.getLocationInWindow(iArr);
                this.mX = iArr[0];
                this.mY = iArr[1];
                if ( getLength((float) (this.mX + CarLampSettingsActivity.this.Width), (float) (this.mY + CarLampSettingsActivity.this.Width), (float) (CarLampSettingsActivity.this.X + CarLampSettingsActivity.this.Width), (float) (CarLampSettingsActivity.this.Y + CarLampSettingsActivity.this.Width)) <= CarLampSettingsActivity.this.controlwidth - CarLampSettingsActivity.this.Width) {
                    view.layout(view.getLeft() + (((int) motionEvent.getX()) - this.x), view.getTop() + (((int) motionEvent.getY()) - this.y), view.getRight() + (((int) motionEvent.getX()) - this.x), view.getBottom() + (((int) motionEvent.getY()) - this.y));
                }
            }
            if (motionEvent.getAction() == 1) {
                CarLampSettingsActivity carLampSettingsActivity = CarLampSettingsActivity.this;
                carLampSettingsActivity.fillColors = carLampSettingsActivity.bitmapBack.getPixel(this.mX + CarLampSettingsActivity.this.Width, this.mY + CarLampSettingsActivity.this.Width);
                StringBuilder sb = new StringBuilder();
                sb.append("--------color-->>");
                sb.append(CarLampSettingsActivity.this.fillColors);
                String str = "CarLampSettingsActivity";
                Log.e(str, sb.toString());
                CarLampSettingsActivity.this.defaul = true;
                GradientDrawable gradientDrawable = (GradientDrawable) CarLampSettingsActivity.this.colour_imi.getBackground();
                gradientDrawable.setColor(CarLampSettingsActivity.this.fillColors);
                gradientDrawable.setStroke(1, CarLampSettingsActivity.this.fillColors);
                int red = Color.red(CarLampSettingsActivity.this.fillColors);
                int green = Color.green(CarLampSettingsActivity.this.fillColors);
                int blue = Color.blue(CarLampSettingsActivity.this.fillColors);
                StringBuilder sb2 = new StringBuilder();
                sb2.append("--------red-->>");
                sb2.append(red);
                Log.e(str, sb2.toString());
                StringBuilder sb3 = new StringBuilder();
                sb3.append("--------green-->>");
                sb3.append(green);
                Log.e(str, sb3.toString());
                StringBuilder sb4 = new StringBuilder();
                sb4.append("--------blue-->>");
                sb4.append(blue);
                Log.e(str, sb4.toString());
            }
            return true;
        }
    }

    static /* synthetic */ int access$004(CarLampSettingsActivity carLampSettingsActivity) {
        int i = carLampSettingsActivity.count + 1;
        carLampSettingsActivity.count = i;
        return i;
    }
    private final class mOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        private mOnTouchListener() {
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == 0) {
                this.x = (int) motionEvent.getX();
                this.y = (int) motionEvent.getY();
            }
            motionEvent.getAction();
            if (motionEvent.getAction() == 1) {
                int left = CarLampSettingsActivity.this.mImageViewColourBackground.getLeft() + this.x;
                int top = CarLampSettingsActivity.this.mImageViewColourBackground.getTop() + this.y;
                int access$1900 = CarLampSettingsActivity.this.wmwidth - ((CarLampSettingsActivity.this.mImageViewColourBackground.getLeft() + this.x) + CarLampSettingsActivity.this.Width);
                int access$2000 = CarLampSettingsActivity.this.wmheight - ((CarLampSettingsActivity.this.mImageViewColourBackground.getTop() + this.y) + CarLampSettingsActivity.this.Width);
                StringBuilder sb = new StringBuilder();
                sb.append("--------wmwidth-->>");
                sb.append(CarLampSettingsActivity.this.wmwidth);
                String str = "CarLampSettingsActivity";
                Log.e(str, sb.toString());
                StringBuilder sb2 = new StringBuilder();
                sb2.append("--------wmheight-->>");
                sb2.append(CarLampSettingsActivity.this.wmheight);
                Log.e(str, sb2.toString());
                CarLampSettingsActivity.this.mImageViewRotundity.layout(left, top, access$1900, access$2000);
            }
            return true;
        }
    }

    private void analysisBluetooth(String str) {
    }

    /* access modifiers changed from: protected */
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        setContentView(R.layout.carlampsettings_activity);
        initViews();
        bindEvents();
        initData();
    }
    public void initViews() {
        this.mContext = this;
        this.colour_imi = (LinearLayout) findViewById(R.id.colour_imi);
        this.chromatic_circle = (ColorPickView) findViewById(R.id.chromatic_circle);
        this.car_lamp_settings_rollback = (Button) findViewById(R.id.car_lamp_settings_rollback);
        this.tv_pure_color = (TextView) findViewById(R.id.tv_pure_color);
        findViewById(R.id.iv_pure_color).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                QBlueToothManager.getInstance().write(BytesUtils.hexStringToBytes("FF551901016F"));
            }
        });
        findViewById(R.id.iv_rgb).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                QBlueToothManager.getInstance().write(BytesUtils.hexStringToBytes("FF5519010270"));
            }
        });
        this.cancel = (Button) findViewById(R.id.cancel);
        this.right_confirm = (Button) findViewById(R.id.right_confirm);
        this.linear_rollback = (LinearLayout) findViewById(R.id.linear_rollback);
        this.mImageViewColourBackground = (ImageView) findViewById(R.id.iv_colour_background);
        this.mImageViewRotundity = (ImageView) findViewById(R.id.iv_rotundity);
    }

    public void bindEvents() {
        this.car_lamp_settings_rollback.setOnClickListener(this);
        this.cancel.setOnClickListener(this);
        this.right_confirm.setOnClickListener(this);
        this.colour_imi.setOnClickListener(this);
        this.linear_rollback.setOnClickListener(this);
        this.bitmapBack = BitmapFactory.decodeResource(getResources(), R.drawable.chromatic_circle);
        this.mImageViewRotundity.setOnTouchListener(new ImageViewOnTouchListener());
        this.mImageViewColourBackground.setOnTouchListener(new mOnTouchListener());
        this.defaultColor = Color.parseColor("#FF0000");
        GradientDrawable gradientDrawable = (GradientDrawable) this.colour_imi.getBackground();
        gradientDrawable.setColor(this.defaultColor);
        gradientDrawable.setStroke(1, this.defaultColor);
        this.chromatic_circle.setOnColorChangedListener(new ColorPickView.OnColorChangedListener() {
            public void onColorChange(int i) {
                StringBuilder sb = new StringBuilder();
                sb.append("--------color-->>");
                sb.append(i);
                String str = "CarLampSettingsActivity";
                Log.e(str, sb.toString());
                CarLampSettingsActivity.this.fillColors = i;
                CarLampSettingsActivity.this.defaul = true;
                GradientDrawable gradientDrawable = (GradientDrawable) CarLampSettingsActivity.this.colour_imi.getBackground();
                gradientDrawable.setColor(i);
                gradientDrawable.setStroke(1, i);
                int red = Color.red(CarLampSettingsActivity.this.fillColors);
                int green = Color.green(CarLampSettingsActivity.this.fillColors);
                int blue = Color.blue(CarLampSettingsActivity.this.fillColors);
                StringBuilder sb2 = new StringBuilder();
                sb2.append("--------red-->>");
                sb2.append(red);
                Log.e(str, sb2.toString());
                StringBuilder sb3 = new StringBuilder();
                sb3.append("--------green-->>");
                sb3.append(green);
                Log.e(str, sb3.toString());
                StringBuilder sb4 = new StringBuilder();
                sb4.append("--------blue-->>");
                sb4.append(blue);
                Log.e(str, sb4.toString());
            }
        });
        WindowManager windowManager = getWindowManager();
        this.wmwidth = windowManager.getDefaultDisplay().getWidth();
        this.wmheight = windowManager.getDefaultDisplay().getHeight();
    }

    public void initData() {
        this.mImageViewRotundity.post(new Runnable() {
            public void run() {
                CarLampSettingsActivity carLampSettingsActivity = CarLampSettingsActivity.this;
                carLampSettingsActivity.Width = carLampSettingsActivity.mImageViewRotundity.getMeasuredWidth() / 2;
                CarLampSettingsActivity carLampSettingsActivity2 = CarLampSettingsActivity.this;
                carLampSettingsActivity2.controlwidth = carLampSettingsActivity2.mImageViewColourBackground.getMeasuredWidth() / 2;
                int[] iArr = new int[2];
                CarLampSettingsActivity.this.mImageViewRotundity.getLocationInWindow(iArr);
                CarLampSettingsActivity.this.X = iArr[0];
                CarLampSettingsActivity.this.Y = iArr[1];
                int[] iArr2 = new int[2];
                CarLampSettingsActivity.this.mImageViewColourBackground.getLocationInWindow(iArr2);
                CarLampSettingsActivity.this.conX = iArr2[0];
                CarLampSettingsActivity.this.conY = iArr2[1];
            }
        });
    }

    public void gainData(String str) {
        this.newCode2 = str;
        analysisBluetooth(str);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel /*2131296378*/:
                finish();
                return;
            case R.id.car_lamp_settings_rollback /*2131296384*/:
            case R.id.linear_rollback /*2131296580*/:
                finish();
                return;
            case R.id.right_confirm /*2131296722*/:
                this.isSuccess = true;
                if (!this.defaul) {
                    this.fillColors = this.defaultColor;
                }
                int red = Color.red(this.fillColors);
                int green = Color.green(this.fillColors);
                int blue = Color.blue(this.fillColors);
                String hexString = Integer.toHexString(red);
                String hexString2 = Integer.toHexString(green);
                String hexString3 = Integer.toHexString(blue);
                String str = "0";
                if (hexString.length() < 2) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(str);
                    sb.append(hexString);
                    hexString = sb.toString();
                }
                if (hexString2.length() < 2) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(str);
                    sb2.append(hexString2);
                    hexString2 = sb2.toString();
                }
                if (hexString3.length() < 2) {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(str);
                    sb3.append(hexString3);
                    hexString3 = sb3.toString();
                }
                String hexString4 = Integer.toHexString((((red + 364) + green) + blue) % 256);
                StringBuilder sb4 = new StringBuilder();
                sb4.append("FF551503");
                sb4.append(hexString);
                sb4.append(hexString2);
                sb4.append(hexString3);
                sb4.append(hexString4);
                this.srtbyte = BytesUtils.hexStringToBytes(sb4.toString());
                setChange();
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    public void setChange() {
        if (this.isSuccess) {
            QBlueToothManager.getInstance().write(this.srtbyte);
        }
    }

    private void lights() {
        if (this.a == 3) {
            this.a = 0;
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    CarLampSettingsActivity.this.hchata();
                }
            }, 1000);
        }
    }

    /* access modifiers changed from: private */
    public void hchata() {
        if (this.lights) {
            this.lights = false;
            return;
        }
        QBlueToothManager.getInstance().write(this.srtbyte);
        this.a++;
        lights();
    }
}
