package com.burkina.functions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;

public class Functions {

	private final static String SHARED_PREFFERENCES_USER = "USER";
	private final static String SHARED_PREFFERENCES_LOGGED = "LOGGED";
	private final static String SHARED_PREFFERENCES_LOCATION = "LOCATION";
	private static final String PASSWORD_SALT = "a61d735c7a646ec8f33925dfb2561fb9"; //BurkinaFasoHash
	Context context;

	public Functions(Context context) {
		this.context = context;
	}
	
	public boolean isSite(){
		DatabaseHandler db = new DatabaseHandler(context);
		
		boolean isSite;
		Cursor cursor = db.rawQuery("SELECT * FROM sites");
		if(cursor.moveToFirst()) isSite = true;
		else isSite = false;
		
		cursor.close();
		db.close();
		return isSite;
	}
	
	public boolean isUser(){
		DatabaseHandler db = new DatabaseHandler(context);
		
		SharedPreferences userPreff = context.getSharedPreferences(SHARED_PREFFERENCES_USER, 0);		
		String username = userPreff.getString("username", "");
		String password = userPreff.getString("password", "");
		
		Cursor cursor = db.query("users", null, 
				"name =? AND crypted_password=?", 
				new String[]{username, password});
		
		boolean isUser;
		
		if(cursor.moveToFirst()){
			int id = Integer.parseInt(cursor.getString(0));
			setLoggedUser(id);
			isUser = true;
		}
		else isUser = false;
		
		cursor.close();
		db.close();
		
		return isUser;		
	}
	
	public void setLoggedUser(int id){
		SharedPreferences userPreff = context.getSharedPreferences(SHARED_PREFFERENCES_LOGGED, 0);
		
		Editor editor = userPreff.edit();
		editor.putInt("id", id);
		editor.commit();
	}
	
	public int getLoggedUser(){
		SharedPreferences userPreff = context.getSharedPreferences(SHARED_PREFFERENCES_LOGGED, 0);
		return userPreff.getInt("id", -1);
	}
	
	public void setUser(String username, String password){
		SharedPreferences userPreff = context.getSharedPreferences(SHARED_PREFFERENCES_USER, 0);
		
		Editor editor = userPreff.edit();
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}
	
	public void removeUser(){
		context.getSharedPreferences(SHARED_PREFFERENCES_USER, 0).edit().clear().commit();
	}

	public void setLocation(String location) {
		SharedPreferences locationPreff = context.getSharedPreferences(
				SHARED_PREFFERENCES_LOCATION, 0);
		Editor editor = locationPreff.edit();
		editor.putString("location", location);
		editor.commit();
	}

	public String isLocationSet() {
		SharedPreferences locationPreff = context.getSharedPreferences(
				SHARED_PREFFERENCES_LOCATION, 0);
		return locationPreff.getString("location", null);
	}

	public String md5(String password) {
		try {
			String salted = PASSWORD_SALT + password;
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(salted.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		return "";
	}
	
	public String getCurrentDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public int getMonthsDifference(Date date1) {
		Date date2 = new Date(System.currentTimeMillis());
		
		int m1 = date1.getYear() * 12 + date1.getMonth();
		int m2 = date2.getYear() * 12 + date2.getMonth();
		return (m2 - m1 + 1);
	}
	
	public int getAgeGroup(String dob) {
		int age_group;
		int days;

		String[] birthArray = dob.split("\\-");
		Integer bDay = Integer.parseInt(birthArray[0]);
		Integer bMonth = Integer.parseInt(birthArray[1]) - 1;
		Integer bYear = Integer.parseInt(birthArray[2]);
		GregorianCalendar birth = new GregorianCalendar(bYear, bMonth, bDay);
		GregorianCalendar current = new GregorianCalendar();
		int yearRange = current.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
		days = current.get(Calendar.DAY_OF_YEAR)
				- birth.get(Calendar.DAY_OF_YEAR) + yearRange * 365;

		if (days >= 0 && days <= 7) {
			age_group = 0;
		} else if (days >= 8 && days <= 60) {
			age_group = 1;
		} else {
			age_group = 2;
		}

		return age_group;
	}
}
