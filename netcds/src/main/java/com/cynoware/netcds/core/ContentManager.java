/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.netcds.core;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ContentManager {

    private static ContentManager mInstance;
    private String mRoot;
    private Map<Integer, String> mContentMap;
    private Resources mResources;

    private ContentManager() {
        mContentMap = new HashMap<>();
    }

    public static ContentManager getInstance() {
        if (mInstance == null)
            mInstance = new ContentManager();

        return mInstance;
    }

    public void init(Resources resources) {
        mResources = resources;
    }

    public String getContent(int resID) {
        String content = mContentMap.get(resID);
        if (content == null) {
            content = load(resID);
        }

        return content;
    }


    private String load(int resID) {
        try {
            InputStreamReader inputReader = new InputStreamReader(mResources.openRawResource(resID));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
