package com.tn.escooter.buletooth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import android.widget.ListView;
public class ScrollDisableListView extends ListView {
    private int mPosition;

    public ScrollDisableListView(Context context) {
        super(context);
    }

    public ScrollDisableListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ScrollDisableListView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked() & 255;
        if (actionMasked == 0) {
            this.mPosition = pointToPosition((int) motionEvent.getX(), (int) motionEvent.getY());
            return super.dispatchTouchEvent(motionEvent);
        } else if (actionMasked == 2) {
            return true;
        } else {
            if (actionMasked == 1 || actionMasked == 3) {
                if (pointToPosition((int) motionEvent.getX(), (int) motionEvent.getY()) == this.mPosition) {
                    super.dispatchTouchEvent(motionEvent);
                } else {
                    setPressed(false);
                    invalidate();
                    return true;
                }
            }
            return super.dispatchTouchEvent(motionEvent);
        }
    }

    public void setHeightOnChildren() {
        ListAdapter adapter = getAdapter();
        if (adapter != null) {
            int count = adapter.getCount();
            int i = 0;
            for (int i2 = 0; i2 < count; i2++) {
                View view = adapter.getView(i2, null, this);
                view.measure(0, 0);
                i += view.getMeasuredHeight();
            }
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.height = i + (getDividerHeight() * (adapter.getCount() - 1));
            setLayoutParams(layoutParams);
        }
    }
}
