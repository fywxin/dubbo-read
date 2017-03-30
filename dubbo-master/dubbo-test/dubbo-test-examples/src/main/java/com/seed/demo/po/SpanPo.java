package com.seed.demo.po;

import com.cyou.fz.dubbo.trace.domain.Span;

public class SpanPo implements java.io.Serializable {

	private static final long serialVersionUID = 12121L;

	//种子
	private String seed;
	//
	private String id;
	//父ID，根节点，父id为null
	private String pid;
	//服务名
	private String appName;
	//耗时
	private Integer duration;
	
	public SpanPo(){}
	
	public SpanPo(Span span){
		this.seed = span.getSeed();
		this.id = span.getId();
		this.pid = span.getPid();
		this.appName = span.getAppName();
	}
	
	public String getSeed() {
		return seed;
	}
	public void setSeed(String seed) {
		this.seed = seed;
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
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
