package com.burkina.activities;

import com.burkina.functions.DatabaseHandler;
import com.burkina.functions.Functions;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Registration extends Activity{
	
	private final static int LOCATION_CODE = 1001;
	EditText usernameText, passwordText, confirmText;
	RadioGroup radioGroup;
	Button locationButton;
	
	int zone_id, userID;
	String type, oldPassword = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		
		usernameText = (EditText)findViewById(R.id.editUsername);
		passwordText = (EditText)findViewById(R.id.editPassword);
		confirmText = (EditText)findViewById(R.id.editConfirm);
		radioGroup = (RadioGroup)findViewById(R.id.radioGroup1);
		locationButton = (Button)findViewById(R.id.buttonLocation);
		Button create = (Button)findViewById(R.id.buttonCreate);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		type = extras.getString("type");
		
		if(type.equals("edit")){
			userID = extras.getInt("id");
			
			DatabaseHandler db = new DatabaseHandler(this);
			
			Cursor cursor = db.query("users", null, "_id=?", new String[]{String.valueOf(userID)});
			if(cursor.moveToFirst()){
				String name = cursor.getString(1);
				oldPassword = cursor.getString(2);
				String admin = cursor.getString(3);
				zone_id = Integer.parseInt(cursor.getString(6));
				
				usernameText.setText(name);
				
				if(admin.equals("t")) radioGroup.check(R.id.radioYes);
				else radioGroup.check(R.id.radioNo);
				
				String zone = db.getVillageNameById(zone_id);
				locationButton.setText(zone);
				create.setText(getString(R.string.confirm));
			}
			cursor.close();
			db.close();
		}
		
		locationButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), ZoneSelector.class);
				startActivityForResult(intent, LOCATION_CODE);
			}
		});
		
		create.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				String username = usernameText.getText().toString();
				String password = passwordText.getText().toString();
				String confirm = confirmText.getText().toString();
				String location = locationButton.getText().toString();
				String admin;
				
				int checked = radioGroup.getCheckedRadioButtonId();
				if(checked == R.id.radioYes) admin = "t";
				else admin = "f";
				
				if(username.length() < 4){
					makeToast(getString(R.string.error_username));
					usernameText.requestFocus();
				}if(password.length() < 4 && oldPassword.length() == 0){
					makeToast(getString(R.string.error_password));
					passwordText.requestFocus();
				}else if(!password.equals(confirm) && oldPassword.length() == 0){
					makeToast(getString(R.string.error_confirm));
					confirmText.requestFocus();
				}else if(location.equals(getString(R.string.center))) {
					makeToast(getString(R.string.error_location));
				}else{					
					if(type.equals("registration")){
						insertUser(location, username, password, admin);
						System.out.println(username + " " + password + " " + location + " " + admin);
						
						Intent returnIntent = new Intent();
						returnIntent.putExtra("username", username);
						returnIntent.putExtra("password", password);
						
						setResult(RESULT_OK, returnIntent);     
						finish();						
					}else if(type.equals("edit")){						
						if(password.length() < 4 && password.length() != 0){
							makeToast(getString(R.string.error_password));
							passwordText.requestFocus();
						}else if(!password.equals(confirm) && password.length() != 0){
							makeToast(getString(R.string.error_confirm));
							confirmText.requestFocus();
						}else{
							String newPassword;
							if(password.length() != 0) newPassword = new Functions(getApplicationContext()).md5(password);
							else newPassword = oldPassword;
							
							updateUser(location, username, newPassword, admin);
							System.out.println(location + " " + username + " " + newPassword + " " + admin);
							
							setResult(RESULT_OK);
							finish();
						}
					}
				}				
			}
		});
	}
	
	public void makeToast(String text){
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	public void insertUser(String location, String username, String password, String admin){
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		
		String global_id = location + "/" + db.getIncrementedID("users");
		
		ContentValues values = new ContentValues();
		values.put("name", username);
		values.put("crypted_password", new Functions(getApplicationContext()).md5(password));
		values.put("admin", admin);
		values.put("zone_id", zone_id);
		values.put("global_id", global_id);
		
		String currentdatetime = new Functions(getApplicationContext()).getCurrentDateTime();
		values.put("created_at", currentdatetime);
		values.put("updated_at", currentdatetime);
		
		db.insert("users", values);
		makeToast(getString(R.string.successfull_create));
	}
	
	public void updateUser(String location, String username, String password, String admin){
		DatabaseHandler db = new DatabaseHandler(this);
		
		ContentValues values = new ContentValues();
		values.put("name", username);
		values.put("crypted_password", password);
		values.put("admin", admin);
		values.put("zone_id", zone_id);
		
		String currentdatetime = new Functions(getApplicationContext()).getCurrentDateTime();
		values.put("updated_at", currentdatetime);
		
		db.update("users", values, "_id=?", new String[]{String.valueOf(userID)});
		makeToast(getString(R.string.successfull_edit));
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
