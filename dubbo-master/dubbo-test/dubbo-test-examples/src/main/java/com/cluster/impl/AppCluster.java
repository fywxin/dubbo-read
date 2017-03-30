package com.cluster.impl;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;

public class AppCluster implements Cluster {

	public final static String NAME = "appList";
	
	@Override
	public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
		return new AppClusterInvoker<T>(directory);
	}

}
