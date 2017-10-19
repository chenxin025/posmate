/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */

package com.cynoware.posmate.httpd;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by JieZhuang on 2017/4/10.
 */

public class ProgressDialogHelper {
    private Context mContext;
    private Dialog mProgressDlg = null;

    public ProgressDialogHelper(Context context ){
        mContext = context;
    }

    public void showProgressDialog(String message) {

        if (mProgressDlg != null)
            mProgressDlg.dismiss();

        mProgressDlg = ProgressDialog.show(mContext, null, message, true);
    }


    public void showProgressDialog(String message, ProgressDialog.OnCancelListener listener) {

        if (mProgressDlg != null)
            mProgressDlg.dismiss();

        mProgressDlg = ProgressDialog.show(mContext, null, message, true);
        mProgressDlg.setCancelable(true);
        mProgressDlg.setOnCancelListener(listener);
    }


    public void showProgressDialog(int messageResID, ProgressDialog.OnCancelListener listener) {

        if (mProgressDlg != null)
            mProgressDlg.dismiss();

        mProgressDlg = ProgressDialog.show(mContext, null, mContext.getString(messageResID), true);
        mProgressDlg.setCancelable(true);
        mProgressDlg.setOnCancelListener(listener);
    }

    public void dismissProgressDialog() {
        if (mProgressDlg != null) {
            mProgressDlg.dismiss();
            mProgressDlg = null;
        }
    }
}
