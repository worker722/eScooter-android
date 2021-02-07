package com.tn.escooter.buletooth;

public class CheckItemBean {
    private boolean brokenDown;
    private String name;
    private int status;

    public CheckItemBean() {
    }

    public CheckItemBean(String str) {
        this.name = str;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public boolean isBrokenDown() {
        return this.brokenDown;
    }

    public void setBrokenDown(boolean z) {
        this.status = 1;
        this.brokenDown = z;
    }

    public void setStatus(int i) {
        this.status = i;
    }

    public boolean isChecking() {
        return this.status != 1;
    }
}
