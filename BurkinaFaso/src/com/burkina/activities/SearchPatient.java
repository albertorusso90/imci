package com.burkina.activities;

import java.util.ArrayList;
import java.util.List;

import com.burkina.containers.PatientData;
import com.burkina.functions.DatabaseHandler;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class SearchPatient extends ListActivity implements TextWatcher{
	
	private static final int EDIT_CODE = 1105;
	private patientAdapter myAdapter;
	private DatabaseHandler db;
	EditText search;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		
		myAdapter = new patientAdapter(this);
		db = new DatabaseHandler(this);
		
		search = (EditText)findViewById(R.id.editSearch);
		search.addTextChangedListener(this);
		search("");
		
		TextView textEmpty = (TextView)findViewById(R.id.empty);
		textEmpty.setText(R.string.empty_patient);
		textEmpty.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), Patient.class);
				intent.putExtra("type", "add");
				startActivity(intent);
			}
		});
		
		getListView().setEmptyView(findViewById(R.id.empty));
		setListAdapter(myAdapter);
		getListView().setCacheColorHint(0);
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getApplication(), DisplayPatient.class);
		intent.putExtra("id", myAdapter.getPatientID(position));
		
		startActivity(intent);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);	
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		PatientData data = myAdapter.getPatient(info.position);
		switch (item.getItemId()) {
		case R.id.edit:
			Intent intent = new Intent(getApplication(), Patient.class);
			intent.putExtra("type", "edit");
			intent.putExtra("id", myAdapter.getPatientID(info.position));
			
			startActivityForResult(intent, EDIT_CODE);
			return true;
		case R.id.remove:
			removePatient(data.getID());
			myAdapter.remove(info.position);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	public void removePatient(int id){
		db.delete("children", "_id =?", new String[]{String.valueOf(id)});
	}
	
	public void search(String temp){
		Cursor cursor = db.query("children", null, "last_name like ?", new String[]{temp + "%"});
		if(cursor.moveToFirst()){
			do {
				int id = Integer.parseInt(cursor.getString(0));
				int village_id = Integer.parseInt(cursor.getString(1));
				String firstname = cursor.getString(2);
				String lastname = cursor.getString(3);
				String dob = cursor.getString(4);
				
				String gender;
				
				if(cursor.getString(5).equals("t")) gender = getString(R.string.male);
				else gender = getString(R.string.female);
				
				int zone_id = Integer.parseInt(cursor.getString(19));
				String global_id = cursor.getString(20);
				
				myAdapter.addItem(new PatientData(id, village_id, firstname, lastname, dob, gender, zone_id, global_id));
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == EDIT_CODE){
				myAdapter.clear();
				search.setText("");
			}
		}
	}
	
public class patientAdapter extends BaseAdapter{
		
		private Context context;
		private List<PatientData> item = new ArrayList<PatientData>();
		
		public patientAdapter(Context context){
			this.context = context;
		}
		
		public void addItem(PatientData data){
			item.add(data);
			notifyDataSetChanged();
		}
		
		public void remove(int position){
			item.remove(position);
			notifyDataSetChanged();
		}
		
		public PatientData getPatient(int position){
			return item.get(position);
		}
		
		public Integer getPatientID(int position){
			return item.get(position).getID();
		}
		
		
		public void clear(){
			item.clear();
			notifyDataSetChanged();
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
		public View getView(int position, View view, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowview = inflater.inflate(R.layout.list_item, parent, false);
			
			TextView label = (TextView)rowview.findViewById(R.id.label);
			
			String firstname = item.get(position).getFirstname();
			String lastname = item.get(position).getLastname();
			String dob = item.get(position).getDOB();
			String gender = item.get(position).getGender();
			
			label.setText(firstname + " " + lastname + "\n" + gender + "\t" + dob);	
			
			int[] imageArr = {R.drawable.arrow_red, R.drawable.arrow_green, R.drawable.arrow_gray};
			
			ImageView img = (ImageView)rowview.findViewById(R.id.imageSelector);
			img.setImageResource(imageArr[position%imageArr.length]);
			
			return rowview;
		}
		
	}

}
