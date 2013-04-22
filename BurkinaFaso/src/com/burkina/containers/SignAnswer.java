package com.burkina.containers;

public class SignAnswer {
	
	int sign_id;
	String type;
	String value;
	
	public SignAnswer(int s_id, String type, String value){
		sign_id = s_id;
		this.type = type;
		this.value = value;
	}
	
	public Integer getSignID(){
		return sign_id;
	}
	
	public String getType(){
		return type;
	}
	
	public String getValue(){
		return value;
	}

}
