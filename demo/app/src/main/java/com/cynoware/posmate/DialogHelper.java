package com.cynoware.posmate;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;

public class DialogHelper {

	private Resources mRes;
	private AlertDialog mDialog;
	private View mContentView;
	
	private DialogInterface.OnClickListener mPositiveClickListener;
	private DialogInterface.OnClickListener mNegativeClickListener;
	private OnCancelListener mOnCancelListener = null;
	private OnDismissListener mOnDismissListener = null;
	private OnKeyListener mOnKeyListener = null;
	
	private String mTitle;
	private String mMessage;
	private String mPositiveText;
	private String mNegativeText;
	
	public DialogHelper(Context context) {
		mRes = context.getResources();
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public void setTitle(int titleResId) {
		mTitle = mRes.getString(titleResId);
	}
	
	public void setMessage(String msg) {
		mMessage = msg;
	}
	
	public void setMessage(int msgResId) {
		mMessage = mRes.getString(msgResId);
	}
	
	public void setPositiveButton(String text, DialogInterface.OnClickListener listener) {
		mPositiveText = text;
		mPositiveClickListener = listener;
	}
	
	public void setPositiveButton(int textResID, DialogInterface.OnClickListener listener) {
		mPositiveText = mRes.getString(textResID);
		mPositiveClickListener = listener;
	}
	
	public void setNegativeButton(String text, DialogInterface.OnClickListener listener) {
		mNegativeText = text;
		mNegativeClickListener = listener;
	}
	
	public void setNegativeButton(int textResID, DialogInterface.OnClickListener listener) {
		mNegativeText = mRes.getString(textResID);
		mNegativeClickListener = listener;
	}
	
	public void setView(View view) {
		mContentView = view;
	}
	
	public void showDialog(Context context) {
		
		Builder builder = new Builder(context);
		if(!TextUtils.isEmpty(mTitle)){
			builder.setTitle(mTitle);
		}

		if(!TextUtils.isEmpty(mMessage)){
			builder.setMessage(mMessage);
		}

		if(null != mContentView){
			builder.setView(mContentView);
		}

		if (!TextUtils.isEmpty(mPositiveText)) {
			builder.setPositiveButton(mPositiveText, mPositiveClickListener);
		}
		if (!TextUtils.isEmpty(mNegativeText)) {
			builder.setNegativeButton(mNegativeText, mNegativeClickListener);
		}
		
		if (mOnCancelListener != null){
			builder.setOnCancelListener(mOnCancelListener);
		}
		
		if (mOnDismissListener != null) {
			builder.setOnDismissListener(mOnDismissListener);
		}
		
		if (mOnKeyListener != null) {
			builder.setOnKeyListener(mOnKeyListener);
		}
		
		mDialog = builder.create();
		
		mDialog.show();
	}
	
	public void setOnKeyListener(OnKeyListener listener){
		mOnKeyListener = listener;
		if (mOnKeyListener != null && mDialog != null) {
			mDialog.setOnKeyListener(mOnKeyListener);
		}
	}
	
	public void setCancelListenter(OnCancelListener listener){
		mOnCancelListener = listener;
		if (mOnCancelListener != null && mDialog != null) {
			mDialog.setOnCancelListener(mOnCancelListener);
		}
	}
	
	public void setCancelable(boolean isCancelable) {
		mDialog.setCancelable(isCancelable);
	}
	
	public void setDismissListener(OnDismissListener dismissListener) {
		mOnDismissListener = dismissListener;
		if (mOnDismissListener != null && mDialog != null) {
			mDialog.setOnDismissListener(mOnDismissListener);
		}
	}
	
	public boolean isShowing() {
		return (mDialog != null && mDialog.isShowing());
	}
	
	public void dismiss() {
		if (isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
	}
}
