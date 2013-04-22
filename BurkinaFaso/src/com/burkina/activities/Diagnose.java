package com.burkina.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.burkina.containers.QuestionData;
import com.burkina.functions.DatabaseHandler;
import com.burkina.functions.Functions;
import com.burkina.functions.GetDiagnosis;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


public class Diagnose extends Activity{
	
	private TableLayout tableLayout;
	private Button next;
	private static final int REQUEST_CODE_DIAGNOSIS = 1005;
	List<QuestionData> questionData = new ArrayList<QuestionData>();
	
	int questionCount = 1, numberOfQuestions;
	boolean isPatientData = true;	
	private static long diagnostic_id;
	int child_id, zone_id;
	double height = 0, weight = 0, muac = 0, temp = 0;
	String global_id, dob, patientVariables = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.diagnose);
		
		DatabaseHandler db = new DatabaseHandler(this);
		Cursor cursor = db.rawQuery("SELECT * FROM sites");
		if(cursor.moveToFirst()) {
			zone_id = Integer.parseInt(cursor.getString(5));
			global_id = db.getVillageNameById(zone_id) + "/" + db.getIncrementedID("diagnostics");
		}
		cursor.close();
		db.close();
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		child_id = extras.getInt("id");
		dob = extras.getString("dob");
		
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dob);			
			int months = new Functions(this).getMonthsDifference(date);
			patientVariables += "data['enfant.months']=" +  months + ";";
		} catch (Exception e) {e.printStackTrace();}

		tableLayout = (TableLayout)findViewById(R.id.tableLayout1);
		
		questionData.add(new QuestionData(0, "height", "height", null, "height(cm)", null));
		questionData.add(new QuestionData(0, "weight", "weight", null, "weight(kg)", null));
		questionData.add(new QuestionData(0, "temp", "enfant.temp", null, "temperature(C)", null));
		questionData.add(new QuestionData(0, "muac", "enfant.muac", null, "muac(cm)", null));
		
		for(int i = 0; i < questionData.size(); i++){
			addQuestion(questionData.get(i).getQuestion());
		}
		
		getNumberOfQuestions();
		
		next = (Button)findViewById(R.id.buttonNext);
		next.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(isPatientData){
					for(int i = 0; i < tableLayout.getChildCount(); i++){
						LinearLayout row = (LinearLayout)tableLayout.getChildAt(i);
						EditText edit = (EditText)row.findViewById(R.id.editInput);
						
						String type = questionData.get(i).getType();
						String answer = edit.getText().toString();
						
						if(type.equals("height")) height = Double.parseDouble(answer) / 100;
						else if(type.equals("weight")) weight = Double.parseDouble(answer);
						else if(type.equals("temp")) temp = Double.parseDouble(answer);
						else if(type.equals("muac")) muac = Double.parseDouble(answer);						
					}	
					
					double wfh = weight/(height*height);
					
					patientVariables += "data['enfant.muac']=" + muac + ";" +
							"data['enfant.wfh']=" + wfh + ";";	
					
					DatabaseHandler db = new DatabaseHandler(getApplication());
					diagnostic_id = db.insertDiagnosis(child_id, muac, height, weight, temp, dob, "open", zone_id, global_id);

					tableLayout.removeAllViews();
					questionData.clear();
					isPatientData = false;
					getQuestions(questionCount);
				}else getAnswers();				
			}
		});	
	}
	
	public void addQuestion(String question){
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.table_row_integer, null);
		
		TextView textQuestion = (TextView)row.findViewById(R.id.textQuestion);
		textQuestion.setText(question);
		
		tableLayout.addView(row);
	}
	
	public void getNumberOfQuestions(){
		DatabaseHandler db = new DatabaseHandler(this);
		
		Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT illness_id) FROM signs");
		if(cursor.moveToFirst()){
			numberOfQuestions = Integer.parseInt(cursor.getString(0));
		}
		cursor.close();
		db.close();
	}
	
	public void getQuestions(int id){
		DatabaseHandler db = new DatabaseHandler(this);
		
		String query = "SELECT * FROM signs s INNER JOIN illnesses i " +
				"ON s.illness_id = i._id WHERE s.illness_id = " + id;
		
		int count = 0;
		Cursor cursor = db.rawQuery(query);
		if(cursor.moveToFirst()){
			do {
				int sign_id = Integer.parseInt(cursor.getString(0));
				String type = cursor.getString(2);
				String key = cursor.getString(16) + "." + cursor.getString(3);
				
				String question = cursor.getString(4);
				String values = cursor.getString(5);
				String dep = cursor.getString(6);
				
				String name = cursor.getString(17);			
				
				questionData.add(new QuestionData(sign_id, type, key, dep, question, name));
				
				if(type.equals("BooleanSign")) addBooleanRow(count);
				else if(type.equals("IntegerSign")) addIntegerRow(count);
				else if(type.equals("ListSign")) addListRow(values, count);
				
				count++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		System.out.println("on getQuestions " + diagnostic_id);
	}
	
	public boolean checkQuestionsForError(){		
		for(int i = 0; i < tableLayout.getChildCount(); i ++){
			LinearLayout row = (LinearLayout)tableLayout.getChildAt(i);
			
			String type = questionData.get(i).getType();			
			
			if(type.equals("IntegerSign")){
				EditText edit = (EditText)row.findViewById(R.id.editInput);
				if(edit.isEnabled() && edit.length() == 0){
					edit.requestFocus();
					return false;
				}			
			}
		}
		return true;
	}
	
	public void insertSignAnswers(){
		for(int i = 0; i < questionData.size(); i++){
			int sign_id = questionData.get(i).getSignID();
			String type = questionData.get(i).getType();
			String answer = questionData.get(i).getAnswer();
			
			ContentValues values = new ContentValues();
			values.put("sign_id", sign_id);
			values.put("diagnostic_global_id", diagnostic_id);
			values.put("zone_id", zone_id);
			values.put("global_id", global_id);
			values.put("type", type);
			
			values.put("boolean_value", answer);
			values.put("integer_value", answer);
			values.put("list_value", answer);
			
			String currentdatetime = new Functions(this).getCurrentDateTime();
			values.put("created_at", currentdatetime);
			values.put("updated_at", currentdatetime);
			
			DatabaseHandler db = new DatabaseHandler(this);			
			db.insert("sign_answers", values);
		}
		System.out.println("on InsertSignAnswer d_id: " + diagnostic_id);
	}
	
	public void getAnswers(){
		boolean isCorrect = checkQuestionsForError();	
		
		if(isCorrect){	
			insertSignAnswers();			
			
			questionCount++;
			
			if(questionCount <= numberOfQuestions){
				tableLayout.removeAllViews();
				questionData.clear();
				getQuestions(questionCount);
			}else {
				//FINISH QUESTIONS, ITS TIME TO OPEN THE RESULTS PAGE
				DatabaseHandler db = new DatabaseHandler(this);
				
				ContentValues values = new ContentValues();
				values.put("state", "close");
				values.put("updated_at", new Functions(this).getCurrentDateTime());
				
				db.update("diagnostics", values, "_id=?", new String[]{String.valueOf(diagnostic_id)});
				System.out.println("update finished " + diagnostic_id);
				
				Intent intent = new Intent(getApplication(), DiagnosisResults.class);
				intent.putExtra("diagnostics_id", diagnostic_id);
				startActivityForResult(intent, REQUEST_CODE_DIAGNOSIS);
			}		
		}else {
			Toast.makeText(getApplication(), getString(R.string.fullfill_question), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void addBooleanRow(final int position){
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.table_row_boolean, null);
		
		String question = questionData.get(position).getQuestion();
		String dep = questionData.get(position).getDep();
		String key = questionData.get(position).getKey();
		
		RadioGroup group = (RadioGroup)row.findViewById(R.id.radioGroup1);
		TextView textQuestion = (TextView)row.findViewById(R.id.textQuestion);
		textQuestion.setText(question);
		
		boolean isEnabled;
		
		questionData.get(position).setAnswer("false");
		
		key = "data={};data['" + key + "']=false;";
		
		if(dep.equals("")) isEnabled = true;
		else isEnabled = new GetDiagnosis().getResult(key + " " + dep);
		
		group.setEnabled(isEnabled);
		for(int i = 0; i < group.getChildCount(); i++){
			((RadioButton)group.getChildAt(i)).setEnabled(isEnabled);
		}
		
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {				
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				boolean set;				
				if(checkedId == R.id.radioYes) set = true;
				else set = false;
				
				questionData.get(position).setAnswer(String.valueOf(set));
				checkEnables();				
			}
		});
		
		tableLayout.addView(row);
	}
	
	public void addIntegerRow(final int position){
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.table_row_integer, null);
		
		String question = questionData.get(position).getQuestion();
		String key = questionData.get(position).getKey();
		String dep = questionData.get(position).getDep();
		
		final EditText edit = (EditText)row.findViewById(R.id.editInput);
		TextView textQuestion = (TextView)row.findViewById(R.id.textQuestion);
		textQuestion.setText(question);
		
		boolean isEnabled;
		
		questionData.get(position).setAnswer("0");
		
		key = "data={};data['" + key + "']=0;";
		
		if(dep.equals("")) isEnabled = true;
		else isEnabled = new GetDiagnosis().getResult(key + " " + dep);
		
		edit.setEnabled(isEnabled);
		
		edit.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				String temp = edit.getText().toString();
				if(temp.length() != 0){
					questionData.get(position).setAnswer(edit.getText().toString());
					checkEnables();
				}else questionData.get(position).setAnswer("0");
			}
		});
		
		tableLayout.addView(row);
	}
	
	public void addListRow(String values, final int position){
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.table_row_list, null);

		String question = questionData.get(position).getQuestion();
		String key = questionData.get(position).getKey();
		String dep = questionData.get(position).getDep();
		
		TextView textQuestion = (TextView)row.findViewById(R.id.textQuestion);
		textQuestion.setText(question);
		
		final Spinner spinner = (Spinner)row.findViewById(R.id.spinner1);
		
		String[] valuesArray = values.split("\\;");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, valuesArray);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		questionData.get(position).setAnswer("'" + spinner.getSelectedItem().toString() + "'");
		
		key = "data={};data['" + key + "']=0;";
		
		boolean isEnabled;
		if(dep.equals("")) isEnabled = true;
		else isEnabled = new GetDiagnosis().getResult(key + " " + dep);
		
		spinner.setEnabled(isEnabled);
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String temp = spinner.getSelectedItem().toString();
				questionData.get(position).setAnswer("'" + temp + "'");
				checkEnables();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		tableLayout.addView(row);
	}
	
	public void checkEnables(){
		String variables = "data={};" + patientVariables;
		for(int i = 0; i < questionData.size(); i++){
			String key = questionData.get(i).getKey();
			String answer = questionData.get(i).getAnswer();
			
			variables += "data['" + key + "']=" + answer + ";";
		}
		
		for(int i = 0; i < tableLayout.getChildCount(); i++){
			String dep = questionData.get(i).getDep();
			
			if(!dep.equals("")){
				boolean result = new GetDiagnosis().getResult(variables + " " + dep);
				
				LinearLayout row = (LinearLayout)tableLayout.getChildAt(i);
				String type = questionData.get(i).getType();
				  
				if(type.equals("BooleanSign")){
					RadioGroup radioGroup = (RadioGroup)row.findViewById(R.id.radioGroup1);
					radioGroup.setEnabled(result);
					for(int j = 0; j < radioGroup.getChildCount(); j++){
						((RadioButton)radioGroup.getChildAt(j)).setEnabled(result);
					}
				}else if(type.equals("IntegerSign")){
					EditText edit = (EditText)row.findViewById(R.id.editInput);
					edit.setEnabled(result);
				}else if(type.equals("ListSign")){
					Spinner spinner = (Spinner)row.findViewById(R.id.spinner1);
					spinner.setEnabled(result);
				}	
			}				
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
    		if(requestCode == REQUEST_CODE_DIAGNOSIS){    			
    			finish();
    		}
    	}
	}
}
