package com.burkina.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.burkina.functions.DatabaseHandler;
import com.burkina.functions.Functions;
import com.burkina.functions.GetDiagnosis;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DiagnosisResults extends ListActivity {

	private DiagnoseAdapter myAdapter;
	
	public final static String baseJS = "function AT_LEAST_TWO_OF(){var trueargs = 0;for (var i = 0; i < arguments.length; ++i) {if (arguments[i]) {trueargs++;}} return (trueargs >= 2);}";

	String variableData = "data={}";
	String variableChild = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		myAdapter = new DiagnoseAdapter(this);
		
		long id = getIntent().getLongExtra("diagnostics_id", -1);
		System.out.println("on DiagnosisResults s_id: " + id);
		getChildrenVariable(id);
		
		new Async().execute(id);
		
		setListAdapter(myAdapter);
		getListView().setCacheColorHint(0);
		setResult(RESULT_OK);
	}
	
	public void getChildrenVariable(long id){
		DatabaseHandler db = new DatabaseHandler(this);
		Cursor cursor = db.query("diagnostics", new String[]{"height", "weight", "mac", "born_on"}, 
				"_id=?", new String[]{String.valueOf(id)});
		if(cursor.moveToFirst()){
			double height = cursor.getDouble(0);
			double weight = cursor.getDouble(1);
			double muac = cursor.getDouble(2);
			String dob = cursor.getString(3);
			
			double wfh = weight/(height*height);
			
			try {
				Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dob);			
				int months = new Functions(this).getMonthsDifference(date);
				variableChild += "data['enfant.muac']=" + muac + ";" +
					"data['enfant.wfh']=" + wfh + ";" + "data['enfant.months']=" +  months + ";";
			} catch (Exception e) {e.printStackTrace();}
		}
	}
	
	public String buildVariable(long d_id, String equation){
		DatabaseHandler db = new DatabaseHandler(getApplication());
		String query = "SELECT s.key, i.key, a.type, a.list_value, a.boolean_value, a.integer_value " +
				"FROM signs s INNER JOIN illnesses i " +
				"ON s.illness_id = i._id " +
				"LEFT JOIN sign_answers a " +
				"ON a.sign_id = s._id " +
				"WHERE a.diagnostic_global_id = " + d_id;
		
		String variable = "data={};" + variableChild;
		Cursor cursor = db.rawQuery(query);
		if(cursor.moveToFirst()){
			do {
				String key = cursor.getString(1) + "." + cursor.getString(0);
				String type = cursor.getString(2);
				
				String value = "";
				if(type.equals("ListSign")) value = cursor.getString(3);
				else if(type.equals("BooleanSign"))value = cursor.getString(4);
				else if(type.equals("IntegerSign"))value = cursor.getString(5);
				
				if(equation.contains(key)) variable += "data['" + key + "']=" + value + ";";
				
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		
		return variable;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long arg1) {
		int id = myAdapter.getID(position);
		Intent intent = new Intent(getApplication(), DisplayTreatment.class);
		intent.putExtra("cid", id);
		startActivity(intent);
	}
	
	public class Async extends AsyncTask<Long, String, Void>{
		
		@Override
		protected Void doInBackground(Long... arg0) {
			long d_id = arg0[0];
			
			DatabaseHandler db = new DatabaseHandler(getApplication());
			Cursor cursor = db.query("classifications", null, null, null);
			if(cursor.moveToFirst()){
				do {
					String equation = cursor.getString(6);
					String variable = buildVariable(d_id, equation);
					System.out.println("variable: " + variable);
					System.out.println("equation: " + equation);
					
					boolean result = new GetDiagnosis().getResult(variable + " " + baseJS + " " + equation);
					System.out.println("result: " + result);
					if(result){
						if(Integer.parseInt(cursor.getString(9)) != 0){
							publishProgress(cursor.getString(0), cursor.getString(5));
						}
					}					
				} while (cursor.moveToNext());
			}
			cursor.close();
			db.close();		
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... item) {
			int id = Integer.parseInt(item[0]);
			String name = item[1];
			
			myAdapter.addItem(new ItemData(id, name));
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
		
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
			label.setText(data.get(position).getName());
			
			int[] imageArr = {R.drawable.arrow_red, R.drawable.arrow_green, R.drawable.arrow_gray};
			
			ImageView img = (ImageView)rowview.findViewById(R.id.imageSelector);
			img.setImageResource(imageArr[position%imageArr.length]);

			return rowview;
		}
	}
	
	public class ItemData{
		int id;
		String name;
		
		public ItemData(int id, String name){
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
