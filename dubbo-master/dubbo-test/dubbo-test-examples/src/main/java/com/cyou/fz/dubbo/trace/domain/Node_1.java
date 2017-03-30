package com.cyou.fz.dubbo.trace.domain;

import java.util.ArrayList;
import java.util.List;

public class Node_1 {
	
	private String seed;
	
	private String appName;
	
	private String intf;
	
	private List<String> methods = new ArrayList<String>(1);

	private String parentAppName;
	
	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getIntf() {
		return intf;
	}

	public void setIntf(String intf) {
		this.intf = intf;
	}
	
	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

	public String getParentAppName() {
		return parentAppName;
	}

	public void setParentAppName(String parentAppName) {
		this.parentAppName = parentAppName;
	}
	
	public void addMethod(String method){
		if(!this.methods.contains(method))
			this.methods.add(method);
	}

	@Override
	public String toString() {
		return "Node [seed=" + seed + ", appName=" + appName + ", intf=" + intf
				+ ", methods=" + methods + ", parentAppName=" + parentAppName
				+ "]";
	}
	
}
