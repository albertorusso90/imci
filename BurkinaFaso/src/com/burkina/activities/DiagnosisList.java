package com.burkina.activities;

import java.util.ArrayList;
import java.util.List;

import com.burkina.functions.DatabaseHandler;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DiagnosisList extends ListActivity{
	
	
	private DiagnoseAdapter myAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
		myAdapter = new DiagnoseAdapter(this);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		int child_id = extras.getInt("id");
		
		DatabaseHandler db = new DatabaseHandler(this);
		Cursor cursor = db.query("diagnostics", null, "child_global_id=? AND state=?", 
				new String[]{String.valueOf(child_id), "close"});
		
		if(cursor.moveToFirst()){
			do {
				int id = Integer.parseInt(cursor.getString(0));
				String time = cursor.getString(11);
				
				myAdapter.addItem(new ItemData(id, time));
			} while (cursor.moveToNext());
		}
		
		TextView textEmpty = (TextView)findViewById(R.id.empty);
		textEmpty.setText(R.string.empty_diagnose);
		
		getListView().setEmptyView(findViewById(R.id.empty));
		setListAdapter(myAdapter);
		getListView().setCacheColorHint(0);	
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long arg1) {
		int id = myAdapter.getID(position);
		Intent intent = new Intent(getApplication(), DiagnosisResults.class);
		intent.putExtra("diagnostics_id", Long.valueOf(id));
		startActivity(intent);
	}
	
	public class DiagnoseAdapter extends BaseAdapter {

		public List<ItemData> data = new ArrayList<ItemData>();
		Context context;

		public DiagnoseAdapter(Context context) {
			this.context = context;
		}

		public void addItem(ItemData item) {
			data.add(item);
			notifyDataSetChanged();
		}
		
		public Integer getID(int position){
			return data.get(position).getID();
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowview = inflater.inflate(R.layout.list_item, parent, false);

			TextView label = (TextView) rowview.findViewById(R.id.label);
			label.setText(data.get(position).getTime());
			
			int[] imageArr = {R.drawable.arrow_red, R.drawable.arrow_green, R.drawable.arrow_gray};
			
			ImageView img = (ImageView)rowview.findViewById(R.id.imageSelector);
			img.setImageResource(imageArr[position%imageArr.length]);

			return rowview;
		}
	}

	public class ItemData{
		int id;
		String time;
		
		public ItemData(int id, String time){
			this.id = id;
			this.time = time;
		}
		
		public Integer getID(){
			return id;
		}
		
		public String getTime(){
			return time;
		}
	}
}
