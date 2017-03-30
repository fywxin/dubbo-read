package com.cyou.fz.dubbo.wrapper;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcException;

public class MyProtocolWrapper implements Protocol {
	
	private Protocol protocol;
	
	public MyProtocolWrapper(Protocol protocol){
		this.protocol = protocol;
	}

	@Override
	public int getDefaultPort() {
		return this.protocol.getDefaultPort();
	}

	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
		System.out.println("Protocol -> export 执行前....................."+protocol.getClass());
		Exporter<T> exporter = this.protocol.export(invoker);
		System.out.println("Protocol -> export 执行后...................."+protocol.getClass());
		return exporter;
	}

	@Override
	public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
		return this.protocol.refer(type, url);
	}

	@Override
	public void destroy() {
		this.protocol.destroy();
	}
}
