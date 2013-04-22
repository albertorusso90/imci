package com.burkina.activities;

import java.util.ArrayList;
import java.util.List;
import com.burkina.containers.ZoneData;
import com.burkina.functions.DatabaseHandler;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ZoneSelector extends ListActivity implements TextWatcher{
	
	private zoneAdapter myAdapter;
	private EditText search;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		
		myAdapter = new zoneAdapter(this);
		
		search = (EditText)findViewById(R.id.editSearch);
		search.setHint(getString(R.string.filter_village));
		search.addTextChangedListener(this);
		search("");
		
		TextView textEmpty = (TextView)findViewById(R.id.empty);
		textEmpty.setText(getString(R.string.empty_village));
		
		getListView().setEmptyView(findViewById(R.id.empty));
		setListAdapter(myAdapter);
		getListView().setCacheColorHint(0);
	}
	
	public void search(String temp){
		DatabaseHandler db = new DatabaseHandler(this);
		Cursor cursor = db.query("zones", null, "village='t' AND name like ?", new String[]{temp + "%"});
		if(cursor.moveToFirst()){
			do {
				int id = Integer.parseInt(cursor.getString(0));
				int p_id = Integer.parseInt(cursor.getString(1));
				String name = cursor.getString(4);
				myAdapter.addItem(new ZoneData(id, p_id, name));
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id2) {		
		int id = myAdapter.getID(position);
		int p_id = myAdapter.getParentID(position);
		String name = myAdapter.getName(position);
		
		displayDialog(name, id, p_id);
	}
	
	public void displayDialog(final String name, final int id, final int p_id){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to set " + name + " as your location center")
				.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent returnIntent = new Intent();
						returnIntent.putExtra("location_name", name);
						returnIntent.putExtra("location_id", id);
						returnIntent.putExtra("location_pid", p_id);
						
						setResult(RESULT_OK, returnIntent);     
						finish();
					}
				}) 
				.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setResult(RESULT_CANCELED);
						finish();
					}
				});
		builder.create().show();
	}
	
	public class zoneAdapter extends BaseAdapter{
		
		private Context context;
		private List<ZoneData> item = new ArrayList<ZoneData>();
		
		public zoneAdapter(Context context){
			this.context = context;
		}
		
		public void addItem(ZoneData zone){
			item.add(zone);
			notifyDataSetChanged();
		}
		
		public void clear(){
			item.clear();
			notifyDataSetChanged();
		}
		
		public Integer getID(int position){
			return item.get(position).getID();
		}
		
		public Integer getParentID(int position){
			return item.get(position).getParentID();
		}
		
		public String getName(int position){
			return item.get(position).getName();
		}
		
		@Override
		public int getCount() {
			return item.size();
		}

		@Override
		public Object getItem(int position) {
			return item.get(position).getName();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowview = inflater.inflate(R.layout.list_item, parent, false);
			
			TextView label = (TextView)rowview.findViewById(R.id.label);
			label.setText(item.get(position).getName());	
			
			int[] imageArr = {R.drawable.arrow_red, R.drawable.arrow_green, R.drawable.arrow_gray};
			
			ImageView img = (ImageView)rowview.findViewById(R.id.imageSelector);
			img.setImageResource(imageArr[position%imageArr.length]);
			
			return rowview;
		}		
	}

	@Override
	public void afterTextChanged(Editable s) {}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String temp = search.getText().toString();
		
		myAdapter.clear();
		if(temp.length() != 0) search(temp);
		else search("");
	}

}
