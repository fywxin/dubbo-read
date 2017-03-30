package com.seed.demo.po;

import com.cyou.fz.dubbo.trace.domain.Node;

public class NodePo implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	//方法ID
	private String id;
	//父方法ID
	private String pid;
	//所属的节点
	private String spanId;
	//接口
	private String intf;
	//调用方法
	private String method;
	//是否客户端
	private Boolean isClient;
	//创建时间
	private Long timestamp;
	//耗时
	private Integer duration;
	
	public NodePo(){}
	
	public NodePo(Node node){
		this.id = node.getId();
		this.pid = node.getPid();
		this.spanId = node.getSpanId();
		this.intf = node.getIntf();
		this.isClient = node.getIsClient();
		this.method = node.getMethod();
		this.timestamp = node.getTimestamp();
		this.duration = node.getDuration();
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
	public String getSpanId() {
		return spanId;
	}
	public void setSpanId(String spanId) {
		this.spanId = spanId;
	}
	public String getIntf() {
		return intf;
	}
	public void setIntf(String intf) {
		this.intf = intf;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Boolean getIsClient() {
		return isClient;
	}
	public void setIsClient(Boolean isClient) {
		this.isClient = isClient;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
