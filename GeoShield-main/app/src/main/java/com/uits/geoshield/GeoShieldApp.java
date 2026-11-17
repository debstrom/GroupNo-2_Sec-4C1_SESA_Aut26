package com.uits.geoshield;

import android.app.Application;

import com.uits.geoshield.utils.ContactRepository;

public class GeoShieldApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ContactRepository.init(this);
    }
}