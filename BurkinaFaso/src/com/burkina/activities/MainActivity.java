package com.burkina.activities;

import com.burkina.functions.DatabaseHandler;
import com.burkina.functions.Functions;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static int REGISTRATION_CODE = 1001;
	private static int DASHBOARD_CODE = 1005;
	private Functions functions = new Functions(this);
	EditText usernameText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);
        
        boolean isSite = functions.isSite();
        if(!isSite){
        	displayDialog();
        }

        if(functions.isUser() && isSite){
        	startActivityForResult(new Intent(getApplication(), Dashboard.class), DASHBOARD_CODE);
        }     
        
        usernameText = (EditText)findViewById(R.id.editUsername);
        passwordText = (EditText)findViewById(R.id.editPassword);   
        final CheckBox checkbox = (CheckBox)findViewById(R.id.checkBox);
        
        Button logIn = (Button)findViewById(R.id.buttonLog);
        logIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = usernameText.getText().toString();
				String password = functions.md5(passwordText.getText().toString());
				
				DatabaseHandler db = new DatabaseHandler(getApplication());
				Cursor cursor = db.query("users", null, 
						"name =? AND crypted_password=?", 
						new String[]{username, password});
				
				if(cursor.moveToFirst()){
					int id = Integer.parseInt(cursor.getString(0));
					functions.setLoggedUser(id);
					
					if(checkbox.isChecked()){
						functions.setUser(username, password);
					}else functions.removeUser();
					
					startActivityForResult(new Intent(getApplication(), Dashboard.class), DASHBOARD_CODE);
				}else Toast.makeText(getApplicationContext(), getString(R.string.invalid), Toast.LENGTH_SHORT).show();
				
				cursor.close();
				db.close();
			}
		});
        
        Button register = (Button)findViewById(R.id.buttonRegister);
        register.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), Registration.class);
				intent.putExtra("type", "registration");
				startActivityForResult(intent, REGISTRATION_CODE);
			}
		});   
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == RESULT_OK){
    		if(requestCode == REGISTRATION_CODE){    			
    			usernameText.setText(data.getStringExtra("username"));
    			passwordText.setText(data.getStringExtra("password"));
    		}
    	}else if(requestCode == DASHBOARD_CODE) finish();
    }
    
    public void displayDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.no_site))
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(getApplication(), NewSite.class);
						startActivity(intent);
					}
				});
		builder.create().show();
	}
    
}
