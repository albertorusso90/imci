package com.burkina.functions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseHandler extends SQLiteAssetHelper{
	private SQLiteDatabase dataBase;
	Context context;

	private static final String DATABASE_NAME = "database"; 
	private static final int DATABASE_VERSION = 1; 

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}
	
	public String getVillageNameById(int id){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query("zones", null, "_id=?", new String[]{String.valueOf(id)}, 
				null, null, null);
		
		String zone = "";
		if(cursor.moveToFirst()){
			zone = cursor.getString(4);
		}
		cursor.close();
		db.close();
		
		return zone;
	}
	
	public Long insertDiagnosis(int child_id, double muac, double height, double weight, 
			double temp, String dob, String state, int zone_id, String global_id){
		SQLiteDatabase db = getWritableDatabase();
		
		int author_global_id = new Functions(context).getLoggedUser();
		int age_group = new Functions(context).getAgeGroup(dob);
		
		ContentValues values = new ContentValues();
		values.put("child_global_id", child_id);
		values.put("author_global_id", author_global_id);
		values.put("mac", muac);
		values.put("height", height);
		values.put("weight", weight);
		values.put("temperature", temp);
		values.put("born_on", dob);
		values.put("state", state);
		values.put("zone_id", zone_id);
		values.put("global_id", global_id);
		values.put("saved_age_group", age_group);
		
		String currentdatetime = new Functions(context).getCurrentDateTime();
		values.put("done_on", currentdatetime);
		values.put("updated_at", currentdatetime);
		if(state.equals("open")) values.put("created_at", currentdatetime);		
		
		long diagnosis_id = db.insert("diagnostics", null, values);
		db.close();		
		
		return diagnosis_id;
	}
	
	public Cursor query(String table, String[] columns, String selection, String[] selectionArgs){
		dataBase = getReadableDatabase();
		return dataBase.query(table, columns, selection, selectionArgs, null, null, null);
	}
	
	public Cursor rawQuery(String query){
		dataBase = getReadableDatabase();
		return dataBase.rawQuery(query, null);
	}
	
	public void insert(String table, ContentValues values){
		SQLiteDatabase db = getWritableDatabase();
		db.insert(table, null, values);
		db.close();
	}
	
	public void update(String table, ContentValues values, String where, String[] args){
		SQLiteDatabase db = getWritableDatabase();
		db.update(table, values, where, args);
		db.close();
	}
	
	public void delete(String table, String where, String[] whereArgs){
		SQLiteDatabase db = getWritableDatabase();
		db.delete(table, where, whereArgs);
		db.close();
	}
	 
	public Integer getIncrementedID(String table){
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.query(table, null, null, null, null, null, null);
		
		int id;
		if(cursor.moveToLast()){
			id = cursor.getInt(0) + 1;
		}else id = 1;
		
		cursor.close();
		db.close();
		
		return id;
	}
	
	public void close(){
		if(dataBase.isOpen()) dataBase.close();
	}
}
