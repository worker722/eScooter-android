package com.tn.escooter.buletooth;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.tn.escooter.MyApplication;
import com.tn.escooter.R;
import com.tn.escooter.utils.BaseOnBluetoothListener;
import com.tn.escooter.utils.BytesUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CheckActivity extends Activity {
    private CheckItemAdapter adapter;
    private ObjectAnimator animator;
    /* access modifiers changed from: private */
    public List<CheckItemBean> checkItem;
    /* access modifiers changed from: private */
    public String currentData;
    /* access modifiers changed from: private */
    public MHandler handler;
    /* access modifiers changed from: private */
    public boolean haveResult = false;
    private ImageView imageResult;
    private ImageView imageViewProgressBar;
    private ScrollDisableListView listView;
    private BaseOnBluetoothListener onBluetoothListener;
    /* access modifiers changed from: private */
    public int percent;
    private List<Integer> resultListHua;
    private List<Integer> resultListNiu;
    private TextView textViewCheckAgain;
    private TextView textViewChecking;
    /* access modifiers changed from: private */
    public TextView textViewCheckingItem;
    private TextView textViewPersent;
    /* access modifiers changed from: private */
    public TextView textViewProcess;
    private TextView textview1;
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        setContentView(R.layout.activity_check);
        initViews();
        initData();
    }
    static class MHandler extends Handler {
        private WeakReference<CheckActivity> reference;

        public MHandler(CheckActivity checkActivity) {
            this.reference = new WeakReference<>(checkActivity);
        }

        public void handleMessage(Message message) {
            CheckActivity checkActivity = (CheckActivity) this.reference.get();
            if (checkActivity != null) {
                int i = message.what;
                if (i != 1) {
                    String str = "FF551E02000074";
                    if (i == 2) {
                        QBlueToothManager.getInstance().write(BytesUtils.hexStringToBytes(str));
                        checkActivity.percent = checkActivity.percent + 2;
                        if (checkActivity.percent >= 60) {
                            checkActivity.textViewCheckingItem.setText(((CheckItemBean) checkActivity.checkItem.get(2)).getName());
                            removeMessages(2);
                            sendEmptyMessageDelayed(4, 100);
                        } else {
                            sendEmptyMessageDelayed(2, 100);
                        }
                    } else if (i == 4) {
                        QBlueToothManager.getInstance().write(BytesUtils.hexStringToBytes(str));
                        checkActivity.percent = checkActivity.percent + 4;
                        if (checkActivity.percent < 100) {
                            sendEmptyMessageDelayed(4, 100);
                        } else if (checkActivity.haveResult) {
                            checkActivity.percent = 100;
                            removeMessages(4);
                            checkActivity.setResultView();
                        } else {
                            removeMessages(4);
                            checkActivity.setResultView();
                        }
                    } else if (i == 5) {
                        checkActivity.startCheck(true);
                    }
                } else {
                    checkActivity.percent = checkActivity.percent + 1;
                    if (checkActivity.percent == 20) {
                        checkActivity.textViewCheckingItem.setText(((CheckItemBean) checkActivity.checkItem.get(1)).getName());
                        removeMessages(1);
                        sendEmptyMessageDelayed(2, 100);
                    } else {
                        sendEmptyMessageDelayed(1, 100);
                    }
                }
                checkActivity.textViewProcess.setText(String.valueOf(checkActivity.percent));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initViews() {
        this.handler = new MHandler(this);
        initCheckItem(MyApplication.TYPE);
        this.textViewProcess = (TextView) findViewById(R.id.tv_percent);
        this.textViewPersent = (TextView) findViewById(R.id.tv_percent_2);
        this.textViewChecking = (TextView) findViewById(R.id.tv_checking);
        this.textview1 = (TextView) findViewById(R.id.textview1);
        this.textViewCheckAgain = (TextView) findViewById(R.id.tv_check_again);
        this.textViewCheckingItem = (TextView) findViewById(R.id.tv_checking_item);
        this.listView = (ScrollDisableListView) findViewById(R.id.list_view);
        this.imageResult = (ImageView) findViewById(R.id.iv_result);
        this.imageViewProgressBar = (ImageView) findViewById(R.id.iv_progress);

        this.adapter = new CheckItemAdapter(this, this.listView);
        this.listView.setAdapter(this.adapter);
        this.adapter.setDatas(this.checkItem);
        findViewById(R.id.lay_back).setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                finish();
            }
        });
        this.textViewCheckAgain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CheckActivity.this.currentData = null;
                CheckActivity.this.startCheck(true);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        this.handler.sendEmptyMessageDelayed(5, 500);
    }

    /* access modifiers changed from: protected */
    public void initData() {
        this.onBluetoothListener = new BaseOnBluetoothListener() {
            public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                if (bluetoothGattCharacteristic.getValue() != null && bluetoothGattCharacteristic.getValue().length > 0) {
                    String bytesToHexStringTwo = BytesUtils.bytesToHexStringTwo(bluetoothGattCharacteristic.getValue(), bluetoothGattCharacteristic.getValue().length);
                    if (bytesToHexStringTwo.length() > 10) {
                        if ("FF551E".equals(bytesToHexStringTwo.substring(0, 6))) {
                            CheckActivity.this.currentData = bytesToHexStringTwo.substring(0, 14);
                            CheckActivity.this.handler.post(new Runnable() {
                                public void run() {
                                    CheckActivity.this.startCheck(false);
                                }
                            });
                        }
                    }
                }
            }
        };
        QBlueToothManager.getInstance().addBluetoothListener(this.onBluetoothListener);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        QBlueToothManager.getInstance().removeBluetoothListener(this.onBluetoothListener);
    }

    private void initCheckItem(int i) {
        if (i == 1) {
            this.checkItem = new ArrayList();
            this.checkItem.add(new CheckItemBean(getString(R.string.car_scooter)));
            this.checkItem.add(new CheckItemBean(getString(R.string.car_brake)));
            this.checkItem.add(new CheckItemBean(getString(R.string.holl_sensor)));
            this.checkItem.add(new CheckItemBean(getString(R.string.hardware)));
            this.checkItem.add(new CheckItemBean(getString(R.string.self_check02)));
            return;
        }
        this.checkItem = new ArrayList();
        this.checkItem.add(new CheckItemBean(getString(R.string.hardware)));
        this.checkItem.add(new CheckItemBean(getString(R.string.holl_sensor)));
        this.checkItem.add(new CheckItemBean(getString(R.string.commu_devices)));
        this.checkItem.add(new CheckItemBean(getString(R.string.self_check02)));
        this.checkItem.add(new CheckItemBean(getString(R.string.circurt_board)));
    }

    /* access modifiers changed from: private */
    public void startCheck(boolean z) {
        if (z || TextUtils.isEmpty(this.currentData)) {
            this.percent = 0;
            this.resultListHua = null;
            this.resultListNiu = null;
            this.haveResult = false;
            QBlueToothManager.getInstance().write(BytesUtils.hexStringToBytes("FF551E02000074"));
            ObjectAnimator objectAnimator = this.animator;
            if (objectAnimator != null && objectAnimator.isRunning()) {
                this.animator.end();
            }
            this.animator = ObjectAnimator.ofFloat(this.imageViewProgressBar, "rotation", new float[]{0.0f, 360.0f});
            this.animator.setRepeatCount(-1);
            this.animator.setInterpolator(new LinearInterpolator());
            this.animator.setDuration(2000);
            this.animator.start();
            this.handler.sendEmptyMessageDelayed(1, 200);
            resetView();
            return;
        }
        analyseData(this.currentData);
    }

    private void resetView() {
        this.textViewCheckAgain.setVisibility(View.GONE);
        this.textViewCheckingItem.setVisibility(View.VISIBLE);
        this.textViewCheckingItem.setText(((CheckItemBean) this.checkItem.get(0)).getName());
        this.textViewChecking.setText(getString(R.string.checking));
        this.textViewProcess.setVisibility(View.VISIBLE);
        this.textViewPersent.setVisibility(View.VISIBLE);
        this.imageResult.setVisibility(View.GONE);
        this.imageViewProgressBar.setImageResource(R.drawable.bg_self_check_yellow);
        if (this.checkItem.size() > 0) {
            for (CheckItemBean checkItemBean : this.checkItem) {
                checkItemBean.setBrokenDown(false);
                checkItemBean.setStatus(0);
            }
            this.adapter.notifyDataSetChanged();
        }
    }

    /* access modifiers changed from: private */
    public void setResultView() {
        if (MyApplication.TYPE == 1) {
            List<CheckItemBean> list = this.checkItem;
            if (list != null && list.size() > 0) {
                for (int i = 0; i < this.checkItem.size(); i++) {
                    if (i == 4) {
                        ((CheckItemBean) this.checkItem.get(i)).setBrokenDown(isBrokenDown(i, MyApplication.TYPE) || isBrokenDown(i + 1, MyApplication.TYPE));
                    } else {
                        ((CheckItemBean) this.checkItem.get(i)).setBrokenDown(isBrokenDown(i, MyApplication.TYPE));
                    }
                }
            }
        } else {
            List<CheckItemBean> list2 = this.checkItem;
            if (list2 != null && list2.size() > 0) {
                for (int i2 = 0; i2 < this.checkItem.size(); i2++) {
                    if (i2 == 0) {
                        ((CheckItemBean) this.checkItem.get(i2)).setBrokenDown(isBrokenDown(0, MyApplication.TYPE) || isBrokenDown(1, MyApplication.TYPE));
                    } else if (i2 == 1) {
                        ((CheckItemBean) this.checkItem.get(i2)).setBrokenDown(isBrokenDown(2, MyApplication.TYPE) || isBrokenDown(3, MyApplication.TYPE));
                    } else if (i2 == 2) {
                        ((CheckItemBean) this.checkItem.get(i2)).setBrokenDown(isBrokenDown(4, MyApplication.TYPE));
                    } else if (i2 == 3) {
                        ((CheckItemBean) this.checkItem.get(i2)).setBrokenDown(isBrokenDown(5, MyApplication.TYPE) || isBrokenDown(8, MyApplication.TYPE));
                    } else if (i2 == 4) {
                        ((CheckItemBean) this.checkItem.get(i2)).setBrokenDown(isBrokenDown(6, MyApplication.TYPE) || isBrokenDown(7, MyApplication.TYPE));
                    }
                }
            }
        }
        this.adapter.setDatas(this.checkItem);
        this.textViewCheckingItem.setVisibility(View.GONE);
        this.textViewCheckAgain.setVisibility(View.VISIBLE);
        int brokenCount = this.adapter.getBrokenCount();
        if (this.animator != null) {
            if (Build.VERSION.SDK_INT >= 19) {
                this.animator.pause();
            }
            this.animator.cancel();
            this.animator.end();
        }
        if (brokenCount == 0) {
            this.imageResult.setVisibility(View.VISIBLE);
            this.imageResult.setImageResource(R.drawable.icon_self_check_regular_1);
            this.textview1.setVisibility(View.GONE);
            this.textViewPersent.setVisibility(View.GONE);
            this.textViewProcess.setVisibility(View.GONE);
            this.textViewChecking.setText(R.string.all_right);
            this.imageViewProgressBar.setImageResource(R.drawable.bg_self_check_yellow);
            return;
        }
        this.imageResult.setImageResource(R.drawable.icon_self_check_fault_1);
        this.imageResult.setVisibility(View.VISIBLE);
        this.textview1.setVisibility(View.VISIBLE);
        this.textViewPersent.setVisibility(View.GONE);
        this.textViewProcess.setVisibility(View.GONE);
        TextView textView = this.textViewChecking;
        StringBuilder sb = new StringBuilder();
        sb.append(brokenCount);
        sb.append(getString(R.string.fault));
        textView.setText(sb.toString());
        this.imageViewProgressBar.setImageResource(R.drawable.bg_self_check_red);
    }

    private boolean isBrokenDown(int i, int i2) {
        boolean z = true;
        if (i2 == 2) {
            List<Integer> list = this.resultListHua;
            if (list == null || list.size() <= i) {
                return false;
            }
            if (((Integer) this.resultListHua.get(i)).intValue() != 1) {
                z = false;
            }
            return z;
        }
        List<Integer> list2 = this.resultListNiu;
        if (list2 == null || list2.size() <= i) {
            return false;
        }
        if (((Integer) this.resultListNiu.get(i)).intValue() != 1) {
            z = false;
        }
        return z;
    }

    private void analyseData(String str) {
        this.resultListHua = new ArrayList();
        this.resultListNiu = new ArrayList();
        char c = 0;
        String substring = str.substring(0, 6);
        StringBuilder sb = new StringBuilder();
        sb.append(substring);
        sb.append("     ");
        sb.append(str);
        Log.d("CyclingFragment1", sb.toString());
        if (substring.hashCode() != 2070318484 || !substring.equals("FF551E")) {
            c = 65535;
        }
        if (c == 0) {
            this.haveResult = true;
            String binaryString = Integer.toBinaryString(Integer.valueOf(str.substring(8, str.length() - 2), 16).intValue());
            if (MyApplication.TYPE == 1) {
                for (int length = binaryString.length() - 1; length >= 0; length--) {
                    this.resultListNiu.add(Integer.valueOf(Integer.parseInt(binaryString.substring(length, length + 1))));
                }
                return;
            }
            for (int length2 = binaryString.length() - 1; length2 >= 0; length2--) {
                this.resultListHua.add(Integer.valueOf(Integer.parseInt(binaryString.substring(length2, length2 + 1))));
            }
        }
    }
}