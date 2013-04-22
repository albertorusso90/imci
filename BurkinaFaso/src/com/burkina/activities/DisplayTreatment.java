package com.burkina.activities;

import java.util.ArrayList;
import java.util.List;

import com.burkina.functions.DatabaseHandler;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayTreatment extends ListActivity{
	
	private PrescriptionAdapter myAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.treatment);
		
		myAdapter = new PrescriptionAdapter(this);
		
		TextView name = (TextView)findViewById(R.id.textTreatmentName);
		TextView desc = (TextView)findViewById(R.id.textTreatmentDesc);
		
		int cid = getIntent().getIntExtra("cid", 0);
		boolean noData = false;
		
		DatabaseHandler db = new DatabaseHandler(this);
		
		Cursor cursor = db.rawQuery("SELECT ifnull(p.duration,'--') as duration, " +
				"ifnull(p.takes,'--') as takes, p.instructions, m.name, t.name, t.description " +
				"FROM prescriptions p INNER JOIN medicines m ON p.medicine_id = m._id " +
				"LEFT JOIN treatments t ON p.treatment_id = t._id " +
				"WHERE t.classification_id =" + String.valueOf(cid));
		
		if(cursor.moveToFirst()){
			name.setText(cursor.getString(4));
			desc.setText(cursor.getString(5));
			
			do {
				String duration = "Duration: " + cursor.getString(0);
				String takes = "Takes: " + cursor.getString(1);
				
				//String instructions = cursor.getString(2);
				String medicine = "Medicine: " + cursor.getString(3);
				
				myAdapter.addItem(duration + "\n" + takes + "\n" + medicine);
			} while (cursor.moveToNext());
		}else noData = true;
		db.close();
		cursor.close();
		
		if(noData){
			Toast.makeText(getApplication(), getString(R.string.no_treatment), Toast.LENGTH_SHORT).show();
			finish();
		}
		
		setListAdapter(myAdapter);
		getListView().setCacheColorHint(0);
	}
	
	public class PrescriptionAdapter extends BaseAdapter{

		private List<String> prescription = new ArrayList<String>();
		Context context;
		
		public PrescriptionAdapter(Context context){
			this.context = context;
		}
		
		public void addItem(String item){
			prescription.add(item);
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return prescription.size();
		}

		@Override
		public Object getItem(int position) {
			return prescription.get(position);
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
			label.setText(prescription.get(position));			
			
			ImageView img = (ImageView)rowview.findViewById(R.id.imageSelector);
			img.setVisibility(View.GONE);
			
			return rowview;
		}
		
	}

}
