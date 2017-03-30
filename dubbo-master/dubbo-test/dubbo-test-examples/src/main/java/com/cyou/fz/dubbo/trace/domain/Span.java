package com.cyou.fz.dubbo.trace.domain;

import java.io.Serializable;

/**
 * 
 * @author Administrator
 *
 */
public class Span implements Serializable {
	private static final long serialVersionUID = 237892173981L;
	
	//种子
	private String seed;
	
	private String id;
	
	private String pid;
	//服务名
	private String appName;
	//最近一个节点
	private transient volatile Node preNode;
	
	public Span(){}
	
	public Span(String seed, String appName, String pid){
		this.id = java.util.UUID.randomUUID().toString();
		this.seed = seed;
		this.appName = appName;
		this.pid = pid;
	}

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
	
	public Node getPreNode() {
		return preNode;
	}

	public void setPreNode(Node preNode) {
		this.preNode = preNode;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	@Override
	public String toString() {
		return "Span [seed=" + seed + ", id=" + id + ", pid=" + pid
				+ ", appName=" + appName + "]";
	}
	
	
}
