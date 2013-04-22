package com.burkina.containers;

public class PatientData {
	
	int id;
	int village_id;
	String firstname;
	String lastname;
	String dob;	
	String gender;
	int zone_id;
	String global_id;
	
	public PatientData(int id, int village_id, String firstname, String lastname, String dob, String gender, int zone_id, String global_id){
		this.id = id;
		this.village_id = village_id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.dob = dob;
		this.gender = gender;
		this.zone_id = zone_id;
		this.global_id = global_id;
	}
	
	public Integer getID(){
		return id;
	}
	
	public Integer getVillageID(){
		return village_id;
	}
	
	public String getFirstname(){
		return firstname;
	}
	
	public String getLastname(){
		return lastname;
	}
	
	public String getDOB(){
		return dob;
	}
	
	public String getGender(){
		return gender;
	}
	
	public Integer getZoneID(){
		return zone_id;
	}
	
	public String getGlobalID(){
		return global_id;
	}

}
