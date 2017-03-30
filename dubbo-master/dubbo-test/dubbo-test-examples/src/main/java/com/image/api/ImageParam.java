package com.image.api;

import java.io.Serializable;
import java.util.Map;

public class ImageParam implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private Map<String, String> extension;
	
	public ImageParam(){
		
	}
	
	public ImageParam(String name, Map<String, String> extension){
		this.name = name;
		this.extension = extension;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getExtension() {
		return extension;
	}

	public void setExtension(Map<String, String> extension) {
		this.extension = extension;
	}
	
	
	
	
	
}
