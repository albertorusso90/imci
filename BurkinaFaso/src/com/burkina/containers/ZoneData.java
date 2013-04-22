package com.burkina.containers;

public class ZoneData {
	
	int id;
	int p_id;
	String name;
	
	public ZoneData(int id, int p_id, String name){
		this.id = id;
		this.p_id = p_id;
		this.name = name;
	}
	
	public Integer getID(){
		return id;
	}
	
	public Integer getParentID(){
		return p_id;
	}
	
	public String getName(){
		return name;
	}
}
