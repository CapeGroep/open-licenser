package org.capegroep.licensemanager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

public class License {
    private final ArrayList<String> appUrls;
    private final ArrayList<String> services;
    private final Date licenseExpirationDate;
    private final String customerName;
    License(DecodedJWT jwt) {
        try {
            JSONObject payload = JSON.parseObject(new String(Base64.getDecoder().decode(jwt.getPayload())));
            long licenseExpirationDate = payload.getLong("expirationdate");
            this.licenseExpirationDate = new Date(licenseExpirationDate * 1000L);
            this.appUrls = getAppUrlsArray(payload.getJSONArray("appurls"));
            this.services = getServicesArray(payload.getJSONArray("services"));
            this.customerName = payload.getString("customername");
        }catch (Exception e) {
            throw new RuntimeException("Unexpected data structure when parsing license from token",e);
        }
    }
    public ArrayList<String> getAppUrls() {
        return appUrls;
    }
    public ArrayList<String> getServices() {
        return services;
    }

    @SuppressWarnings("unused")
    public String getCustomerName() {
        return customerName;
    }

    public Date getLicenseExpirationDate() {
        return licenseExpirationDate;
    }
    private  ArrayList<String> getServicesArray(JSONArray payload){
        ArrayList<String> arr = new ArrayList<>();
        for (int i = 0; i < payload.size(); i++) {
            JSONObject kv = payload.getJSONObject(i);
            arr.add(kv.getString("serviceValue"));
        }
        return arr;
    }
    private  ArrayList<String> getAppUrlsArray(JSONArray payload){
        ArrayList<String> arr = new ArrayList<>();
        for (int i = 0; i < payload.size(); i++) {
            JSONObject kv = payload.getJSONObject(i);
            arr.add(kv.getString("URL"));
        }
        return arr;
    }
}
