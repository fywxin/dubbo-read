package com.alibaba.dubbo.examples.tmp;

import org.springframework.beans.factory.annotation.Required;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcException;

@SuppressWarnings("all")
public class Protocol$Adpative implements Protocol {

	public int getDefaultPort() {
		throw new UnsupportedOperationException("method public abstract int Protocol.getDefaultPort() of interface Protocol is not adaptive method!");
	}

	public Invoker refer(java.lang.Class arg0, URL arg1) throws RpcException {
		if (arg1 == null)
			throw new IllegalArgumentException("url == null");
		URL url = arg1;
		String extName = url.getProtocol() == null ? "dubbo" : url.getProtocol();
		if (extName == null)
			throw new IllegalStateException("Fail to get extension(Protocol) name from url("+ url.toString() + ") use keys([protocol])");
		
		Protocol extension = (Protocol)ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);
		return extension.refer(arg0, arg1);
	}

	public Exporter export(Invoker arg0) throws RpcException {
		if (arg0 == null)
			throw new IllegalArgumentException("Invoker argument == null");
		if (arg0.getUrl() == null)
			throw new IllegalArgumentException("Invoker argument getUrl() == null");
		URL url = arg0.getUrl();
		String extName = url.getProtocol() == null ? "dubbo" : url.getProtocol();
		if (extName == null)
			throw new IllegalStateException("Fail to get extension(Protocol) name from url("+ url.toString() + ") use keys([protocol])");
		
		Protocol extension = (Protocol)ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);
		return extension.export(arg0);
	}
	
	public void destroy() {
		throw new UnsupportedOperationException("method public abstract void Protocol.destroy() of interface Protocol is not adaptive method!");
	}
}
