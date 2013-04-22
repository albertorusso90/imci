package com.burkina.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.burkina.functions.DatabaseHandler;
import com.burkina.functions.Functions;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Patient extends Activity{
	
	private final static int LOCATION_CODE = 1001;
	private DatabaseHandler db;
	
	private EditText editFirstname, editLastname;
	private RadioGroup radioGroup;
	private DatePicker picker;
	Button villageButton, accept, cancel;
	
	int village_id, zone_id, patientID;
	String type;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_patient);
		
		editFirstname = (EditText)findViewById(R.id.editFirstname);
		editLastname = (EditText)findViewById(R.id.editLastname);
		radioGroup = (RadioGroup)findViewById(R.id.radioGroup1);
		picker = (DatePicker)findViewById(R.id.datePicker1);	
		
		villageButton = (Button)findViewById(R.id.buttonLocation);
		accept = (Button)findViewById(R.id.buttonCreate);
		cancel = (Button)findViewById(R.id.buttonCancel);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		type = extras.getString("type");
		
		db = new DatabaseHandler(this);
		Calendar cal = Calendar.getInstance();
		
		if(type.equals("add")){
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			
			picker.init(year, month, day, null);
		}else if(type.equals("edit")){
			patientID = extras.getInt("id");
			
			accept.setText(getString(R.string.edit));
			
			Cursor cursor = db.query("children", null, "_id =?", 
					new String[]{String.valueOf(patientID)});
			
			if(cursor.moveToFirst()){
				
				editFirstname.setText(cursor.getString(2));
				editLastname.setText(cursor.getString(3));
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
				Date date = null;
				try {
					date = format.parse(cursor.getString(4));
				} catch (Exception e) {e.printStackTrace();}
				
				cal.setTime(date);
				
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int day = cal.get(Calendar.DAY_OF_MONTH);		
				
				picker.init(year, month, day, null);
				
				if(cursor.getString(5).equals("t")) radioGroup.check(R.id.radioMale);
				else radioGroup.check(R.id.radioFemale);
				
				village_id = Integer.parseInt(cursor.getString(1));			
			}
			cursor.close();
			db.close();
			
			cursor = db.query("zones", null, "_id =?", new String[]{String.valueOf(village_id)});
			if(cursor.moveToFirst()){
				villageButton.setText(cursor.getString(4));
			}
			cursor.close();
			db.close();
		}
	
		
		
		villageButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), ZoneSelector.class);
				startActivityForResult(intent, LOCATION_CODE);
			}
		});
		
		accept.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				String firstname = editFirstname.getText().toString();
				String lastname = editLastname.getText().toString();
				
				RadioButton radioGender = (RadioButton)findViewById(radioGroup.getCheckedRadioButtonId());
				String gender = radioGender.getText().toString();
				
				if(gender.equals(getString(R.string.male))) gender = "t";
				else gender = "f";
				
				String dob = picker.getYear() + "-" + (picker.getMonth() + 1) + "-" + picker.getDayOfMonth();
				String village = villageButton.getText().toString();
				
				if(firstname.length() < 4) {
					makeToast(getString(R.string.error_firstname));
					editFirstname.requestFocus();
				}else if(lastname.length() < 4) {
					makeToast(getString(R.string.error_lastname));
					editLastname.requestFocus();
				}else if(village.equals(getString(R.string.village))){
					makeToast(getString(R.string.error_village));
				}else if(type.equals("add")){
					insertPatient(firstname, lastname, gender, dob);
					finish();
				}else if(type.equals("edit")){
					updatePatient(firstname, lastname, gender, dob);
					setResult(RESULT_OK);
					finish();
				}
			}
		});
		
		cancel.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	
	public void makeToast(String text){
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}

	public void insertPatient(String firstname, String lastname, String gender, String dob){
		Cursor cursor = db.query("zones", null, "_id=?", new String[]{String.valueOf(zone_id)});
		
		String global_id = "";
		if(cursor.moveToFirst()){
			global_id = cursor.getString(4);
		}		
		cursor.close();		
		db.close();
		
		global_id += "/" + db.getIncrementedID("children");
		
		System.out.println("inserting -- " + village_id + " " + firstname + " " + lastname + " "
				+ gender	+ " " + dob + " " + global_id + " " + zone_id);
		
		ContentValues values = new ContentValues();
		values.put("village_id", village_id);
		values.put("first_name", firstname);
		values.put("last_name", lastname);
		values.put("gender", gender);
		values.put("born_on", dob);
		values.put("global_id", global_id);
		values.put("zone_id", zone_id);

		String currentdatetime = new Functions(this).getCurrentDateTime();
		values.put("created_at", currentdatetime);
		values.put("updated_at", currentdatetime);
		
		db.insert("children", values);
		makeToast(getString(R.string.success_patient));
		
	}
	
	public void updatePatient(String firstname, String lastname, String gender, String dob){
		System.out.println("updating -- " + village_id + " " + firstname + " " + lastname + " "
				+ gender + " " + dob);
		ContentValues values = new ContentValues();
		values.put("village_id", village_id);
		values.put("first_name", firstname);
		values.put("last_name", lastname);
		values.put("gender", gender);
		values.put("born_on", dob);

		String currentdatetime = new Functions(this).getCurrentDateTime();
		values.put("updated_at", currentdatetime);
		
		db.update("children", values, "_id =?", new String[]{String.valueOf(patientID)});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == LOCATION_CODE){
				String location = data.getStringExtra("location_name");
				village_id = data.getIntExtra("location_id", 1);
				zone_id = data.getIntExtra("location_pid", 1);
				villageButton.setText(location);
			}
		}
	}

}
