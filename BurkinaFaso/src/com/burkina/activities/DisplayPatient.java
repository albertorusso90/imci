package com.burkina.activities;

import com.burkina.functions.DatabaseHandler;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayPatient extends Activity{
	
	String dob = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_patient);
		
		TextView textName = (TextView)findViewById(R.id.textName);
		TextView textGender = (TextView)findViewById(R.id.textGender);
		TextView textDOB = (TextView)findViewById(R.id.textDOB);
		TextView textVillage = (TextView)findViewById(R.id.textVillage);
		
		DatabaseHandler db = new DatabaseHandler(this);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		final int id = extras.getInt("id");
		
		Cursor cursor = db.query("children", null, "_id =?", new String[]{String.valueOf(id)});
		if(cursor.moveToFirst()){
			int village_id = Integer.parseInt(cursor.getString(1));
			String firstname = cursor.getString(2);
			String lastname = cursor.getString(3);
			dob = cursor.getString(4);
			
			String gender = cursor.getString(5);
			if(gender.equals("t")) gender = getString(R.string.male);
			else gender = getString(R.string.female);
			
			String created = cursor.getString(16);
			String updated = cursor.getString(17);
			
			System.out.println("USER: " + village_id + " " + firstname + " " + lastname + " "
					+ dob + " " + gender);
			
			System.out.println("created: " + created);
			System.out.println("updated: " + updated);	
			
			textName.setText(firstname + " " + lastname);
			textGender.setText(gender);
			textDOB.setText(dob);
			textVillage.setText(db.getVillageNameById(village_id));
		}
		cursor.close();
		db.close(); 
		
		Button diagnose = (Button)findViewById(R.id.buttonNew);
		diagnose.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), Diagnose.class);
				intent.putExtra("id", id);
				intent.putExtra("dob", dob);
				startActivity(intent);
			}
		});
		
		Button previous = (Button)findViewById(R.id.buttonPrevious);
		previous.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplication(), DiagnosisList.class);
				intent.putExtra("id", id);
				startActivity(intent);
			}
		});
		
	}
	
	public void makeToast(String text){
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
}
