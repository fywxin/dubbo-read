package com.cyou.fz.dubbo.filter.domain;

import java.io.Serializable;

public class App implements Serializable {

	private static final long serialVersionUID = -12238947912831L;
	
	private String traceId;
	
	private String rpcId;

	private String appName;
	
	private String ip;
	
	public App(){}
	
	public App(String ip, String appName, String traceId, String rpcId){
		this.ip = ip;
		this.appName = appName;
		this.traceId = traceId;
		this.rpcId = rpcId;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getRpcId() {
		return rpcId;
	}

	public void setRpcId(String rpcId) {
		this.rpcId = rpcId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public String toString() {
		return "App [traceId=" + traceId + ", rpcId=" + rpcId + ", appName="
				+ appName + ", ip=" + ip + "]";
	}
	
}
