package com.tn.escooter.utils;

public class MapSearchResultItem {
    private String address_detail;
    private String address_title;
    private String place_id;
    private String search_address;

    public String getSearch_address() {
        return this.search_address;
    }

    public void setSearch_address(String search_address2) {
        this.search_address = search_address2;
    }

    public MapSearchResultItem(String address_title2, String address_detail2, String place_id2, String search_address2) {
        this.address_title = address_title2;
        this.address_detail = address_detail2;
        this.place_id = place_id2;
        this.search_address = search_address2;
    }

    public String getAddress_title() {
        return this.address_title;
    }

    public void setAddress_title(String address_title2) {
        this.address_title = address_title2;
    }

    public String getAddress_detail() {
        return this.address_detail;
    }

    public void setAddress_detail(String address_detail2) {
        this.address_detail = address_detail2;
    }

    public String getPlace_id() {
        return this.place_id;
    }

    public void setPlace_id(String place_id2) {
        this.place_id = place_id2;
    }
}
