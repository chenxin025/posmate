package com.cynoware.webapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by chenxin on 2017/10/26.
 */

public class PrintConfigActivity extends Activity {

    private static final String TAG = "PrintConfigActivity";



    private EditText mEditPid;
    private EditText mEditVid;
    private Button   mBtnOk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this,PrintConfigActivity.class);
        setContentView(R.layout.printer_config);
        SharePreferenceUtil.getInstance().init(this);
        init();
    }

    private void init(){
        mEditPid = (EditText)findViewById(R.id.key_priter_pid);
        mEditVid = (EditText)findViewById(R.id.key_priter_vid);
        mBtnOk   = (Button)findViewById(R.id.key_printer_ok);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeConfigs();
            }
        });
        showConfigValues();

    }

    private void showConfigValues(){
        int pid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_PID,0);
        int vid = SharePreferenceUtil.getInstance().getInt(PrintConstants.KEY_SP_VID,0);
        if (pid == 0){
            mEditPid.setText("");
        }else{
            mEditPid.setText(""+pid);
        }

        if (vid == 0){
            mEditVid.setText("");
        }else{
            mEditVid.setText(""+vid);
        }
    }

    private void writeConfigs(){
        String strPid = mEditPid.getText().toString();
        String strVid = mEditVid.getText().toString();
        Log.i(TAG,"==writeConfigs===strPid:"+strPid+ "  strVid="+strVid);
        if (TextUtils.isEmpty(strPid) || TextUtils.isEmpty(strVid)){
            Toast.makeText(this,"请输入相关的值",Toast.LENGTH_SHORT).show();
            return;
        }

        SharePreferenceUtil.getInstance().putInt(PrintConstants.KEY_SP_PID,Integer.parseInt(strPid));
        SharePreferenceUtil.getInstance().putInt(PrintConstants.KEY_SP_VID,Integer.parseInt(strVid));
        Toast.makeText(this,"保存成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
