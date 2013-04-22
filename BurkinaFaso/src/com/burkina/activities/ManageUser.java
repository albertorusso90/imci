package com.burkina.activities;

import java.util.ArrayList;
import java.util.List;
import com.burkina.functions.DatabaseHandler;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ManageUser extends ListActivity{
	
	private static final int REQUEST_CODE_USER = 1084;
	private UserAdapter myAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
		myAdapter = new UserAdapter(this);
		search();
		
		setListAdapter(myAdapter);
		getListView().setCacheColorHint(0);
		registerForContextMenu(getListView());
	}
	
	public void search(){
		DatabaseHandler db = new DatabaseHandler(this);
		
		Cursor cursor = db.query("users", null, null, null);
		if(cursor.moveToFirst()){
			do {
				int id = Integer.parseInt(cursor.getString(0));
				String name = cursor.getString(1);
				
				myAdapter.addItem(new UserData(id, name));
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long arg0) {
		int id = myAdapter.getUserID(position);
		
		Intent intent = new Intent(getApplication(), Registration.class);
		intent.putExtra("type", "edit");
		intent.putExtra("id", id);
		startActivityForResult(intent, REQUEST_CODE_USER);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == REQUEST_CODE_USER){
				myAdapter.clear();
				search();
			}
		}
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		menu.removeItem(R.id.edit);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.remove:
			removeUser(myAdapter.getUserID(info.position));
			myAdapter.clear();
			search();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	public void removeUser(int id){
		DatabaseHandler db = new DatabaseHandler(this);
		db.delete("users", "_id =?", new String[]{String.valueOf(id)});
	}
	
	public class UserAdapter extends BaseAdapter{

		public List<UserData> item = new ArrayList<UserData>();
		Context context;
		
		public UserAdapter(Context context){
			this.context = context;
		}
		
		public void addItem(UserData data){
			item.add(data);
			notifyDataSetChanged();
		}
		
		public void clear(){
			item.clear();
			notifyDataSetChanged();
		}
		
		public Integer getUserID(int position){
			return item.get(position).getID();
		}
		
		public String getUserName(int position){
			return item.get(position).getName();
		}
		
		@Override
		public int getCount() {
			return item.size();
		}

		@Override
		public Object getItem(int position) {
			return item.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent){
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
	
	public class UserData{
		int id;
		String name;
		
		public UserData(int id, String name){
			this.id = id;
			this.name = name;
		}
		
		public Integer getID(){
			return id;
		}
		
		public String getName(){
			return name;
		}
	}

}
