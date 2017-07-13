/*
 * Posmate Httpd
 * Programmed by Jie Zhuang <jiezhuang.cn@gmail.com>
 * Copyright (c) Cynoware 2016-2020
 */


package com.cynoware.posmate.httpd;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cynoware.posmate.sdk.led.LED;
import com.cynoware.posmate.sdk.listener.ResultCallBack;


public class SettingFragment extends Fragment {

    private MainActivity mMainActivity;
    private LinearLayout mLayoutNP10, mLayoutNP11, mLayoutP140;
    private Drawable mDrawableNormal, mDrawableSelected;
    private LinearLayout mLayoutInternalPrinter, mLayoutScanner, mLayoutCDS;
    private CheckBox mCheckCDSInternal, mCheckCDSCOM1, mCheckCDSCOM2;
    private ProgressDialogHelper mProgressDialogHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();

        mProgressDialogHelper = new ProgressDialogHelper(mMainActivity);

        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        mDrawableNormal = ContextCompat.getDrawable(mMainActivity, R.drawable.posset_normal);
        mDrawableSelected = ContextCompat.getDrawable(mMainActivity, R.drawable.posset_selected);

        mLayoutNP10 = (LinearLayout) rootView.findViewById(R.id.layoutPosSetNP10);
        mLayoutNP11 = (LinearLayout) rootView.findViewById(R.id.layoutPosSetNP11);
        mLayoutP140 = (LinearLayout) rootView.findViewById(R.id.layoutPosSetP140);

        mLayoutInternalPrinter = (LinearLayout) rootView.findViewById(R.id.layoutInternalPrinter);
        mLayoutScanner = (LinearLayout) rootView.findViewById(R.id.layoutScanner);
        mLayoutCDS = (LinearLayout) rootView.findViewById(R.id.layoutCDS);


        mLayoutNP10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSuite(ServerService.SUITE_NP10);
            }
        });

        mLayoutNP11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSuite(ServerService.SUITE_NP11);
            }
        });

        mLayoutP140.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSuite(ServerService.SUITE_P140);
            }
        });

        Button btnTestPrinter = (Button) rootView.findViewById(R.id.btnTestInternalPrinter);
        btnTestPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerService service = mMainActivity.getServerService();
                if (service != null) {
                    service.print("OK!");
                }
            }
        });


        Button btnTestScanner = (Button) rootView.findViewById(R.id.btnTestScanner);
        btnTestScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialogHelper.showProgressDialog("Scanning...", new ProgressDialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        ServerService service = mMainActivity.getServerService();
                        if (service != null) {
                            service.closeScanner();
                        }
                    }
                });

                ServerService service = mMainActivity.getServerService();
                if (service != null) {

                    service.startScanner(new ResultCallBack() {
                        @Override
                        public void onFailed() {
                            mProgressDialogHelper.dismissProgressDialog();
                            Toast.makeText(mMainActivity, "FAIL", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onStrResult(String s) {
                            mProgressDialogHelper.dismissProgressDialog();
                            Toast.makeText(mMainActivity, s, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        mCheckCDSInternal = (CheckBox) rootView.findViewById(R.id.checkCDSInternal);
        mCheckCDSCOM1 = (CheckBox) rootView.findViewById(R.id.checkCDSCOM1);
        mCheckCDSCOM2 = (CheckBox) rootView.findViewById(R.id.checkCDSCOM2);

        ServerService service = mMainActivity.getServerService();
        if (service != null) {
            mCheckCDSInternal.setChecked(service.getCDS(ServerService.COM_0));
            mCheckCDSCOM1.setChecked(service.getCDS(ServerService.COM_1));
            mCheckCDSCOM2.setChecked(service.getCDS(ServerService.COM_2));
        }


        mCheckCDSInternal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ServerService service = mMainActivity.getServerService();
                if (service != null) {
                    service.setCDS(ServerService.COM_0, isChecked);
                }
            }
        });


        mCheckCDSCOM1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ServerService service = mMainActivity.getServerService();
                if (service != null) {
                    service.setCDS(ServerService.COM_1, isChecked);
                }
            }
        });


        mCheckCDSCOM2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ServerService service = mMainActivity.getServerService();
                if (service != null) {
                    service.setCDS(ServerService.COM_2, isChecked);
                }
            }
        });

        Button btnTestCDS = (Button) rootView.findViewById(R.id.btnTestCDS);
        btnTestCDS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerService service = mMainActivity.getServerService();
                if (service != null) {
                    service.showLED(LED.CMD_INIT_TYPE, "");
                    service.showLED(LED.CMD_PRICE_TYPE, "8888.88");
                }

            }
        });

        setSuite(mMainActivity.getServerService().getSuite());

        return rootView;
    }


    private void setSuite(int suite) {

        ServerService service = mMainActivity.getServerService();
        if (service == null) {
            Toast.makeText(mMainActivity, "Pos Suite Not Ready", Toast.LENGTH_SHORT).show();
            return;
        }

        service.setSuite(suite);

        if (suite == ServerService.SUITE_NP10) {
            mLayoutNP10.setBackground(mDrawableSelected);
            mLayoutNP11.setBackground(mDrawableNormal);
            mLayoutP140.setBackground(mDrawableNormal);

            mLayoutInternalPrinter.setVisibility(View.VISIBLE);
            mLayoutScanner.setVisibility(View.VISIBLE);

            mLayoutCDS.setVisibility(View.VISIBLE);
            mCheckCDSInternal.setVisibility(View.VISIBLE);
            mCheckCDSCOM1.setVisibility(View.VISIBLE);
            mCheckCDSCOM2.setVisibility(View.VISIBLE);

        } else if (suite == ServerService.SUITE_NP11) {
            mLayoutNP10.setBackground(mDrawableNormal);
            mLayoutNP11.setBackground(mDrawableSelected);
            mLayoutP140.setBackground(mDrawableNormal);

            mLayoutInternalPrinter.setVisibility(View.GONE);
            mLayoutScanner.setVisibility(View.VISIBLE);

            mLayoutCDS.setVisibility(View.VISIBLE);
            mCheckCDSInternal.setVisibility(View.VISIBLE);
            mCheckCDSCOM1.setVisibility(View.VISIBLE);
            mCheckCDSCOM2.setVisibility(View.GONE);


        } else if (suite == ServerService.SUITE_P140) {
            mLayoutNP10.setBackground(mDrawableNormal);
            mLayoutNP11.setBackground(mDrawableNormal);
            mLayoutP140.setBackground(mDrawableSelected);

            mLayoutInternalPrinter.setVisibility(View.GONE);
            mLayoutScanner.setVisibility(View.GONE);

            mLayoutCDS.setVisibility(View.VISIBLE);
            mCheckCDSInternal.setVisibility(View.GONE);
            mCheckCDSCOM1.setVisibility(View.VISIBLE);
            mCheckCDSCOM2.setVisibility(View.VISIBLE);
        }
    }
}
