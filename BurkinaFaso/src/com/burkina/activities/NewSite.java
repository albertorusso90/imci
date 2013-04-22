package com.burkina.activities;

import com.burkina.functions.DatabaseHandler;
import com.burkina.functions.Functions;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewSite extends Activity{
	
	private final static int LOCATION_CODE = 1001;
	EditText editPhone;
	Button locationButton;
	
	int zone_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.site);
		
		editPhone = (EditText)findViewById(R.id.editPhone);
		
		locationButton = (Button)findViewById(R.id.buttonLocation);
		locationButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), ZoneSelector.class);
				startActivityForResult(intent, LOCATION_CODE);
			}
		});
		
		Button create = (Button)findViewById(R.id.buttonCreate);
		create.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				String phone = editPhone.getText().toString();
				String location = locationButton.getText().toString();
				
				if(phone.length() < 6){
					Toast.makeText(getApplication(), getString(R.string.error_phone), Toast.LENGTH_SHORT).show();
					editPhone.requestFocus();
				}else if(location.equals(getString(R.string.center))){
					Toast.makeText(getApplication(), getString(R.string.error_location), Toast.LENGTH_SHORT).show();
				}else{
					insertSite(phone, location);
					Toast.makeText(getApplication(), getString(R.string.successfull_site), Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		});
	}
	
	public void insertSite(String phone, String location){
		DatabaseHandler db = new DatabaseHandler(this);
		
		String global_id = location + "/" + db.getIncrementedID("sites");
		
		ContentValues values = new ContentValues();
		values.put("phone", phone);
		values.put("zone_id", zone_id);
		values.put("global_id", global_id);
		
		String currentdatetime = new Functions(getApplicationContext()).getCurrentDateTime();
		values.put("created_at", currentdatetime);
		values.put("updated_at", currentdatetime);

		db.insert("sites", values);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == LOCATION_CODE){
				String location = data.getStringExtra("location_name");
				zone_id = data.getIntExtra("location_id", 1);
				locationButton.setText(location);
			}
		}
	}

}
