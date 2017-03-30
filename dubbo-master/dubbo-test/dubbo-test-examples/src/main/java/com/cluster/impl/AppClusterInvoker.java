package com.cluster.impl;

import java.util.List;

import com.alibaba.dubbo.registry.integration.RegistryDirectory.InvokerDelegete;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker;

public class AppClusterInvoker<T> extends AbstractClusterInvoker<T> {

	public AppClusterInvoker(Directory<T> directory) {
		super(directory);
	}

	@Override
	protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers,
			LoadBalance loadbalance) throws RpcException {
		Invoker cInvoker = invocation.getInvoker();
//		if(cInvoker != null){
//			System.out.println("-------CU--"+cInvoker.getUrl());
//		}else{
//			System.out.println("-------CU-- NULL");
//		}
		
		Result result = null;
		InvokerDelegete<T> invokerDelegete = null;
		int i=0;
		for(Invoker<T> invoker : invokers){
			i++;
			invokerDelegete = (InvokerDelegete<T>)invoker;
			System.out.println("-------PU--"+i+" -- "+invokerDelegete.getProviderUrl());
			//if(invokerDelegete.getProviderUrl().getIp().equals("10.59.96.183")){
				System.out.println("调用："+invokerDelegete.getProviderUrl().getIp());
				result = invoker.invoke(invocation);
//			}
//			if(i>=invokers.size())
//				break;
		}
		
		return result;
	}

}
