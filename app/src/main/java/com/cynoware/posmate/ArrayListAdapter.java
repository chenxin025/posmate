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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ArrayListAdapter extends BaseAdapter {
	
	private class ViewHolder {
		TextView tvItem;
	}
	
	private Context mContext;
	private List<String> mList;
	
	public ArrayListAdapter(Context context) {
		mContext = context;
		mList = new ArrayList<String>();
	}

	public void add( String item, boolean isRefresh ){
		mList.add(item);
		
		if( isRefresh )
			notifyDataSetChanged();
	}
	
	public void removeAll(){
		mList.clear();
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
			convertView = LinearLayout.inflate(mContext, R.layout.item_simple_text, null);
			viewHolder.tvItem = (TextView) convertView.findViewById(R.id.tvItemSimple);
			convertView.setTag(viewHolder);
		}
		
		String item = mList.get(position);
		viewHolder.tvItem.setText( item );
				
		return convertView;
	}

	
}
