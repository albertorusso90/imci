package com.burkina.containers;

public class QuestionData {
	
	int sign_id;
	String type;
	String key;
	String dep;
	String question;
	String name;
	
	String answer;
	
	public QuestionData(int sign_id, String type, String key, String dep, String question, String name){
		this.sign_id = sign_id;
		this.type = type;
		this.key = key;
		this.dep = dep;
		this.question = question;
		this.name = name;
	}
	
	public Integer getSignID(){
		return sign_id;
	}
	
	public String getType(){
		return type;
	}
	
	public String getKey(){
		return key;
	}
	
	public String getDep(){
		return dep;
	}
	
	public String getQuestion(){
		return question;
	}
	
	public String name(){
		return name;
	}
	
	public void setAnswer(String input){
		answer = input;
	}
	
	public String getAnswer(){
		return answer;
	}

}
