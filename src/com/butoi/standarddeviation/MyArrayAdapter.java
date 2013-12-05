package com.butoi.standarddeviation;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class MyArrayAdapter<T> extends ArrayAdapter<T> {
	
	private int layoutResource;
	private Context appContext;
	private DeleteButtonCallback callback;
	
	private static class ViewHolder {
		TextView listText;
		ImageButton deleteButton;
    }

	public MyArrayAdapter(Context context, int resource, List<T> objects) {
		super(context, resource, objects);
		layoutResource = resource;
		appContext = context;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder; // view lookup cache stored in tag
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(appContext).inflate(layoutResource, null);
			viewHolder.listText = (TextView) convertView.findViewById(R.id.listText);
			viewHolder.deleteButton = (ImageButton) convertView.findViewById(R.id.deleteButton);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.listText.setText(getItem(position).toString());
		viewHolder.deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callback.deletePressed(position);
			}
		});
		return convertView;
	}
	
	public interface DeleteButtonCallback {
		public void deletePressed(int position);
	}
	
	public void setCallback(DeleteButtonCallback callback) {
		this.callback = callback;
	}
}
