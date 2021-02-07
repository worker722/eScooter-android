package com.tn.escooter.buletooth;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tn.escooter.R;

import java.util.List;

public class CheckItemAdapter extends BaseAdapter {
    private Context context;
    private List<CheckItemBean> datas;
    private ScrollDisableListView listView;

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return 0;
    }

    public CheckItemAdapter(Context context2, ScrollDisableListView scrollDisableListView) {
        this.context = context2;
        this.listView = scrollDisableListView;
    }

    public void setDatas(List<CheckItemBean> list) {
        this.datas = list;
        notifyDataSetChanged();
    }

    public int getBrokenCount() {
        int i = 0;
        if (getCount() > 0) {
            for (CheckItemBean isBrokenDown : this.datas) {
                if (isBrokenDown.isBrokenDown()) {
                    i++;
                }
            }
        }
        return i;
    }

    public int getCount() {
        List<CheckItemBean> list = this.datas;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        CheckItemBean checkItemBean = (CheckItemBean) this.datas.get(i);
        View inflate = LayoutInflater.from(this.context).inflate(R.layout.item_check_car, null);
        TextView textView = (TextView) inflate.findViewById(R.id.tv_result);
        ImageView imageView = (ImageView) inflate.findViewById(R.id.iv_result);
        ProgressBar progressBar = (ProgressBar) inflate.findViewById(R.id.progress_bar);
        ((TextView) inflate.findViewById(R.id.tv_name)).setText(checkItemBean.getName());
        if (checkItemBean.isChecking()) {
            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            if (checkItemBean.isBrokenDown()) {
                textView.setText(R.string.fault);
                textView.setTextColor(Color.parseColor("#ff0000"));
                imageView.setImageResource(R.drawable.icon_self_check_fault_1);
            } else {
                textView.setText(R.string.normal);
                textView.setTextColor(Color.parseColor("#FF007F"));
                imageView.setImageResource(R.drawable.icon_self_check_regular_1);
            }
        }
        return inflate;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        ScrollDisableListView scrollDisableListView = this.listView;
        if (scrollDisableListView != null) {
            scrollDisableListView.setHeightOnChildren();
        }
    }
}
