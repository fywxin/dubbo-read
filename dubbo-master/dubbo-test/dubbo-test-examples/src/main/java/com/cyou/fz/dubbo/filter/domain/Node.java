package com.cyou.fz.dubbo.filter.domain;

import java.io.Serializable;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;

public class Node implements Serializable {

	private static final long serialVersionUID = -122318947912831L;
	
	private String rpcId;
	
	//开始时间
	private Long startTime;
	
	private Integer duration;
	
	private Boolean isClient;
	
	private String method;
	
	private Invocation invocation;
	
	private Result result;
	
	//传输量大小？？

	public String getRpcId() {
		return rpcId;
	}

	public void setRpcId(String rpcId) {
		this.rpcId = rpcId;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Boolean getIsClient() {
		return isClient;
	}

	public void setIsClient(Boolean isClient) {
		this.isClient = isClient;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Invocation getInvocation() {
		return invocation;
	}

	public void setInvocation(Invocation invocation) {
		this.invocation = invocation;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Node [rpcId=" + rpcId + ", startTime=" + startTime
				+ ", duration=" + duration + ", isClient=" + isClient
				+ ", method=" + method + ", invocation=" + invocation
				+ ", result=" + result + "]";
	}
}
