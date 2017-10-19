/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */


package com.cynoware.posmate.httpd;

import android.content.Intent;

import com.cynoware.posmate.sdk.util.SharePrefManager;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SharePrefManager.getInstance().init(this);

        // Start POS server service.
        Intent intent = new Intent(this, ServerService.class);
        startService(intent);
    }
}
