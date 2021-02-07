package com.tn.escooter.buletooth;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tn.escooter.R;
import com.tn.escooter.utils.MapSearchResultItem;

import java.util.ArrayList;

public class MyListViewAdapter extends ArrayAdapter<MapSearchResultItem> {
    private Context mContext;
    private ArrayList<MapSearchResultItem> mapSearchResultItems;
    private int viewResourceId ;

    public class ViewHolder {
        public TextView map_search_navage_detail;
        public TextView map_search_navage_title;

        public ViewHolder() {
        }
    }
    public MyListViewAdapter(Context context, int viewResourceId , ArrayList<MapSearchResultItem> mapSearchResultItems2) {
        super(context, viewResourceId );
        this.mapSearchResultItems = mapSearchResultItems2;
        this.mContext = context;
        this.viewResourceId = viewResourceId;
    }

    public int getCount() {
        return this.mapSearchResultItems.size();
    }

    public MapSearchResultItem getItem(int position) {
        return this.mapSearchResultItems.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (0 == 0) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(viewResourceId, null);
            viewHolder.map_search_navage_detail = (TextView) convertView.findViewById(R.id.map_search_navage_detail);
            viewHolder.map_search_navage_title = (TextView) convertView.findViewById(R.id.map_search_navage_title);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MapSearchResultItem mapSearchResultItem = (MapSearchResultItem) this.mapSearchResultItems.get(position);
        String address_detail = mapSearchResultItem.getAddress_detail();
        String address_title = mapSearchResultItem.getAddress_title();
        String address = mapSearchResultItem.getSearch_address();
        if (TextUtils.isEmpty(address_detail)) {
            viewHolder.map_search_navage_detail.setVisibility(View.GONE);
        }
        if (address_title.equals(address)) {
            viewHolder.map_search_navage_title.setTextColor(this.mContext.getColor(R.color.primary_color));
        } else {
            viewHolder.map_search_navage_title.setTextColor(this.mContext.getColor(R.color.text_gray));
        }
        viewHolder.map_search_navage_detail.setText(address_detail);
        viewHolder.map_search_navage_title.setText(address_title);
        return convertView;
    }
}