package com.burkina.activities;

import com.burkina.functions.Functions;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Dashboard extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);		

		Button add = (Button) findViewById(R.id.buttonAddPatient);
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), Patient.class);
				intent.putExtra("type", "add");
				startActivity(intent);
			}
		});

		Button search = (Button) findViewById(R.id.buttonSearchPatient);
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplication(), SearchPatient.class));
			}
		});

		Button manage = (Button) findViewById(R.id.buttonManageUser);
		manage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplication(), ManageUser.class));
			}
		});

	}

	public void disconnect() {
		new Functions(getApplication()).removeUser();

		finish();

		Intent a = new Intent(this, MainActivity.class);
		a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(a);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.logout:
			disconnect();
			break;

		default:
			break;
		}
		return true;
	}

}
