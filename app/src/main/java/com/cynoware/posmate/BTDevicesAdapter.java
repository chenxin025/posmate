/**
 * POSMATE ANDROID
 * 
 * COPYRIGHT (C) CYNOWARE CO.,LTD
 * 
 * VERSION 1.1.0, 20160503, Jie Zhuang
 * 
 */

package com.cynoware.posmate;

import java.util.ArrayList;
import java.util.List;

import com.cynoware.posmate.R;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 蓝牙搜索列表适配器
 * 
 * @ClassName BlueToothAdapter
 */
public class BTDevicesAdapter extends BaseAdapter {
	
	private class ViewHolder {
		TextView tvName;
		TextView tvAddr;
		ImageView imgUnpair;
	}
	
	public interface OnItemClickListener {
		void onClick(int position);
	}

	private Context mContext;
	private List<BluetoothDevice> mList;
	private OnItemClickListener mClickListener;
	private boolean mIsRemovedButton = true;
	
	public BTDevicesAdapter(Context context) {
		mContext = context;
		mList = new ArrayList<BluetoothDevice>();
	}
	
	public BTDevicesAdapter(Context context, boolean isRemoveButton ) {
		mIsRemovedButton = isRemoveButton;
		mContext = context;
		mList = new ArrayList<BluetoothDevice>();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mClickListener = listener;
	}
	
	public void add( BluetoothDevice item, boolean isRefresh ){
		mList.add(item);
		
		if( isRefresh )
			notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void removeItem( int position, boolean refresh ){
		if( position < 0 || position > mList.size() )
			return;
		
		mList.remove( position );
		
		if( refresh )
			notifyDataSetChanged();
		
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder;
		
		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = LinearLayout.inflate(mContext, R.layout.item_bt_device, null);
			viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvBTName);
			viewHolder.tvAddr = (TextView) convertView.findViewById(R.id.tvBTAddr);
			viewHolder.imgUnpair = (ImageView) convertView.findViewById(R.id.imgUnpair);
			
			convertView.setTag(viewHolder);
		}
		
		BluetoothDevice item = mList.get(position);
		if( item != null ){
			String name = item.getName();
			String addr = item.getAddress();
			
			if( name == null || name.isEmpty() )
				name = addr;
			
			viewHolder.tvName.setText( name );
			viewHolder.tvAddr.setText( addr );
		}
		
		if( mIsRemovedButton ){		
			viewHolder.imgUnpair.setVisibility( View.VISIBLE );
			viewHolder.imgUnpair.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mClickListener.onClick(position);
				}
			});
		}else{
			viewHolder.imgUnpair.setVisibility( View.INVISIBLE );
		}
			
		return convertView;
	}

	
}
