package com.cyou.fz.dubbo.trace.domain;

import java.io.Serializable;

/**
 * 服务端内调用
 * @author Administrator
 *
 */
public class Node implements Serializable{
	private static final long serialVersionUID = 23432452351L;
	
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
	

	public Node(){}
	
	public Node(String intf, String method, Boolean isClient){
		this.intf = intf;
		this.method = method;
		this.isClient = isClient;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((intf == null) ? 0 : intf.hashCode());
		result = prime * result
				+ ((isClient == null) ? 0 : isClient.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		
		if(other.getId() == null)
			return false;
		return other.getId().equals(this.id);
	}

	@Override
	public String toString() {
		return "Node [intf=" + intf + ", method=" + method + ", isClient="
				+ isClient + ", timestamp=" + timestamp + ", duration="
				+ duration + ", id=" + id + ", pid=" + pid + "]";
	}
	
}
